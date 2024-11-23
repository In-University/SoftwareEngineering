package com.example.payment.controller;

import com.example.payment.dto.request.TransactionPayload;
import com.example.payment.model.Order;
import com.example.payment.model.Transaction;
import com.example.payment.repository.TransactionRepository;
import com.example.payment.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
    @Autowired
    private ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${payment.sepay.apiKey}")
    private String bearerToken;

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
//    @GetMapping("/check-v1/{orderCode}")
//    public ResponseEntity<String> checkOrderPaymentStatus(@PathVariable String orderCode) {
//        Optional<Order> order = orderService.getOrder(orderCode);
//        System.out.println("orderCode::::"+ orderCode);
//        System.out.println(order);
//        if (order.isPresent() && order.get().getStatus().equals("Completed")) {
//            return ResponseEntity.ok("PAID");
//        } else {
//            return ResponseEntity.ok("Đơn hàng chưa thanh toán.");
//        }
//    }

    @GetMapping("/check/{orderCode}")
    public ResponseEntity<String> fetchAndProcessTransactions(@PathVariable String orderCode) {
        String url = "https://my.sepay.vn/userapi/transactions/list?limit=20";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + bearerToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode transactions = jsonNode.get("transactions");
            // System.out.println("transactions::::"+ transactions);
            if (transactions != null && transactions.isArray()) {
                for (JsonNode transaction : transactions) {
                    String code = transaction.get("code").asText(null);
                    if (code != null && code.equals(orderCode)) {
                        Optional<Order> order = orderService.getOrder(code);
                        BigDecimal transactionAmount = new BigDecimal(transaction.get("amount_in").asText("0"));
                        if (order.isPresent() && order.get().getAmount().compareTo(transactionAmount) == 0) {
                            orderService.updateOrderStatus(order.get().getOrderCode(), Order.OrderStatus.COMPLETED);
                            return ResponseEntity.ok("PAID");
                        }
                    }
                }
            }
            return ResponseEntity.ok(orderCode + " not found");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());

        }
    }

}
