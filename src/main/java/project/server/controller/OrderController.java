package project.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.server.repository.ProductRepository;
import project.server.service.StockService;

import java.util.Random;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final StockService stockService;
    private final ProductRepository productRepository;

    private static final Random RANDOM = new Random();

    @PostMapping("/product")
    public ResponseEntity<String> createOrder(@RequestParam Long quantity) {
        try {
            return ResponseEntity.ok(stockService.orderProduct(randomProduct(), quantity));
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body("[ERROR] 주문 실패: " + error.getMessage());
        }
    }

    private Long randomProduct() {
        long total = productRepository.count();
        return RANDOM.nextLong(total) + 1;
    }
}
