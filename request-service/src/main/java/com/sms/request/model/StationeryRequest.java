package com.sms.request.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a stationery request in request_db.stationery_requests.
 */
@Entity
@Table(name = "stationery_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationeryRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "student_email", length = 255)
    private String studentEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "admin_comment", columnDefinition = "TEXT")
    private String adminComment;

    @Column(name = "request_date", updatable = false)
    @CreationTimestamp
    private LocalDateTime requestDate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequestItem> items = new ArrayList<>();

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED, FULFILLED
    }
}
