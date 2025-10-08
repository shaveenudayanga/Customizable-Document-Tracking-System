package com.example.notification.service;

import org.springframework.stereotype.Service;

@Service
public class SmsSenderService {

    // This is a stub. Integrate Twilio / Nexmo / other in real deployments.
    public void sendSms(String phoneNumber, String message) {
        // For demo, just log or print — in production call provider's API
        System.out.println("[SMS] to=" + phoneNumber + " msg=" + message);
    }
}
