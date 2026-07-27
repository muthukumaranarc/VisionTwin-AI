package com.visiontwin.backend.repository;

import com.visiontwin.backend.entity.DiagnosisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DiagnosisReportRepository extends JpaRepository<DiagnosisReport, UUID> {
    List<DiagnosisReport> findAllByOrderByTimestampDesc();
}
