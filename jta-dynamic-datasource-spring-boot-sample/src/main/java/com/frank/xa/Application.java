package com.frank.xa;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.frank.xa.config.DynamicDataSourceAutoConfiguration;
import com.frank.xa.config.MutiDataSourceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

//注意为tk.mybatis.spring.annotation.MapperScan
@MapperScan("com.frank.*.mapper")
//由于要配置多数据源，因此需要禁用掉springboot的自动单数据源配置类，改用自定义动态数据源
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DruidDataSourceAutoConfigure.class,
        XADataSourceAutoConfiguration.class,
        DynamicDataSourceAutoConfiguration.class
})
@EnableTransactionManagement
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

