package com.visiontwin.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "reference_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Machine machine;

    @Column(nullable = false)
    private String filename; // e.g. Main_Shaft.jpg

    @Column(nullable = false)
    private String partName; // e.g. Main Shaft

    private Float circleX; // Normalized X coordinate of the circle (0.0 to 1.0)
    private Float circleY; // Normalized Y coordinate of the circle (0.0 to 1.0)
    private Float circleRadius; // Normalized radius of the circle (0.0 to 1.0)

    @Column(nullable = false)
    private String filePath;
}
