package com.docutrace.user_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "magic_token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MagicToken {
    @Id
    @Column(name = "token_id", columnDefinition = "UUID")
    private UUID tokenId;

    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "purpose", nullable = false, length = 40)
    private String purpose; // email_verify | password_reset | mfa_bind

    @Column(name = "token_hash", nullable = false, length = 200)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
