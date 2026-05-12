package com.nslindia.procurezone.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO for email sending requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String body;
    private String templateCode;
    private Map<String, Object> templateVariables;
    private List<EmailAttachment> attachments;
    private boolean isHtml;

    @Builder.Default
    private boolean logEmail = true;

    /**
     * Add a TO recipient
     */
    public void addToRecipient(String email) {
        if (to == null) {
            to = new ArrayList<>();
        }
        to.add(email);
    }

    /**
     * Add a CC recipient
     */
    public void addCcRecipient(String email) {
        if (cc == null) {
            cc = new ArrayList<>();
        }
        cc.add(email);
    }

    /**
     * Add an attachment
     */
    public void addAttachment(EmailAttachment attachment) {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        attachments.add(attachment);
    }
}
