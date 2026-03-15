# Spring Boot 内嵌 Tomcat 工作流程与整合流程（详细版）

> 适用版本：以 Spring Boot 2.4+ / 3.x 为主（核心机制一致），重点讲内嵌容器模式。

## 1. 一句话先看全貌

Spring Boot 启动时创建 `ServletWebServerApplicationContext`，在 `refresh()` 阶段通过 `TomcatServletWebServerFactory` 创建并启动 Tomcat，把 `DispatcherServlet`、Filter、Listener 等注册进 `ServletContext`，随后请求经 Tomcat 管线进入 Spring MVC 处理。

---

## 2. 启动时序（从 `main` 到端口监听）

```java
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

### 2.1 `SpringApplication.run(...)` 关键阶段

1. **推断应用类型**：识别为 `SERVLET`（存在 Servlet API / Spring MVC）。
2. **创建容器上下文**：通常是 `AnnotationConfigServletWebServerApplicationContext`。
3. **准备 Environment**：读取系统变量、命令行参数、配置文件等属性源。
4. **加载 Bean 定义**：处理 `@SpringBootApplication` + 自动配置。
5. **`refresh()` 刷新上下文**：这是 Tomcat 真正创建与启动的核心阶段。

### 2.2 `refresh()` 中与 Tomcat 相关的关键点

在 `ServletWebServerApplicationContext` 中：

1. 调用 `onRefresh()`。
2. 进入 `createWebServer()`。
3. 从容器拿到 `ServletWebServerFactory`（Tomcat 场景是 `TomcatServletWebServerFactory`）。
4. 调用 `factory.getWebServer(initializers...)` 创建 `TomcatWebServer`。
5. 启动 Tomcat，绑定端口（如 `server.port=8080`）。
6. 触发 `ServletContextInitializer`，注册 Servlet / Filter / Listener。

---

## 3. Spring Boot 与 Tomcat 的整合是怎么“自动发生”的

## 3.1 自动配置入口

常见关键自动配置（按职责理解即可）：

- `ServletWebServerFactoryAutoConfiguration`：提供内嵌容器工厂 Bean。
- `EmbeddedWebServerFactoryCustomizerAutoConfiguration`：把 `server.*` 配置绑定并定制工厂。
- `DispatcherServletAutoConfiguration`：创建 `DispatcherServlet` 及其注册 Bean。
- `WebMvcAutoConfiguration`：装配 MVC 基础设施（HandlerMapping、HandlerAdapter 等）。

## 3.2 为什么默认就是 Tomcat

- Spring Boot Web Starter 默认依赖 `spring-boot-starter-tomcat`。
- 类路径满足条件时，Tomcat 工厂配置生效。
- 如果排除 Tomcat 并引入 Jetty/Undertow，对应工厂会替代生效。

## 3.3 Boot 如何把 Spring 组件“桥接”到 Tomcat

通过 `ServletContextInitializer` 机制把 Spring Bean 注册为 Servlet 容器组件：

- `DispatcherServletRegistrationBean`
- `FilterRegistrationBean`（如字符编码过滤器、Spring Security 过滤器链）
- `ServletListenerRegistrationBean`

这一步发生在 WebServer 创建后、应用可对外服务前。

---

## 4. Tomcat 启动后，请求如何走到 Controller

一次 HTTP 请求的主路径：

1. 请求到达 `Connector`（NIO/线程池）。
2. 进入 Coyote / Catalina 管线（`Engine -> Host -> Context -> Wrapper`）。
3. 命中 `DispatcherServlet`（通常映射 `/`）。
4. `DispatcherServlet` 调 `HandlerMapping` 找处理器（Controller 方法）。
5. `HandlerAdapter` 执行目标方法。
6. 返回值经 `HttpMessageConverter` 序列化（如 JSON）。
7. Tomcat 回写响应并结束连接/复用。

---

## 5. 配置与扩展点（实战最常用）

## 5.1 纯配置方式（`application.yml`）

```yaml
server:
  port: 8081
  servlet:
    context-path: /demo
  tomcat:
    threads:
      max: 200
    max-connections: 8192
    accept-count: 100
```

## 5.2 Java 定制方式

```java
@Bean
WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
    return factory -> {
        factory.setPort(8082);
        factory.addConnectorCustomizers(connector -> connector.setProperty("relaxedQueryChars", "|{}[]"));
    };
}
```

常见可定制点：

- 端口、上下文路径、压缩、连接超时
- 线程池与连接参数
- Access Log
- SSL（HTTPS）
- 自定义 `Valve` / `Context` 参数

---

## 6. 内嵌 Tomcat vs 外置 Tomcat（WAR 部署）

- **内嵌模式（默认）**：`java -jar` 即可运行；容器随应用发布，最常见于微服务。
- **外置模式**：打成 WAR 交给独立 Tomcat；需要 `SpringBootServletInitializer` 适配。
- 本质区别：谁负责创建 Servlet 容器。内嵌是应用自己创建，外置是应用服务器创建。

---

## 7. 常见启动问题排查清单

1. **端口冲突**：`Port 8080 was already in use`。
2. **容器类型不匹配**：引入了 WebFlux 依赖导致不是 Servlet 应用。
3. **自动配置未生效**：检查 `@SpringBootApplication` 扫描范围和条件注解。
4. **Filter 顺序问题**：用 `@Order` 或 `FilterRegistrationBean#setOrder` 明确顺序。
5. **上下文路径/反向代理问题**：确认 `server.servlet.context-path` 与网关转发前缀。

---

## 8. 记忆版（面试/复盘）

- **创建谁**：`SpringApplication` 创建 `ServletWebServerApplicationContext`。
- **何时建容器**：`refresh()` 阶段建并启 Tomcat。
- **谁来整合**：自动配置 + `ServletContextInitializer`。
- **谁处理请求**：Tomcat 接入，Spring MVC 分发到 Controller。
- **怎么改行为**：`server.*` + `WebServerFactoryCustomizer` + MVC/Filter 扩展。

