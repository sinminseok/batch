package study.batch.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import study.batch.entity.OrderHistory;
import study.batch.repository.OrderHistoryRepository;
import study.batch.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.batch.helper.OrderHelper.createOrder;

@SpringBatchTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes={OrderBackupConfiguration.class, TestBatchLegacyConfig.class})
@EnableJpaRepositories(basePackages = "study.batch.repository")
@EntityScan(basePackages = "study.batch.entity")
public class OrderBackupConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;


    @BeforeEach
    public void tearDown() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    public void Order_를_날짜로_필터링해_orderHistory_에_저장한다() throws Exception {
        //given
        LocalDateTime orderDate = LocalDateTime.of(2024, 8, 11, 15, 30, 45);

        for(int i=1; i<=3; i++){
            orderRepository.save(createOrder(i, orderDate));
        }

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestDate", "20240811")
                .toJobParameters();
        //when

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<OrderHistory> all = orderHistoryRepository.findAll();
        Assertions.assertThat(all.size()).isEqualTo(3);
    }
}
