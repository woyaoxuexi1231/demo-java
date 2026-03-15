package org.hulei.gateway;

import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 一个过滤器工厂类,也是用于打印请求时长,顺便打印参数
 *
 * @author hl
 * @since 2023/5/25 0:36
 */

@Slf4j
@Component
public class RequestTimeGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestTimeGatewayFilterFactory.Config> implements Ordered {

    /**
     * 用于在请求的属性中存储请求开始时间的键。
     */
    private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";

    /**
     * 用于指定配置类中参数的顺序。
     *
     * @return list
     */
    @Override
    public List<String> shortcutFieldOrder() {
        // 这个方法主要决定了配置类的参数顺序,然后决定注入到Config的哪个参数
        return CollectionUtil.newArrayList("withParams", "prefix", "suffix");
        // return CollectionUtil.newArrayList("withParams", "suffix", "prefix");
    }

    public RequestTimeGatewayFilterFactory() {
        // 它的目的是初始化过滤器工厂并指定配置类的类型。
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        log.info("RequestTime start");

        return (exchange, chain) -> {
            long startTime = System.currentTimeMillis();

            return chain.filter(exchange)
                    .doOnSubscribe(sub -> log.info("Request subscribed"))
                    .doOnSuccess(v -> log.info("Request success"))
                    .doOnError(e -> log.error("Request error", e))
                    .then(Mono.fromRunnable(() -> {
                        StringBuilder sb = new StringBuilder(exchange.getRequest().getURI().getRawPath())
                                .append(": ")
                                .append(System.currentTimeMillis() - startTime)
                                .append("ms");
                        if (config.isWithParams()) {
                            sb.append(" params:").append(exchange.getRequest().getQueryParams());
                        }
                        log.info("prefix: {}, suffix: {}, withParams: {}",
                                config.prefix, config.suffix, config.withParams);
                        log.info(sb.toString());
                    }));
        };
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }


    /**
     * 这个类的作用呢
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Config {

        private boolean withParams;

        private String prefix;

        private String suffix;

    }
}
