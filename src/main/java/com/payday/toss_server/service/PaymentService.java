package com.payday.toss_server.service;

import com.payday.toss_server.Entity.Payment;
import com.payday.toss_server.Entity.Request;
import com.payday.toss_server.dto.PaymentDTO;
import com.payday.toss_server.dto.RequestDTO;
import com.payday.toss_server.repository.PaymentRepository;
import com.payday.toss_server.repository.RequestRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RequestRepository requestRepository;
    private final ModelMapper modelMapper;

    public PaymentService(PaymentRepository paymentRepository, RequestRepository requestRepository, ModelMapper modelMapper) {
        this.paymentRepository = paymentRepository;
        this.requestRepository = requestRepository;
        this.modelMapper = modelMapper;
    }

    public void insertRequest(RequestDTO requsetDTO) {
        requestRepository.save(modelMapper.map(requsetDTO, Request.class));
    }

    public void insertPayment(PaymentDTO paymentDTO) {
        paymentRepository.save(modelMapper.map(paymentDTO, Payment.class));
    }

}
