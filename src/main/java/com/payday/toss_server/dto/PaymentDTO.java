package com.payday.toss_server.dto;

import lombok.Data;

@Data
public class PaymentDTO {

    /* 결제 요청을 위해 Client 측에서 값을 받아 오기 위한 DTO */

    private String paymentType; // 결제 타입 (카드/현금)
    private Long amount;        // 가격
    private String orderName;   // 주문명
    private String successUrl;  // 성공 시, 리다이렉트 될 URL
    private String failUrl;     // 실패 시, 리다이렉트 될 URL

}
