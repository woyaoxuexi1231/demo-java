package org.hulei.jdbc.controller;

import com.netflix.discovery.converters.Auto;
import jakarta.ws.rs.POST;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hulei.entity.jpa.pojo.DemoStockQuote;
import org.hulei.util.dto.ResultDTO;
import org.hulei.util.utils.ResultDTOBuild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author hulei
 * @since 2026/3/15 20:41
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class GeneratedKeyHolderController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/get-generated-key")
    public ResultDTO<Map<String, Object>> getGeneratedKey() {
        log.info("开始插入数据并获取自增主键");

        Map<String, Object> result = new HashMap<>();

        try {
            // 创建 KeyHolder 用于保存生成的主键
            KeyHolder holder = new GeneratedKeyHolder();
            log.info("步骤1：创建 GeneratedKeyHolder 对象用于保存生成的主键");

            // 生成测试数据
            DemoStockQuote stockQuote = DemoStockQuote.gen();
            log.info("步骤2：生成测试数据 - {}", stockQuote);

            // 插入 SQL
            String insertSql = "insert into demo_stock_quotes (code, name, current_price, change_percent, change_amount, volume, turnover, high, low, open_price, pre_close, source, data_time) " +
                    "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            log.info("步骤3：准备插入 SQL: {}", insertSql);

            // 执行插入操作
            int updateCount = jdbcTemplate.update(
                    connection -> {
                        // 配置 PreparedStatement 返回自增键
                        PreparedStatement preparedStatement = connection.prepareStatement(
                                insertSql,
                                Statement.RETURN_GENERATED_KEYS
                        );
                        preparedStatement.setString(1, stockQuote.getCode());
                        preparedStatement.setString(2, stockQuote.getName());
                        preparedStatement.setBigDecimal(3, stockQuote.getCurrentPrice());
                        preparedStatement.setBigDecimal(4, stockQuote.getChangePercent());
                        preparedStatement.setBigDecimal(5, stockQuote.getChangeAmount());
                        preparedStatement.setLong(6, stockQuote.getVolume());
                        preparedStatement.setBigDecimal(7, stockQuote.getTurnover());
                        preparedStatement.setBigDecimal(8, stockQuote.getHigh());
                        preparedStatement.setBigDecimal(9, stockQuote.getLow());
                        preparedStatement.setBigDecimal(10, stockQuote.getOpenPrice());
                        preparedStatement.setBigDecimal(11, stockQuote.getPreClose());
                        preparedStatement.setString(12, stockQuote.getSource());
                        preparedStatement.setTimestamp(13, stockQuote.getDataTime() != null ? java.sql.Timestamp.from(stockQuote.getDataTime()) : null);

                        log.info("步骤4：配置 PreparedStatement 返回自增键（Statement.RETURN_GENERATED_KEYS）");
                        log.info("步骤5：设置参数 - {}", stockQuote);
                        return preparedStatement;
                    },
                    holder
            );

            log.info("步骤6：插入操作完成，影响行数: {}", updateCount);

            // 从 KeyHolder 中获取生成的主键
            // KeyHolder.getKeyList() 返回 List<Map<String, Object>>，每个 Map 包含主键信息
            // 主键的键名可能是 "GENERATED_KEY" 或实际的列名（如 "id"）
            Long generatedId = Optional.of(holder.getKeyList())
                    .filter(list -> !list.isEmpty())
                    .map(list -> {
                        Map<String, Object> keyMap = list.get(0);
                        log.info("步骤7：KeyHolder 返回的主键 Map: {}", keyMap);

                        // 尝试从 Map 中提取主键值
                        // 优先查找常见的键名
                        Object keyValue = keyMap.get("GENERATED_KEY");
                        if (keyValue == null) {
                            keyValue = keyMap.get("id");
                        }
                        if (keyValue == null) {
                            // 如果找不到常见键名，取第一个值
                            keyValue = keyMap.values().iterator().next();
                        }

                        // 转换为 Long
                        if (keyValue instanceof Number) {
                            return ((Number) keyValue).longValue();
                        } else if (keyValue != null) {
                            return Long.parseLong(keyValue.toString());
                        }
                        return null;
                    })
                    .orElse(-1L);

            if (generatedId.equals(-1L)) {
                log.info("步骤8：未能获取到生成的主键");
                result.put("generatedId", null);
                result.put("success", false);
            } else {
                log.info("步骤8：成功获取生成的主键 ID: {}", generatedId);
                result.put("generatedId", generatedId);
                result.put("success", true);
            }

            result.put("stockQuote", stockQuote);
            result.put("updateCount", updateCount);
            return ResultDTOBuild.resultSuccessBuild(result);

        } catch (Exception e) {
            log.error("插入数据并获取主键时发生异常", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResultDTOBuild.resultErrorBuild("插入数据失败: " + e.getMessage());
        }
    }

}
