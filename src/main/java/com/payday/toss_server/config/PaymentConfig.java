package com.payday.toss_server.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

@Configuration
@Getter
public class PaymentConfig {

    @Value("${toss.payment.client_key}")
    private String clientKey;

    @Value("${toss.payment.secret_key}")
    private String secretKey;

    @Value("${toss.payment.success_url}")
    private String successUrl;

    @Value("${toss.payment.fail_url}")
    private String failUrl;

    // 토스 페이먼츠 API 요청 시, 필요한 URL
    public static final String URL = "https://api.tosspayments.com/v1/payments/";

}
