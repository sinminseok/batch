package study.batch.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import study.batch.entity.Order;
import study.batch.entity.OrderHistory;
import study.batch.repository.OrderHistoryRepository;
import study.batch.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


/**
 * @SpringBatchTest 은 자동으로 JobLauncherTestUtils, JobRepositoryTestUtils 빈들을 생성해준다.
 */
@SpringBatchTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes={ParameterConfiguration.class, TestBatchLegacyConfig.class})
@EnableJpaRepositories(basePackages = "study.batch.repository")
@EntityScan(basePackages = "study.batch.entity")
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
    private JobRepositoryTestUtils jobRepositoryTestUtils;


    @AfterEach
    public void tearDown() {
        jobRepositoryTestUtils.removeJobExecutions();
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
