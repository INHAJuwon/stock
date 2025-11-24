package project.server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long stock;
    private Long price;
    private Long sold = 0L;

    public Product(String name, Long stock, Long price) {
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public void decrease(Long quantity) {
        this.stock -= quantity;
        this.sold += quantity;
    }

    public void addStock(Long quantity) { // 입고
        this.stock += quantity;
    }
}
