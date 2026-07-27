package com.visiontwin.backend.service;

import com.visiontwin.backend.entity.Machine;
import com.visiontwin.backend.entity.ReferenceImage;
import com.visiontwin.backend.repository.MachineRepository;
import com.visiontwin.backend.repository.ReferenceImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MachineService {

    private final MachineRepository machineRepository;
    private final ReferenceImageRepository referenceImageRepository;
    private final StorageService storageService;

    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    public Machine getMachineById(UUID id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Machine not found: " + id));
    }

    @Transactional
    public Machine createMachine(String name, String manufacturer, String model,
                                 MultipartFile thumbnail, MultipartFile manualPdf,
                                 MultipartFile userGuidePdf) throws IOException {
        log.info("Creating machine: {} {} {}", name, manufacturer, model);
        
        String thumbnailPath = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbnailPath = storageService.storeFile(thumbnail, "thumbnails");
        }

        String manualPath = null;
        if (manualPdf != null && !manualPdf.isEmpty()) {
            manualPath = storageService.storeFile(manualPdf, "manuals");
        }

        String userGuidePath = null;
        if (userGuidePdf != null && !userGuidePdf.isEmpty()) {
            userGuidePath = storageService.storeFile(userGuidePdf, "userguides");
        }

        Machine machine = Machine.builder()
                .name(name)
                .manufacturer(manufacturer)
                .model(model)
                .thumbnailPath(thumbnailPath)
                .manualPdfPath(manualPath)
                .userGuidePdfPath(userGuidePath)
                .build();

        return machineRepository.save(machine);
    }

    @Transactional
    public ReferenceImage addReferenceImage(UUID machineId, MultipartFile file, String partName,
                                            float x, float y, float radius) throws IOException {
        log.info("Adding reference image for machine {} with part name: {}", machineId, partName);
        Machine machine = getMachineById(machineId);

        String filePath = storageService.storeFile(file, "refimages");
        String filename = file.getOriginalFilename();
        if (filename == null) {
            filename = partName.replaceAll("\\s+", "_") + ".jpg";
        }

        ReferenceImage refImage = ReferenceImage.builder()
                .machine(machine)
                .filename(filename)
                .partName(partName)
                .circleX(x)
                .circleY(y)
                .circleRadius(radius)
                .filePath(filePath)
                .build();

        return referenceImageRepository.save(refImage);
    }

    public List<ReferenceImage> getReferenceImages(UUID machineId) {
        return referenceImageRepository.findByMachineId(machineId);
    }
}
