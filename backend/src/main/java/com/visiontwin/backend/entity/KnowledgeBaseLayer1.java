package com.visiontwin.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

@Document(collection = "knowledge_base_layer1")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseLayer1 {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private UUID machineId;

    private String contentJson;
}
