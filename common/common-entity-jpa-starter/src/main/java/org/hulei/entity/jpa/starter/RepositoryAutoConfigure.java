package org.hulei.entity.jpa.starter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA Repository 自动配置类
 * <p>
 * 配置 JPA 实体扫描和 Repository 扫描，实现自动配置功能。
 * 包括实体类扫描、Repository 接口扫描和组件扫描。
 * <p>
 * <h3>功能说明：</h3>
 * <ul>
 *     <li><strong>@EntityScan</strong>：扫描 JPA 实体类（org.hulei.entity.jpa.pojo）</li>
 *     <li><strong>@EnableJpaRepositories</strong>：启用 JPA Repository 并扫描接口（自动引入的关键）</li>
 *     <li><strong>@ComponentScan</strong>：扫描组件，解决编译器 Bean 报错</li>
 * </ul>
 * <p>
 * <h3>扫描路径：</h3>
 * <ul>
 *     <li>实体类：{@code org.hulei.entity.jpa.pojo}</li>
 *     <li>Repository：{@code org.hulei.entity.jpa.starter.dao}</li>
 *     <li>组件：{@code org.hulei.entity.jpa.starter.dao}</li>
 * </ul>
 * <p>
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>{@code @EnableJpaRepositories} 是自动引入的关键，必须配置</li>
 *     <li>{@code @ComponentScan} 主要用于解决编译器的 Bean 报错</li>
 *     <li>确保实体类和 Repository 接口在指定的包路径下</li>
 * </ul>
 *
 * @author hulei
 * @since 2025/8/7 22:01
 * @version 1.0
 */
@Slf4j
@EntityScan(basePackages = {"org.hulei.entity.jpa.pojo"})
@EnableJpaRepositories(basePackages = "org.hulei.entity.jpa.starter.dao")
@ComponentScan(basePackages = "org.hulei.entity.jpa.starter.dao")
@Configuration
public class RepositoryAutoConfigure {

    /**
     * 构造函数
     * <p>
     * 当配置类被加载时，记录 JPA Repository 自动配置的日志。
     */
    public RepositoryAutoConfigure() {
        log.info("启用 JPA Repository 自动配置");
        log.debug("实体类扫描路径: org.hulei.entity.jpa.pojo");
        log.debug("Repository 扫描路径: org.hulei.entity.jpa.starter.dao");
    }
}
