package org.hulei.common.autoconfigure.result;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 统一结果处理切面配置类
 * <p>
 * 用于配置统一结果处理切面的扫描范围。
 * 可以通过配置文件设置切面拦截的包路径。
 * <p>
 * <h3>配置说明：</h3>
 * <ul>
 *     <li>配置前缀：{@code result}（当前已注释，可通过取消注释启用）</li>
 *     <li>配置项：{@code result.scan-range}（扫描范围，AOP 切点表达式）</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>配置统一结果处理的切点表达式</li>
 *     <li>动态配置 AOP 拦截范围</li>
 * </ul>
 * <p>
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>当前 {@code @ConfigurationProperties} 注解已注释，需要取消注释才能生效</li>
 *     <li>需要在配置类上使用 {@code @EnableConfigurationProperties} 启用配置</li>
 * </ul>
 *
 * @author hulei42031
 * @since 2022-06-10 17:14
 * @version 1.0
 */
@Data
@ConfigurationProperties(prefix = "result")
public class ResultAspectConfiguration {

    /**
     * 统一结果处理扫描范围
     * <p>
     * AOP 切点表达式，用于指定需要拦截的包路径。
     * 例如：{@code "execution(* org.hulei..*Controller.*(..))"}
     */
    private String scanRange;
}
