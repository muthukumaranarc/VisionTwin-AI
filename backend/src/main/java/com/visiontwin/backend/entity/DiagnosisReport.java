package com.visiontwin.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "diagnosis_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisReport {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private UUID machineId;

    private String machineName;

    private String problemDescription;

    private String uploadedImagePath;

    private String diagnosisProblem;

    private String diagnosisSolution;

    private Float highlightX;
    private Float highlightY;
    private Float highlightRadius;

    private LocalDateTime timestamp;

    @Transient
    @Builder.Default
    @ToString.Exclude
    private List<ChatMessage> chatHistory = new ArrayList<>();
}
