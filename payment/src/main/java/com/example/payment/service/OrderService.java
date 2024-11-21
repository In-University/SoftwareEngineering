package com.example.payment.service;

import com.example.payment.model.Order;
import com.example.payment.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public String generateQrCodeUrl(String orderCode, BigDecimal amount) {
        String accountNumber = "014028268888";
        String bankCode = "MB";
        String template = "compact";
        boolean download = false;

        return String.format(
                "https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%s&des=%s&template=%s&download=%s",
                accountNumber,
                bankCode,
                amount,
                orderCode,
                template,
                download
        );
    }

    public Order createOrder(String paymentMethod, BigDecimal amount, Long userId) {
        Order order = new Order();
        order.setPaymentMethod(paymentMethod);
        order.setAmount(amount);
        order.setUserId(userId);
        order.setOrderCode(generateOrderCode(userId));
        orderRepository.save(order);
        return order;
    }

    public String generateOrderCode(Long userId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
        String currentDate = dateFormat.format(new Date());
        Random random = new Random();
        int randomNumber = random.nextInt(9000) + 1000;
        return "CLA" + userId + currentDate + randomNumber;
    }


    public Optional<Order> getOrder(String orderCode) {
        return orderRepository.findByOrderCode(orderCode);
    }

    public boolean updateOrderStatus(String orderCode, String status) {
        Optional<Order> orderOptional = getOrder(orderCode);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(status);
            orderRepository.save(order);
            return true;
        }

        return false;
    }


}
