package com.nslindia.procurezone.notification.controller;

import com.nslindia.procurezone.notification.model.EmailTemplate;
import com.nslindia.procurezone.notification.repository.EmailTemplateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateRepository emailTemplateRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'VIEWER', 'PROCUREMENT', 'PLANTMANAGER')")
    public ResponseEntity<Page<EmailTemplate>> getEmailTemplates(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(emailTemplateRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'VIEWER', 'PROCUREMENT', 'PLANTMANAGER')")
    public ResponseEntity<EmailTemplate> getEmailTemplateById(@PathVariable Integer id) {
        return emailTemplateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> updateEmailTemplate(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {

        return emailTemplateRepository.findById(id)
                .map(template -> {
                    if (updates.containsKey("subject")) {
                        template.setSubject((String) updates.get("subject"));
                    }
                    if (updates.containsKey("body")) {
                        template.setBody((String) updates.get("body"));
                    }
                    if (updates.containsKey("name")) {
                        template.setName((String) updates.get("name"));
                    }
                    if (updates.containsKey("description")) {
                        template.setDescription((String) updates.get("description"));
                    }
                    if (updates.containsKey("status")) {
                        Object statusVal = updates.get("status");
                        if (statusVal instanceof Number) {
                            template.setStatus(((Number) statusVal).intValue());
                        }
                    }

                    // Set modifier info
                    String modifier = (authentication != null) ? authentication.getName() : "system";
                    template.setLastModifiedBy(modifier);
                    template.setLastModifiedDate(LocalDateTime.now());

                    return ResponseEntity.ok(emailTemplateRepository.save(template));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

