package project.server.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.server.domain.Product;
import project.server.repository.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PessimisticLockStockServiceTest {
    @InjectMocks
    private PessimisticLockStockService stockService;

    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderService orderService;

    private static final Long AUTO_INFLOW_QUANTITY = 500L;

    @Test
    void 상품주문_성공() {
        Long productId = 1L;
        Long initialStock = 100L;
        Long orderQuantity = 10L;
        String expectedMessage = " 주문 성공";

        Product product = new Product("TestName", initialStock, 10000L);
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(product);

        String actualMessage = stockService.orderProduct(productId, orderQuantity);

        assertEquals(90L, product.getStock());
        assertEquals(expectedMessage, actualMessage);
        verify(productRepository, times(1)).save(product);
        verify(orderService, times(1)).createOrder(productId, orderQuantity, product.getPrice());
    }

    @Test
    void 상품주문_재고부족() {
        Long productId = 2L;
        Long initialStock = 5L;
        long orderQuantity = 10L;

        Long expectedStock = initialStock + AUTO_INFLOW_QUANTITY - orderQuantity;

        Product product = new Product("LowStock", initialStock, 10000L);

        try {
            java.lang.reflect.Field idField = Product.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(product, productId);
        } catch (IllegalAccessException | NoSuchFieldException error) {
            throw new RuntimeException("테스트 상품 ID 설정 실패", error);
        }

        String autoInflowMsg = String.format("재고 부족. 상품 ID: %d -> 자동 입고(%d개) 완료.\n ", productId, AUTO_INFLOW_QUANTITY);
        String expectedMessage = autoInflowMsg + " 주문 성공";

        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(product);

        String actualMessage = stockService.orderProduct(productId, orderQuantity);

        assertEquals(expectedStock, product.getStock());
        assertEquals(expectedMessage, actualMessage);

        verify(productRepository, times(1)).save(product);
        verify(orderService, times(1)).createOrder(anyLong(), anyLong(), anyLong());
    }

    @Test
    void 존재하지_않는_상품ID로_주문_시_예외_발생() {
        Long nonExistentId = 99L;

        when(productRepository.findByIdWithPessimisticLock(nonExistentId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            stockService.orderProduct(nonExistentId, 1L);
        });

        verify(productRepository, never()).save(any());
        verify(orderService, never()).createOrder(anyLong(), anyLong(), anyLong());
    }
}