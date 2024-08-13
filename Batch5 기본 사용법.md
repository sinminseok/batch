## Spring Batch

****


**Spring Batch란**

`배치(Batch)` 는 일괄처리 라는 의미를 가지고 있습니다. 즉, 데이터를 실시간으로 처리하는 것이 아닌 일괄적으로 처리하는 작업을 의미합니다. 이러한 배치 프로그램을 만들때는 다음 조건을 만족해야합니다.

```
(1) 대용량 데이터 : 대량의 데이터를 조회, 전달, 계산등의 처리를 할 수 있어야 합니다.

(2) 자동화 : 심각한 문제 해결을 제외하곤 사용자의 개입이 없이 실행되어야 합니다.

(3) 견고성 : 잘못된 데이터를 충돌/중단 없이 처리할 수 있어야 합니다.

(4) 신뢰성 : 무엇이 잘못되었는지 추적할 수 있어야 합니다. (로깅, 알림)

(5) 성능 : 지정한 시간 안에 처리를 완료하거나 동시에 실행되는 다른 어플리케이션을 방해하지 않도록 수행되어야 합니다.
```

****
**Spring Batch의 주요 구성요소**

```
- Job : Job 은 배치 작업의 전체적인 실행 단위입니다. 하나의 Step 으로 구성됩니다.

- JobInstance : Job 의 실행 시도를 나타내며, 동일한 Job 에 대해 여러번 실행될 수 있고 각 실행은 고유한 JobInstance 를 가질 수 있습니다.

- JobExecution : JobInstance 의 실행 상태 및 메타 데이터를 포함합니다. Job 의 시작시간, 종료 시간, 상태등의 정보를 제공합니다.

- JobParameter : Job 실행 시 외부에서 전달되는 파라미터를 나타냅니다. Job 재실행 시 파라미터를 사용해 동일한 JobInstance 를 재 생성할 수 있습니다.

- JobLauncher : Job 을 실행시키는데 사용되는 인터페이스입니다. Job 과 JobParameter 를 사용해 Job 을 실행합니다.

- JobRepository : Job,JobInstance,JobExecution 등의 상태와 메타 데이터를 저장하고 관리하는 저장소입니다. 데이터 베이스에 이러한 정보를 저장해 추적, 관리할 수 있습니다.

- Step : Job 을 구성하는 개별단위 작업입니다. Step 은 Tasklet 로 구성됩니다.

- Tasklet : Step 내에서 단일 작업을 수행하는 구성요소 입니다. 단일 작업을 처리하고 완료 상태를 반환합니다.

- Chunk : 대용량 데이터를 일정 크기 (chunk) 로 나누어 처리하는 방식입니다. 각 Chunk 는 ItemReader, ItemProcessor, ItemWriter 로 구성됩니다.

- ItemReader : Chunk 기반 처리에서 데이터를 읽어오는 역할을 합니다.
 
- ItemWriter : Chunk 기반 처리에서 데이터를 저장하는 역할을 합니다.

- ItemProcessor : 읽어온 데이터를 처리하는 역할을 합니다. 데이터를 변환하거나, 필터링 하는 등의 중간 처리 작업을 수행합니다.

```

**Spring Batch 5 로 Job 만들어 보기**

✅ **build.gradle**

```
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-batch'
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.mysql:mysql-connector-j'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testImplementation 'org.springframework.batch:spring-batch-test'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

✅ **SimpleJobConfiguration**

```
package study.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RequiredArgsConstructor // 생성자 DI 를 위한 애노테이션
@Configuration
public class SimpleJobConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Bean
    public Job simpleJob (){
        return new JobBuilder("simpleJob", jobRepository)
                .start(simpleStep1())
                .build();
    }

    @Bean
    public Step simpleStep1(){
        return new StepBuilder("simpleStep1", jobRepository)
                .tasklet(testTasklet(), platformTransactionManager).build();
    }
    @Bean
    public Tasklet testTasklet(){
        return ((contribution, chunkContext) -> {
            log.info(">>>>> This is Step1");
            return RepeatStatus.FINISHED;
        });
    }
}
```

간단한 Job 을 생성하는 코드입니다.

먼저 메서드에서 사용할 JobRepository 와 PlatformTransactionManager 를 @Autowired 를 통해 주입받아 필드로 선언합니다. 이렇게 선언된 값들은 Job 과 Step 을 만들때 사용될 예정입니다.

**JobRepository 는 배치 작업의 메타데이터와 실행 상태를 저장하고 관리하는 역할**을 합니다. 배치 작업의 시작, 종료, 중단, 재 시작등의 상태를 추적합니다.

**PlatformTransactionManager 는 트랜잭션 관리를 담당하는 인터페이스입니다**. 배치작업 간 단계(Step) 에서의 트랜잭션 경계를 정의하고 관리합니다.

`simpleJob()`  

Job 을 생성하는 메서드입니다. .start() 내부에 있는 simpleStep1 은 Job 이 실행될때 처음 단계에서 시작합니다.

`simpleStep1()`  

Step 을 생성하는 메서드입니다. .tasklet() 내부에서는 Step 내부에서 실행될 Tasklet 을 나타냅니다.

`testTasklet()`  

람다식을 활용해 Tasklet 을 정의합니다. 로그 메시지를 출력하고 작업이 완료되면 RepeatStatus.FINISHED 를 반환해 Step 이 종료됐음을 알립니다.



