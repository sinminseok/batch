package study.batch.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 배치 테스트 환경 설정
 */
@Configuration
@EnableAutoConfiguration
@EnableBatchProcessing
public class TestBatchLegacyConfig {

    /**
     * JobLauncherTestUtils : Batch Job 을 테스트 환경에서 실행할 Utils 클래스입니다. CLI 등으로 실행하는 Job 을 테스트 코드에서 Job 을 실행할 수 있도록 지원
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        // application-test.yml 설정을 적용
        dataSource.setDriverClassName("org.h2.Driver");  // H2 드라이버 클래스 이름
        dataSource.setUrl("jdbc:h2:~/testDB;MODE=MySQL");  // H2 데이터베이스 URL (MySQL 모드)
        dataSource.setUsername("sa");  // 데이터베이스 접속 사용자 이름
        dataSource.setPassword("");  // 데이터베이스 접속 비밀번호 (비어 있음)

        return dataSource;
    }

}
