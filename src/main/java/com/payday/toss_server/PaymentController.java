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
@Controller
@RequestMapping(value="/toss")
public class PaymentController {

    private final PaymentConfig paymentConfig;

    @Autowired
    public PaymentController(PaymentConfig paymentConfig) {
        this.paymentConfig = paymentConfig;
    }

    @PostMapping(value = "/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody) throws Exception {

        log.info("confirm 호출!");

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
        boolean isSuccess = code == 200;
        // 응답이 200일 경우, 입력 스트림을 열어 서버로부터의 데이터를 받음 (아닐 경우, 에러 스트림)
        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

        // TODO: 결제 성공 및 실패 비즈니스 로직을 구현
        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8); // 응답 스트림을 문자열로 읽기 위한 Reader 생성
        JSONObject jsonObject = (JSONObject) parser.parse(reader); // 응답 데이터를 JSON 객체로 파싱
        responseStream.close();

        return ResponseEntity.status(code).body(jsonObject);
    }

//    @GetMapping(value = "/success")
//    public String paymentResult(
//            @RequestParam(value = "orderId") String orderId,
//            @RequestParam(value = "amount") Integer amount,
//            @RequestParam(value = "paymentKey") String paymentKey) throws Exception {
//
//        String secretKey = "test_ak_ZORzdMaqN3wQd5k6ygr5AkYXQGwy:";
//
//        Base64.Encoder encoder = Base64.getEncoder();
//        byte[] encodedBytes = encoder.encode(secretKey.getBytes("UTF-8"));
//        String authorizations = "Basic " + new String(encodedBytes, 0, encodedBytes.length);
//
//        URL url = new URL("https://api.tosspayments.com/v1/payments/" + paymentKey);
//
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestProperty("Authorization", authorizations);
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setRequestMethod("POST");
//        connection.setDoOutput(true);
//        JSONObject obj = new JSONObject();
//        obj.put("orderId", orderId);
//        obj.put("amount", amount);
//
//        OutputStream outputStream = connection.getOutputStream();
//        outputStream.write(obj.toString().getBytes("UTF-8"));
//
//        int code = connection.getResponseCode();
//        boolean isSuccess = code == 200 ? true : false;
//        model.addAttribute("isSuccess", isSuccess);
//
//        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
//
//        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
//        JSONParser parser = new JSONParser();
//        JSONObject jsonObject = (JSONObject) parser.parse(reader);
//        responseStream.close();
//        model.addAttribute("responseStr", jsonObject.toJSONString());
//        System.out.println(jsonObject.toJSONString());
//
//        model.addAttribute("method", (String) jsonObject.get("method"));
//        model.addAttribute("orderName", (String) jsonObject.get("orderName"));
//
//        if (((String) jsonObject.get("method")) != null) {
//            if (((String) jsonObject.get("method")).equals("카드")) {
//                model.addAttribute("cardNumber", (String) ((JSONObject) jsonObject.get("card")).get("number"));
//            } else if (((String) jsonObject.get("method")).equals("가상계좌")) {
//                model.addAttribute("accountNumber", (String) ((JSONObject) jsonObject.get("virtualAccount")).get("accountNumber"));
//            } else if (((String) jsonObject.get("method")).equals("계좌이체")) {
//                model.addAttribute("bank", (String) ((JSONObject) jsonObject.get("transfer")).get("bank"));
//            } else if (((String) jsonObject.get("method")).equals("휴대폰")) {
//                model.addAttribute("customerMobilePhone", (String) ((JSONObject) jsonObject.get("mobilePhone")).get("customerMobilePhone"));
//            }
//        } else {
//            model.addAttribute("code", (String) jsonObject.get("code"));
//            model.addAttribute("message", (String) jsonObject.get("message"));
//        }
//
//        return "success";
//    }
//
//    @GetMapping(value = "/fail")
//    public String paymentResult(
//            @RequestParam(value = "message") String message,
//            @RequestParam(value = "code") Integer code
//    ) throws Exception {
//
//        model.addAttribute("code", code);
//        model.addAttribute("message", message);
//
//        return "fail";
//    }

}
