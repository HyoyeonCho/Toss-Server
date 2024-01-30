package com.payday.toss_server.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class PaymentConfig {

    @Value("${toss.payment.client_key}")
    private String clientKey;

    @Value("${toss.payment.secret_key}")
    private String secretKey;

    // 토스 페이먼츠 API 요청 시, 필요한 URL
    public static final String URL = "https://api.tosspayments.com/v1/payments/";

}
