package org.hulei.entity.mybatisplus.starter;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 自动配置类
 * <p>
 * 配置 MyBatis-Plus 的相关功能，包括 Mapper 扫描和分页插件配置。
 * <p>
 * <h3>功能说明：</h3>
 * <ul>
 *     <li>扫描 MyBatis Mapper 接口（org.hulei.entity.mybatisplus.starter.mapper）</li>
 *     <li>配置 MyBatis-Plus 拦截器，启用分页功能</li>
 *     <li>指定数据库类型为 MySQL</li>
 * </ul>
 * <p>
 * <h3>分页插件说明：</h3>
 * <ul>
 *     <li>使用 {@link PaginationInnerInterceptor} 实现分页功能</li>
 *     <li>支持多种数据库类型，当前配置为 MySQL</li>
 *     <li>自动处理分页查询的 SQL 语句</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>分页查询数据</li>
 *     <li>使用 MyBatis-Plus 的分页功能</li>
 * </ul>
 * <p>
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>确保数据库类型配置正确（当前为 MySQL）</li>
 *     <li>如果使用其他数据库，需要修改 {@code DbType} 参数</li>
 *     <li>Mapper 接口必须在指定的包路径下</li>
 * </ul>
 *
 * @author hulei
 * @since 2025/8/8 0:51
 * @version 1.0
 */
@Slf4j
@MapperScan(basePackages = "org.hulei.entity.mybatisplus.starter.mapper")
@Configuration
public class AutoConfigure {

    /**
     * 配置 MyBatis-Plus 拦截器
     * <p>
     * 创建并配置 {@link MybatisPlusInterceptor}，添加分页拦截器。
     * 分页拦截器使用 MySQL 数据库类型。
     * <p>
     * <h3>功能说明：</h3>
     * <ul>
     *     <li>启用 MyBatis-Plus 分页功能</li>
     *     <li>自动处理分页查询的 SQL 语句</li>
     *     <li>支持使用 {@code Page} 对象进行分页查询</li>
     * </ul>
     * <p>
     * <h3>使用示例：</h3>
     * <pre>
     * {@code
     * Page<User> page = new Page<>(1, 10);
     * userMapper.selectPage(page, null);
     * }
     * </pre>
     * <p>
     * <h3>注意事项：</h3>
     * <ul>
     *     <li>当前配置为 MySQL 数据库，如果使用其他数据库需要修改</li>
     *     <li>支持的数据库类型：MYSQL、ORACLE、POSTGRE_SQL、H2、SQL_SERVER 等</li>
     * </ul>
     *
     * @return MybatisPlusInterceptor MyBatis-Plus 拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info("配置 MyBatis-Plus 拦截器，启用分页功能（数据库类型: MySQL）");
        
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页拦截器，指定数据库类型为 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        log.debug("MyBatis-Plus 拦截器配置完成");
        return interceptor;
    }
}
