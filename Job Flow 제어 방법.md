Job 플로우 제어하기

🎯 여러개의 Step 들간 순서, 흐름 제어해보기


가장 먼저 소개할 방법은 Next 입니다. 쉬운 개념이니 코드부터 확인해보겠습니다.



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


simpleJob1() 을 살펴보면 .start() 이후에 .next() 를 선언했고, 내부에 각각 실행시킬 step 들을 넣어줬습니다. next() 는 순차적으로 Step 을 연결시킬때 사용합니다.



그러면 실행을 시켜보면,,, 두둥






org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'jobLauncherApplicationRunner' defined in class path resource [org/springframework/boot/autoconfigure/batch/BatchAutoConfiguration.class]: Job name must be specified in case of multiple jobs



이런 에러가 발생합니다. 이전에 만들어둔 SimpleJobConfiguration 과 방금 만든 SimpleNextJobConfiguration 에서 각각 Job 을 만들었는데, 어떤 Job 을 실행시킬지 spring batch 에서 알 수 없어 발생한 에러같습니다.



이를 해결할 방법은 크게 두가지가 있습니다.



- application.yml 에서 실행할 Job 지정하기

- 애플리케이션 코드 수정



(이번 포스팅에선 applcation.yml 에서 job 을 직접 지정하는 방법을 사용하겠습니다.)



# Spring Datasource Configuration
spring:

datasource:
initialization-mode : always
driver-class-name: com.mysql.cj.jdbc.Driver
url: jdbc:mysql://localhost:3306/batch?serverTimezone=Asia/Seoul
username: [username]
password: [password]
batch:
job:
enabled: true
name: simpleNextJob
jdbc:
initialize-schema: always


이런식으로 batch.job.name 에 실행할 job 이름을 넣어줍니다. 그리고 실행시키면?






짜잔! 정상적으로 simpleNextJob 이 step1 -> step2 ->step3  순서로 실행되는걸 확인할 수 있습니다.



🎯 조건별 흐름 제어하기
배치 프로세스는 한가지 플로우만을 가지지 않습니다. 예를 들어 step1, step2, step3 가 있는 상황에서 모두 순차적으로 정상 작동하면 좋겠지만, step1 에서 오류가 발생한 경우 나머지 뒤에 있는 step 들이 실행되지 않습니다. 때문에 각각의 step 이 어떻게 동작하냐에 따라 흐름을 제어할 수 있어야 합니다.



예를 들어 step1 이 정상일때는 step2, step3 가 순차적으로 실행되어야 하고, step1 이 비정상일때는 step2 를 건너뛰고 step3 가 실행되어야 할 수 있습니다.



배치 프로세스는 이러한 각각의 step 에 대한 조건별 흐름을 제어할 수 있어야 합니다. 코드는 다음과 같습니다.



각각의 step 시나리오는 다음과 같습니다.



- step1 실패시 : step1 -> step3

- step1 성공시 : step1 -> step2 -> step3

package batch.example.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.Executors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StepNextConditionalJobConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Bean
    public Job simpleNextConditionalJob1() {
        return new JobBuilder("stepNextConditionalJob", jobRepository)
                .start(conditionalJobStep1())
                    .on("FAILED") // 만약 ExitStatus.FAILED 라면?
                    .to(conditionalJobStep3())// step3 로 이동한다.
                    .on("*")//step3 와 관계 없이
                    .end()//steo 3로 이동하면 FLOW 종료
                .from(conditionalJobStep1()) // step1 로부터
                    .on("*")// FAILED 외에 모든 경우
                    .to(conditionalJobStep2()) //step2 로 이동!
                    .next(conditionalJobStep3())// step2 가 정상 종료된다면 step3 로 이동한다.
                    .on("*")// step3 의 결과와 상관없이
                    .end() // step3 로 이동하면 FLOW 종료
                .end()// Job 종료
                .build();
    }

    @Bean
    public Step conditionalJobStep1() {
        return new StepBuilder("conditionalJobStep1", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step1");
                    contribution.setExitStatus(ExitStatus.FAILED);
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    @Bean
    public Step conditionalJobStep2() {
        return new StepBuilder("conditionalJobStep2", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step2");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }
    @Bean
    public Step conditionalJobStep3() {
        return new StepBuilder("conditionalJobStep3", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step3");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }


}


simpleNextConditionalJob1() 메서드는 Job 내부에서 step 의 실행 흐름도를 정의했습니다. 어려운 내용은 아니여서, 주석을 참고하시면 충분히 이해할 수 있을겁니다! 주의해서 봐야할 곳은 .on() 에서  "FAILED" 를 캐치하는데, 이는 상태값이 ExitStatus 라는 점 입니다. 즉, 분기 처리를 위해 상태값 조정이 필요하면 ExitStatus 를 조정해야 합니다.



conditionalJobStep1 에서  contribution.setExitStatus(ExitStatus.FAILED) 를 설정해 step1 에서 FAILED 를 발생 시켰습니다.



이를 실행시키면 다음과 같이 step1 이후 step3 가 실행되는 것을 확인할 수 있습니다 .






다음과 같이 코드를 살작 수정해서 실행시키면



    @Bean
    public Step conditionalJobStep1() {
        return new StepBuilder("conditionalJobStep1", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step1");
                    //contribution.setExitStatus(ExitStatus.FAILED);
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }



짠 step1 에서 FAILED 가 발생하지 않아 step1 -> step2 -> step3 순서로 실행되는 것을 확인할 수 있습니다



🎯 Decide
위 방식에서 진행한 분기 처리 방식은 두가지 문제가 있습니다.



(1) Step 의 역할이 2개 이상이 됩니다. 즉, Step 이 실제로 처리해야할 로직 이외에 분기 처리를 위한 로직이 추가됩니다.

(2) 다양한 분기 로직 처리가 어렵습니다. ExitStatus 를 커스텀하게 고치기 위해선 Listener 를 생성해 Job Flow 에 등록하는 등 번거롭습니다.



명확하게 Step 들간의 Flow 분기만 담당하고 다양한 분기처리가 가능한 타입이 바로 JobExecutionDecider 입니다. 이를 이용한 예제 코드는 다음과 같습니다.



package batch.example.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Random;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DeciderJobConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Bean
    public Job deciderJob(){
        return new JobBuilder("deciderJob", jobRepository)
                .start(startStep())
                .next(decider()) // 홀수, 짝수 구분
                .from(decider()) //decider 상태가
                    .on("ODD") //ODD 라면
                    .to(oddStep())//oddStep 실행
                .from(decider()) // decider 상태가
                    .on("EVEN") // EVEN 이라면
                    .to(evenStep())//evenStep 으로 간다.
                .end()
                .build();
    }

    @Bean
    public Step startStep(){
        return new StepBuilder("startStep", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>> Start");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    @Bean
    public Step evenStep(){
        return new StepBuilder("evenStep", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info("짝수 입니다.");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    @Bean
    public Step oddStep() {
        return new StepBuilder("conditionalJobStep3", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info("홀수 입니다.");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    @Bean
    public JobExecutionDecider decider() {
        return new OddDecider();
    }

    /**
     * 모든 분기 로직은 OddDecider 가 담당. Step 과 역할과 책임이 분리
     */
    public static class OddDecider implements JobExecutionDecider {

        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            Random random = new Random();

            int randomNumber = random.nextInt(50) + 1;
            log.info("랜덤 숫자:{}", randomNumber);

            if(randomNumber % 2 == 0){
                return new FlowExecutionStatus("EVEN");
            }
            return new FlowExecutionStatus("ODD");
        }
    }
}


해당 코드에서는 분기 로직을 OddDecider 가 담당합니다. JobExecutionDecider 를 상속받아 분기로직을 구현하고 이를 decider 메서드에서 호출합니다. 이렇게 분리된 분기 로직을 이용해 Step 과 역할과 책임을 분리 했습니다. 위 코드를 실행시켜보면..






뾰로롱 OddDecider 가 분기처리(홀수 짝수 구분) 를 한 뒤 각각의 상황에 따라 step 을 실행시키고 있습니다.!  정리해보면, JobExecutionDecider 는 Step 들의 Flow 속에서 분기만을 담당하는 타입 임을 확인할 수 있습니다.



