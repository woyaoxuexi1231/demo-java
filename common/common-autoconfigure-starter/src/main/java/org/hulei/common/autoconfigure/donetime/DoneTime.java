package org.hulei.common.autoconfigure.donetime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法执行时间统计注解
 * <p>
 * 标注在方法上，用于统计该方法的执行时间。
 * 配合 {@link DoneTimeAspect} 切面使用。
 * <p>
 * <h3>功能说明：</h3>
 * <ul>
 *     <li>统计方法执行时间（毫秒）</li>
 *     <li>记录方法入参和返回值</li>
 *     <li>记录类名和方法名</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>性能监控和优化</li>
 *     <li>方法执行时间分析</li>
 *     <li>调试和问题排查</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * @DoneTime
 * public void someMethod() {
 *     // 方法执行时间会被统计
 * }
 * }
 * </pre>
 * <p>
 * <h3>注解说明：</h3>
 * <ul>
 *     <li><strong>@Retention(RetentionPolicy.RUNTIME)</strong>：运行时保留，可通过反射获取</li>
 *     <li><strong>@Target(ElementType.METHOD)</strong>：只能标注在方法上</li>
 *     <li><strong>@Inherited</strong>：可被子类继承</li>
 * </ul>
 *
 * @author h1123
 * @since 2022/5/22 16:42
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface DoneTime {
}
