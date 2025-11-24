package project.server.service;

public interface StockService {
    void initStock(String name, Long stock, Long price);
    String orderProduct(Long productId, Long quantity);
}
