package org.hulei.gateway;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Cloud Gateway应用
 * <p>
 * 基于Spring WebFlux和Reactor模型构建的异步非阻塞API网关
 * </p>
 * <p>
 * 核心功能：
 * <ul>
 *   <li>请求路由：根据断言工厂匹配请求并路由到目标服务</li>
 *   <li>过滤器链处理：对请求和响应进行加工处理</li>
 *   <li>服务发现和负载均衡：集成服务注册中心实现服务发现</li>
 * </ul>
 * </p>
 * <p>
 * 断言工厂（Predicate Factory）：
 * <p>
 * 断言工厂用于判断请求是否符合某个条件，决定请求是否应该被当前路由规则匹配。
 * 常见断言工厂包括：After、Before、Between、Cookie、Header、Host、Method、Path、Query、RemoteAddr等。
 * </p>
 * </p>
 * <p>
 * 过滤器工厂（Filter Factory）：
 * <p>
 * 过滤器工厂用于对请求和响应进行加工处理，如添加/删除请求头、修改请求体、鉴权、限流、日志记录等。
 * 常见过滤器包括：AddRequestHeader、AddResponseHeader、RewritePath、Retry、Hystrix等。
 * </p>
 * </p>
 * <p>
 * 监控端点：
 * <ul>
 *   <li>GET /actuator/gateway/globalfilters - 查看全局过滤器</li>
 *   <li>GET /actuator/gateway/routefilters - 查看路由过滤器</li>
 * </ul>
 * </p>
 *
 * @author hulei
 * @since 2023/5/5
 */
@Slf4j
@RestController
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {

    /**
     * Spring应用上下文
     */
    static ApplicationContext applicationContext;

    /**
     * 应用启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        log.info("Spring Cloud Gateway应用开始启动");
        try {
            applicationContext = SpringApplication.run(GatewayApplication.class, args);
            log.info("Spring Cloud Gateway应用启动完成");
        } catch (Exception e) {
            log.error("Spring Cloud Gateway应用启动失败", e);
            System.exit(1);
        }
    }

    /**
     * 测试接口
     * <p>
     * 使用Sentinel进行限流保护的测试接口
     * </p>
     *
     * @param name 名称参数
     * @return 问候语
     */
    @SentinelResource(value = "yourApi", blockHandler = "handleBlock")
    @GetMapping("/hi")
    public String hi(@RequestParam("name") String name) {
        log.debug("处理hi请求, name={}", name);
        return "hello " + name;
    }

    /**
     * Sentinel限流降级处理方法
     * <p>
     * 当接口被限流时调用此方法
     * </p>
     *
     * @param name 名称参数
     * @param ex   限流异常
     * @return 降级响应
     */
    public String handleBlock(String name, BlockException ex) {
        log.warn("hi接口被限流, name={}", name, ex);
        return "hi 接口被限流了！";
    }
}
