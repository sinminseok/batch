package study.batch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackingNumber;

    private LocalDateTime shippedDate;

    private String carrier;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public void updateOrder(Order order){
        this.order = order;
    }

}
