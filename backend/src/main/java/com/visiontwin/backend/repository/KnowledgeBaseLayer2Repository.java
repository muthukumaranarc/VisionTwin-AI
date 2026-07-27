package com.visiontwin.backend.repository;

import com.visiontwin.backend.entity.KnowledgeBaseLayer2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeBaseLayer2Repository extends JpaRepository<KnowledgeBaseLayer2, UUID> {
    List<KnowledgeBaseLayer2> findByMachineId(UUID machineId);
    void deleteByMachineId(UUID machineId);
}
