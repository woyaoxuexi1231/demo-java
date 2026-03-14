package org.hulei.common.autoconfigure.result;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 统一结果处理切面自动配置类
 * <p>
 * 配置统一结果处理的 AOP 切面，用于拦截方法调用并统一处理返回结果。
 * 通过 {@link AspectJExpressionPointcutAdvisor} 配置切点表达式和通知。
 * <p>
 * <h3>功能说明：</h3>
 * <ul>
 *     <li>配置 AOP 切面，拦截指定包路径下的方法</li>
 *     <li>统一处理方法的返回结果和异常</li>
 *     <li>通过配置文件动态设置切点表达式</li>
 * </ul>
 * <p>
 * <h3>启用条件：</h3>
 * <ul>
 *     <li>配置项：{@code result.enable}（可选）</li>
 *     <li>需要配置 {@code result.scan-range} 切点表达式</li>
 * </ul>
 * <p>
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>当前配置类已注释，需要取消注释才能生效</li>
 *     <li>需要配合 {@link ResultAspectConfiguration} 使用</li>
 *     <li>确保配置了正确的切点表达式</li>
 * </ul>
 *
 * @author hulei42031
 * @since 2022-06-10 16:22
 * @version 1.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "result.enable", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(ResultAspectConfiguration.class)
public class ResultAspectAutoConfiguration {

    /**
     * 统一结果处理切面配置
     */
    @Autowired
    private ResultAspectConfiguration resultAspectConfiguration;

    /**
     * 创建 AOP 切面顾问
     * <p>
     * 配置 {@link AspectJExpressionPointcutAdvisor}，设置切点表达式和通知。
     * 使用 {@code @ConditionalOnMissingBean} 确保只有一个实例。
     *
     * @return AspectJExpressionPointcutAdvisor AOP 切面顾问
     */
    @Bean
    @ConditionalOnMissingBean
    public AspectJExpressionPointcutAdvisor aspectJExpressionPointcutAdvisor() {
        String scanRange = resultAspectConfiguration.getScanRange();
        log.info("配置统一结果处理切面，扫描范围: {}", scanRange);
        
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(scanRange);
        advisor.setAdvice(new ResultAdvice());
        
        log.debug("统一结果处理切面配置完成");
        return advisor;
    }
}
