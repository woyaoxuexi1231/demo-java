package org.hulei.common.autoconfigure.donetime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 方法执行时间统计自动配置类
 * <p>
 * 自动配置方法执行时间统计切面，用于统计标注了 {@code @DoneTime} 注解的方法的执行时间。
 * <p>
 * <h3>功能说明：</h3>
 * <ul>
 *     <li>导入 {@link DoneTimeAspect} 切面类</li>
 *     <li>支持通过配置启用或禁用</li>
 * </ul>
 * <p>
 * <h3>启用条件：</h3>
 * <ul>
 *     <li>配置项：{@code done.time.enable}（当前已注释，默认启用）</li>
 *     <li>配置值：{@code true}（如果启用条件注解）</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>性能监控和优化</li>
 *     <li>方法执行时间分析</li>
 *     <li>调试和问题排查</li>
 * </ul>
 * <p>
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>当前配置默认启用，如需条件控制可取消注释 {@code @ConditionalOnProperty}</li>
 *     <li>需要在方法上标注 {@code @DoneTime} 注解才能统计执行时间</li>
 * </ul>
 *
 * @author hulei42031
 * @since 2022-05-23 17:41
 * @version 1.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "done.time.enable", havingValue = "true", matchIfMissing = true)
@Import({DoneTimeAspect.class})
public class DoneTimeAutoConfiguration {

    /**
     * 构造函数
     * <p>
     * 当配置类被加载时，记录启用方法执行时间统计的日志。
     */
    public DoneTimeAutoConfiguration() {
        log.info("启用方法执行时间统计切面（@DoneTime）");
    }
}
