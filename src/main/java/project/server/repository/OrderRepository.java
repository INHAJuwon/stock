package project.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.server.domain.Order;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT SUM(o.totalRevenue) FROM Order o")
    Optional<Long> sumTotalRevenue();
}