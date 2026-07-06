package com.nslindia.procurezone.common.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Persists a {@link LocalDateTime} as a 19-character "yyyy-MM-dd HH:mm:ss" string.
 *
 * <p>Several legacy Issue Note audit columns (issue_note_lmd, issue_note_storesby_date,
 * issue_note_approvedby_date, issue_note_rm_approvedby_date, issue_note_details_lmd) are
 * {@code varchar(20)} in the production schema. Binding a raw {@code LocalDateTime} sends a
 * value with fractional seconds (e.g. "2026-07-04 07:25:17.738893", 26 chars), which MySQL
 * rejects with "Data truncation: Data too long". A fixed 19-char format fits varchar(20) and is
 * also accepted verbatim by a real {@code datetime} column, so this converter is safe on either.
 *
 * <p>NOT auto-applied — annotate individual fields with {@code @Convert}. It must not touch
 * columns used in JPQL range/sort (e.g. issue_note_date), which stay native.
 *
 * <p>Read is tolerant of the several formats legacy rows may hold.
 */
@Converter
public class VarcharDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute == null ? null : attribute.format(FMT);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            return null;
        }
        String s = dbValue.trim();
        // Canonical "yyyy-MM-dd HH:mm:ss"
        try {
            return LocalDateTime.parse(s, FMT);
        } catch (Exception ignored) {
            // fall through
        }
        // Strip fractional seconds if the legacy value carried them
        try {
            int dot = s.indexOf('.');
            String noFrac = dot > 0 ? s.substring(0, dot) : s;
            return LocalDateTime.parse(noFrac.replace('T', ' '), FMT);
        } catch (Exception ignored) {
            // fall through
        }
        // ISO 8601 with 'T'
        try {
            return LocalDateTime.parse(s);
        } catch (Exception ignored) {
            // fall through
        }
        // Date-only "yyyy-MM-dd" → midnight
        try {
            return LocalDate.parse(s.length() >= 10 ? s.substring(0, 10) : s).atStartOfDay();
        } catch (Exception ignored) {
            return null;
        }
    }
}
