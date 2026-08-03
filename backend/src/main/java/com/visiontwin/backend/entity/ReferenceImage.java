package com.visiontwin.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

@Document(collection = "reference_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceImage {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private UUID machineId;

    private String filename; // e.g. Main_Shaft.jpg

    private String partName; // e.g. Main Shaft

    private Float circleX; // Normalized X coordinate of the circle (0.0 to 1.0)
    private Float circleY; // Normalized Y coordinate of the circle (0.0 to 1.0)
    private Float circleRadius; // Normalized radius of the circle (0.0 to 1.0)

    private String filePath;
}
