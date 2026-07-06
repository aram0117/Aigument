package com.example.aigument.common.infra.sms.service;

import com.example.aigument.common.config.RandomCodeConfig;
import com.example.aigument.common.exception.CustomException;
import com.example.aigument.common.infra.sms.dto.request.AuthCodeRequest;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiEmptyResponseException;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.example.aigument.common.exception.ErrorCode.SMS_SEND_FAILED;
import static com.example.aigument.common.infra.redis.RedisKeys.smsAuth;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolapiService {

    private static final long AUTH_CODE_TTL_MINUTES = 3L;

    @Value("${solapi.key}")
    private String solapiApiKey;

    @Value("${solapi.secret}")
    private String solapiApiSecretKey;

    @Value("${solapi.sender.phonenumber}")
    private String solapiSenderPhoneNumber;

    private final RandomCodeConfig randomCodeConfig;

    private final StringRedisTemplate redisTemplate;

    private DefaultMessageService messageService;


    // solapi 인스턴스 초기화
    @PostConstruct
    public void init() {
        this.messageService = SolapiClient.INSTANCE.createInstance(solapiApiKey, solapiApiSecretKey);
    }


    public void sendAuthCode(AuthCodeRequest request) {

        String targetNumber = request.getPhoneNumber();
        String verificationCode = generateVerificationCode();

        // sms 메시지 전송
        sendSms(targetNumber, verificationCode);

        // Redis 저장 (Key 일관성 유지)
        String redisKey = smsAuth(targetNumber);
        redisTemplate.opsForValue().set(redisKey, verificationCode, AUTH_CODE_TTL_MINUTES, TimeUnit.MINUTES);
    }


    /**
     * 랜덤 코드 생성 메서드
     */
    private String generateVerificationCode() {

        int randomCode = randomCodeConfig.secureRandom().nextInt(1000000); // 0 ~ 999,999

        return String.format("%06d", randomCode); // 6자리 미만 난수 공백 -> 0으로 치환
    }

    /**
     * sms 전송 메서드
     * solapi 메시지 예제 (kotlin sdk 1.0.3 version 기준) <a href="https://solapi.com/developers/sdk/java-sendingexample">...</a>
     * sms 기능만 필요 o, solapi json 응답 필요 x
     */
    private void sendSms(String recipientNumber, String verificationCode) {

        Message message = new Message();
        message.setFrom(solapiSenderPhoneNumber); // 발신자 번호
        message.setTo(recipientNumber); // 수신자 번호
        message.setText(String.format("[Aigument] 인증번호는 [%s]입니다.", verificationCode));

        try {
            messageService.send(message);
        } catch (AbstractMethodError e) {  // 응답 파싱 과정의 라이브러리 충돌 무시 (문자는 발송됨)
            log.info("인증 코드 발송 성공");
        } catch (SolapiMessageNotReceivedException | SolapiEmptyResponseException | SolapiUnknownException e) {
            throw new CustomException(SMS_SEND_FAILED);
        }
    }
}
