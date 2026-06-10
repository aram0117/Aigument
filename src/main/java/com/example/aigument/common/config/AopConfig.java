package com.example.aigument.common.config;

import com.example.aigument.common.aop.AopOrder;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(order = AopOrder.TRANSACTION)
public class AopConfig {
}
