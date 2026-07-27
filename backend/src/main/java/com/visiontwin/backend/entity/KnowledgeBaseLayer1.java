package com.visiontwin.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "knowledge_base_layer1")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseLayer1 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "machine_id", nullable = false, unique = true)
    private UUID machineId;

    @Lob
    @Column(nullable = false, length = 1000000)
    private String contentJson;
}
