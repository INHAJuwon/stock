package project.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.server.domain.Product;
import project.server.repository.ProductRepository;

@Service("pessimisticLockStockService")
@RequiredArgsConstructor
public class PessimisticLockStockService implements StockService {
    private final ProductRepository productRepository;
    private final OrderService orderService;
    private static final Long AUTO_INFLOW_QUANTITY = 500L;

    @Override
    @Transactional
    public void initStock(String name, Long stock, Long price) {
        productRepository.save(new Product(name, stock, price));
    }

    @Override
    @Transactional
    public String orderProduct(Long productId, Long quantity) {
        Product product = findProduct(productId);
        String logMessage = "";
        if (product.getStock() < quantity) {
            logMessage = String.format("재고 부족. 상품 ID: %d -> 자동 입고(%d개) 완료.\n ", product.getId(), AUTO_INFLOW_QUANTITY);
            product.addStock(AUTO_INFLOW_QUANTITY);
        }

        product.decrease(quantity);
        productRepository.save(product);

        orderService.createOrder(productId, quantity, product.getPrice());
        return logMessage + " 주문 성공";
    }

    private Product findProduct(Long productId) {
        Product product = productRepository.findByIdWithPessimisticLock(productId);
        if (product == null)
            throw new IllegalArgumentException("상품 ID를 찾을 수 없습니다.");
        return product;
    }
}
