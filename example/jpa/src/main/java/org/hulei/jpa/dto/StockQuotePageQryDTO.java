package org.hulei.jpa.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hulei.util.dto.PageQryReqDTO;

import java.math.BigDecimal;

/**
 * 股票行情分页查询请求 DTO
 * <p>
 * 继承 {@link PageQryReqDTO}，包含分页参数和业务筛选条件
 * </p>
 *
 * @author hulei
 * @since 2026/3/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StockQuotePageQryDTO extends PageQryReqDTO {

    /**
     * 股票代码（可选，精确匹配）
     */
    private String code;

    /**
     * 股票名称（可选，模糊查询）
     */
    private String name;

    /**
     * 最低价格（可选）
     */
    private BigDecimal minPrice;

    /**
     * 最高价格（可选）
     */
    private BigDecimal maxPrice;

    /**
     * 数据来源（可选）
     */
    private String source;
}
