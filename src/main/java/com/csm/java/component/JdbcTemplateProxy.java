package com.csm.java.component;

import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcTemplateProxy extends JdbcTemplate {

    private final HikariDataSourceProxy hikariDataSourceProxy;

    public JdbcTemplateProxy(HikariDataSourceProxy dataSource) {
        super(dataSource);

        this.hikariDataSourceProxy = dataSource;
    }

    public boolean getCircuitState() {
        return hikariDataSourceProxy.getCircuitState();
    }

}
