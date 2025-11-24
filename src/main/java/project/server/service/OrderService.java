package project.server.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.server.domain.Order;
import project.server.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void createOrder(Long productId, Long quantity, Long pricePerUnit) {
        Order order = new Order(productId, quantity, pricePerUnit);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Long getTotalRevenue() {
        return orderRepository.sumTotalRevenue().orElse(0L);
    }
}
