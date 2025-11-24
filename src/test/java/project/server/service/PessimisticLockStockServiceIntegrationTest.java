package project.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import project.server.domain.Product;
import project.server.repository.OrderRepository;
import project.server.repository.ProductRepository;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PessimisticLockStockServiceIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StockService pessimisticLockStockService;

    private Long testProductId;
    private static final Long INITIAL_STOCK = 5L;
    private static final Long PRODUCT_PRICE = 10000L;
    private static final Long AUTO_INFLOW_QUANTITY = 500L;
    private static final int THREAD_COUNT = 100;
    private static final Long ORDER_QUANTITY_PER_THREAD = 10L;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        orderRepository.deleteAll();

        Product product = new Product("비관적락_테스트_상품", INITIAL_STOCK, PRODUCT_PRICE);
        productRepository.save(product);

        Product savedProduct = productRepository.findAll().getFirst();
        testProductId = savedProduct.getId();
    }

    @Test
    void 비관적_락_동시성_제어_및_재고_입고_테스트() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    pessimisticLockStockService.orderProduct(testProductId, ORDER_QUANTITY_PER_THREAD);
                } catch (PessimisticLockingFailureException error) {
                    System.err.println("대기 시간 초과: " + error.getMessage());
                } catch (IllegalArgumentException error) {
                    System.err.println("주문 오류: " + error.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Product finalProduct = productRepository.findById(testProductId)
                .orElseThrow(() -> new AssertionError("테스트 상품을 찾을 수 없습니다."));

        Long expectedFinalStock = INITIAL_STOCK + (2 * AUTO_INFLOW_QUANTITY) - (THREAD_COUNT * ORDER_QUANTITY_PER_THREAD);

        assertEquals(expectedFinalStock, finalProduct.getStock(), "최종 재고가 예상과 다릅니다.");

        long totalOrders = orderRepository.count();
        assertEquals(THREAD_COUNT, totalOrders, "총 주문 건수가 예상과 다릅니다.");

        Long expectedTotalRevenue = THREAD_COUNT * ORDER_QUANTITY_PER_THREAD * PRODUCT_PRICE;
        Long actualTotalRevenue = orderRepository.sumTotalRevenue().orElse(0L);
        assertEquals(expectedTotalRevenue, actualTotalRevenue, "총 수익이 예상과 다릅니다.");
    }
}