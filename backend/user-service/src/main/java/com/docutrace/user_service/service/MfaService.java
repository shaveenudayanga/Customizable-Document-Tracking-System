package com.docutrace.user_service.service;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Service;

@Service
public class MfaService {
    private final DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(HashingAlgorithm.SHA1), new SystemTimeProvider());

    public boolean verify(String secret, String code) {
        if (secret == null || secret.isBlank()) return false;
        verifier.setTimePeriod(30);
        verifier.setAllowedTimePeriodDiscrepancy(1);
        return verifier.isValidCode(secret, code);
    }
}
