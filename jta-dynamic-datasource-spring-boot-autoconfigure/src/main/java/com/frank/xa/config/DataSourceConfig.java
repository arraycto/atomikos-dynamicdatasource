package com.frank.xa.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * druid多数据源配置类：
 * 1.结合mybatis实现动态切换数据源：DataSourceContextHolder.setDBType("gp");
 * 2.也可使用单独使用JdbcTemplate：
 *
 * @Autowired
 * @Qualifier("gpTemplate") JdbcTemplate gpTemplate;
 */
@Configuration
public class DataSourceConfig {

    /**
     * 根据数据源创建SqlSessionFactory
     */
    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("dynamicDataSource") DataSource dynamicDataSource)
            throws Exception {

        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dynamicDataSource);  //指定数据源(这个必须有，否则报错)
        // MultiDataSourceTransactionFactory：解决在事务中多数据源切换失败的问题
        bean.setTransactionFactory(new MultiDataSourceTransactionFactory());
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/**/*.xml"));
        bean.setConfigLocation(new PathMatchingResourcePatternResolver()
                .getResource("classpath:config/other/mybatis.xml"));
        return bean.getObject();

    }

    @Bean
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }


    /**
     * 创建动态数据源事务管理器
     * 无论是JPA还是JDBC都实现自接口PlatformTransactionManager,如果你添加的是spring-boot-starter-jdbc依赖，
     * 框架会默认注入DataSourceTransactionManager,如果是spring-boot-starter-data-jpa,框架会默认注入JpaTransactionManager
     */
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "spring.jta", name = "enabled", havingValue = "false")
    public PlatformTransactionManager dynamicTxManager(@Qualifier("dynamicDataSource") DataSource dataSource) throws Exception {
        return new DataSourceTransactionManager(dataSource);
    }


//    @Bean(name = "atomikosUserTransaction")
//    @ConditionalOnProperty(prefix = "spring.jta", name = "enabled", havingValue = "true")
//    public UserTransaction userTransaction() throws Throwable {
//        UserTransactionImp userTransactionImp = new UserTransactionImp();
//        userTransactionImp.setTransactionTimeout(10000);
//        return userTransactionImp;
//    }
//
//    @Bean(name = "atomikosTransactionManager")
//    @ConditionalOnMissingBean(TransactionManager.class)
//    @ConditionalOnProperty(prefix = "spring.jta", name = "enabled", havingValue = "true")
//    public TransactionManager atomikosTransactionManager() throws Throwable {
//        UserTransactionManager userTransactionManager = new UserTransactionManager();
//        userTransactionManager.setForceShutdown(false);
//        return userTransactionManager;
//    }
//
//    @Bean(name = "jtaTransactionManager")
//    @ConditionalOnMissingBean(PlatformTransactionManager.class)
//    @ConditionalOnProperty(prefix = "spring.jta", name = "enabled", havingValue = "true")
//    public PlatformTransactionManager transactionManager(
//            @Qualifier("atomikosUserTransaction") UserTransaction userTransaction,
//            @Qualifier("atomikosTransactionManager") TransactionManager transactionManager
//    ) {
//        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, transactionManager);
//        jtaTransactionManager.setAllowCustomIsolationLevels(true);
//        return jtaTransactionManager;
//    }

}
