package com.marketplace.messagingservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "userId")
public class UserStats {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "registered_at")
    private Instant registeredAt;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified;

    @Column(name = "completed_deals_buyer", nullable = false)
    private int completedDealsBuyer;

    @Column(name = "completed_deals_seller", nullable = false)
    private int completedDealsSeller;

    @Column(name = "successful_interactions", nullable = false)
    private int successfulInteractions;

    @Column(name = "reports_received", nullable = false)
    private int reportsReceived;

    @Column(name = "total_interactions", nullable = false)
    private int totalInteractions;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
