package org.hulei.util.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 分页查询请求 DTO
 * <p>
 * 用于封装分页查询的请求参数，包括页码和每页记录数。
 * 使用 Jakarta Bean Validation 进行参数校验。
 * <p>
 * <h3>字段说明：</h3>
 * <ul>
 *     <li><strong>pageNum</strong>：页码（从 1 开始），不能为 null</li>
 *     <li><strong>pageSize</strong>：每页记录数，不能为 null</li>
 * </ul>
 * <p>
 * <h3>参数校验：</h3>
 * <ul>
 *     <li>使用 {@code @NotNull} 确保参数不为 null</li>
 *     <li>建议添加 {@code @Positive} 或 {@code @Min(1)} 确保参数为正数</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>Controller 层接收分页查询参数</li>
 *     <li>Service 层进行分页查询</li>
 * </ul>
 *
 * @author woaixuexi
 * @since 2024/3/10 14:25
 * @version 1.0
 */
@Data
public class PageQryReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码（从 1 开始）
     * <p>
     * 不能为 null，建议为正整数
     */
    @NotNull(message = "分页参数 pageNum 不能为 null")
    @Positive(message = "分页参数 pageNum 必须为正数")
    private Integer pageNum = 1;

    /**
     * 每页记录数
     * <p>
     * 不能为 null，建议为正整数
     */
    @NotNull(message = "分页参数 pageSize 不能为 null")
    @Positive(message = "分页参数 pageSize 必须为正数")
    private Integer pageSize = 10;
}
