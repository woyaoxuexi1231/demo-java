package org.hulei.common.autoconfigure;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.SocketException;
import java.util.UUID;

/**
 * 服务器端口监听器
 * <p>
 * 监听 Web 服务器初始化事件，记录服务器端口信息，并提供网络接口查询功能。
 * </p>
 *
 * @author hulei
 * @since 2024/11/13
 */
@RequestMapping("/server-port-listener")
@RestController
@Getter
@Slf4j
public class ServerPortListener {

    /**
     * 服务器端口
     */
    private int port;

    /**
     * 当前应用的唯一标识
     */
    private final UUID uuid = UUID.randomUUID();

    /**
     * 监听 Web 服务器初始化事件
     * <p>
     * 当 Web 服务器启动完成后，记录端口信息并打印项目访问地址。
     * </p>
     *
     * @param event Web 服务器初始化事件
     * @throws Exception 如果获取本地主机信息失败
     */
    @EventListener
    public void onApplicationEvent(WebServerInitializedEvent event) throws Exception {
        this.port = event.getWebServer().getPort();
        log.info("获得端口：{}", port);
        log.info("project url: http://{}:{}", ProjectUrlAutoConfiguration.getLocalHost(), port);
        log.info("swagger-ui index: http://{}:{}/swagger-ui/index.html", ProjectUrlAutoConfiguration.getLocalHost(), port);
        log.info("当前应用的唯一标识：{}", uuid);
    }

    /**
     * 打印网络接口信息
     * <p>
     * 输出所有可用的网络接口及其 IP 地址信息。
     * </p>
     *
     * @throws SocketException 如果获取网络接口信息失败
     */
    @RequestMapping("/print-network-interfaces")
    public void printNetworkInterfaces() throws SocketException {
        ProjectUrlAutoConfiguration.getNetworkInterfaces();
    }
}

