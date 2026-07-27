package com.visiontwin.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "diagnosis_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID machineId;

    @Column(nullable = false)
    private String machineName;

    @Column(nullable = false)
    private String problemDescription;

    @Column(nullable = false)
    private String uploadedImagePath;

    @Column(nullable = false)
    private String diagnosisProblem;

    @Column(nullable = false, length = 2000)
    private String diagnosisSolution;

    private Float highlightX;
    private Float highlightY;
    private Float highlightRadius;

    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<ChatMessage> chatHistory = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
