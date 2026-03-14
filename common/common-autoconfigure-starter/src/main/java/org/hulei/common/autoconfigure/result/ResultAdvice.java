package org.hulei.common.autoconfigure.result;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hulei.util.utils.ResultDTOBuild;

/**
 * 结果统一封装拦截器
 * <p>
 * 实现 MethodInterceptor 接口，使用 AOP Alliance 标准接口进行方法拦截。
 * 统一处理方法执行结果，将异常转换为统一的错误响应格式。
 * <p>
 * <h3>AOP Alliance 说明：</h3>
 * <ul>
 *     <li><strong>AOP Alliance</strong>：AOP 联盟标准接口，提供跨框架的 AOP 支持</li>
 *     <li><strong>MethodInterceptor</strong>：方法拦截器接口，用于拦截方法调用</li>
 *     <li><strong>兼容性</strong>：可以与 Spring AOP、AspectJ 等框架配合使用</li>
 * </ul>
 * <p>
 * <h3>功能特性：</h3>
 * <ul>
 *     <li><strong>异常捕获</strong>：捕获方法执行过程中的所有异常</li>
 *     <li><strong>统一响应</strong>：将异常转换为统一的错误响应格式</li>
 *     <li><strong>日志记录</strong>：记录异常信息，便于问题排查</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>统一 API 响应格式</li>
 *     <li>异常统一处理</li>
 *     <li>减少重复的异常处理代码</li>
 *     <li>提高代码的可维护性</li>
 * </ul>
 * <p>
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>需要在 Spring 配置中注册为 Bean</li>
 *     <li>可以通过 ProxyFactoryBean 或自动代理配置使用</li>
 *     <li>建议结合 @ControllerAdvice 使用，提供更完善的异常处理</li>
 * </ul>
 *
 * @author hulei42031
 * @since 2022-06-16 15:28
 * @version 1.0
 */
@Slf4j
public class ResultAdvice implements MethodInterceptor {

    /**
     * 拦截方法调用
     * <p>
     * 在目标方法执行前后进行处理，捕获异常并转换为统一的错误响应。
     * <p>
     * <h3>执行流程：</h3>
     * <ol>
     *     <li>执行目标方法（methodInvocation.proceed()）</li>
     *     <li>如果方法正常执行，返回方法返回值</li>
     *     <li>如果方法抛出异常，捕获异常并记录日志</li>
     *     <li>返回统一的错误响应（ResultDTOBuild.resultErrorBuild("failed")）</li>
     * </ol>
     * <p>
     * <h3>异常处理：</h3>
     * <ul>
     *     <li>捕获所有 Exception 类型的异常</li>
     *     <li>记录异常消息和堆栈信息</li>
     *     <li>返回统一的错误响应格式</li>
     * </ul>
     * <p>
     * <h3>注意事项：</h3>
     * <ul>
     *     <li>只捕获 Exception，不捕获 Error（如 OutOfMemoryError）</li>
     *     <li>错误消息固定为 "failed"，可以根据异常类型提供更详细的错误信息</li>
     *     <li>建议在生产环境中隐藏详细的异常信息</li>
     * </ul>
     *
     * @param methodInvocation 方法调用对象，包含目标方法的信息
     * @return 方法执行结果，或统一的错误响应
     * @throws Throwable 如果方法执行失败且无法处理
     */
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String className = methodInvocation.getMethod().getDeclaringClass().getSimpleName();
        String methodName = methodInvocation.getMethod().getName();
        log.debug("拦截方法调用: {}.{}", className, methodName);

        try {
            log.debug("开始执行方法: {}.{}", className, methodName);
            Object result = methodInvocation.proceed();
            log.debug("方法执行成功: {}.{}", className, methodName);
            return result;
        } catch (Exception e) {
            log.error("========== 方法执行异常 ==========");
            log.error("类名: {}", className);
            log.error("方法名: {}", methodName);
            log.error("异常类型: {}", e.getClass().getName());
            log.error("异常消息: {}", e.getMessage());
            log.error("方法执行异常", e);
            
            // 返回统一的错误响应格式
            // 注意：生产环境建议根据异常类型返回更详细的错误信息
            return ResultDTOBuild.resultErrorBuild("failed");
        }
    }
}
