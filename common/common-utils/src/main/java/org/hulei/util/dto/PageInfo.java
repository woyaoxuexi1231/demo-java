package org.hulei.util.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页信息封装类
 * <p>
 * 用于封装分页查询的结果信息，包括当前页、每页数量、总页数、总记录数、结果集等。
 * 支持泛型，可以封装任意类型的数据列表。
 * <p>
 * <h3>字段说明：</h3>
 * <ul>
 *     <li><strong>pageNum</strong>：当前页码（从 1 开始）</li>
 *     <li><strong>pageSize</strong>：每页记录数</li>
 *     <li><strong>pages</strong>：总页数</li>
 *     <li><strong>prePage</strong>：上一页页码</li>
 *     <li><strong>nextPage</strong>：下一页页码</li>
 *     <li><strong>hasPreviousPage</strong>：是否有上一页</li>
 *     <li><strong>hasNextPage</strong>：是否有下一页</li>
 *     <li><strong>total</strong>：总记录数</li>
 *     <li><strong>list</strong>：当前页的数据列表</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>分页查询结果封装</li>
 *     <li>前端分页组件数据展示</li>
 *     <li>分页导航信息计算</li>
 * </ul>
 *
 * @param <T> 分页数据的类型
 * @author hulei
 * @since 2024/10/15
 * @version 1.0
 */
@Data
public class PageInfo<T> {

    /**
     * 当前页码（从 1 开始）
     */
    private int pageNum;

    /**
     * 每页记录数
     */
    private int pageSize;

    /**
     * 总页数
     */
    private int pages;

    /**
     * 上一页页码
     */
    private int prePage;

    /**
     * 下一页页码
     */
    private int nextPage;

    /**
     * 是否有上一页
     */
    private boolean hasPreviousPage = false;

    /**
     * 是否有下一页
     */
    private boolean hasNextPage = false;

    /**
     * 总记录数
     */
    protected long total;

    /**
     * 当前页的数据列表
     */
    protected List<T> list;
}