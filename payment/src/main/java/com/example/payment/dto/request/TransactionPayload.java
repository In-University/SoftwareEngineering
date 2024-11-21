package com.example.payment.dto.request;

import lombok.Data;

@Data
public class TransactionPayload {
    private Long id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String subAccount;
    private String transferType;
    private Double transferAmount;
    private Double accumulated;
    private String code;
    private String content;
    private String referenceCode;
    private String description;
}

