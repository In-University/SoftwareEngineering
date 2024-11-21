package com.example.payment.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "orders")
public class Order {
    @Id
    private String orderCode;
    private String paymentMethod;
    private BigDecimal amount;
    private Long userId;
    private String status;
    @PrePersist
    public void setDefaultStatus() {
        if (this.status == null) {
            this.status = "Pending";
        }
    }

}

