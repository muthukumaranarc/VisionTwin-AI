package com.visiontwin.backend.config;

import com.visiontwin.backend.entity.ChatMessage;
import com.visiontwin.backend.entity.DiagnosisReport;
import com.visiontwin.backend.entity.Machine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import java.time.LocalDateTime;

/**
 * Replaces the JPA @PrePersist / @PreUpdate callbacks for MongoDB documents:
 * sets createdAt/updatedAt on machines and timestamp on reports/messages.
 */
@Configuration
@Slf4j
public class MongoAuditListener extends AbstractMongoEventListener<Object> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object source = event.getSource();
        LocalDateTime now = LocalDateTime.now();

        if (source instanceof Machine machine) {
            if (machine.getCreatedAt() == null) {
                machine.setCreatedAt(now);
            }
            machine.setUpdatedAt(now);
        } else if (source instanceof DiagnosisReport report) {
            if (report.getTimestamp() == null) {
                report.setTimestamp(now);
            }
        } else if (source instanceof ChatMessage message) {
            if (message.getTimestamp() == null) {
                message.setTimestamp(now);
            }
        }
    }
}
