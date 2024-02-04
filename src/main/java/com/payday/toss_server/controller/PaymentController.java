package com.payday.toss_server.controller;

import com.payday.toss_server.dto.UserDTO;
import com.payday.toss_server.service.PaymentService;
import com.payday.toss_server.config.PaymentConfig;
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

    @GetMapping(value = "/userInfo")
    public ResponseEntity<UserDTO> selectUser() {
        // 현재 로그인한 사용자의 정보를 조회합니다.
        // [임시] 샘플 코드에서는 실제 인증 정보 대신 식별자로 간단히 사용자를 조회하겠습니다.
        long userId = 1;

        return ResponseEntity.ok().body(paymentService.selectUser(userId));
    }

    @PostMapping(value = "/request")
    public ResponseEntity<String> insertRequest(@RequestBody RequestDTO requestDTO) {
        // 결제 성공/실패 여부 상관없이 사용자가 결제를 요청할 시, 해당 정보를 DB에 저장합니다. (선택 사항)
        paymentService.insertRequest(requestDTO);

        return ResponseEntity.ok().body("결제 요청 정보 저장 완료");
    }

    @PostMapping(value = "/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody PaymentDTO paymentDTO) throws Exception {

        // 토스 API에 보낼 JSON 객체를 생성합니다.
        JSONObject obj = new JSONObject();
        obj.put("orderId", paymentDTO.getOrderId());
        obj.put("amount", paymentDTO.getAmount());
        obj.put("paymentKey", paymentDTO.getPaymentKey());

        // 토스 API는 시크릿 키를 사용자 ID로 사용하며 비밀번호는 사용하지 않습니다.
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가한 후, 인코딩하는 과정이 필요합니다.
        String secretKey = paymentConfig.getSecretKey();
        String authorizations = "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        // 토스의 결제 승인 API를 요청합니다.
        URL url = new URL(PaymentConfig.URL + "confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes(StandardCharsets.UTF_8));

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;
        // 응답이 200일 경우, 입력 스트림을 열어 토스로부터 데이터를 받습니다.
        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

        JSONParser parser = new JSONParser();
        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8); // 응답 스트림을 문자열로 읽기 위한 Reader 생성
        JSONObject jsonObject = (JSONObject) parser.parse(reader); // 응답 데이터를 JSON 객체로 파싱
        responseStream.close();

        String errorCode = (String) jsonObject.get("code");
        if(errorCode == null || !errorCode.equals("ALREADY_PROCESSED_PAYMENT")) {
            // 응답에 따라 DB에 최종 결제 결과를 등록합니다. (이미 처리된 결제일 경우는 등록하지않습니다.)
            char payYN = isSuccess ? 'Y' : 'N';
            paymentDTO.setPayYN(payYN);
            paymentService.insertPayment(paymentDTO);
        }

        return ResponseEntity.status(code).body(jsonObject);
    }

}
