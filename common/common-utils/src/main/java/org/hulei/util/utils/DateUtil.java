package org.hulei.util.utils;

import java.util.Date;
import java.util.TimeZone;

/**
 * 日期工具类
 * <p>
 * 提供日期相关的工具方法，包括获取指定时区的当前时间。
 * <p>
 * <h3>功能说明：</h3>
 * <ul>
 *     <li>获取指定时区（东八区，北京时间）的当前时间</li>
 * </ul>
 * <p>
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>当前实现存在时区处理问题，建议使用 {@link java.time} 包下的新日期时间 API</li>
 *     <li>推荐使用 {@link java.time.ZonedDateTime} 或 {@link java.time.LocalDateTime} 替代 {@link Date}</li>
 * </ul>
 *
 * @author woaixuexi
 * @since 2024/4/9 23:54
 * @version 1.0
 */
public class DateUtil {

    /**
     * 东八区时区标识（北京时间）
     */
    private static final String ASIA_SHANGHAI = "Asia/Shanghai";

    /**
     * 获取指定时区的当前时间
     * <p>
     * 获取东八区（北京时间）的当前时间。
     * <p>
     * <h3>注意事项：</h3>
     * <ul>
     *     <li>当前实现存在时区处理问题，{@code getRawOffset()} 返回的是标准时区偏移，可能不准确</li>
     *     <li>建议使用 {@link java.time.ZonedDateTime#now(java.time.ZoneId)} 替代</li>
     *     <li>例如：{@code ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))}</li>
     * </ul>
     *
     * @return Date 当前时间（东八区）
     */
    public static Date getDate() {
        // 设置时区为东八区（北京时间）
        TimeZone timeZone = TimeZone.getTimeZone(ASIA_SHANGHAI);
        // 获取当前时间
        Date now = new Date();
        // 根据指定时区获取当前时间
        // 注意：此实现可能不准确，建议使用 java.time 包下的新 API
        return new Date(now.getTime() + timeZone.getRawOffset());
    }
}
