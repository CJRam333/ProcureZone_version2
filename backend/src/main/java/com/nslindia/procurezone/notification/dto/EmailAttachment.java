package com.nslindia.procurezone.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for email attachments
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachment {

    private String filename;
    private byte[] content;
    private String contentType;

    /**
     * Create attachment from byte array
     */
    public static EmailAttachment of(String filename, byte[] content, String contentType) {
        return EmailAttachment.builder()
                .filename(filename)
                .content(content)
                .contentType(contentType)
                .build();
    }

    /**
     * Create Excel attachment
     */
    public static EmailAttachment excel(String filename, byte[] content) {
        return of(filename, content, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    /**
     * Create PDF attachment
     */
    public static EmailAttachment pdf(String filename, byte[] content) {
        return of(filename, content, "application/pdf");
    }

    /**
     * Create CSV attachment
     */
    public static EmailAttachment csv(String filename, byte[] content) {
        return of(filename, content, "text/csv");
    }
}
