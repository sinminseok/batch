//package study.batch.entity;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//@Entity
//@Setter
//@Getter
//public class Item {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String name;
//
//    @ManyToOne
//    @JoinColumn(name = "order_id")
//    private Order teacher;
//}