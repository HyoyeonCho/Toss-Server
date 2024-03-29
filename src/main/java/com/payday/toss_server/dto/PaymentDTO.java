package com.payday.toss_server.dto;

import lombok.Data;

import java.sql.Date;

@Data
public class PaymentDTO {

    /* 결제 승인 시, 저장될 데이터 */

    private String orderId;
    private String paymentKey;
    private String customerKey;
    private String orderName;
    private long amount;
    private Date payTime;
    private char payYN;

}
