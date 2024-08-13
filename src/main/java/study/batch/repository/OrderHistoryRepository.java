package study.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.batch.entity.OrderHistory;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
}
