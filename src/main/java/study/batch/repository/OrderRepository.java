package study.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import study.batch.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
