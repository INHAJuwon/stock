package project.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import project.server.service.StockService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {
    private final StockService stockService;

    @PostMapping
    public ResponseEntity<String> registerProduct(@RequestParam(required = false) String name, @RequestParam Long stock, @RequestParam Long price) {
        String finalName = name;
        if (name == null || name.trim().isEmpty()) {
            finalName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_product";
        }
        stockService.initStock(finalName, stock, price);
        return ResponseEntity.ok("상품 등록 및 초기 재고 설정 완료.");
    }
}
