package com.frank.xa.util;

import com.frank.xa.Application;
import org.springframework.beans.BeansException;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

@Configuration
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtil.applicationContext == null) {
            SpringUtil.applicationContext = applicationContext;
        }
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    public static Object getBean(Class clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> anoType) {
        return getApplicationContext().getBeansWithAnnotation(anoType);
    }

    /**
     * 应用程序是否为解压方式启动
     */
    public static boolean isFileStart() {
        return "file".equals(Application.class.getResource("Application.class").getProtocol());
    }

    /**
     * 应用程序是否以jar包方式启动
     */
    public static boolean isJarStart() {
        return "jar".equals(Application.class.getResource("Application.class").getProtocol());
    }

    /**
     * 通过指定路径获取资源,根据不同的location协议返回不同类型的Resource实现实例
     *
     * @param location 可以指定（protocol）协议名称,http、https、ftp、file、classpath等，'classpath*:' 获取所有资源
     * @return
     */
    public static Resource getResource(String location) {
        return getApplicationContext().getResource(location);
    }

    /**
     * 根据locationPattern获取所有匹配资源
     *
     * @param locationPattern Ant风格模式匹配，the location pattern to resolve
     * @return
     * @throws IOException
     */
    public static Resource[] getResources(String locationPattern) throws IOException {
        return getApplicationContext().getResources(locationPattern);
    }

    /**
     * 获取远程请求接口的客户端IP
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknow".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknow".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknow".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                    // 根据网卡取本机ip
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP，多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) {
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

    /**
     * 获取当前应用程序的配置环境
     */
    public static String getActiveProfile() {
        return applicationContext.getEnvironment().getActiveProfiles()[0];
    }

    /**
     * 获取当前应用部署根目录
     *
     * @return
     */
    public static String deployPath() {
        if (isJarStart()) {
            ApplicationHome home = new ApplicationHome(SpringUtil.class);
            final File deploy = home.getSource().getParentFile();
            try {
                return deploy.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (isFileStart()) {
            final URL resource = Application.class.getResource("/");
            if (resource != null) return resource.getPath();
        }
        return null;
    }
}
