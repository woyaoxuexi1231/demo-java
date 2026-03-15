package org.hulei.gateway.config;

import org.hulei.gateway.RequestTimeGatewayFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Configuration;

/**
 * @projectName: study-demo
 * @package: com.hundsun.demo.springcloud.gateway.config
 * @className: BeanConfig
 * @description:
 * @author: h1123
 * @createDate: 2023/5/7 18:35
 */

@Slf4j
@Configuration
public class BeanConfig {

    // @Bean
    public RouteLocator requestTimeFilterRouteLocator(RouteLocatorBuilder builder, RequestTimeGatewayFilterFactory requestTimeGatewayFilterFactory) {

        /*
        🔑 RouteLocator 是干什么的？
            RouteLocator 是 路由定位器接口，
            👉 它的作用就是 提供一组路由规则（Route） 给 Gateway 用。
        Spring Cloud Gateway 的核心逻辑是：
          1.收到请求
          2.匹配路由（Route）
          3.执行对应的过滤器链
          4.转发到目标服务（下游服务）
        而 路由规则（Route） 就是靠 RouteLocator 提供的。
         */

        return builder.routes()
                .route( // 创建新路由,对于请求路径为 “/get” 的请求，只有第一个路由规则会被匹配到并执行，即使有第二个路由规则也匹配路径为 “/get”。因此，第二个路由规则将被忽略，不会被执行。
                        r -> r.path("/get") // 表示这个路由规则会匹配请求路径为 “/get” 的请求
                                .filters(f -> f.filter(requestTimeGatewayFilterFactory.apply(new RequestTimeGatewayFilterFactory.Config(true, "hello", "world")))
                                        .addRequestHeader("X-Response-Default-Foo", "Default-Bar") // 在转发请求的响应中添加了一个名为 X-Response-Default-Foo 值为 Default-Bar 的头信息
                                        .addRequestParameter("add-something", "hahaha") // 直接添加一个参数
                                )
                                .uri("https://httpbin.org") // 设置路由的 URI。
                )
                .route("netflix-provider-hi", r -> r
                        .path("/netflix-provider/hi") // 匹配请求路径
                        .filters(f -> f.stripPrefix(1)) // 去掉 eureka-client 这个路径
                        .uri("lb://NETFLIX-PROVIDER") // 转发到Eureka中名为SERVICE-NAME的服务
                )
                .build();

    }

}
