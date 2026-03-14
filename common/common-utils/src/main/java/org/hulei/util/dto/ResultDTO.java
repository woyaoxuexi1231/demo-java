package org.hulei.util.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * <p>
 * 用于统一封装 API 接口的响应结果，包含状态码、状态信息和数据。
 * 支持泛型，可以封装任意类型的数据。
 * <p>
 * <h3>字段说明：</h3>
 * <ul>
 *     <li><strong>code</strong>：状态码，200 表示成功，其他值表示失败</li>
 *     <li><strong>msg</strong>：状态信息，描述操作结果或错误信息</li>
 *     <li><strong>data</strong>：响应数据，泛型类型，可以是任意类型</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>统一 API 响应格式</li>
 *     <li>前后端数据交互</li>
 *     <li>异常处理和错误信息返回</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * // 成功响应
 * ResultDTO<User> result = ResultDTOBuild.resultSuccessBuild(user);
 * // code: 200, msg: "success", data: user
 *
 * // 错误响应
 * ResultDTO<?> result = ResultDTOBuild.resultErrorBuild("操作失败");
 * // code: -1, msg: "操作失败", data: null
 * }
 * </pre>
 *
 * @param <E> 响应数据的类型
 * @author hulei42031
 * @since 2022-06-10 15:55
 * @version 1.0
 */
@Data
public class ResultDTO<E> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     * <p>
     * 200 表示成功，其他值表示失败（如 -1）
     */
    private int code;

    /**
     * 状态信息
     * <p>
     * 描述操作结果或错误信息，如 "success"、"操作失败" 等
     */
    private String msg;

    /**
     * 响应数据
     * <p>
     * 泛型类型，可以是任意类型的数据，如实体对象、列表等
     */
    private E data;
}
