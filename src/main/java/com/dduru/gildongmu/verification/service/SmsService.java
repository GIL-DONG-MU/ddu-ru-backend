package com.dduru.gildongmu.verification.service;

import com.dduru.gildongmu.verification.exception.SmsSendFailedException;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
public class SmsService {

    @Value("${coolsms.api-key}")
    private String apiKey;

    @Value("${coolsms.api-secret}")
    private String apiSecret;

    @Value("${coolsms.from-number}")
    private String fromNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    /**
     * SMS 발송 (CoolSMS SDK)
     * @param phoneNumber 수신자 전화번호
     * @param messageText 발송할 메시지
     */
    public void sendSms(String phoneNumber, String messageText) {
        try {
            Message message = new Message();
            message.setFrom(fromNumber);
            message.setTo(phoneNumber);
            message.setText(messageText);

            SingleMessageSendingRequest request = new SingleMessageSendingRequest(message);
            SingleMessageSentResponse response = this.messageService.sendOne(request);

            if (response == null) {
                log.error("CoolSMS 발송 실패: 응답이 null입니다. phoneNumber={}", phoneNumber);
                throw new SmsSendFailedException("SMS 발송에 실패했습니다.");
            }

            // SDK는 성공 시 statusCode가 null이거나 특정 값일 수 있으므로 에러 메시지 확인
            String statusCode = response.getStatusCode();
            if (statusCode != null && !statusCode.equals("2000")) {
                String statusMessage = response.getStatusMessage();
                log.error("CoolSMS 발송 실패: statusCode={}, statusMessage={}, phoneNumber={}", 
                        statusCode, statusMessage, phoneNumber);
                throw new SmsSendFailedException("SMS 발송에 실패했습니다: " + statusMessage);
            }

            log.info("CoolSMS 발송 성공: phoneNumber={}, messageId={}, statusCode={}", 
                    phoneNumber, response.getMessageId(), statusCode);
        } catch (SmsSendFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("CoolSMS 발송 중 오류 발생: phoneNumber={}", phoneNumber, e);
            throw new SmsSendFailedException("SMS 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
