package com.frank.xa.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author Frank_Lei
 * @Description 加载自定义配置处理类
 * 注意：虽然使用@PropertySource加载指定配置更方便，但是不推荐使用。
 * 因为Spring Boot在刷新ApplicationContext之前就准备好了Environment，而使用@PropertySource的配置加载太迟,基本不会影响到自动配置
 * @CreateTime 2019年10月22日 11:31:00
 */
public class AdditionalEnvironment implements EnvironmentPostProcessor {

    /**
     * 加载多个配置文件
     */
    private final ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();

    private List<PropertySourceLoader> propertySourceLoaders;

    public AdditionalEnvironment() {
        super();
        this.propertySourceLoaders = SpringFactoriesLoader.loadFactories(PropertySourceLoader.class, getClass().getClassLoader());
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        for (String activeProfile : environment.getActiveProfiles()) {
            for (PropertySourceLoader propertySourceLoader : this.propertySourceLoaders) {
                for (String fileExtension : propertySourceLoader.getFileExtensions()) {
                    String location = ResourceUtils.CLASSPATH_URL_PREFIX + "/config/" + activeProfile + "/**/*." + fileExtension;
                    try {
                        Resource[] resources = this.patternResolver.getResources(location);
                        for (Resource resource : resources) {
                            List<PropertySource<?>> propertySources = propertySourceLoader.load(resource.getFilename(), resource);
                            if (null != propertySources && !propertySources.isEmpty()) {
                                propertySources.forEach(environment.getPropertySources()::addLast);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
