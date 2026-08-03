package com.visiontwin.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "machines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Machine {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String name;

    private String manufacturer;

    private String model;

    private String thumbnailPath;
    private String manualPdfPath;
    private String userGuidePdfPath;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    @Builder.Default
    @ToString.Exclude
    private List<ReferenceImage> referenceImages = new ArrayList<>();
}
