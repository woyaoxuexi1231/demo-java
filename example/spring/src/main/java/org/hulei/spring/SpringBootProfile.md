# Spring Boot 配置文件加载与 Profile 机制（详细版）

> 适用版本：以 Spring Boot 2.4+ / 3.x 为主（`ConfigData` 机制）。

## 1. 先给结论（你关心的几个问题）

1. **位置有先后，后加载的优先级更高**（同 key 会覆盖前面的值）。
2. **`application-{profile}.*` 会覆盖同位置下的 `application.*`**。
3. **`spring.profiles.active` 优先于 `spring.profiles.default`**；`default` 只在未激活任何 profile 时生效。
4. **两个配置里有相同 key，最终取“优先级更高”的那个**。
5. **`spring.a.b=x` 与 `spring.a.c=y` 这种不同子键通常可合并，同时可读取到 `b` 和 `c`**。

---

## 2. 配置文件“位置”加载顺序（默认）

默认会从以下位置找 `application.*`（和 profile 变体）：

- `classpath:/`
- `classpath:/config/`
- `file:./`
- `file:./config/`
- `file:./config/*/`

理解优先级时建议按 **从低到高** 记忆：

`classpath:/` < `classpath:/config/` < `file:./` < `file:./config/` < `file:./config/*/`

也就是说，外部目录（尤其当前目录下的 `config`）通常会覆盖 jar 包内部配置。

---

## 3. 同一位置下的“文件前缀/文件名”规则

## 3.1 默认前缀

- 默认前缀是 `application`，即：
  - `application.properties`
  - `application.yml`
  - `application.yaml`
  - `application-{profile}.properties|yml|yaml`

## 3.2 自定义前缀

可通过 `spring.config.name` 修改（例如 `myapp`）。
如果配置多个名称（逗号分隔），会按声明顺序处理，后处理的同名键可覆盖先处理的。

## 3.3 同目录下 `.properties` 与 `.yml` 同时存在

同位置同前缀下，`.properties` 的优先级通常高于 `.yml/.yaml`。

---

## 4. Profile 激活、默认、优先级

## 4.1 `active` 与 `default`

- `spring.profiles.active`：显式激活哪些 profile。
- `spring.profiles.default`：仅在没有任何 active profile 时作为回退。

因此：

- **有 active -> 用 active，default 基本不参与。**
- **无 active -> 才使用 default（默认值是 `default`）。**

## 4.2 profile 文件覆盖基础文件

以 `dev` 为例：

- `application.yml`（基础）
- `application-dev.yml`（环境专属）

同 key 冲突时，`application-dev.yml` 覆盖 `application.yml`。

## 4.3 激活来源谁更高

常见来源优先级（高到低，和配置文件相关部分）：

1. 命令行参数（`--spring.profiles.active=prod`）
2. 系统属性 / 环境变量
3. 配置文件中的 `spring.profiles.active`

高优先级来源可覆盖低优先级来源的 active 声明。

---

## 5. 两个配置里有相同配置，谁生效？

看两件事：

1. **属性源优先级**（命令行 > 环境变量 > 外部配置文件 > 包内配置文件 ...）
2. **同一属性源内的加载顺序**（后加载覆盖先加载）

只要 key 完全相同，最终就保留“最后胜出”的值。

示例：

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db1

# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://prod-host:3306/db1
```

激活 `prod` 后，最终 `spring.datasource.url=jdbc:mysql://prod-host:3306/db1`。

---

## 6. 你问的合并问题：`spring.a.b` 和 `spring.a.c`

结论：**通常会合并**。

例如：

```properties
# A 文件
spring.a.b=x

# B 文件（优先级更高或后加载）
spring.a.c=y
```

最终 Environment 中通常同时存在：

- `spring.a.b = x`
- `spring.a.c = y`

因为它们是同一前缀下不同子键，不冲突。

## 6.1 但有两个重要例外

1. **同一个叶子 key 冲突**：如两边都定义 `spring.a.b`，高优先级覆盖低优先级。
2. **集合类型（List）常是整体替换而非逐项合并**：高优先级列表会替换低优先级列表；Map 更容易按 key 合并。

---

## 7. 推荐验证方式（最稳）

1. 启动时加 `--debug` 看自动配置与属性来源日志。
2. 用 Actuator 的 `/actuator/env`、`/actuator/configprops` 观察最终生效值与来源。
3. 对关键配置做单元测试或集成测试，固定预期行为（特别是多 profile 场景）。

---

## 8. 实战记忆口诀

- **位置**：外部覆盖内部。
- **profile**：专属覆盖基础。
- **active/default**：active 优先，default 兜底。
- **同 key**：高优先级赢。
- **同前缀不同子键**：通常可合并（列表要小心替换行为）。

---

## 9. 你这个场景的结论：多个 `application-eureka.yml` 谁生效？

前提先确认：你写的是 `spring.profiles.active=erueka`，这个是拼写错误。要激活 `application-eureka.yml`，应是 `spring.profiles.active=eureka`。

在 profile 被正确激活后，优先级可按下面记：

1. **外部配置（`file:./config/`、`file:./` 等）最高**
2. **当前应用自身 classpath（通常是本项目 `src/main/resources`）次之**
3. **依赖包里的 classpath 配置更低**

如果同一个 key 在三处都定义，最终取高优先级来源的值（外部覆盖内部）。

### 9.1 你的三个来源同时存在时

- 你当前项目里有 `application-eureka.yml`
- 引入的另一个 jar 里也有 `application-eureka.yml`
- 外部目录也放了 `application-eureka.yml`

同 key 冲突时，通常是：**外部 > 当前项目 > 依赖包**。

### 9.2 一个关键风险：classpath 同名文件不要依赖“谁先加载”

当“当前项目 + 多个依赖 jar”都含同名 `application-eureka.yml` 时，classpath 内部的先后受类路径顺序影响，不应作为稳定契约。工程上应避免在多个 jar 放同名 `application-{profile}.yml` 并定义同一批关键 key。

### 9.3 实战建议

- 把最终想覆盖的值放到外部 `config` 目录。
- 依赖包尽量用独立前缀（如 `hulei.xxx`），避免与应用核心配置同 key 冲突。
- 需要显式引入某个依赖配置时，优先用 `spring.config.import` 做可见、可控的装载。
