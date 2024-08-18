package study.batch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "OrderTable")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    private LocalDateTime orderDate;

    // Item과의 One-to-Many 관계 설정
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Shipment> shipments = new ArrayList<>();

    public void addItem(Item item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        item.updateOrder(this);
    }

    public void addShipment(Shipment shipment) {
        if (this.shipments == null) {
            this.shipments = new ArrayList<>();
        }
        this.shipments.add(shipment);
        shipment.updateOrder(this);
    }

    public Integer calculatePrice() {
        return items.stream()
                .mapToInt(Item::getPrice)
                .sum();
    }
}