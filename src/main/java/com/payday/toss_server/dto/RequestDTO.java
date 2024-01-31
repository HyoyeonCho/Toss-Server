package com.payday.toss_server.dto;

import lombok.Data;

import java.sql.Date;

@Data
public class RequestDTO {

    /* 사용자가 결제 요청 시, 저장될 데이터 */

    private String orderId;
    private String customerKey;
    private String orderName;
    private long amount;
    private Date reqTime;

}
