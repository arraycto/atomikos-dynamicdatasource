package com.frank.xa.config;

import org.apache.ibatis.transaction.Transaction;
import org.mybatis.spring.transaction.SpringManagedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Frank_Lei
 * @Description <P>jta atomikos分布式事务下多数据源切换</P>
 * 解决spring事务中数据源切换失效问题
 * 主连接 connection：多数据源操作组成的事务中第一个数据源的连接
 * 副连接 connection：多数据源中除第一个数据源外所有数据源的连接
 * @CreateTime 2021年01月21日 10:37:00
 * @see SpringManagedTransaction
 * <p>
 */
public class MultiDataSourceTransaction implements Transaction {

    private static final Logger logger = LoggerFactory.getLogger(MultiDataSourceTransaction.class);

    private final DataSource dataSource;

    private Connection mainConnection;

    private final String dbName;

    private final ConcurrentMap<String, Connection> otherConnectionMap;

    public MultiDataSourceTransaction(DataSource dataSource) {
        Assert.notNull(dataSource, "No DataSource specified");
        this.dataSource = dataSource;
        otherConnectionMap = new ConcurrentHashMap<>();
        dbName = DataSourceContextHolder.getDBName();
    }


    @Override
    public Connection getConnection() throws SQLException {
        String dbName = DataSourceContextHolder.getDBName();
        if (dbName.equals(this.dbName)) {
            if (mainConnection != null) return mainConnection;
            else {
                openMainConnection();
                return mainConnection;
            }
        } else {
            if (!otherConnectionMap.containsKey(dbName)) {
                try {
                    Connection conn = dataSource.getConnection();
                    otherConnectionMap.put(dbName, conn);
                } catch (SQLException ex) {
                    throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", ex);
                }
            }
            return otherConnectionMap.get(dbName);
        }

    }


    private void openMainConnection() throws SQLException {
        this.mainConnection = DataSourceUtils.getConnection(this.dataSource);
        boolean isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.mainConnection, this.dataSource);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "JDBC Connection ["
                            + this.mainConnection
                            + "] will"
                            + (isConnectionTransactional ? " " : " not ")
                            + "be managed by Spring");
        }
    }

    @Override
    public void commit() throws SQLException {
        // do nothing
    }

    @Override
    public void rollback() throws SQLException {
        // do nothing
    }

    @Override
    public void close() throws SQLException {
        // do nothing
    }

    @Override
    public Integer getTimeout() throws SQLException {
        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.dataSource);
        return holder != null && holder.hasTimeout() ? holder.getTimeToLiveInSeconds() : null;
    }
}

