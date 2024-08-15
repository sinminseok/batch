package study.batch.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import study.batch.entity.Order;
import study.batch.entity.OrderHistory;
import study.batch.repository.OrderHistoryRepository;
import study.batch.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBatchTest // (1)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest()
@Import(ParameterConfiguration.class)
@EnableBatchProcessing
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ParameterConfigurationTest {


    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Job parameterJob;

    @BeforeEach
    public void setUp() {
        jobLauncherTestUtils.setJob(parameterJob);
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM BATCH_STEP_EXECUTION_CONTEXT");
        jdbcTemplate.execute("DELETE FROM BATCH_JOB_EXECUTION_CONTEXT");
        jdbcTemplate.execute("DELETE FROM BATCH_JOB_EXECUTION_PARAMS");
        jdbcTemplate.execute("DELETE FROM BATCH_STEP_EXECUTION");
        jdbcTemplate.execute("DELETE FROM BATCH_JOB_EXECUTION");
        jdbcTemplate.execute("DELETE FROM BATCH_JOB_INSTANCE");
    }


    @Test
    public void test1() throws Exception {
        LocalDateTime orderDate = LocalDateTime.of(2022, 8, 11, 15, 30, 45);

        orderRepository.save(Order.builder()
                .orderDate(orderDate)
                .orderNumber("ORDER1")
                .build());

        orderRepository.save(Order.builder()
                .orderDate(orderDate)
                .orderNumber("ORDER2")
                .build());

        orderRepository.save(Order.builder()
                .orderDate(orderDate)
                .orderNumber("ORDER3")
                .build());

        JobParameters jobParameters = new JobParametersBuilder()

                .addString("requestDate", "20220811")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        Assertions.assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<OrderHistory> all = orderHistoryRepository.findAll();
        assertThat(all.size()).isEqualTo(3);
        assertThat(all.get(0).getOrderDateTime()).isEqualTo(orderDate);

    }


}
