package com.example.payment.controller;

import com.example.payment.model.Order;
import com.example.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequestMapping
    public String show(Model model) {
        return "payment-form";
    }

    @PostMapping
    public String processOrder(
            @RequestParam String paymentMethod,
            @RequestParam BigDecimal amount,
            @RequestParam Long userId,
            Model model) {

        Order order = orderService.createOrder(paymentMethod, amount, userId);
        System.out.println(order);
        if ("transfer".equals(paymentMethod)) {
            String qrCodeUrl = orderService.generateQrCodeUrl(order.getOrderCode(), amount);
            model.addAttribute("orderId", order.getOrderCode());
            model.addAttribute("qrCodeUrl", qrCodeUrl);
            return "qr-code-view";
        }
        return "redirect:/orders/" + order.getOrderCode();
    }
}

