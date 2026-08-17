package com.nslindia.procurezone.integration.sap.watcher;

import com.nslindia.procurezone.integration.sap.dto.MaterialImportResult;
import com.nslindia.procurezone.integration.sap.entity.StockImportHistory;
import com.nslindia.procurezone.integration.sap.repository.StockImportHistoryRepository;
import com.nslindia.procurezone.integration.sap.service.MaterialImportService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Event-driven SAP stock import.
 *
 * <p>Watches one or more local directories from {@code sap.csv.import.path} (comma-separated;
 * default {@code /home/issuenote/issue,/home/nsl/issuenote/issue}). A Material file that appears in
 * ANY watched directory is imported. Only files named exactly {@code Material(YYYY-MM-DD).CSV} are
 * imported — every other file in those directories (Plant_Indent…, Quality_Info…, PRD-*.CSV, …) is
 * ignored. Each file is processed exactly once, tracked by filename in {@code tbl_stock_import_history}
 * (dedup is by filename, so the same-named file in either directory is treated as one snapshot).
 * Replaces the old three cron triggers.
 *
 * <p>The import upserts the AUTHORITATIVE stock table {@code tbl_map_company_plant_material}
 * (see {@link MaterialImportService}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SapMaterialFileWatcher {

    /** Strict match — only Material(YYYY-MM-DD).CSV, nothing else. */
    private static final Pattern MATERIAL_FILE = Pattern.compile("^Material\\(\\d{4}-\\d{2}-\\d{2}\\)\\.CSV$");

    private static final long STABILITY_WAIT_MS = 3000L;   // gap between size samples
    private static final int STABILITY_MAX_RETRIES = 10;   // cap so a file being continuously written can't loop forever

    private final MaterialImportService materialImportService;
    private final StockImportHistoryRepository historyRepository;

    // Comma-separated list of directories to watch. A Material file appearing in ANY of them is
    // imported. Defaults to the primary path plus the secondary /home/nsl/issuenote/issue.
    @Value("${sap.csv.import.path:/home/issuenote/issue,/home/nsl/issuenote/issue}")
    private String[] importPaths;

    @Value("${sap.csv.watch.enabled:true}")
    private boolean watchEnabled;

    private volatile boolean running = false;
    private WatchService watchService;
    private Thread watchThread;
    // Maps each registered directory's WatchKey back to its Path, so an event resolves to the dir it
    // came from (needed now that multiple directories share one WatchService).
    private final Map<WatchKey, Path> watchedDirs = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!watchEnabled) {
            log.info("SAP material file watcher is disabled (sap.csv.watch.enabled=false)");
            return;
        }

        // Resolve every configured path that actually exists as a directory (skip any that don't).
        List<Path> dirs = new ArrayList<>();
        for (String raw : importPaths) {
            String trimmed = raw == null ? "" : raw.trim();
            if (trimmed.isEmpty()) continue;
            Path dir = Paths.get(trimmed);
            if (Files.isDirectory(dir)) {
                dirs.add(dir);
            } else {
                log.warn("SAP import path '{}' is not a directory — skipping it", trimmed);
            }
        }
        if (dirs.isEmpty()) {
            log.warn("No valid SAP import directories in {} — file watcher NOT started",
                    Arrays.toString(importPaths));
            return;
        }

        // 1) Catch anything dropped while the app was down (newest unprocessed across ALL dirs).
        try {
            scanOnStartup(dirs);
        } catch (Exception e) {
            log.error("SAP startup scan failed: {}", e.getMessage(), e);
        }

        // 2) Start the live watch — register every directory on one shared WatchService.
        try {
            watchService = FileSystems.getDefault().newWatchService();
            for (Path dir : dirs) {
                WatchKey key = dir.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
                watchedDirs.put(key, dir);
            }
            running = true;
            watchThread = new Thread(this::watchLoop, "sap-material-file-watcher");
            watchThread.setDaemon(true);
            watchThread.start();
            log.info("SAP material file watcher started on {} (matching Material(YYYY-MM-DD).CSV)", dirs);
        } catch (IOException e) {
            log.error("Failed to start SAP material file watcher on {}: {}",
                    Arrays.toString(importPaths), e.getMessage(), e);
        }
    }

    /**
     * Startup scan: import the NEWEST unprocessed Material file only. Each import is an ABSOLUTE
     * overwrite of stock, so the most recent file is the current truth and supersedes any older
     * unprocessed ones — importing only the latest is sufficient and correct, and avoids applying a
     * stale snapshot over a newer one.
     */
    private void scanOnStartup(List<Path> dirs) {
        // Collect every matching Material file across ALL watched dirs, then import only the newest
        // unprocessed one — each import is an ABSOLUTE overwrite of stock, so the most recent file
        // (by embedded date) is the current truth and supersedes older ones, wherever it landed.
        List<File> matches = new ArrayList<>();
        for (Path dir : dirs) {
            File[] found = dir.toFile().listFiles((d, name) -> MATERIAL_FILE.matcher(name).matches());
            if (found != null) {
                matches.addAll(Arrays.asList(found));
            }
        }
        if (matches.isEmpty()) {
            log.info("SAP startup scan: no Material(YYYY-MM-DD).CSV files present in {}", dirs);
            return;
        }
        // Newest by filename (the date is embedded and zero-padded, so lexical == chronological).
        Optional<File> newest = matches.stream().max(Comparator.comparing(File::getName));
        newest.ifPresent(f -> {
            if (alreadyProcessed(f.getName())) {
                log.info("SAP startup scan: newest file {} already processed — nothing to do", f.getName());
            } else {
                log.info("SAP startup scan: importing newest unprocessed file {}", f.getName());
                processFile(f);
            }
        });
    }

    private void watchLoop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (running) log.error("SAP watch service error: {}", e.getMessage(), e);
                break;
            }
            // Resolve which watched directory this key/event belongs to (each dir has its own key).
            Path dir = watchedDirs.get(key);
            if (dir == null) {
                key.cancel();
                continue;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                String fileName = String.valueOf(event.context());
                if (!MATERIAL_FILE.matcher(fileName).matches()) {
                    log.debug("Ignoring non-Material file event: {}", fileName);
                    continue;
                }
                File file = dir.resolve(fileName).toFile();
                if (file.exists()) {
                    processFile(file);
                }
            }
            if (!key.reset()) {
                // This directory is no longer watchable; drop it. Stop only when none remain.
                watchedDirs.remove(key);
                log.warn("SAP watch key for '{}' no longer valid — no longer watching it", dir);
                if (watchedDirs.isEmpty()) {
                    log.warn("No SAP watch directories remain — watcher stopping");
                    break;
                }
            }
        }
    }

    /**
     * Import one Material CSV file exactly once. Skips files already recorded SUCCESS; otherwise
     * waits for the file to stop growing, imports, and records the outcome (SUCCESS/FAILED) so a
     * FAILED file is retried on the next event/restart rather than being marked done.
     */
    private synchronized void processFile(File file) {
        String fileName = file.getName();
        try {
            if (alreadyProcessed(fileName)) {
                log.debug("SAP import: {} already processed (SUCCESS/PARTIAL) — skipping", fileName);
                return;
            }

            if (!waitUntilStable(file)) {
                log.warn("SAP import: {} still changing after {} retries — will retry on next event",
                        fileName, STABILITY_MAX_RETRIES);
                return; // do not record; a later event/restart will pick it up
            }

            MaterialImportResult result = materialImportService.importMaterialsFromFile(file.getAbsolutePath());
            int rows = result.getSuccessfulInserts() + result.getSuccessfulUpdates();
            int failures = result.getFailures();

            if (failures == 0) {
                recordResult(fileName, StockImportHistory.STATUS_SUCCESS, rows, truncate(result.getSummary()));
                log.info("SAP import SUCCESS: file={}, rowsUpdated={}, at={}", fileName, rows, LocalDateTime.now());
            } else if (rows > 0) {
                // Partial: some rows persisted, some failed (e.g. pre-existing duplicates). Counts as
                // processed (not blindly re-run) but the failures are recorded and visible.
                recordResult(fileName, StockImportHistory.STATUS_PARTIAL, rows,
                        truncate(rows + " updated, " + failures + " failed: " + result.getSummary()));
                log.warn("SAP import PARTIAL: file={}, rowsUpdated={}, rowsFailed={}", fileName, rows, failures);
            } else {
                recordResult(fileName, StockImportHistory.STATUS_FAILED, 0, truncate(result.getSummary()));
                log.error("SAP import FAILED: file={}, {}", fileName, result.getSummary());
            }
        } catch (Exception e) {
            log.error("SAP import FAILED: file={}, error={}", fileName, e.getMessage(), e);
            try {
                recordResult(fileName, StockImportHistory.STATUS_FAILED, 0, truncate("Exception: " + e.getMessage()));
            } catch (Exception ignored) {
                // history write is best-effort; never mask the original error
            }
        }
    }

    /** A file counts as processed (not to be re-run) if a prior SUCCESS or PARTIAL row exists. */
    private boolean alreadyProcessed(String fileName) {
        return historyRepository.findByFileName(fileName)
                .map(h -> StockImportHistory.STATUS_SUCCESS.equals(h.getStatus())
                        || StockImportHistory.STATUS_PARTIAL.equals(h.getStatus()))
                .orElse(false);
    }

    /** Fully-written check: size unchanged across a {@value #STABILITY_WAIT_MS} ms interval. */
    private boolean waitUntilStable(File file) {
        long lastSize = -1L;
        for (int i = 0; i < STABILITY_MAX_RETRIES; i++) {
            long size = file.length();
            if (size == lastSize && size > 0) {
                return true;
            }
            lastSize = size;
            try {
                Thread.sleep(STABILITY_WAIT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        // final compare after the last sleep
        return file.length() == lastSize && lastSize > 0;
    }

    /** Upsert by filename so a retried FAILED file updates its row (file_name is UNIQUE). */
    private void recordResult(String fileName, String status, int rowsUpdated, String message) {
        StockImportHistory row = historyRepository.findByFileName(fileName)
                .orElseGet(() -> StockImportHistory.builder().fileName(fileName).build());
        row.setImportedAt(LocalDateTime.now());
        row.setRowsUpdated(rowsUpdated);
        row.setStatus(status);
        row.setMessage(message);
        historyRepository.save(row);
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
                // shutting down
            }
        }
    }
}
