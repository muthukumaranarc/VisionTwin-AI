package com.visiontwin.backend.repository;

import com.visiontwin.backend.entity.LearnMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LearnMessageRepository extends MongoRepository<LearnMessage, UUID> {
    List<LearnMessage> findByMachineIdAndSessionIdOrderByTimestampAsc(UUID machineId, String sessionId);
    void deleteByMachineIdAndSessionId(UUID machineId, String sessionId);
}
