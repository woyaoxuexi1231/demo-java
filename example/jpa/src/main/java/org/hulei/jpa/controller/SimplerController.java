package org.hulei.jpa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hulei.entity.jpa.pojo.DemoStockQuote;
import org.hulei.jpa.repository.DemoStockQuoteRepository;
import org.hulei.jpa.dto.StockQuotePageQryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * JPA 简单控制器
 *
 * <p>演示 Spring Data JPA 的基本功能，包括分页查询、条件查询等</p>
 *
 * @author hulei
 * @since 2024/10/10 22:58
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SimplerController {

    private final DemoStockQuoteRepository repository;

    // ==================== 新增操作 ====================

    /**
     * 新增单条股票行情数据
     *
     * @param demoStockQuote 股票行情对象
     * @return 新增后的对象（包含生成的 ID）
     */
    @PostMapping("/stock-quotes")
    public DemoStockQuote create(@RequestBody DemoStockQuote demoStockQuote) {
        // 设置创建时间
        demoStockQuote.setCreatedAt(Instant.now());
        return repository.save(demoStockQuote);
    }

    /**
     * 批量新增股票行情数据
     *
     * @param stockQuotes 股票行情列表
     * @return 新增后的对象列表
     */
    @PostMapping("/stock-quotes/batch")
    public List<DemoStockQuote> batchCreate(@RequestBody List<DemoStockQuote> stockQuotes) {
        // 设置创建时间
        Instant now = Instant.now();
        stockQuotes.forEach(quote -> quote.setCreatedAt(now));
        return repository.saveAll(stockQuotes);
    }

    // ==================== 查询操作 ====================

    /**
     * 分页查询股票行情数据
     *
     * @param qryDTO 查询参数对象（包含分页参数和筛选条件）
     * @return 分页结果
     */
    @PostMapping("/stock-quotes/page")
    public Page<DemoStockQuote> pageQuery(@RequestBody StockQuotePageQryDTO qryDTO) {
        // 创建分页对象，按 ID 降序排序
        PageRequest pageRequest = PageRequest.of(
                qryDTO.getPageNum() - 1,
                qryDTO.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );

        // 根据条件调用不同的 repository 方法
        String code = qryDTO.getCode();
        String name = qryDTO.getName();
        BigDecimal minPrice = qryDTO.getMinPrice();
        BigDecimal maxPrice = qryDTO.getMaxPrice();
        String source = qryDTO.getSource();
        
        if (code != null || name != null || minPrice != null || maxPrice != null || source != null) {
            return repository.findWithDynamicConditions(code, name, minPrice, maxPrice, source, pageRequest);
        } else {
            return repository.findAll(pageRequest);
        }
    }

    /**
     * 根据 ID 查询详情
     *
     * @param id 主键 ID
     * @return 股票行情详情
     */
    @GetMapping("/stock-quotes/{id}")
    public DemoStockQuote getById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到股票行情数据，ID: " + id));
    }

    /**
     * 根据代码查询列表
     *
     * @param code 股票代码
     * @return 股票行情列表
     */
    @GetMapping("/stock-quotes/code/{code}")
    public List<DemoStockQuote> listByCode(@PathVariable String code) {
        return repository.findByCode(code);
    }

    /**
     * 条件查询列表（GET 方式）
     *
     * @param code     股票代码（可选）
     * @param name     股票名称（可选，模糊查询）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @param source   数据来源（可选）
     * @return 股票行情列表
     */
    @GetMapping("/stock-quotes/list")
    public List<DemoStockQuote> listByConditions(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String source) {
        
        // 根据条件调用不同的 repository 方法
        if (code != null || name != null || minPrice != null || maxPrice != null || source != null) {
            return repository.findWithDynamicConditionsNoPage(code, name, minPrice, maxPrice, source);
        } else {
            return repository.findAll();
        }
    }

    // ==================== 更新操作 ====================

    /**
     * 全量更新股票行情数据
     *
     * @param id             主键 ID
     * @param demoStockQuote 股票行情对象
     * @return 更新后的对象
     */
    @PostMapping("/stock-quotes/update/{id}")
    public DemoStockQuote update(
            @PathVariable Long id,
            @RequestBody DemoStockQuote demoStockQuote) {
        
        // 先查询是否存在
        DemoStockQuote existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到股票行情数据，ID: " + id));
        
        // 更新字段
        existing.setCode(demoStockQuote.getCode());
        existing.setName(demoStockQuote.getName());
        existing.setCurrentPrice(demoStockQuote.getCurrentPrice());
        existing.setChangePercent(demoStockQuote.getChangePercent());
        existing.setChangeAmount(demoStockQuote.getChangeAmount());
        existing.setVolume(demoStockQuote.getVolume());
        existing.setTurnover(demoStockQuote.getTurnover());
        existing.setHigh(demoStockQuote.getHigh());
        existing.setLow(demoStockQuote.getLow());
        existing.setOpenPrice(demoStockQuote.getOpenPrice());
        existing.setPreClose(demoStockQuote.getPreClose());
        existing.setSource(demoStockQuote.getSource());
        existing.setDataTime(demoStockQuote.getDataTime());
        
        return repository.save(existing);
    }

    /**
     * 部分更新股票行情数据
     *
     * @param id             主键 ID
     * @param demoStockQuote 股票行情对象（只传需要更新的字段）
     * @return 更新后的对象
     */
    @PostMapping("/stock-quotes/partial-update/{id}")
    public DemoStockQuote partialUpdate(
            @PathVariable Long id,
            @RequestBody DemoStockQuote demoStockQuote) {
        
        DemoStockQuote existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到股票行情数据，ID: " + id));
        
        // 只更新非空字段
        if (demoStockQuote.getCode() != null) {
            existing.setCode(demoStockQuote.getCode());
        }
        if (demoStockQuote.getName() != null) {
            existing.setName(demoStockQuote.getName());
        }
        if (demoStockQuote.getCurrentPrice() != null) {
            existing.setCurrentPrice(demoStockQuote.getCurrentPrice());
        }
        if (demoStockQuote.getChangePercent() != null) {
            existing.setChangePercent(demoStockQuote.getChangePercent());
        }
        if (demoStockQuote.getChangeAmount() != null) {
            existing.setChangeAmount(demoStockQuote.getChangeAmount());
        }
        if (demoStockQuote.getVolume() != null) {
            existing.setVolume(demoStockQuote.getVolume());
        }
        if (demoStockQuote.getTurnover() != null) {
            existing.setTurnover(demoStockQuote.getTurnover());
        }
        if (demoStockQuote.getHigh() != null) {
            existing.setHigh(demoStockQuote.getHigh());
        }
        if (demoStockQuote.getLow() != null) {
            existing.setLow(demoStockQuote.getLow());
        }
        if (demoStockQuote.getOpenPrice() != null) {
            existing.setOpenPrice(demoStockQuote.getOpenPrice());
        }
        if (demoStockQuote.getPreClose() != null) {
            existing.setPreClose(demoStockQuote.getPreClose());
        }
        if (demoStockQuote.getSource() != null) {
            existing.setSource(demoStockQuote.getSource());
        }
        if (demoStockQuote.getDataTime() != null) {
            existing.setDataTime(demoStockQuote.getDataTime());
        }
        
        return repository.save(existing);
    }

    /**
     * 批量更新价格（使用 Repository 的 JPQL 更新）
     *
     * @param params   参数 Map，包含 code 和 newPrice
     * @return 受影响的记录数
     */
    @PostMapping("/stock-quotes/batch-update-price")
    public int batchUpdatePrice(@RequestBody Map<String, Object> params) {
        String code = (String) params.get("code");
        BigDecimal newPrice = (BigDecimal) params.get("newPrice");
        return repository.updatePriceByCode(code, newPrice);
    }

    // ==================== 删除操作 ====================

    /**
     * 根据 ID 删除单条数据
     *
     * @param id 主键 ID
     */
    @PostMapping("/stock-quotes/delete/{id}")
    public void deleteById(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("未找到股票行情数据，ID: " + id);
        }
        repository.deleteById(id);
        log.info("删除成功，ID: {}", id);
    }

    /**
     * 根据代码批量删除
     *
     * @param code 股票代码
     */
    @PostMapping("/stock-quotes/delete-by-code")
    public void deleteByCode(@RequestParam String code) {
        List<DemoStockQuote> list = repository.findByCode(code);
        if (list.isEmpty()) {
            throw new RuntimeException("未找到股票代码的数据，Code: " + code);
        }
        repository.deleteAll(list);
        log.info("删除成功，Code: {}, 删除数量：{}", code, list.size());
    }

    /**
     * 批量删除（根据 ID 列表）
     *
     * @param ids ID 列表
     */
    @PostMapping("/stock-quotes/batch-delete")
    public void batchDelete(@RequestBody List<Long> ids) {
        List<DemoStockQuote> list = repository.findAllById(ids);
        if (list.isEmpty()) {
            throw new RuntimeException("未找到要删除的数据");
        }
        repository.deleteAll(list);
        log.info("批量删除成功，删除数量：{}", list.size());
    }

    /**
     * 删除所有数据
     */
    @PostMapping("/stock-quotes/delete-all")
    public void deleteAll() {
        long count = repository.count();
        repository.deleteAll();
        log.info("删除所有数据，删除数量：{}", count);
    }
}

