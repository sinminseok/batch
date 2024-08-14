package study.batch.parameters;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Getter
@NoArgsConstructor
public class CreateDateJobParameter {

    /**
     * Enum, Long, String 타입은 직접 필드로 받아도 형변환이 가능 즉, 개별 setter 를 사용하지 않고 직접 주입받는것이 가능
     */
    @Value("#{jobParameters[status]}")
    private String status;

    private LocalDate requestDate;

    @Value("#{jobParameters[requestDate]}")
    public void setCreateDate(String requestDate) {
        this.requestDate = LocalDate.parse(requestDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
