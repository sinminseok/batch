package study.batch.config;

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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import study.batch.entity.Order;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SimpleChunkConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

//    @Autowired
//    private BatchConfig batchConfig;

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
                .<Order, String>chunk(10, platformTransactionManager)
                .reader(reader(null))
                .processor(processor(null))
                .writer(writer())
                .build();
    }

    /**
     * ItemReader
     */
    @Bean
    @StepScope
    public JdbcCursorItemReader<Order> reader(@Value("#{jobParameters['requestDate']}") String requestDate) {
        System.out.println("reader requestDate = " + requestDate);

        JdbcCursorItemReader<Order> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("SELECT id, order_number, order_date FROM order_table");
        reader.setRowMapper(new BeanPropertyRowMapper<>(Order.class)); //DB 에서 읽어온 각 행의 데이터를 Teacher 객체로 변환하여 처리할 수 있도록 ㅘ는 설정
        return reader;
    }


    /**
     * processor
     */
    @Bean
    @StepScope
    public ItemProcessor<Order, String> processor(@Value("#{jobParameters['requestDate']}") String requestDate) {
        return order -> {
            String orderDate = parseOrderDate(order.getOrderDate());
            String stringrequestDate = parseDateToString(requestDate);

            if (orderDate.equals(stringrequestDate)) {
                return String.format("processor => %s", order.getOrderNumber().toUpperCase(Locale.ROOT));
            }

            return null;  // 필터 조건에 맞지 않으면 null을 반환
        };
    }


    /**
     * ItemWriter
     */
    @Bean
    public ItemWriter<String> writer() {
        return item -> System.out.println(String.join(", ", item));
    }

    private static String parseDateToString(String requestDate) {
        LocalDate date = LocalDate.parse(requestDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return date.format(formatter);
    }

    private static String parseOrderDate(LocalDateTime localDateTime){
        LocalDate orderDate = localDateTime.toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return orderDate.format(formatter);
    }

}
