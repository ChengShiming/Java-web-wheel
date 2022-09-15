package com.csm.java.component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class HikariDataSourceProxy implements DataSource {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicBoolean circuitState = new AtomicBoolean(false);

    private final AtomicReference<HikariConfig> configReference;

    private final AtomicReference<DataSource> dataSourceReference = new AtomicReference<>();

    public HikariDataSourceProxy(HikariConfig configuration) {
        this.configReference = new AtomicReference<>(configuration);
        //初始化DataSource，失败时打开熔断
        setCircuitState(!initDataSource());

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        //watchdog线程
        Thread watchdog = new Thread(() -> {
            try {
                while (true) {
                    if (getCircuitState()) {//熔断打开
                        if (isActive(configuration.getJdbcUrl())) {//测试网络连通性
                            if (getDataSource() == null) {
                                //初始化DataSource，失败时打开熔断
                                setCircuitState(!initDataSource());
                            } else {
                                setCircuitState(false);
                            }
                        }
                        Thread.sleep(10 * 1000);
                    } else {
                        Thread.sleep(60 * 1000);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
        watchdog.setDaemon(true);
        watchdog.setName("DynamicDataSource-WatchDog");
        watchdog.start();
    }

    private boolean initDataSource() {
        try {
            HikariConfig config = this.configReference.get();
            if (!isActive(config.getJdbcUrl())) {
                return false;
            }

            HikariDataSource dataSource = new HikariDataSource(config);
            this.dataSourceReference.set(dataSource);
            return true;
        } catch (Exception e) {
            logger.error("initDataSource error", e);
            return false;
        }
    }

    //ip端口连通测试
    private boolean isActive(String jdbcUrl) {
        String str = jdbcUrl.substring(13);
        int end = str.indexOf("/");
        String hostPort = str.substring(0, end);
        String[] hostPortArr = hostPort.split(":");
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostPortArr[0], Integer.parseInt(hostPortArr[1])), 3000);

            return true;
        } catch (Exception e) {
            logger.error("connect error, hostPort = " + hostPort);
            return false;
        }
    }

    public boolean getCircuitState() {
        return this.circuitState.get();
    }

    public void setCircuitState(boolean state) {
        this.circuitState.set(state);
    }

    private DataSource getDataSource() {
        return this.dataSourceReference.get();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        getDataSource().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getDataSource().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getDataSource().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getDataSource().getParentLogger();
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return getDataSource().getConnection();
        } catch (Exception e) {
            //连接异常立即开启熔断
            setCircuitState(true);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getDataSource().getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getDataSource().isWrapperFor(iface);
    }
}
