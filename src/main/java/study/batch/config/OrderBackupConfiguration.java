package study.batch.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import study.batch.entity.Order;
import study.batch.entity.OrderHistory;
import study.batch.parameters.CreateDateJobParameter;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class OrderBackupConfiguration {

    private static final String JOB_NAME = "orderBackupBatch";
    private static final String STEP_NAME = JOB_NAME + "STEP";

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final CreateDateJobParameter jobParameter;

    @Bean("orderBackupJobParameter")
    @JobScope
    public CreateDateJobParameter orderBackupJobParameter() {
        return new CreateDateJobParameter();
    }

    @Bean
    public Job job(){
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(step())
                .build();
    }

    @Bean
    @JobScope
    public Step step() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<Order, OrderHistory>chunk(10, platformTransactionManager)
                .reader(orderBackupReader())  // 이름 변경
                .processor(orderBackupProcessor())  // 이름 변경
                .writer(orderBackupWriter())  // 이름 변경
                .build();
    }

    @Bean(name = JOB_NAME + "orderBackupReader")
    @StepScope
    public JpaPagingItemReader<Order> orderBackupReader() {  // 이름 변경
        Map<String, Object> params = new HashMap<>();
        params.put("requestDate", jobParameter.getRequestDate());

        String query = "SELECT o FROM Order o WHERE DATE(o.orderDate) = :requestDate";

        return new JpaPagingItemReaderBuilder<Order>()
                .name("orderBackupReader")  // 이름 변경
                .entityManagerFactory(entityManagerFactory)
                .pageSize(10)
                .queryString(query)
                .parameterValues(Map.of("requestDate", params.get("requestDate")))
                .build();
    }

    @Bean(name = JOB_NAME + "orderBackupProcessor")
    public ItemProcessor<Order, OrderHistory> orderBackupProcessor() {  // 이름 변경
        return order -> OrderHistory.builder()
                .orderNumber(order.getOrderNumber())
                .orderDateTime(order.getOrderDate())
                .orderPrice(order.calculatePrice())
                .build();
    }

    @Bean(name = JOB_NAME + "orderBackupWriter")
    public JpaItemWriter<OrderHistory> orderBackupWriter() {  // 이름 변경
        JpaItemWriter<OrderHistory> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
