package org.hulei.gateway.mysql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 从 MySQL 数据库动态加载 Gateway 路由定义。
 * 支持保存、删除并自动发布 RefreshRoutesEvent 实现动态刷新。
 */
@Component
public class MysqlRouteDefinitionRepository implements RouteDefinitionRepository {

    private final GatewayRouteRepository repository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher publisher;

    public MysqlRouteDefinitionRepository(GatewayRouteRepository repository,
                                          ObjectMapper objectMapper,
                                          ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.publisher = publisher;
    }

    /**
     * 从数据库加载所有启用的路由定义。
     */
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<GatewayRoute> routes = repository.findByStatus(1);
        List<RouteDefinition> definitions = routes.stream()
                .map(this::convertToRouteDefinition)
                .collect(Collectors.toList());
        return Flux.fromIterable(definitions);
    }

    /**
     * 保存新路由到数据库并刷新路由缓存。
     */
    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            try {
                GatewayRoute entity = new GatewayRoute();
                entity.setId(r.getId());
                entity.setUri(r.getUri().toString());
                entity.setPredicates(objectMapper.writeValueAsString(r.getPredicates()));
                entity.setFilters(objectMapper.writeValueAsString(r.getFilters()));
                entity.setOrderNum(r.getOrder());
                entity.setStatus(1);
                repository.save(entity);
                // 发布刷新事件
                publisher.publishEvent(new RefreshRoutesEvent(this));
            } catch (Exception e) {
                return Mono.error(e);
            }
            return Mono.empty();
        });
    }

    /**
     * 删除数据库中指定路由并刷新。
     */
    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            repository.deleteById(id);
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.empty();
        });
    }

    /**
     * 实体转换为 RouteDefinition 对象。
     */
    private RouteDefinition convertToRouteDefinition(GatewayRoute entity) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(entity.getId());
        definition.setUri(URI.create(entity.getUri()));
        try {
            if (entity.getPredicates() != null && !entity.getPredicates().isEmpty()) {
                List<PredicateDefinition> predicates = objectMapper.readValue(
                        entity.getPredicates(), new TypeReference<List<PredicateDefinition>>() {
                        });
                definition.setPredicates(predicates);
            }
            if (entity.getFilters() != null && !entity.getFilters().isEmpty()) {
                List<FilterDefinition> filters = objectMapper.readValue(
                        entity.getFilters(), new TypeReference<List<FilterDefinition>>() {
                        });
                definition.setFilters(filters);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse route definitions from JSON", e);
        }
        definition.setOrder(entity.getOrderNum() == null ? 0 : entity.getOrderNum());
        return definition;
    }
}
