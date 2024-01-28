package com.payday.toss_server.dto;

import lombok.Data;

@Data
public class PaymentResDTO {

    /* PaymentDTO로 요청을 한 후,  */

    private String paymentType;
    private Long amount;
    private String orderName;
    private String orderId;
    private String clientEmail;
    private String clientName;
    private String successUrl;
    private String failUrl;
    private String failReason;
    private boolean cancelYN;
    private String cancelReason;
    private String createdAt;

}
