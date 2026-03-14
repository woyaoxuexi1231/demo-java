package org.hulei.common.autoconfigure.donetime;

import cn.hutool.core.date.StopWatch;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.text.DecimalFormat;

/**
 * 方法执行时间统计切面
 * <p>
 * 使用 AOP 切面技术统计方法执行时间，支持两种切点定义方式：
 * <ul>
 *     <li><strong>包路径切点</strong>：拦截指定包下所有 Controller 类的方法</li>
 *     <li><strong>注解切点</strong>：拦截所有标注了 @DoneTime 注解的方法</li>
 * </ul>
 * <p>
 * <h3>AOP 切面说明：</h3>
 * <ul>
 *     <li><strong>环绕通知（@Around）</strong>：在目标方法执行前后都可以执行</li>
 *     <li><strong>切点（@Pointcut）</strong>：定义需要拦截的方法</li>
 *     <li><strong>连接点（ProceedingJoinPoint）</strong>：包含目标方法的信息</li>
 * </ul>
 * <p>
 * <h3>功能特性：</h3>
 * <ul>
 *     <li>统计方法执行时间（毫秒）</li>
 *     <li>记录方法入参和返回值</li>
 *     <li>支持数字格式化（千分位分隔符）</li>
 *     <li>记录类名和方法名</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>性能监控和优化</li>
 *     <li>方法执行时间分析</li>
 *     <li>调试和问题排查</li>
 *     <li>性能基准测试</li>
 * </ul>
 *
 * @author h1123
 * @since 2022/5/22 16:44
 * @version 1.0
 */
@Aspect
// @Component  // 如果需要自动扫描，可以取消注释
@Slf4j
public class DoneTimeAspect {

    /**
     * 包路径切点
     * <p>
     * 扫描 org.hulei 包下所有 Controller 类的方法。
     * <p>
     * <h3>切点表达式说明：</h3>
     * <ul>
     *     <li><strong>execution</strong>：执行表达式</li>
     *     <li><strong>*</strong>：任意返回类型</li>
     *     <li><strong>org.hulei..*Controller</strong>：org.hulei 包及其子包下所有以 Controller 结尾的类</li>
     *     <li><strong>.*(..)</strong>：任意方法，任意参数</li>
     * </ul>
     */
    @Pointcut("execution(* org.hulei..*Controller.*(..))")
    public void point1() {
        // 切点方法体为空，仅用于定义切点
    }
    
    /**
     * 注解切点
     * <p>
     * 以 @DoneTime 注解为切点，拦截所有标注了该注解的方法。
     * <p>
     * <h3>注解切点说明：</h3>
     * <ul>
     *     <li><strong>@annotation</strong>：注解切点表达式</li>
     *     <li>只拦截标注了指定注解的方法</li>
     *     <li>更加精确和灵活</li>
     * </ul>
     */
    @Pointcut("@annotation(org.hulei.common.autoconfigure.donetime.DoneTime)")
    public void point2() {
        // 切点方法体为空，仅用于定义切点
    }

    /**
     * 基于包路径的环绕通知（已禁用）
     * <p>
     * 拦截 org.hulei 包下所有 Controller 类的方法，统计执行时间。
     * <p>
     * <h3>注意事项：</h3>
     * <ul>
     *     <li>当前方法已被注释，不会生效</li>
     *     <li>如果需要启用，取消 @Around 注解的注释</li>
     *     <li>建议使用注解切点（aroundByAnnotation），更加精确</li>
     * </ul>
     *
     * @param joinPoint 连接点，包含目标方法的信息
     * @return 方法执行结果
     * @throws Throwable 如果方法执行失败
     */
    // @Around(value = "point1()", argNames = "joinPoint")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("========== 包路径切面开始执行 ==========");
        
        // 计时器：使用 Hutool 的 StopWatch 进行高精度计时
        StopWatch stopWatch = new StopWatch();
        // 方法参数：获取目标方法的参数数组
        Object[] param = joinPoint.getArgs();
        // 方法结果：用于存储方法返回值
        Object rsp = null;

        try {
            stopWatch.start();
            log.debug("执行方法: {}", joinPoint.getSignature().getName());
            rsp = joinPoint.proceed();
            return rsp;
        } finally {
            stopWatch.stop();
            // 记录方法执行信息
            // DecimalFormat 模式说明：
            //   #：可选数字位（如果是0则不显示）
            //   ,：千分位分隔符
            //   例如：1234 格式化为 "1,234"
            String formattedTime = new DecimalFormat("#,###").format(stopWatch.getLastTaskTimeNanos() / 1000 / 1000);
            log.info("========== 包路径切面执行完成 ==========");
            log.info("方法名：{}，耗时：{} 毫秒，入参：{}，结果：{}",
                    joinPoint.getSignature().getName(),
                    formattedTime,
                    param,
                    rsp);
        }
    }

    /**
     * 基于 @DoneTime 注解的环绕通知
     * <p>
     * 拦截所有标注了 @DoneTime 注解的方法，统计执行时间。
     * <p>
     * <h3>执行流程：</h3>
     * <ol>
     *     <li>创建计时器并开始计时</li>
     *     <li>获取方法参数和类名、方法名</li>
     *     <li>执行目标方法（joinPoint.proceed()）</li>
     *     <li>停止计时并记录执行信息</li>
     * </ol>
     * <p>
     * <h3>日志信息：</h3>
     * <ul>
     *     <li>类名：目标方法所在的类</li>
     *     <li>方法名：目标方法的名称</li>
     *     <li>耗时：方法执行时间（毫秒，带千分位分隔符）</li>
     *     <li>入参：方法参数数组</li>
     *     <li>结果：方法返回值</li>
     * </ul>
     *
     * @param joinPoint 连接点，包含目标方法的信息
     * @return 方法执行结果
     * @throws Throwable 如果方法执行失败
     */
    @Around("point2()")
    public Object aroundByAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("========== @DoneTime 注解切面开始执行 ==========");
        
        // 计时器：使用 Hutool 的 StopWatch 进行高精度计时
        StopWatch stopWatch = new StopWatch();
        // 方法参数：获取目标方法的参数数组
        Object[] param = joinPoint.getArgs();
        // 方法结果：用于存储方法返回值
        Object rsp = null;
        
        // 获取方法签名和类名
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.debug("拦截方法: {}.{}", className, methodName);

        try {
            stopWatch.start();
            log.debug("开始执行方法: {}.{}", className, methodName);
            rsp = joinPoint.proceed();
            log.debug("方法执行完成: {}.{}", className, methodName);
            return rsp;
        } finally {
            stopWatch.stop();
            // DecimalFormat 模式说明：
            //   #：可选数字位（如果是0则不显示）
            //   ,：千分位分隔符
            //   例如：1234 格式化为 "1,234"
            //   时间单位转换：纳秒 -> 微秒 -> 毫秒（除以 1000 / 1000）
            String formattedTime = new DecimalFormat("#,###").format(stopWatch.getLastTaskTimeNanos() / 1000 / 1000);
            
            log.info("========== @DoneTime 注解切面执行完成 ==========");
            log.info("【@DoneTime 注解切面】类名：{}，方法名：{}，耗时：{} 毫秒，入参：{}，结果：{}",
                    className,
                    methodName,
                    formattedTime,
                    param,
                    rsp);
        }
    }
}
