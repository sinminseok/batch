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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import study.batch.entity.Order;
import study.batch.entity.OrderHistory;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SimpleChunkConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private DataSource dataSource;

    @Bean
    public Job customerJob() {
        return new JobBuilder("customerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(simpleChunkStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step simpleChunkStep(@Value("#{jobParameters['requestDate']}") String requestDate) {
        return new StepBuilder("simpleChunkStep", jobRepository)
                .<Order, OrderHistory>chunk(10, platformTransactionManager)
                .reader(reader(null))
                .processor(processor())
                .writer(writer())
                .build();
    }

    /**
     * ItemReader
     */
    @Bean
    @StepScope
    public JdbcCursorItemReader<Order> reader(@Value("#{jobParameters['requestDate']}") String requestDate) {
        JdbcCursorItemReader<Order> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);

        // 수정된 쿼리: 요청된 날짜와 일치하는 주문만 선택
        String sql = "SELECT id, order_number, order_date FROM order_table " +
                "WHERE DATE_FORMAT(order_date, '%Y%m%d') = ?";

        reader.setSql(sql);
        reader.setPreparedStatementSetter(ps -> ps.setString(1, parseDateToString(requestDate)));
        reader.setRowMapper(new BeanPropertyRowMapper<>(Order.class)); //DB 에서 읽어온 각 행의 데이터를 Order 객체로 변환하여 처리할 수 있도록 설정
        return reader;
    }

    /**
     * processor
     */
    @Bean
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
    @Bean
    public JpaItemWriter<OrderHistory> writer() {
        JpaItemWriter<OrderHistory> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    private static String parseDateToString(String requestDate) {
        LocalDate date = LocalDate.parse(requestDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return date.format(formatter);
    }

}
