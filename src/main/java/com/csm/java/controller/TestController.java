package com.csm.java.controller;

import com.csm.java.component.JdbcTemplateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    @Qualifier("externalJdbcTemplate")
    private JdbcTemplateProxy externalJdbcTemplate;

    @RequestMapping("/select")
    public ResponseEntity<Void> select() {
        try {
            //execute sql
            externalJdbcTemplate.execute("select 1");

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
