package com.payday.toss_server.controller;

import com.payday.toss_server.service.PaymentService;
import com.payday.toss_server.config.PaymentConfig;
import com.payday.toss_server.dto.ConfirmDTO;
import com.payday.toss_server.dto.PaymentDTO;
import com.payday.toss_server.dto.RequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@CrossOrigin
@Controller
@RequestMapping("/toss")
public class PaymentController {

    private final PaymentConfig paymentConfig;
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentConfig paymentConfig, PaymentService paymentService) {
        this.paymentConfig = paymentConfig;
        this.paymentService = paymentService;
    }

    @PostMapping(value = "/request")
    public ResponseEntity<?> requestPayment(@RequestBody RequestDTO requestDTO) {
        /* 결제 성공/실패 여부 상관없이 사용자가 결제 요청 시, 로그 등록 */
        paymentService.insertRequest(requestDTO);

        return ResponseEntity.ok().body("결제 요청 로그 등록 완료");
    }

    @PostMapping(value = "/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody ConfirmDTO confirmDTO) throws Exception {

        // JSON 객체에 필요한 데이터 추가
        JSONObject obj = new JSONObject();
        obj.put("orderId", confirmDTO.getOrderId());
        obj.put("amount", confirmDTO.getAmount());
        obj.put("paymentKey", confirmDTO.getPaymentKey());

        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용, 비밀번호는 사용X
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론 추가
        String secretKey = paymentConfig.getSecretKey();
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        // 결제를 승인하면 결제수단에서 금액 차감
        URL url = new URL(PaymentConfig.URL + "confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true); // 요청에 데이터를 포함

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes(StandardCharsets.UTF_8)); // obj(JSON 객체)를 바이트 배열로 변환하여 출력 스트림에 전송

        int code = connection.getResponseCode();
        log.info("code: " + code);
        boolean isSuccess = code == 200;
        // 응답이 200일 경우, 입력 스트림을 열어 서버로부터의 데이터를 받음 (아닐 경우, 에러 스트림)
        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

        JSONParser parser = new JSONParser();
        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8); // 응답 스트림을 문자열로 읽기 위한 Reader 생성
        JSONObject jsonObject = (JSONObject) parser.parse(reader); // 응답 데이터를 JSON 객체로 파싱
        responseStream.close();

        return ResponseEntity.status(code).body(jsonObject);
    }

    @PostMapping(value = "/result")
    public ResponseEntity<?> insertPayment(@RequestBody PaymentDTO paymentDTO) {
        /* confirm 이후, 결제 결과 로그 등록 */
        paymentService.insertPayment(paymentDTO);

        return ResponseEntity.ok().body("결제 결과 로그 등록 완료");
    }

}
