package com.payday.toss_server.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.sql.Date;

@Getter
@Setter
@Entity
@DynamicInsert
@Table(name="TBL_PAYMENT")
public class Payment {

    @Id
    @Column(name="ORDER_ID")
    private String orderId;

    @Column(name="PAYMENT_KEY")
    private String paymentKey;

    @Column(name="CUSTOMER_KEY")
    private String customerKey;

    @Column(name="ORDER_NAME")
    private String orderName;

    @Column(name="AMOUNT")
    private long amount;

    @Column(name="PAY_TIME")
    private Date payTime;

    @Column(name="PAY_YN")
    private char payYN;

}
