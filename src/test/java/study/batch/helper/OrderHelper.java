package study.batch.helper;

import study.batch.entity.Item;
import study.batch.entity.Order;
import study.batch.entity.Shipment;

import java.time.LocalDateTime;

public class OrderHelper {

    public static Order createOrder(int id, LocalDateTime orderDate){
        Order order = Order.builder().orderNumber("ORDER1").orderDate(orderDate).build();
        order.addItem(Item.builder().name("가방" + id).price(1000).build());
        order.addItem(Item.builder().name("연필" + id).price(500).build());
        order.addItem(Item.builder().name("신발" + id).price(3500).build());
        order.addShipment(Shipment.builder().trackingNumber("trackNumber" + id).carrier("carrier" + id).build());
        order.addShipment(Shipment.builder().trackingNumber("trackNumber" + id).carrier("carrier" + id).build());
        return order;
    }
}
