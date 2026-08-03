package com.visiontwin.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "learn_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnMessage {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private UUID machineId;
    private String sessionId;

    private String sender; // USER or AI

    private String messageText;

    private LocalDateTime timestamp;
}
