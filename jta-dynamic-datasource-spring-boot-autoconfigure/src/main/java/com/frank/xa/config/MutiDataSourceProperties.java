package com.frank.xa.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Description:
 * User: Frank_Lei 00707
 * Date: 2020-06-29
 * Time: 15:55
 */
@ConfigurationProperties(prefix = "spring.datasource")
public class MutiDataSourceProperties {

    /**
     * 多数据源配置
     */
    private List<DataSourceProperties> muti;

    public List<DataSourceProperties> getMuti() {
        return muti;
    }

    public void setMuti(List<DataSourceProperties> muti) {
        this.muti = muti;
    }
}
