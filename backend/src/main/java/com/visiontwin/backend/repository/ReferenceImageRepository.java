package com.visiontwin.backend.repository;

import com.visiontwin.backend.entity.ReferenceImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReferenceImageRepository extends MongoRepository<ReferenceImage, UUID> {
    List<ReferenceImage> findByMachineId(UUID machineId);
}
