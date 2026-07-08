package com.example.aigument.common.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class SmsProperties {

    @Value("${sms.auth-code.ttl-minutes:3}")
    private long authCodeTtlMinutes;

    @Value("${solapi.key}")
    private String solapiApiKey;

    @Value("${solapi.secret}")
    private String solapiApiSecretKey;

    @Value("${solapi.sender.phonenumber}")
    private String solapiSenderPhoneNumber;
}
