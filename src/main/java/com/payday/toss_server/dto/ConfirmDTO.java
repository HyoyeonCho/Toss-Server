package com.payday.toss_server.dto;

import lombok.Data;

@Data
public class ConfirmDTO {

    /* 결제 승인 요청 시, 넘어오는 데이터 */

    private String orderId;
    private long amount;
    private String paymentKey;

}
