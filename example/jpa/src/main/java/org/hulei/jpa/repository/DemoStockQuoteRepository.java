package org.hulei.jpa.repository;

import org.hulei.entity.jpa.pojo.DemoStockQuote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * @author hulei
 * @since 2026/3/14 18:06
 */

@Repository
public interface DemoStockQuoteRepository extends JpaRepository<DemoStockQuote, Long> {

    // ==================== 方法命名查询 ====================

    /**
     * 根据代码查询（精确匹配）
     */
    List<DemoStockQuote> findByCode(String code);

    /**
     * 根据名称模糊查询
     */
    List<DemoStockQuote> findByNameContaining(String name);

    /**
     * 根据价格范围查询
     */
    List<DemoStockQuote> findByCurrentPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 根据来源查询
     */
    List<DemoStockQuote> findBySource(String source);

    /**
     * 根据创建时间范围查询
     */
    List<DemoStockQuote> findByCreatedAtBetween(Instant startTime, Instant endTime);

    /**
     * 组合条件查询（代码 + 名称）
     */
    List<DemoStockQuote> findByCodeAndNameContaining(String code, String name);

    /**
     * 按价格排序查询
     */
    List<DemoStockQuote> findByOrderByCurrentPriceDesc();

    /**
     * 分页查询按代码
     */
    Page<DemoStockQuote> findByCode(String code, Pageable pageable);

    /**
     * 检查代码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 统计某代码的记录数
     */
    long countByCode(String code);

    /**
     * 删除某代码的所有记录
     */
    void deleteByCode(String code);

    // ==================== @Query 注解查询 ====================

    /**
     * JPQL 查询 - 根据代码查询
     */
    @Query("SELECT d FROM DemoStockQuote d WHERE d.code = :code")
    List<DemoStockQuote> queryByCode(@Param("code") String code);

    /**
     * JPQL 查询 - 根据名称模糊查询
     */
    @Query("SELECT d FROM DemoStockQuote d WHERE d.name LIKE %:name%")
    List<DemoStockQuote> queryByNameContaining(@Param("name") String name);

    /**
     * JPQL 查询 - 价格范围查询
     */
    @Query("SELECT d FROM DemoStockQuote d WHERE d.currentPrice BETWEEN :minPrice AND :maxPrice")
    List<DemoStockQuote> queryByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * JPQL 查询 - 动态多条件查询（分页）
     */
    @Query("SELECT d FROM DemoStockQuote d WHERE " +
            "(:code IS NULL OR d.code = :code) AND " +
            "(:name IS NULL OR d.name LIKE %:name%) AND " +
            "(:minPrice IS NULL OR d.currentPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR d.currentPrice <= :maxPrice) AND " +
            "(:source IS NULL OR d.source = :source)")
    Page<DemoStockQuote> findWithDynamicConditions(
            @Param("code") String code,
            @Param("name") String name,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("source") String source,
            Pageable pageable);

    /**
     * JPQL 查询 - 动态多条件查询（不分页，返回 List）
     */
    @Query("SELECT d FROM DemoStockQuote d WHERE " +
            "(:code IS NULL OR d.code = :code) AND " +
            "(:name IS NULL OR d.name LIKE %:name%) AND " +
            "(:minPrice IS NULL OR d.currentPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR d.currentPrice <= :maxPrice) AND " +
            "(:source IS NULL OR d.source = :source)")
    List<DemoStockQuote> findWithDynamicConditionsNoPage(
            @Param("code") String code,
            @Param("name") String name,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("source") String source);

    /**
     * 原生 SQL 查询 - 根据代码查询
     */
    @Query(value = "SELECT * FROM test.demo_stock_quotes WHERE code = :code", nativeQuery = true)
    List<DemoStockQuote> queryByCodeNative(@Param("code") String code);

    /**
     * 原生 SQL 查询 - 价格范围查询
     */
    @Query(value = "SELECT * FROM test.demo_stock_quotes WHERE current_price BETWEEN :minPrice AND :maxPrice", nativeQuery = true)
    List<DemoStockQuote> queryByPriceRangeNative(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * 原生 SQL 查询 - 统计总数
     */
    @Query(value = "SELECT COUNT(*) FROM test.demo_stock_quotes", nativeQuery = true)
    long countTotal();

    /**
     * 原生 SQL 查询 - 按代码统计
     */
    @Query(value = "SELECT COUNT(*) FROM test.demo_stock_quotes WHERE code = :code", nativeQuery = true)
    long countByCodeNative(@Param("code") String code);

    // ==================== 更新操作 ====================

    /**
     * JPQL 更新 - 批量更新价格
     */
    @Modifying
    @Transactional
    @Query("UPDATE DemoStockQuote d SET d.currentPrice = :newPrice WHERE d.code = :code")
    int updatePriceByCode(@Param("code") String code, @Param("newPrice") BigDecimal newPrice);

    /**
     * 原生 SQL 更新 - 批量更新来源
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE test.demo_stock_quotes SET source = :source WHERE code = :code", nativeQuery = true)
    int updateSourceByCode(@Param("code") String code, @Param("source") String source);

    /**
     * JPQL 更新 - 批量更新涨跌幅
     */
    @Modifying
    @Transactional
    @Query("UPDATE DemoStockQuote d SET d.changePercent = :changePercent, d.changeAmount = :changeAmount WHERE d.code = :code")
    int updateChangeInfo(@Param("code") String code, 
                         @Param("changePercent") BigDecimal changePercent, 
                         @Param("changeAmount") BigDecimal changeAmount);

    /**
     * 原生 SQL 更新 - 批量更新成交量和成交额
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE test.demo_stock_quotes SET volume = :volume, turnover = :turnover WHERE code = :code", nativeQuery = true)
    int updateVolumeAndTurnover(@Param("code") String code, 
                                 @Param("volume") Long volume, 
                                 @Param("turnover") BigDecimal turnover);

    // ==================== 聚合查询 ====================

    /**
     * JPQL 聚合 - 查询平均价格
     */
    @Query("SELECT AVG(d.currentPrice) FROM DemoStockQuote d")
    BigDecimal getAveragePrice();

    /**
     * JPQL 聚合 - 查询最高价格
     */
    @Query("SELECT MAX(d.currentPrice) FROM DemoStockQuote d")
    BigDecimal getMaxPrice();

    /**
     * JPQL 聚合 - 查询最低价格
     */
    @Query("SELECT MIN(d.currentPrice) FROM DemoStockQuote d")
    BigDecimal getMinPrice();

    /**
     * 原生 SQL 聚合 - 按代码分组统计
     */
    @Query(value = "SELECT code, COUNT(*) as count, AVG(current_price) as avg_price FROM test.demo_stock_quotes GROUP BY code", nativeQuery = true)
    List<Object[]> getStatisticsByCode();

    /**
     * JPQL 聚合 - 统计总记录数
     */
    @Query("SELECT COUNT(d) FROM DemoStockQuote d")
    long countAllRecords();
}
