package com.payday.toss_server;

import com.payday.toss_server.config.PaymentConfig;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

    @Autowired
    public PaymentController(PaymentConfig paymentConfig) {
        this.paymentConfig = paymentConfig;
    }

    @PostMapping(value = "/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody) throws Exception {

        JSONParser parser = new JSONParser();
        String orderId;
        String amount;
        String paymentKey;
        try {
            // Client에서 받은 String(JSON 형태)을 JSON 객체로 파싱
            JSONObject requestData = (JSONObject) parser.parse(jsonBody);
            paymentKey = (String) requestData.get("paymentKey");
            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        // 새 JSON 객체를 생성하여 필요한 데이터를 추가
        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

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

        // TODO: 결제 성공 및 실패 비즈니스 로직을 구현
        // 결제 성공 시, paymentKey 및 orderId는 서버에 필수로 저장 (결제 조회, 결제 취소에 사용되는 값)
        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8); // 응답 스트림을 문자열로 읽기 위한 Reader 생성
        JSONObject jsonObject = (JSONObject) parser.parse(reader); // 응답 데이터를 JSON 객체로 파싱
        responseStream.close();

        return ResponseEntity.status(code).body(jsonObject);
    }



}
