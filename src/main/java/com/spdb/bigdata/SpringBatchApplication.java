package com.spdb.bigdata;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * Created by zhp on 2019年12月06日 星期五
 */
@SpringBootApplication
public class SpringBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBatchApplication.class, args);
    }

    @Bean
    private static DataSource getDatasource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setMaxActive(100);
        dataSource.setInitialSize(10);
        dataSource.setUrl("jdbc:mysql://localhost:3306/bigdata?" +
                "serverTimezone=GMT%2B8&allowMultiQueries=true&useUnicode=true&" +
                "characterEncoding=UTF-8&useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("root1234");
        System.out.println("dataSource inited.");
        return dataSource;
    }
}
