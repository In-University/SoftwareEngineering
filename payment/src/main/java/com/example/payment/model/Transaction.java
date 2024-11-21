package com.example.payment.model;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gateway;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "sub_account")
    private String subAccount;

    @Column(name = "amount_in", precision = 20, scale = 2)
    private BigDecimal amountIn;

    @Column(name = "amount_out", precision = 20, scale = 2)
    private BigDecimal amountOut;

    @Column(name = "accumulated", precision = 20, scale = 2)
    private BigDecimal accumulated;

    private String code;

    @Column(name = "transaction_content", columnDefinition = "TEXT")
    private String transactionContent;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

