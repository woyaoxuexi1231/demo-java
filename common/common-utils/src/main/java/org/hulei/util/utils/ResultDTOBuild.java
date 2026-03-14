package org.hulei.util.utils;

import org.hulei.util.dto.ResultDTO;

/**
 * 统一响应结果构建工具类
 * <p>
 * 提供静态方法用于快速构建 {@link ResultDTO} 对象，包括成功响应和错误响应。
 * 简化统一响应格式的创建过程。
 * <p>
 * <h3>功能说明：</h3>
 * <ul>
 *     <li>构建默认成功响应（无数据）</li>
 *     <li>构建成功响应（带数据）</li>
 *     <li>构建错误响应（使用默认错误码 -1）</li>
 *     <li>构建错误响应（自定义错误码）</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>Controller 层统一返回格式</li>
 *     <li>异常处理中构建错误响应</li>
 *     <li>业务逻辑中构建响应结果</li>
 * </ul>
 *
 * @author hulei42031
 * @since 2022-06-10 16:03
 * @version 1.0
 */
public class ResultDTOBuild {

    /**
     * 成功状态码
     */
    private static final int SUCCESS_CODE = 200;

    /**
     * 成功消息
     */
    private static final String SUCCESS_MSG = "success";

    /**
     * 默认错误码
     */
    private static final int DEFAULT_ERROR_CODE = -1;

    /**
     * 构建默认成功响应（无数据）
     * <p>
     * 创建一个状态码为 200、消息为 "success"、数据为 null 的成功响应。
     * <p>
     * <h3>使用场景：</h3>
     * <ul>
     *     <li>操作成功但无需返回数据</li>
     *     <li>删除、更新等操作的成功响应</li>
     * </ul>
     *
     * @return ResultDTO 成功响应对象（无数据）
     */
    public static ResultDTO<?> resultDefaultBuild() {
        ResultDTO<?> resultDTO = new ResultDTO<>();
        resultDTO.setCode(SUCCESS_CODE);
        resultDTO.setMsg(SUCCESS_MSG);
        return resultDTO;
    }

    /**
     * 构建成功响应（带数据）
     * <p>
     * 创建一个状态码为 200、消息为 "success"、包含指定数据的成功响应。
     * <p>
     * <h3>使用场景：</h3>
     * <ul>
     *     <li>查询操作成功返回数据</li>
     *     <li>创建操作成功返回创建的对象</li>
     * </ul>
     *
     * @param <Rsp> 响应数据的类型
     * @param rsp 响应数据，可以是任意类型
     * @return ResultDTO 成功响应对象（包含数据）
     */
    public static <Rsp> ResultDTO<Rsp> resultSuccessBuild(Rsp rsp) {
        ResultDTO<Rsp> resultDTO = new ResultDTO<>();
        resultDTO.setCode(SUCCESS_CODE);
        resultDTO.setMsg(SUCCESS_MSG);
        resultDTO.setData(rsp);
        return resultDTO;
    }

    /**
     * 构建错误响应（使用默认错误码）
     * <p>
     * 创建一个状态码为 -1、消息为指定错误信息的错误响应。
     * <p>
     * <h3>使用场景：</h3>
     * <ul>
     *     <li>业务逻辑错误</li>
     *     <li>参数验证失败</li>
     *     <li>通用错误响应</li>
     * </ul>
     *
     * @param <Rsp> 响应数据的类型（通常为 Void 或 null）
     * @param msg 错误消息
     * @return ResultDTO 错误响应对象
     */
    public static <Rsp> ResultDTO<Rsp> resultErrorBuild(String msg) {
        ResultDTO<Rsp> resultDTO = new ResultDTO<>();
        resultDTO.setCode(DEFAULT_ERROR_CODE);
        resultDTO.setMsg(msg);
        return resultDTO;
    }

    /**
     * 构建错误响应（自定义错误码）
     * <p>
     * 创建一个状态码为指定值、消息为指定错误信息的错误响应。
     * <p>
     * <h3>注意事项：</h3>
     * <ul>
     *     <li>当前实现中，即使传入自定义错误码，仍使用默认错误码 -1</li>
     *     <li>建议修改实现，使用传入的 code 参数</li>
     * </ul>
     * <p>
     * <h3>使用场景：</h3>
     * <ul>
     *     <li>需要特定错误码的业务场景</li>
     *     <li>与前端约定的错误码体系</li>
     * </ul>
     *
     * @param <Rsp> 响应数据的类型（通常为 Void 或 null）
     * @param code 错误码（注意：当前实现未使用此参数，仍使用默认错误码 -1）
     * @param msg 错误消息
     * @return ResultDTO 错误响应对象
     */
    public static <Rsp> ResultDTO<Rsp> resultErrorBuild(int code, String msg) {
        ResultDTO<Rsp> resultDTO = new ResultDTO<>();
        resultDTO.setCode(code);  // 修复：使用传入的 code 参数
        resultDTO.setMsg(msg);
        return resultDTO;
    }
}
