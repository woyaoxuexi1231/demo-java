package org.hulei.entity.mybatisplus.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 批量执行工具类
 * <p>
 * 提供批量执行数据库操作的通用方法，使用 MyBatis 的 BATCH 执行模式提高性能。
 * 适用于需要批量插入、更新、删除等场景。
 * <p>
 * <h3>功能说明：</h3>
 * <ul>
 *     <li>使用 BATCH 执行模式，提高批量操作性能</li>
 *     <li>自动管理事务（提交和回滚）</li>
 *     <li>自动关闭 SqlSession</li>
 *     <li>异常处理和日志记录</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>批量插入数据</li>
 *     <li>批量更新数据</li>
 *     <li>批量删除数据</li>
 * </ul>
 * <p>
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>BATCH 模式下，不能正确返回每条语句的影响条数</li>
 *     <li>建议在数据量较大时使用，小批量操作可能不如普通模式高效</li>
 *     <li>如果数据量非常大，建议分批处理，避免内存溢出</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * List<User> users = ...;
 * BatchExecutor.exeBatch(sqlSessionTemplate, users, (session, user) -> {
 *     UserMapper mapper = session.getMapper(UserMapper.class);
 *     mapper.insert(user);
 * });
 * }
 * </pre>
 *
 * @author hulei
 * @since 2024/10/10 20:55
 * @version 1.0
 */
@Slf4j
public class BatchExecutor {

    /**
     * 批量执行数据库操作
     * <p>
     * 使用 MyBatis 的 BATCH 执行模式，对列表中的每个元素执行指定的操作。
     * 自动管理事务和资源，异常时自动回滚。
     * <p>
     * <h3>执行流程：</h3>
     * <ol>
     *     <li>创建 BATCH 模式的 SqlSession（自动提交为 false）</li>
     *     <li>遍历列表，对每个元素执行 consumer 操作</li>
     *     <li>提交事务</li>
     *     <li>关闭 SqlSession</li>
     * </ol>
     * <p>
     * <h3>异常处理：</h3>
     * <ul>
     *     <li>如果执行过程中发生异常，自动回滚事务</li>
     *     <li>记录错误日志，包含异常堆栈信息</li>
     *     <li>确保 SqlSession 被正确关闭</li>
     * </ul>
     * <p>
     * <h3>注意事项：</h3>
     * <ul>
     *     <li>BATCH 模式下，不能正确返回每条语句的影响条数</li>
     *     <li>如果数据量非常大，建议分批处理，避免内存溢出</li>
     *     <li>可以在 consumer 中调用 {@code sqlSession.flushStatements()} 手动刷新语句</li>
     *     <li>可以在 consumer 中调用 {@code sqlSession.clearCache()} 清理缓存</li>
     * </ul>
     *
     * @param <T> 列表类型，必须是 List 的子类
     * @param <R> 列表元素类型
     * @param sqlSessionTemplate SqlSessionTemplate 实例
     * @param list 要处理的数据列表
     * @param consumer 对每个元素执行的操作，接收 SqlSession 和元素作为参数
     */
    public static <T extends List<R>, R> void exeBatch(SqlSessionTemplate sqlSessionTemplate, 
                                                       T list, 
                                                       BiConsumer<SqlSession, R> consumer) {
        if (list == null || list.isEmpty()) {
            log.warn("批量执行列表为空，跳过执行");
            return;
        }

        log.debug("开始批量执行，数据量: {}", list.size());
        
        // 创建 BATCH 模式的 SqlSession，自动提交为 false
        SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory()
                .openSession(ExecutorType.BATCH, false);
        
        try {
            // 遍历列表，对每个元素执行操作
            for (R item : list) {
                consumer.accept(sqlSession, item);
            }
            
            // 可选：手动刷新语句（如果需要立即执行）
            // sqlSession.flushStatements();
            
            // 提交事务
            sqlSession.commit();
            log.info("批量执行成功，处理数据量: {}", list.size());
            
            // 可选：清理缓存，防止内存溢出（数据量很大时建议使用）
            // sqlSession.clearCache();
            
        } catch (Exception e) {
            // 异常回滚
            log.error("批量执行出现异常，数据量: {}", list.size(), e);
            sqlSession.rollback();
            throw new RuntimeException("批量执行失败", e);
        } finally {
            // 确保 SqlSession 被正确关闭
            sqlSession.close();
            log.debug("批量执行完成，SqlSession 已关闭");
        }
    }
}
