package com.frank.xa.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Frank_Lei
 * @Description 动态多数据源自动注册，实现 BeanFactoryPostProcessor 用来注册数据源，实现EnvironmentAware用来读取数据源配置
 * @CreateTime 2021年01月21日 10:37:00
 */
@Configuration
@EnableConfigurationProperties(value = {MutiDataSourceProperties.class})
public class DynamicDataSourceAutoConfiguration implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceAutoConfiguration.class);

    /**
     * 环境，配置上下文
     */
    private Environment env;

    private final static ConfigurationPropertyNameAliases alias = new ConfigurationPropertyNameAliases();

    /**
     * 由于部分数据源配置属性名称可能存在差异，因此使用别名消除这些差异引起的问题
     */
    static {
        alias.addAliases("url", "jdbc-url");
        alias.addAliases("username", "user");
    }

    /**
     * 参数绑定工具 springboot2.x 提供
     * 通过Binder api 手动实现 @ConfigurationProperties 注解功能
     */
    private Binder binder;

    /**
     * EnviromentAware接口提供的方法,获取环境变量
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
        //绑定配置器
        this.binder = Binder.get(env);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.info("******开始自动配置动态多数据源及atomikos分布式事务**********");

        Boolean jtaEnabled = this.env.getProperty("spring.jta.enabled", Boolean.class, false);


        Class<?> datasourceType = null;

        // 检查数据源是否支持JTA分布式事务
        if (jtaEnabled) {
            logger.info("==> 已开启JTA分布式事务, 由atomikos支持");
            String className = env.getProperty("spring.jta.atomikos.datasource.xa-data-source-class-name", String.class);
            Assert.notNull(className, "JTA事务模式下必须指定spring.jta.atomikos.datasource.xa-data-source-class-name！");
            try {
                datasourceType = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (!XADataSource.class.isAssignableFrom(datasourceType)) {
                throw new IllegalArgumentException(String.format("JTA事务模式下指定的数据源(%s)必须实现javax.sql.XADataSource", datasourceType));
            }
        }


        //多数据源的容器
        Map<Object, Object> dataSourceMap = new HashMap<>();
        //创建动态数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();

        MutiDataSourceProperties mutiDataSourceProperties = binder.bind("spring.datasource", MutiDataSourceProperties.class).get();
        List<DataSourceProperties> dataSourcePropertiesList = mutiDataSourceProperties.getMuti();
        try {
            //动态注册数据源
            for (DataSourceProperties dataSourceProperties : dataSourcePropertiesList) {
                String name = dataSourceProperties.getName();
                if (!StringUtils.hasText(name)) {
                    throw new IllegalArgumentException(String.format("第【%d】个数据源缺少必名称参数name", dataSourcePropertiesList.indexOf(dataSourceProperties) + 1));
                }
                logger.info("==> 开始加载数据源【{}】. . .", name);

                DataSource dataSource;

                // 使用XADataSource + Atomikos 实现JTA事务
                if (jtaEnabled) {
                    AtomikosDataSourceBean atomikosDataSourceBean = binder.bind("spring.jta.atomikos.datasource", AtomikosDataSourceBean.class).get();
                    XADataSource xaDataSource = this.bindXaProperties((XADataSource) datasourceType.newInstance(), dataSourceProperties);
                    atomikosDataSourceBean.setXaDataSource(xaDataSource);
                    atomikosDataSourceBean.setUniqueResourceName(name);
                    dataSource = atomikosDataSourceBean;
                } else {
                    dataSource = dataSourceProperties.initializeDataSourceBuilder().build();
                }

                beanFactory.registerSingleton(name + "DataSource", dataSource);
                logger.info("==> 注册datasource: {}", name + "DataSource");
                beanFactory.registerSingleton(name + "JdbcTemplate", new JdbcTemplate(dataSource));
                logger.info("==> 注册jdbctemplate: {}", name + "JdbcTemplate");

                if (dataSourcePropertiesList.indexOf(dataSourceProperties) == 0) {
                    //设置默认数据源
                    dynamicDataSource.setDefaultTargetDataSource(dataSource);
                    logger.info("==> 默认数据源为配置文件中第一个：{}", name);
                }
                dataSourceMap.put(name, dataSource);
                logger.info("<== 数据源【{}】注册完成！", name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //注册数据源名称
        DataSourceContextHolder.registerKey(dataSourceMap.keySet());
        //设置可切换的目标数据源
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        //注册进spring容器
        beanFactory.registerSingleton("dynamicDataSource", dynamicDataSource);
        logger.info("**************配置完毕！****************");
    }

    private DataSource bind(Class<? extends DataSource> clazz, Map<String, Object> properties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source.withAliases(alias));
        //通过类型绑定参数并获得实例对象
        return binder.bind(ConfigurationPropertyName.EMPTY, Bindable.of(clazz)).get();
    }

    private XADataSource bindXaProperties(XADataSource target, DataSourceProperties dataSourceProperties) {
        Binder binder = new Binder(getBinderSource(dataSourceProperties));
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(target));
        return target;
    }

    private ConfigurationPropertySource getBinderSource(DataSourceProperties dataSourceProperties) {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource();
        source.put("user", dataSourceProperties.determineUsername());
        source.put("password", dataSourceProperties.determinePassword());
        source.put("url", dataSourceProperties.determineUrl());
        source.putAll(dataSourceProperties.getXa().getProperties());
        ConfigurationPropertyNameAliases aliases = new ConfigurationPropertyNameAliases();
        aliases.addAliases("user", "username");
        return source.withAliases(aliases);
    }

    @SuppressWarnings("unchecked")
    private Class<?> getClass(String className) {
        Class<?> datasourceClass;
        if (StringUtils.hasText(className)) {
            try {
                datasourceClass = (Class<? extends DataSource>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("can not resolve class with className: " + className);
            }
        } else {
            // 默认为hikariCP数据源，与springboot默认数据源保持一致
            datasourceClass = HikariDataSource.class;
        }
        return datasourceClass;
    }
}
