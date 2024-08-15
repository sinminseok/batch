package study.batch.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import study.batch.entity.Order;
import study.batch.entity.OrderHistory;
import study.batch.parameters.CreateDateJobParameter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ParameterConfiguration {

    private static final String JOB_NAME = "parameterJob";

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;


    private final CreateDateJobParameter jobParameter;

    @Bean(JOB_NAME + "jobParameter")
    @JobScope
    public CreateDateJobParameter jobParameter(){
        return new CreateDateJobParameter();
    }


    @Bean
    public Job parameterJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(parameterJobStep())
                .build();
    }


    @Bean
    @JobScope
    public Step parameterJobStep() {
        return new StepBuilder(JOB_NAME + "Step", jobRepository)
                .<Order, OrderHistory>chunk(10, platformTransactionManager)
                .reader(jpaPagingItemReader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    /**
     * ItemReader
     */
    @Bean(name = "jpaPagingItemReader")
    @StepScope
    public JpaPagingItemReader<Order> jpaPagingItemReader() {
        Map<String, Object> params = new HashMap<>();
        params.put("requestDate", jobParameter.getRequestDate());
        params.put("status", jobParameter.getStatus());
        // JPQL 쿼리를 사용하여 요청된 날짜와 일치하는 주문만 선택
        String jpql = "SELECT o FROM Order o WHERE DATE(o.orderDate) = :requestDate";

        return new JpaPagingItemReaderBuilder<Order>()
                .name("parameterReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(10)
                .queryString(jpql)
                .parameterValues(Map.of("requestDate", params.get("requestDate"))) // 파라미터 설정
                .build();
    }

    /**
     * processor
     */
    @Bean(name = "parameterProcessor")
    @StepScope
    public ItemProcessor<Order, OrderHistory> processor() {
        return order -> OrderHistory.builder()
                .orderNumber(order.getOrderNumber())
                .orderDateTime(order.getOrderDate())
                .build();
    }

    /**
     * ItemWriter
     */
    @Bean(name = "parameterWriter")
    public JpaItemWriter<OrderHistory> writer() {
        JpaItemWriter<OrderHistory> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

}
