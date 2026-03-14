package org.hulei.util.utils;

import com.google.common.base.CaseFormat;

/**
 * 字符串命名格式转换工具类
 * <p>
 * 提供字符串在不同命名格式之间转换的工具方法，基于 Google Guava 的 {@link CaseFormat}。
 * 支持小驼峰（lowerCamel）和下划线（lower_underscore）之间的转换。
 * <p>
 * <h3>功能说明：</h3>
 * <ul>
 *     <li>小驼峰转下划线：userName → user_name</li>
 *     <li>下划线转小驼峰：user_name → userName</li>
 * </ul>
 * <p>
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>Java 对象属性名与数据库字段名转换</li>
 *     <li>API 参数名格式转换</li>
 *     <li>代码生成工具</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * // 小驼峰转下划线
 * String result = StringCaseUtil.lowerCamelToUnderscore("userName");
 * // 结果: "user_name"
 *
 * // 下划线转小驼峰
 * String result = StringCaseUtil.underscoreToLowerCamel("user_name");
 * // 结果: "userName"
 * }
 * </pre>
 *
 * @author hulei42031
 * @since 2022-10-20 14:38
 * @version 1.0
 */
public class StringCaseUtil {

    /**
     * 小驼峰命名转下划线命名
     * <p>
     * 将小驼峰格式的字符串转换为下划线格式。
     * 例如：userName → user_name
     * <p>
     * <h3>转换规则：</h3>
     * <ul>
     *     <li>大写字母前插入下划线</li>
     *     <li>所有字母转为小写</li>
     * </ul>
     * <p>
     * <h3>使用示例：</h3>
     * <pre>
     * {@code
     * lowerCamelToUnderscore("userName")     // "user_name"
     * lowerCamelToUnderscore("userNameInfo")  // "user_name_info"
     * lowerCamelToUnderscore("id")            // "id"
     * }
     * </pre>
     *
     * @param lowerCamel 小驼峰格式的字符串，如 "userName"
     * @return 下划线格式的字符串，如 "user_name"
     */
    public static String lowerCamelToUnderscore(String lowerCamel) {
        if (lowerCamel == null || lowerCamel.isEmpty()) {
            return lowerCamel;
        }
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, lowerCamel);
    }

    /**
     * 下划线命名转小驼峰命名
     * <p>
     * 将下划线格式的字符串转换为小驼峰格式。
     * 例如：user_name → userName
     * <p>
     * <h3>转换规则：</h3>
     * <ul>
     *     <li>下划线后的字母转为大写</li>
     *     <li>移除所有下划线</li>
     *     <li>首字母保持小写</li>
     * </ul>
     * <p>
     * <h3>使用示例：</h3>
     * <pre>
     * {@code
     * underscoreToLowerCamel("user_name")     // "userName"
     * underscoreToLowerCamel("user_name_info") // "userNameInfo"
     * underscoreToLowerCamel("id")           // "id"
     * }
     * </pre>
     *
     * @param underscore 下划线格式的字符串，如 "user_name"
     * @return 小驼峰格式的字符串，如 "userName"
     */
    public static String underscoreToLowerCamel(String underscore) {
        if (underscore == null || underscore.isEmpty()) {
            return underscore;
        }
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, underscore);
    }
}
