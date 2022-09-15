package com.csm.java.config;

import com.csm.java.component.HikariDataSourceProxy;
import com.csm.java.component.JdbcTemplateProxy;
import com.zaxxer.hikari.HikariConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceExternalConfig {

    @Bean
    @Qualifier("externalDataSource")
    public HikariDataSourceProxy externalDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai");
        config.setUsername("root");
        config.setPassword("root");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return new HikariDataSourceProxy(config);
    }

    @Bean
    @Qualifier("externalJdbcTemplate")
    public JdbcTemplateProxy swJdbcTemplate(@Qualifier("externalDataSource") HikariDataSourceProxy dataSource) {
        return new JdbcTemplateProxy(dataSource);
    }

}
