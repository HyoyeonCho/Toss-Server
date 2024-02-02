package com.payday.toss_server.service;

import com.payday.toss_server.Entity.Payment;
import com.payday.toss_server.Entity.Request;
import com.payday.toss_server.dto.PaymentDTO;
import com.payday.toss_server.dto.RequestDTO;
import com.payday.toss_server.dto.UserDTO;
import com.payday.toss_server.repository.PaymentRepository;
import com.payday.toss_server.repository.RequestRepository;
import com.payday.toss_server.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public PaymentService(PaymentRepository paymentRepository, RequestRepository requestRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.paymentRepository = paymentRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public UserDTO selectUser(long userId) {
        return userRepository.findByUserId(userId)
                .map(user -> modelMapper.map(user, UserDTO.class))
                .orElse(null);
    }

    @Transactional
    public void insertRequest(RequestDTO requestDTO) {
        requestRepository.save(modelMapper.map(requestDTO, Request.class));
    }

    @Transactional
    public void insertPayment(PaymentDTO paymentDTO) {
        paymentRepository.save(modelMapper.map(paymentDTO, Payment.class));
    }

}
