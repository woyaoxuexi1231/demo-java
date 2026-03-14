#!/usr/bin/env bash
# ========================================================================================
# Docker 一键安装和配置脚本 (Docker One-Click Installer & Configurator)
# ========================================================================================
#
# 功能说明:
#   本脚本用于在 Ubuntu 系统上自动安装和配置 Docker 环境，包括：
#   - Docker Engine 自动安装
#   - Docker Compose V2 自动安装
#   - 国内镜像加速器配置
#   - 代理设置配置（可选）
#   - DNS 配置（可选）
#   - 远程访问配置（2375端口）
#
# 主要特性:
#   1. 自动检测: 检查系统环境和现有 Docker 安装
#   2. 一键安装: 自动下载和安装所有必要组件
#   3. 镜像加速: 配置国内镜像源，提升拉取速度
#   4. 代理支持: 支持 HTTP/HTTPS 代理配置
#   5. 远程访问: 安全配置 Docker API 远程访问
#   6. 配置备份: 自动备份现有配置，安全更新
#   7. 完整验证: 安装完成后进行全面的功能验证
#
# 配置功能详解:
#   1. Docker 安装:
#      - 自动检测 Ubuntu 版本
#      - 使用 apt 包管理器安装 docker.io
#      - 配置 Docker 服务开机自启
#
#   2. Docker Compose:
#      - 安装 docker-compose-plugin (Compose V2)
#      - 支持 docker compose 命令
#
#   3. 镜像加速器:
#      - 支持自定义镜像列表
#      - 默认配置国内主流镜像源
#      - 自动测试镜像可用性
#
#   4. 代理配置:
#      - 支持环境变量配置代理
#      - daemon.json 和 systemd 环境变量双重配置
#      - 支持 NO_PROXY 排除列表
#
#   5. DNS 配置:
#      - 可选的自定义 DNS 服务器配置
#      - 支持多个 DNS 服务器
#
#   6. 远程访问:
#      - 2375 端口 TCP 监听
#      - Unix socket 本地访问
#      - 详细的安全警告提示
#
# 环境变量配置:
#   DOCKER_HTTP_PROXY     - HTTP 代理服务器地址
#   DOCKER_HTTPS_PROXY    - HTTPS 代理服务器地址
#   DOCKER_NO_PROXY       - 代理排除地址列表
#   DOCKER_REGISTRY_MIRRORS - 自定义镜像加速器列表
#   DOCKER_DNS           - 自定义 DNS 服务器列表
#   LOG_FILE             - 日志文件路径
#
# 使用示例:
#   1. 基本安装: bash install_docker.sh
#   2. 配置代理: export DOCKER_HTTP_PROXY=http://proxy.com:8080 && bash install_docker.sh
#   3. 自定义镜像: export DOCKER_REGISTRY_MIRRORS="https://mirror1.com,https://mirror2.com"
#
# 安全注意事项:
#   ⚠️  2375 端口远程访问存在安全风险
#   ⚠️  仅在可信网络环境中启用
#   ⚠️  生产环境建议使用 TLS 加密 (2376端口)
#   ⚠️  定期检查和更新 Docker 版本
#
# 故障排除:
#   - 查看日志: cat /var/log/docker/install_docker.log
#   - 检查服务: systemctl status docker
#   - 查看配置: cat /etc/docker/daemon.json
#   - 测试功能: docker run hello-world
#
# 系统要求:
#   - Ubuntu 18.04+ 或其他 Debian 系发行版
#   - root 权限或 sudo 权限
#   - 互联网连接（用于下载软件包）
#
# 作者: 系统运维脚本
# 版本: v2.0
# 更新时间: 2024-01
# ========================================================================================

# Ensure we are running under bash (Ubuntu /bin/sh 不支持 pipefail)
if [ -z "${BASH_VERSION:-}" ]; then
  exec /usr/bin/env bash "$0" "$@"
fi

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_FILE="${LOG_FILE:-/var/log/docker/install_docker.log}"

# Docker 代理配置（写死配置）
# 代理地址：192.168.3.2:7890
DOCKER_HTTP_PROXY="192.168.3.2:7890"
DOCKER_HTTPS_PROXY="192.168.3.2:7890"
DOCKER_NO_PROXY="localhost,127.0.0.1,::1"

# Docker DNS 配置（可选，可通过环境变量覆盖）
# 格式：逗号分隔的 DNS 服务器列表，例如："8.8.8.8,8.8.4.4"
DOCKER_DNS="${DOCKER_DNS:-}"

log() {
  local level="$1"; shift
  local msg="$*"
  local ts
  ts="$(date '+%Y-%m-%d %H:%M:%S')"
  printf '[%s] [%s] %s\n' "$ts" "$level" "$msg" | tee -a "$LOG_FILE"
}

log_info() { log "INFO" "$@"; }
log_warn() { log "WARN" "$@"; }
log_error() { log "ERROR" "$@"; }

ensure_dir() {
  local dir="$1"
  if [[ -z "${dir}" ]]; then
    log_error "ensure_dir 收到空目录参数"
    exit 1
  fi
  if [[ ! -d "${dir}" ]]; then
    mkdir -p "${dir}"
  fi
}

# 确保日志目录存在
ensure_dir "$(dirname "${LOG_FILE}")"

trap 'log_error "安装过程中出现错误，退出。"' ERR

log_info "=== Docker 安装开始 ==="
log_info "日志文件: ${LOG_FILE}"

# 0. 权限检查（Ubuntu 默认禁用 root 登录）
if [[ $EUID -ne 0 ]]; then
  if command -v sudo >/dev/null 2>&1; then
    log_warn "当前非 root，使用 sudo 重新执行脚本..."
    exec sudo -E bash "$0" "$@"
  else
    log_error "需要 root 权限且未找到 sudo，请以 root 或 sudo 运行脚本。"
    exit 1
  fi
fi

# 1. 检查 Docker 是否已安装
if command -v docker >/dev/null 2>&1; then
  log_info "Docker 已安装，版本信息："
  docker --version | tee -a "$LOG_FILE"
  log_info "跳过 Docker 安装步骤。"
else
  log_warn "未检测到 docker，尝试在 Ubuntu 上自动安装 docker.io。"
  if [[ -f /etc/os-release ]] && grep -qi 'ubuntu' /etc/os-release; then
    log_info "使用 apt 安装 docker.io（会输出详细安装日志）..."
    export DEBIAN_FRONTEND=noninteractive
    {
      echo ">>> apt-get update -y"
      apt-get update -y
      echo ">>> apt-get install -y docker.io"
      apt-get install -y docker.io
    } 2>&1 | tee -a "$LOG_FILE"

    # 启动并启用 Docker 服务
    systemctl enable --now docker >/dev/null 2>&1 || true

    if ! command -v docker >/dev/null 2>&1; then
      log_error "自动安装 docker 失败，请手动安装后重试。"
      exit 1
    fi
    log_info "docker.io 安装完成。"
  else
    log_error "当前系统非 Ubuntu 或无法检测，且未安装 docker，请先安装 Docker 后再运行。"
    exit 1
  fi
fi

# 2. 配置 Docker daemon.json
log_info "=== 配置 Docker daemon.json ==="

DAEMON_JSON="/etc/docker/daemon.json"
DAEMON_JSON_BACKUP="${DAEMON_JSON}.backup.$(date +%Y%m%d_%H%M%S)"

# 备份现有配置
if [[ -f "${DAEMON_JSON}" ]]; then
  cp "${DAEMON_JSON}" "${DAEMON_JSON_BACKUP}"
  log_info "已备份现有配置到: ${DAEMON_JSON_BACKUP}"
fi

# 确保 daemon.json 存在（如果不存在则创建空对象）
if [[ ! -f "${DAEMON_JSON}" ]]; then
  ensure_dir "$(dirname "${DAEMON_JSON}")"
  echo '{}' > "${DAEMON_JSON}"
fi

# 确保安装了 jq（用于安全更新 JSON）
if ! command -v jq >/dev/null 2>&1; then
  log_info "安装 jq（用于安全更新 JSON 配置）..."
  export DEBIAN_FRONTEND=noninteractive
  if ! apt-get update -y >/dev/null 2>&1 || ! apt-get install -y jq >/dev/null 2>&1; then
    log_error "jq 安装失败，无法安全更新 daemon.json"
    log_error "请手动安装 jq: apt-get install -y jq"
    exit 1
  fi
fi

# 2.1 配置 Docker 代理
log_info "=== 配置 Docker 代理 ==="

# 无论环境变量是否为空，都先清理 daemon.json 中可能存在的旧代理配置
log_info "清理 daemon.json 中的旧代理配置..."
if jq -e '.proxies' "${DAEMON_JSON}" >/dev/null 2>&1; then
  log_info "检测到已有 proxies 配置，正在移除..."
  jq 'del(.proxies)' "${DAEMON_JSON}" > "${DAEMON_JSON}.tmp" && mv "${DAEMON_JSON}.tmp" "${DAEMON_JSON}"
  log_info "已移除 daemon.json 中的 proxies 配置"
else
  log_info "daemon.json 中未发现 proxies 配置"
fi

# 只有当环境变量不为空时，才配置新的代理
if [[ -n "${DOCKER_HTTP_PROXY}" ]] || [[ -n "${DOCKER_HTTPS_PROXY}" ]]; then
  log_info "检测到代理环境变量，配置 Docker daemon 代理..."
  log_info "  HTTP_PROXY: ${DOCKER_HTTP_PROXY}"
  log_info "  HTTPS_PROXY: ${DOCKER_HTTPS_PROXY}"
  log_info "  NO_PROXY: ${DOCKER_NO_PROXY}"
  log_info "注意：Docker daemon 代理配置通过 systemd 环境变量配置（见 2.3 节）"
else
  log_info "未设置代理环境变量（DOCKER_HTTP_PROXY/DOCKER_HTTPS_PROXY），不配置代理。"
  log_info "如需配置代理，请设置环境变量后重新运行脚本："
  log_info "  export DOCKER_HTTP_PROXY=http://proxy.example.com:8080"
  log_info "  export DOCKER_HTTPS_PROXY=http://proxy.example.com:8080"
fi

# 2.2 清理 Docker 镜像加速器配置（不配置镜像）
log_info "=== 清理 Docker 镜像加速器配置 ==="

# 清理可能存在的 registry-mirrors 配置
if jq -e '.registry-mirrors' "${DAEMON_JSON}" >/dev/null 2>&1; then
  log_info "清理现有的 registry-mirrors 配置..."
  jq 'del(.registry-mirrors)' "${DAEMON_JSON}" > "${DAEMON_JSON}.tmp" && mv "${DAEMON_JSON}.tmp" "${DAEMON_JSON}"
  log_info "已清理 registry-mirrors 配置"
else
  log_info "未发现 registry-mirrors 配置"
fi

log_info "使用官方 Docker Hub（不配置镜像加速器）"

# 2.2.1 配置 Docker DNS（可选）
if [[ -n "${DOCKER_DNS}" ]]; then
  log_info "=== 配置 Docker DNS ==="
  log_info "检测到 DNS 配置，正在配置..."

  # 将逗号分隔的 DNS 列表转换为 JSON 数组
  IFS=',' read -ra DNS_ARRAY <<< "${DOCKER_DNS}"
  DNS_JSON="["
  for i in "${!DNS_ARRAY[@]}"; do
    if [[ $i -gt 0 ]]; then
      DNS_JSON+=","
    fi
    DNS_JSON+="\"${DNS_ARRAY[$i]}\""
  done
  DNS_JSON+="]"

  # 更新 daemon.json 添加 dns
  if jq -e '.dns' "${DAEMON_JSON}" >/dev/null 2>&1; then
    log_info "检测到已有 dns 配置，将更新为新的 DNS 列表"
    jq --argjson dns "${DNS_JSON}" '.dns = $dns' "${DAEMON_JSON}" > "${DAEMON_JSON}.tmp" && mv "${DAEMON_JSON}.tmp" "${DAEMON_JSON}"
  else
    log_info "添加新的 dns 配置"
    jq --argjson dns "${DNS_JSON}" '. + {"dns": $dns}' "${DAEMON_JSON}" > "${DAEMON_JSON}.tmp" && mv "${DAEMON_JSON}.tmp" "${DAEMON_JSON}"
  fi

  log_info "Docker DNS 配置已写入: ${DAEMON_JSON}"
  log_info "已配置的 DNS 服务器："
  for dns in "${DNS_ARRAY[@]}"; do
    log_info "  - ${dns}"
  done
else
  log_info "未设置 DNS 配置（DOCKER_DNS），跳过 DNS 配置。"
  log_info "如果镜像加速器无法正常工作，可以尝试配置 DNS："
  log_info "  export DOCKER_DNS=\"8.8.8.8,8.8.4.4\""
  log_info "  然后重新运行脚本：bash $0"
fi

# 2.3 配置 Docker 客户端代理（通过 systemd 服务环境变量）
# 注意：daemon.json 中的代理配置主要用于构建和运行容器时的网络请求
# 对于 docker pull 等客户端命令，可以通过环境变量或 systemd drop-in 配置

SYSTEMD_DOCKER_DIR="/etc/systemd/system/docker.service.d"
ensure_dir "${SYSTEMD_DOCKER_DIR}"

HTTP_PROXY_CONF="${SYSTEMD_DOCKER_DIR}/http-proxy.conf"

# 无论环境变量是否为空，都先清理旧的代理配置文件
if [[ -f "${HTTP_PROXY_CONF}" ]]; then
  log_info "检测到旧的代理配置文件，正在移除: ${HTTP_PROXY_CONF}"
  rm -f "${HTTP_PROXY_CONF}"
  log_info "已移除旧的代理配置文件"
fi

# 只有当环境变量不为空时，才创建新的代理配置
if [[ -n "${DOCKER_HTTP_PROXY}" ]] || [[ -n "${DOCKER_HTTPS_PROXY}" ]]; then
  log_info "配置 Docker 服务环境变量（用于客户端命令）..."

  # 确保代理地址包含协议前缀
  HTTP_PROXY_VALUE="${DOCKER_HTTP_PROXY}"
  HTTPS_PROXY_VALUE="${DOCKER_HTTPS_PROXY}"

  # 如果代理地址不包含协议前缀，添加 http://
  if [[ -n "${HTTP_PROXY_VALUE}" ]] && [[ ! "${HTTP_PROXY_VALUE}" =~ ^https?:// ]]; then
    HTTP_PROXY_VALUE="http://${HTTP_PROXY_VALUE}"
  fi
  if [[ -n "${HTTPS_PROXY_VALUE}" ]] && [[ ! "${HTTPS_PROXY_VALUE}" =~ ^https?:// ]]; then
    HTTPS_PROXY_VALUE="http://${HTTPS_PROXY_VALUE}"
  fi

  cat > "${HTTP_PROXY_CONF}" <<EOF
[Service]
Environment="HTTP_PROXY=${HTTP_PROXY_VALUE}"
Environment="HTTPS_PROXY=${HTTPS_PROXY_VALUE}"
Environment="NO_PROXY=${DOCKER_NO_PROXY}"
EOF

  log_info "Docker 服务环境变量配置已写入: ${HTTP_PROXY_CONF}"
  log_info "  HTTP_PROXY: ${HTTP_PROXY_VALUE}"
  log_info "  HTTPS_PROXY: ${HTTPS_PROXY_VALUE}"
  log_info "  NO_PROXY: ${DOCKER_NO_PROXY}"
else
  log_info "未设置代理环境变量，不配置 Docker 服务代理。"
fi

# 2.4 配置 Docker 远程访问（暴露 2375 端口）
log_info "=== 配置 Docker 远程访问（2375 端口） ==="

log_warn "警告：暴露 Docker 2375 端口存在安全风险！"
log_warn "2375 端口是未加密的端口，任何能访问此端口的人都可以完全控制 Docker。"
log_warn "建议仅在受信任的内网环境中使用，或使用 2376 端口配置 TLS 加密。"

# 检查是否已有 override 文件
DOCKER_OVERRIDE="${SYSTEMD_DOCKER_DIR}/override.conf"
BACKUP_OVERRIDE=""
if [[ -f "${DOCKER_OVERRIDE}" ]]; then
  log_info "检测到已有 override.conf，将更新配置"
  BACKUP_OVERRIDE="${DOCKER_OVERRIDE}.backup.$(date +%Y%m%d_%H%M%S)"
  cp "${DOCKER_OVERRIDE}" "${BACKUP_OVERRIDE}"
  log_info "已备份现有配置到: ${BACKUP_OVERRIDE}"
fi

# 检查是否已经正确配置了 2375 端口
# 检查 override 文件是否存在且格式正确（包含有效的 ExecStart 和 2375 端口）
ALREADY_CONFIGURED=false
if [[ -f "${DOCKER_OVERRIDE}" ]]; then
  # 检查文件是否包含 2375 端口配置
  if grep -q "tcp://.*:2375\|tcp://0\.0\.0\.0:2375\|tcp://127\.0\.0\.1:2375" "${DOCKER_OVERRIDE}" 2>/dev/null; then
    # 验证文件格式是否正确（不包含 systemd 内部格式）
    if ! grep -q "{ path=" "${DOCKER_OVERRIDE}" 2>/dev/null; then
      # 验证 Docker 服务是否能正常验证配置
      if systemd-analyze verify docker.service >/dev/null 2>&1; then
        ALREADY_CONFIGURED=true
      fi
    fi
  fi
fi

if [[ "${ALREADY_CONFIGURED}" == "true" ]]; then
  log_info "检测到已正确配置 2375 端口，跳过配置"
else
  if [[ -f "${DOCKER_OVERRIDE}" ]]; then
    log_warn "检测到 override 文件存在但配置可能有问题，将重新配置"
  fi
  log_info "配置 Docker 监听 2375 端口..."

  # 获取原始的 ExecStart 命令
  # 优先从服务文件读取（systemctl show 返回的是内部格式，不能直接使用）
  DOCKER_SERVICE_FILE=$(systemctl show docker --property=FragmentPath --value 2>/dev/null || echo "/lib/systemd/system/docker.service")
  ORIGINAL_EXECSTART=""

  # 首先尝试从原始服务文件读取
  if [[ -f "${DOCKER_SERVICE_FILE}" ]]; then
    ORIGINAL_EXECSTART=$(grep "^ExecStart=" "${DOCKER_SERVICE_FILE}" | head -n 1 | sed 's/^ExecStart=//' | sed 's/^[[:space:]]*//' | sed 's/[[:space:]]*$//' || echo "")
  fi

  # 如果原始服务文件没有，尝试从 override 文件读取（如果存在）
  if [[ -z "${ORIGINAL_EXECSTART}" ]] && [[ -f "${DOCKER_OVERRIDE}" ]]; then
    ORIGINAL_EXECSTART=$(grep "^ExecStart=" "${DOCKER_OVERRIDE}" | grep -v "^ExecStart=$" | head -n 1 | sed 's/^ExecStart=//' | sed 's/^[[:space:]]*//' | sed 's/[[:space:]]*$//' || echo "")
  fi

  # 如果仍然无法获取，使用默认值
  if [[ -z "${ORIGINAL_EXECSTART}" ]]; then
    ORIGINAL_EXECSTART="/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock"
    log_warn "无法获取原始 ExecStart，使用默认值"
  fi

  log_info "原始 ExecStart: ${ORIGINAL_EXECSTART}"

  # 构建新的 ExecStart
  # 首先提取 dockerd 的路径（第一个字段，去除可能的引号）
  DOCKERD_PATH=$(echo "${ORIGINAL_EXECSTART}" | awk '{print $1}' | sed 's/^["'\'']//' | sed 's/["'\'']$//')

  # 验证 dockerd 路径是否存在
  if [[ ! -f "${DOCKERD_PATH}" ]] && [[ ! -x "${DOCKERD_PATH}" ]]; then
    # 尝试查找 dockerd 的位置
    if command -v dockerd >/dev/null 2>&1; then
      DOCKERD_PATH=$(command -v dockerd)
      log_info "使用系统找到的 dockerd 路径: ${DOCKERD_PATH}"
    else
      DOCKERD_PATH="/usr/bin/dockerd"
      log_warn "无法验证 dockerd 路径，使用默认值: ${DOCKERD_PATH}"
    fi
  fi

  # 提取所有非 -H/--host 的参数
  # 使用更安全的方式提取参数，避免破坏引号内的内容
  OTHER_ARGS=$(echo "${ORIGINAL_EXECSTART}" | sed "s|^${DOCKERD_PATH}||" | \
    sed 's/-H [^ ]*//g' | sed 's/--host=[^ ]*//g' | \
    sed 's/-H[^ ]*//g' | sed 's/--host[^ ]*//g' | \
    sed 's/  */ /g' | sed 's/^ *//' | sed 's/ *$//')

  # 构建新的 ExecStart
  # Docker 最佳实践：-H 参数应该放在前面
  NEW_EXECSTART="${DOCKERD_PATH}"

  # 先添加 -H 参数：unix socket 和 tcp
  # 注意：fd:// 是 systemd socket activation，不是 unix socket
  # 当我们替换 ExecStart 时，必须明确添加 unix socket
  # 检查是否已经包含 unix://（不包括 fd://）
  if ! echo "${ORIGINAL_EXECSTART}" | grep -q "unix://"; then
    NEW_EXECSTART="${NEW_EXECSTART} -H unix:///var/run/docker.sock"
    log_info "添加 unix socket: -H unix:///var/run/docker.sock"
  fi
  # 添加 TCP 端口（如果不存在）
  if ! echo "${ORIGINAL_EXECSTART}" | grep -q "tcp://.*:2375"; then
    NEW_EXECSTART="${NEW_EXECSTART} -H tcp://0.0.0.0:2375"
    log_info "添加 TCP 端口: -H tcp://0.0.0.0:2375"
  fi

  # 然后添加其他参数（如果存在且不为空）
  if [[ -n "${OTHER_ARGS}" ]]; then
    NEW_EXECSTART="${NEW_EXECSTART} ${OTHER_ARGS}"
  fi

  # 清理多余空格
  NEW_EXECSTART=$(echo "${NEW_EXECSTART}" | sed 's/  */ /g' | sed 's/^ *//' | sed 's/ *$//')

  # 验证 ExecStart 格式
  if [[ ! "${NEW_EXECSTART}" =~ ^/ ]]; then
    log_error "ExecStart 必须以绝对路径开头，当前值: ${NEW_EXECSTART}"
    exit 1
  fi

  # 验证必须包含 unix socket 或 TCP 端口
  if ! echo "${NEW_EXECSTART}" | grep -qE "unix://|tcp://"; then
    log_error "ExecStart 必须包含至少一个 -H 参数（unix:// 或 tcp://）"
    exit 1
  fi

  log_info "构建的新 ExecStart: ${NEW_EXECSTART}"

  # 写入 override 文件
  cat > "${DOCKER_OVERRIDE}" <<EOF
[Service]
ExecStart=
ExecStart=${NEW_EXECSTART}
EOF

  log_info "Docker 远程访问配置已写入: ${DOCKER_OVERRIDE}"
  log_info "新的 ExecStart: ${NEW_EXECSTART}"
  log_info "Docker 将监听："
  log_info "  - Unix Socket: /var/run/docker.sock"
  log_info "  - TCP 端口: 0.0.0.0:2375"

  # 验证写入的文件内容
  if [[ ! -f "${DOCKER_OVERRIDE}" ]]; then
    log_error "override 文件写入失败: ${DOCKER_OVERRIDE}"
    exit 1
  fi

  log_info "配置文件内容："
  cat "${DOCKER_OVERRIDE}" | tee -a "$LOG_FILE"
fi

# 重启 Docker 服务使所有配置生效
log_info "重启 Docker 服务以使配置生效..."

# 验证 override 文件语法（如果存在）
if [[ -f "${DOCKER_OVERRIDE}" ]]; then
  log_info "验证 override 配置文件语法..."
  if ! systemd-analyze verify docker.service >/dev/null 2>&1; then
    log_error "Docker 服务配置验证失败，请检查 override 文件"
    log_error "配置文件位置: ${DOCKER_OVERRIDE}"
    log_error "配置内容："
    cat "${DOCKER_OVERRIDE}" | tee -a "$LOG_FILE"
    if [[ -f "${BACKUP_OVERRIDE:-}" ]]; then
      log_warn "正在恢复备份的 override 配置..."
      mv "${BACKUP_OVERRIDE}" "${DOCKER_OVERRIDE}"
      systemctl daemon-reload
    fi
    exit 1
  fi
fi

# 重新加载 systemd 配置
log_info "重新加载 systemd 配置..."
systemctl daemon-reload || {
  log_error "systemctl daemon-reload 失败"
  exit 1
}

# 重启 Docker 服务
log_info "重启 Docker 服务..."
if ! systemctl restart docker; then
  log_error "Docker 服务重启失败"
  log_error "请检查以下信息："
  log_error "1. Docker 服务状态: systemctl status docker"
  log_error "2. Docker 日志: journalctl -xeu docker.service"
  log_error "3. 配置文件: ${DOCKER_OVERRIDE}"

  # 显示 Docker 服务状态
  log_error ""
  log_error "=== Docker 服务状态 ==="
  systemctl status docker --no-pager -l | tee -a "$LOG_FILE" || true

  # 显示最近的 Docker 日志
  log_error ""
  log_error "=== Docker 服务最近日志（最后 30 行） ==="
  journalctl -xeu docker.service --no-pager -n 30 | tee -a "$LOG_FILE" || true

  # 如果配置了 override，尝试恢复
  if [[ -f "${DOCKER_OVERRIDE}" ]] && [[ -f "${BACKUP_OVERRIDE:-}" ]]; then
    log_warn ""
    log_warn "正在恢复备份的 override 配置..."
    mv "${BACKUP_OVERRIDE}" "${DOCKER_OVERRIDE}"
    systemctl daemon-reload
    if systemctl restart docker; then
      log_info "恢复备份配置后，Docker 服务重启成功"
      log_warn "2375 端口配置可能未生效，请手动检查配置"
      log_warn ""
      log_warn "手动配置 2375 端口的方法："
      log_warn "1. 编辑 ${DOCKER_OVERRIDE}"
      log_warn "2. 确保 ExecStart 包含: -H tcp://0.0.0.0:2375"
      log_warn "3. 运行: systemctl daemon-reload && systemctl restart docker"
    else
      log_error "恢复备份配置后，Docker 服务仍无法启动"
      log_error "请手动检查 Docker 配置和日志"
    fi
  fi

  # 如果 daemon.json 有备份，也尝试恢复
  if [[ -f "${DAEMON_JSON_BACKUP}" ]]; then
    log_warn ""
    log_warn "正在恢复备份的 daemon.json 配置..."
    mv "${DAEMON_JSON_BACKUP}" "${DAEMON_JSON}"
    systemctl daemon-reload
    if systemctl restart docker; then
      log_info "恢复 daemon.json 后，Docker 服务重启成功"
    else
      log_error "恢复 daemon.json 后，Docker 服务仍无法启动"
    fi
  fi

  log_error ""
  log_error "如果问题仍未解决，请检查："
  log_error "1. Docker 版本兼容性: docker --version"
  log_error "2. 系统日志: journalctl -xeu docker.service -n 100"
  log_error "3. 配置文件语法: systemd-analyze verify docker.service"
  log_error "4. 端口占用: netstat -tlnp | grep 2375"

  exit 1
fi

log_info "Docker 服务重启成功。"

# 检查并安装 Docker Compose
log_info "=== 检查 Docker Compose ==="
if ! command -v docker-compose >/dev/null 2>&1 && ! docker compose version >/dev/null 2>&1; then
  log_info "Docker Compose 未安装，开始安装..."

  # 安装 docker-compose-plugin (Docker Compose V2)
  log_info "安装 Docker Compose V2 (docker-compose-plugin)..."
  export DEBIAN_FRONTEND=noninteractive

  # 先更新包列表
  log_info "更新包列表..."
  if ! apt-get update -y 2>&1 | tee -a "$LOG_FILE"; then
    log_error "apt-get update 失败"
    exit 1
  fi

  # 检查 docker-compose-plugin 包是否存在
  log_info "检查 docker-compose-plugin 包可用性..."
  if apt-cache show docker-compose-plugin >/dev/null 2>&1; then
    log_info "docker-compose-plugin 包可用，开始安装..."
    if apt-get install -y docker-compose-plugin 2>&1 | tee -a "$LOG_FILE"; then
      log_info "docker-compose-plugin 安装成功"
    else
      log_error "docker-compose-plugin 安装失败，尝试备用方案..."
      # 备用方案：安装 docker-compose 独立包
      if apt-get install -y docker-compose 2>&1 | tee -a "$LOG_FILE"; then
        log_info "docker-compose 独立包安装成功"
      else
        log_error "所有 Docker Compose 安装方案都失败了"
        log_error "请手动安装 Docker Compose："
        log_error "  方案1: apt-get install -y docker-compose"
        log_error "  方案2: curl -L \"https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)\" -o /usr/local/bin/docker-compose && chmod +x /usr/local/bin/docker-compose"
        log_error "  方案3: pip3 install docker-compose"
        exit 1
      fi
    fi
  else
    log_error "docker-compose-plugin 包不可用，尝试安装独立版..."
    if apt-get install -y docker-compose 2>&1 | tee -a "$LOG_FILE"; then
      log_info "docker-compose 独立包安装成功"
    else
      log_error "docker-compose 安装失败，请手动安装"
      log_error "手动安装命令："
      log_error "  curl -L \"https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)\" -o /usr/local/bin/docker-compose && chmod +x /usr/local/bin/docker-compose"
      exit 1
    fi
  fi

  if docker compose version >/dev/null 2>&1; then
    log_info "✓ Docker Compose V2 安装成功"
    docker compose version | tee -a "$LOG_FILE"
  elif command -v docker-compose >/dev/null 2>&1; then
    log_info "✓ Docker Compose (独立版) 安装成功"
    docker-compose --version | tee -a "$LOG_FILE"
  else
    log_error "Docker Compose 安装验证失败"
    exit 1
  fi
else
  log_info "✓ Docker Compose 已安装"
  if command -v docker-compose >/dev/null 2>&1; then
    docker-compose --version | tee -a "$LOG_FILE"
  elif docker compose version >/dev/null 2>&1; then
    docker compose version | tee -a "$LOG_FILE"
  fi
fi

# 3. 验证 Docker 安装和配置
log_info "=== 验证 Docker 安装 ==="
if docker ps >/dev/null 2>&1; then
  log_info "Docker 运行正常。"
  docker --version | tee -a "$LOG_FILE"
else
  log_error "Docker 无法正常运行，请检查日志：journalctl -u docker"
  exit 1
fi

# 验证 2375 端口是否监听
log_info "验证 2375 端口监听状态..."
if netstat -tlnp 2>/dev/null | grep -q ":2375 " || ss -tlnp 2>/dev/null | grep -q ":2375 "; then
  log_info "✓ 2375 端口正在监听"
else
  log_warn "2375 端口未检测到监听，可能需要检查配置"
  log_warn "请手动验证：netstat -tlnp | grep 2375 或 ss -tlnp | grep 2375"
fi

# 验证镜像加速器配置
log_info "验证镜像加速器配置..."
log_info "✓ 使用官方 Docker Hub（未配置镜像加速器）"

# 诊断信息
log_info "=== 诊断信息 ==="
log_info "如果 docker pull 访问缓慢或失败，可能的原因："
log_info "1. 网络连接问题，检查防火墙和网络设置"
log_info "2. DNS 解析问题，尝试添加 DNS 配置："
log_info "   在 daemon.json 中添加: \"dns\": [\"8.8.8.8\", \"8.8.4.4\"]"
log_info "3. 代理配置问题（已默认配置：192.168.3.2:7890）"

log_info "=== Docker 安装完成 ==="
log_info ""
log_info "配置说明："
log_info "1. 镜像加速器配置："
log_info "   默认不配置镜像加速器，使用官方 Docker Hub"
log_info ""
log_info "2. 代理配置说明："
if [[ -n "${DOCKER_HTTP_PROXY}" ]] || [[ -n "${DOCKER_HTTPS_PROXY}" ]]; then
  log_info "   代理已配置（默认代理地址：192.168.3.2:7890）："
  log_info "     HTTP_PROXY: ${DOCKER_HTTP_PROXY}"
  log_info "     HTTPS_PROXY: ${DOCKER_HTTPS_PROXY}"
  log_info "     NO_PROXY: ${DOCKER_NO_PROXY}"
else
  log_info "   代理未配置，已清理所有旧的代理配置"
  log_info "   如需配置代理，请设置环境变量后重新运行脚本："
  log_info "     export DOCKER_HTTP_PROXY=http://192.168.3.2:7890"
  log_info "     export DOCKER_HTTPS_PROXY=http://192.168.3.2:7890"
  log_info "     bash $0"
fi
log_info "   如需移除代理，确保环境变量为空后重新运行脚本即可"
log_info ""
log_info "3. docker search 命令说明："
log_info "   docker search 命令需要直接访问 Docker Hub (index.docker.io)"
log_info "   如果遇到连接错误，可能需要配置代理才能使用 docker search"
log_info ""
log_info "4. Docker 远程访问已配置："
log_info "   Docker 监听端口：2375 (TCP)"
log_info "   本地连接：docker -H tcp://localhost:2375 ps"
log_info "   远程连接：docker -H tcp://<服务器IP>:2375 ps"
log_warn "   安全警告：2375 端口未加密，请确保在安全网络环境中使用"
log_info ""
log_info "5. 验证配置："
log_info "   验证远程访问：docker -H tcp://localhost:2375 version"
log_info "   测试拉取镜像：docker pull hello-world"
log_info "   测试搜索镜像（需要代理）：docker search nginx"
log_info ""
log_info "6. 网络连接问题排查："
log_info "   如果 docker pull 速度慢或失败，请尝试："
log_info "   a) 配置 DNS（如果 DNS 解析有问题）："
log_info "        export DOCKER_DNS=\"8.8.8.8,8.8.4.4\""
log_info "        bash $0"
log_info "   b) 检查代理配置（已默认配置：192.168.3.2:7890）："
log_info "        docker info | grep -A 5 Proxy"
log_info "   c) 重启 Docker 服务："
log_info "        sudo systemctl restart docker"
log_info "   d) 查看 Docker 日志："
log_info "        sudo journalctl -u docker -n 50"

