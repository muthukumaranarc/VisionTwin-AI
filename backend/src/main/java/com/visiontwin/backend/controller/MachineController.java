package com.visiontwin.backend.controller;

import com.visiontwin.backend.entity.Machine;
import com.visiontwin.backend.entity.ReferenceImage;
import com.visiontwin.backend.service.MachineService;
import com.visiontwin.backend.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
@Slf4j
public class MachineController {

    private final MachineService machineService;
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<List<Machine>> getAllMachines() {
        return ResponseEntity.ok(machineService.getAllMachines());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Machine> getMachineById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(machineService.getMachineById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Machine> createMachine(
            @RequestParam("name") String name,
            @RequestParam("manufacturer") String manufacturer,
            @RequestParam("model") String model,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "manual", required = false) MultipartFile manual,
            @RequestParam(value = "userGuide", required = false) MultipartFile userGuide) {
        try {
            Machine machine = machineService.createMachine(name, manufacturer, model, thumbnail, manual, userGuide);
            return ResponseEntity.ok(machine);
        } catch (IOException e) {
            log.error("Failed to create machine", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/{id}/ref-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReferenceImage> addReferenceImage(
            @PathVariable UUID id,
            @RequestParam("partName") String partName,
            @RequestParam("circleX") float circleX,
            @RequestParam("circleY") float circleY,
            @RequestParam("circleRadius") float circleRadius,
            @RequestParam("image") MultipartFile image) {
        try {
            ReferenceImage refImage = machineService.addReferenceImage(id, image, partName, circleX, circleY, circleRadius);
            return ResponseEntity.ok(refImage);
        } catch (Exception e) {
            log.error("Failed to add reference image", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/ref-images")
    public ResponseEntity<List<ReferenceImage>> getReferenceImages(@PathVariable UUID id) {
        return ResponseEntity.ok(machineService.getReferenceImages(id));
    }

    /**
     * File server endpoint to read uploaded media and documents.
     */
    @GetMapping("/files/{folderType}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String folderType,
            @PathVariable String filename) {
        try {
            Path file = storageService.loadFile(filename, folderType);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Failed to serve file: " + filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
