package com.visiontwin.backend.repository;

import com.visiontwin.backend.entity.Machine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface MachineRepository extends MongoRepository<Machine, UUID> {
}
