package com.example.payment.controller;

import com.example.payment.dto.request.TransactionPayload;
import com.example.payment.model.Order;
import com.example.payment.model.Transaction;
import com.example.payment.repository.TransactionRepository;
import com.example.payment.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private OrderService orderService;

    @PostMapping
    public void receiveWebhook(@RequestBody TransactionPayload payload) {
        try {
            Transaction transaction = new Transaction();
            transaction.setGateway(payload.getGateway());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime transactionDate = LocalDateTime.parse(payload.getTransactionDate(), formatter);
            transaction.setTransactionDate(transactionDate);

            transaction.setAccountNumber(payload.getAccountNumber());
            transaction.setSubAccount(payload.getSubAccount());
            transaction.setAccumulated(BigDecimal.valueOf(payload.getAccumulated()));
            transaction.setCode(payload.getCode());
            transaction.setTransactionContent(payload.getContent());
            transaction.setReferenceNumber(payload.getReferenceCode());
            transaction.setBody(payload.getDescription());

            if ("in".equals(payload.getTransferType())) {
                transaction.setAmountIn(BigDecimal.valueOf(payload.getTransferAmount()));
                Optional<Order> order = orderService.getOrder(payload.getCode());
                if(order.isPresent()) {
                    orderService.updateOrderStatus(order.get().getOrderCode(), "Completed");
                }
            } else if ("out".equals(payload.getTransferType())) {
                transaction.setAmountOut(BigDecimal.valueOf(payload.getTransferAmount()));
            }

            transactionRepository.save(transaction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/check-payment/{orderCode}")
    public ResponseEntity<String> checkOrderPaymentStatus(@PathVariable String orderCode) {
        Optional<Order> order = orderService.getOrder(orderCode);
        System.out.println("orderCode::::"+ orderCode);
        System.out.println(order);
        if (order.isPresent() && order.get().getStatus().equals("Completed")) {
            return ResponseEntity.ok("PAID");
        } else {
            return ResponseEntity.ok("Đơn hàng chưa thanh toán.");
        }
    }

}
