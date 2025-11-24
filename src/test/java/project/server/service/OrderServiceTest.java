package project.server.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.server.domain.Order;
import project.server.repository.OrderRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Test
    void 주문생성_성공() {
        Long productId = 10L;
        Long quantity = 5L;
        Long pricePerUnit = 2000L;

        orderService.createOrder(productId, quantity, pricePerUnit);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void 총수익_계산_성공() {
        Long expectedRevenue = 150000L;

        when(orderRepository.sumTotalRevenue()).thenReturn(Optional.of(expectedRevenue));

        Long actualRevenue = orderService.getTotalRevenue();

        assertEquals(expectedRevenue, actualRevenue);
        verify(orderRepository, times(1)).sumTotalRevenue();
    }

    @Test
    void 총수익_계산_결과없을때_0반환() {
        Long expectedRevenue = 0L;

        when(orderRepository.sumTotalRevenue()).thenReturn(Optional.empty());

        Long actualRevenue = orderService.getTotalRevenue();

        assertEquals(expectedRevenue, actualRevenue);
    }
}