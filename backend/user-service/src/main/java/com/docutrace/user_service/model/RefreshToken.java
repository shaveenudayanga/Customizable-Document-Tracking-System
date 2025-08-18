package com.docutrace.user_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @Column(name = "token_id", columnDefinition = "UUID")
    private UUID tokenId;

    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 200)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "replaced_by")
    private UUID replacedBy;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "ip")
    private String ip;

    @Column(name = "ua")
    private String ua;
}
