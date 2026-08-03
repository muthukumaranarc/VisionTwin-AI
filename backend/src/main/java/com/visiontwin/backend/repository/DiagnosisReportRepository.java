package com.visiontwin.backend.repository;

import com.visiontwin.backend.entity.DiagnosisReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DiagnosisReportRepository extends MongoRepository<DiagnosisReport, UUID> {
    List<DiagnosisReport> findAllByOrderByTimestampDesc();
}
