package com.visiontwin.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

@Document(collection = "knowledge_base_layer2")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseLayer2 {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private UUID machineId;

    private String contentChunk;

    private double[] embedding;

    private String source; // e.g. MANUAL, USER_GUIDE, REF_IMAGE
}
