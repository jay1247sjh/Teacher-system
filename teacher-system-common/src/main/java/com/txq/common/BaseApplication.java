package com.txq.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication(scanBasePackages = {
        "com.txq.common",
        "com.txq.interfaces",
        "com.txq.application",
        "com.txq.domain",
        "com.txq.infrastructure"
})
public abstract class BaseApplication {

    private static final Logger log = LoggerFactory.getLogger(BaseApplication.class);

    public void run(String[] args) {
        // 如果没有指定环境，默认为dev
        if (System.getProperty("spring.profiles.active") == null) {
            System.setProperty("spring.profiles.active", "dev");
        }
        SpringApplication app = new SpringApplication(this.getClass());
        app.setBannerMode(Banner.Mode.CONSOLE);
        // 启动SpringBoot应用
        ConfigurableApplicationContext context = app.run(args);
        // 打印相关信息
        printAppInfo(context);
    }

    private void printAppInfo(ConfigurableApplicationContext context) {
        Environment env = context.getEnvironment();
        // 获取启动类名称
        String appName = env.getProperty("spring.application.name");
        // 获取启动类端口
        String port = env.getProperty("server.port");
        // 获取运行环境
        String profile = String.join(",", env.getActiveProfiles());
        // 获取启动IP
        String host = "unknown";
        // 获取当前java经常pid
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        log.info("\n----------------------------------------------------------\n" +
                        "✅ 应用启动成功！\n" +
                        "🌐 应用名称: {}\n" +
                        "🔗 访问地址: http://{}:{}\n" +
                        "🧪 当前环境: {}\n" +
                        "🆔 PID: {}\n" +
                        "----------------------------------------------------------",
                appName, host, port, profile, pid);
    }
}
