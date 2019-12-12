package com.spdb.bigdata.batch;

import com.spdb.bigdata.listener.JobListener;
import com.spdb.bigdata.model.Man;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.orm.JpaNativeQueryProvider;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by EalenXie on 2018/9/10 14:50.
 * :@EnableBatchProcessing提供用于构建批处理作业的基本配置
 */
@Configuration
@EnableBatchProcessing
public class DataBatchConfiguration {
    private static final Logger log = LoggerFactory.getLogger(DataBatchConfiguration.class);

    @Resource
    private JobBuilderFactory jobBuilderFactory;    //用于构建JOB

    @Resource
    private StepBuilderFactory stepBuilderFactory;  //用于构建Step

    @Resource
    private EntityManagerFactory emf;           //注入实例化Factory 访问数据

    @Resource
    private JobListener jobListener;            //简单的JOB listener

    @Resource
    private DataSource dataSource;

    /**
     * 一个简单基础的Job通常由一个或者多个Step组成
     */
    @Bean
    public Job dataHandleJob() {
        return jobBuilderFactory.get("dataHandleJob").
                incrementer(new RunIdIncrementer()).
                start(handleDataStep()).    //start是JOB执行的第一个step
//                next(xxxStep()).
//                next(xxxStep()).
//                ...
        listener(jobListener).      //设置了一个简单JobListener
                build();
    }

    /**
     * 一个简单基础的Step主要分为三个部分
     * ItemReader : 用于读取数据
     * ItemProcessor : 用于处理数据
     * ItemWriter : 用于写数据
     */
    @Bean
    public Step handleDataStep() {
        return stepBuilderFactory.get("getData").
                <Man, Man>chunk(100).        // <输入,输出> 。chunk通俗的讲类似于SQL的commit; 这里表示处理(processor)100条后写入(writer)一次。
                faultTolerant().retryLimit(3).
                retry(Exception.class).skipLimit(100).
                skip(Exception.class). //捕捉到异常就重试,重试100次还是异常,JOB就停止并标志失败
                reader(getDataReader()).         //指定ItemReader
                processor(getDataProcessor()).   //指定ItemProcessor
                writer(getDataWriter()).         //指定ItemWriter
                build();
    }


        @Bean
        public ItemReader<? extends Man> getDataReader() {
            JdbcPagingItemReader<Man> reader = new JdbcPagingItemReader<>();
            reader.setDataSource(dataSource);
            reader.setFetchSize(3);
            MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
            queryProvider.setSelectClause("select *");
            queryProvider.setFromClause("from Man");

            Map<String, Order> sortKeys = new HashMap<>();
            sortKeys.put("age",Order.DESCENDING);
            queryProvider.setSortKeys(sortKeys);

            reader.setQueryProvider(queryProvider);
            System.out.println("$$$ 读 ###");

            reader.setRowMapper(new BeanPropertyRowMapper<>(Man.class));

            try {
                reader.afterPropertiesSet();
            } catch (Exception e) {
                e.printStackTrace();
            }
            reader.setSaveState(true);
            return reader;
        }

    @Bean
    public ItemProcessor<Man, Man> getDataProcessor() {
        ItemProcessor<Man, Man> itemProcessor = new ItemProcessor<Man, Man>() {
            @Override
            public Man process(Man Man) throws Exception {
                log.info("处理 data : " + Man.toString());  //模拟  假装处理数据,这里处理就是打印一下
                return Man;
            }
        };
        return itemProcessor;
        //        lambda也可以写为:
        //        return Man -> {
        //            log.info("processor data : " + Man.toString());
        //            return Man;
        //        };
    }
    @Bean
    public ItemWriter<Man> getDataWriter() {
        System.out.println("¥￥￥￥ getDataWriter ￥￥￥");
        return list -> {
            for (Man Man : list) {
                log.info("写数据 : " + Man); //模拟 假装写数据 ,这里写真正写入数据的逻辑
            }
        };
    }
/*
//    @Bean
//    public ItemReader<? extends Man> getDataReader() {
//        // 读取数据,这里可以用JPA,JDBC,JMS 等方式 读入数据
//        JpaPagingItemReader<Man> reader = new JpaPagingItemReader<>();
//        // 这里选择JPA方式读数据 一个简单的 native SQL
//        String sqlQuery = "SELECT * FROM Man";
//        try {
//            JpaNativeQueryProvider<Man> queryProvider = new JpaNativeQueryProvider<>();
//            queryProvider.setSqlQuery(sqlQuery);
//            queryProvider.setEntityClass(Man.class);
//            queryProvider.afterPropertiesSet();
//            reader.setEntityManagerFactory(emf);
//            reader.setPageSize(3);
//            reader.setQueryProvider(queryProvider);
//            reader.afterPropertiesSet();
//            // 所有ItemReader和ItemWriter实现都会在ExecutionContext提交之前将其当前状态存储在其中,如果不希望这样做,可以设置setSaveState(false)
//            reader.setSaveState(true);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return reader;
//    }
*/
    /*
    @Bean
    public ItemReader<? extends Man> getDataReader() {
        InMemoryStudentReader reader = new InMemoryStudentReader();
        try {
            reader.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reader;
    }

 */
}