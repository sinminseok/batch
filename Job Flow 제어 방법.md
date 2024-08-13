# 🎯 Job Flow 제어하기

Spring Batch에서 Job의 Flow를 제어하는 방법에 대해 알아보겠습니다. 이번 글에서는 **Next**와 **조건별 흐름 제어**, 그리고 **Decide** 방법을 다룹니다.

## Next를 이용한 순차적 Step 연결

가장 먼저 `Next`를 사용하여 Step을 순차적으로 연결하는 방법을 소개합니다. 코드는 아래와 같습니다.

```java
package batch.example.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Configuration
public class SimpleNextJobConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Bean
    public Job simpleNextJob1() {
        return new JobBuilder("simpleNextJob", jobRepository)
                .start(simpleNextStep1())
                .next(simpleNextStep2())
                .next(simpleNextStep3())
                .build();
    }

    @Bean
    public Step simpleNextStep1() {
        return new StepBuilder("simpleNextStep1", jobRepository)
                .tasklet(testNextTasklet(), platformTransactionManager)
                .build();
    }

    @Bean
    public Step simpleNextStep2() {
        return new StepBuilder("simpleNextStep2", jobRepository)
                .tasklet(testNextTasklet2(), platformTransactionManager)
                .build();
    }

    @Bean
    public Step simpleNextStep3() {
        return new StepBuilder("simpleNextStep3", jobRepository)
                .tasklet(testNextTasklet3(), platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet testNextTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is Next Step1");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet testNextTasklet2() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is Next Step2");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet testNextTasklet3() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is Next Step3");
            return RepeatStatus.FINISHED;
        };
    }
}
