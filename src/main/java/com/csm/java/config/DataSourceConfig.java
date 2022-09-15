package com.csm.java.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 默认数据源配置
 */
@Configuration
public class DataSourceConfig {

    @Primary
    @Bean
    public HikariDataSource dataSourcePrimary() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai");
        config.setUsername("root");
        config.setPassword("root");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return new HikariDataSource(config);
    }

    @Primary
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
