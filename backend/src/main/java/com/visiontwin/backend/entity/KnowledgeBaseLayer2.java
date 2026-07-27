package com.visiontwin.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "knowledge_base_layer2")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseLayer2 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "machine_id", nullable = false)
    private UUID machineId;

    @Column(nullable = false, length = 10000)
    private String contentChunk;

    @Convert(converter = DoubleArrayConverter.class)
    @Column(nullable = false, length = 50000)
    private double[] embedding;

    @Column(nullable = false)
    private String source; // e.g. MANUAL, USER_GUIDE, REF_IMAGE
}
