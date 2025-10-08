package com.docutrace.api_gateway.security;

import com.docutrace.api_gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenValidator {

    private final JwtProperties properties;
    private final JwtParser jwtParser;

    public JwtTokenValidator(JwtProperties properties) {
        this.properties = properties;
        this.jwtParser = properties.isEnabled() ? buildParser(properties) : null;
    }

    public Claims validate(String token) {
        if (!properties.isEnabled()) {
            throw new JwtValidationException("JWT validation is disabled");
        }
        if (!StringUtils.hasText(token)) {
            throw new JwtValidationException("Token is blank");
        }
        try {
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtValidationException("Invalid or expired JWT", ex);
        }
    }

    private JwtParser buildParser(JwtProperties properties) {
        SecretKey key = properties.secretKey();
        JwtParserBuilder builder = Jwts.parser().verifyWith(key);

        if (properties.hasIssuer()) {
            builder.requireIssuer(properties.getIssuer());
        }
        if (properties.hasAudience()) {
            builder.requireAudience(properties.getAudience());
        }
        if (properties.getClockSkewSeconds() > 0) {
            builder.clockSkewSeconds(properties.getClockSkewSeconds());
        }

        return builder.build();
    }
}
