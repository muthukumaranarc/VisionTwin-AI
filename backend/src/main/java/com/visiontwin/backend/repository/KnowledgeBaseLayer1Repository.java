package com.visiontwin.backend.repository;

import com.visiontwin.backend.entity.KnowledgeBaseLayer1;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnowledgeBaseLayer1Repository extends MongoRepository<KnowledgeBaseLayer1, UUID> {
    Optional<KnowledgeBaseLayer1> findByMachineId(UUID machineId);
    void deleteByMachineId(UUID machineId);
}
