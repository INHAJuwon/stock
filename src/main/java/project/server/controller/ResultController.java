package project.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.server.domain.Product;
import project.server.repository.ProductRepository;
import project.server.service.OrderService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/result")
public class ResultController {
    private final ProductRepository productRepository;
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<String> getFinalResult() {
        List<Product> stockStatus = productRepository.findAll();
        Long totalRevenue = orderService.getTotalRevenue();
        String result = "";

        log.info("========================================");
        log.info("🚀 JMeter 테스트 최종 결과 보고");
        log.info("최종 총 수익: {}원", totalRevenue);
        log.info("재고 현황:\n");
        for (Product product : stockStatus) {
            log.info("상품 ID: {}, 이름: {}, 판매 개수: {}개, 최종 재고: {}개\n", product.getId(), product.getName(), product.getSold(), product.getStock());
            result += String.format("상품 ID: %d, 이름: %s, 판매 개수: %d개, 최종 재고: %d개\n", product.getId(), product.getName(), product.getSold(), product.getStock());
        }
        log.info("========================================");

        String responseBody = String.format("테스트 완료.\n\n" + "최종 수익: %d원\n\n재고 현황:\n%s", totalRevenue, result);

        return ResponseEntity.ok(responseBody);
    }
}