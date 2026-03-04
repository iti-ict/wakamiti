/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.lucene;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Factory for Lucene index infrastructure (directory + analyzer).
 * <p>
 * The goal is to centralize creation details so {@code DatabaseSupport}
 * can focus on search orchestration.
 */
public final class LuceneIndexFactory {

    private LuceneIndexFactory() {
    }

    /**
     * Creates a {@link LuceneIndex} for a specific key.
     *
     * @param key logical key (connection, table and columns)
     * @param indexDir base directory; if blank, in-memory directory is used
     * @return newly created Lucene index wrapper
     * @throws IOException if filesystem directory cannot be created/opened
     */
    public static LuceneIndex createIndex(LuceneIndexKey key, String indexDir) throws IOException {
        Directory directory = createDirectory(key, indexDir);
        Analyzer analyzer = createAnalyzer();
        return new LuceneIndex(directory, analyzer);
    }

    /**
     * Creates target Lucene directory according to configuration.
     *
     * @param key index identity key
     * @param indexDir configured base directory
     * @return memory or filesystem Lucene directory
     * @throws IOException if filesystem directory cannot be created/opened
     */
    private static Directory createDirectory(LuceneIndexKey key, String indexDir) throws IOException {
        if (indexDir == null || indexDir.isBlank()) {
            return new ByteBuffersDirectory();
        }
        Path path = Paths.get(indexDir,
                sanitizePathSegment(key.alias()),
                sanitizePathSegment(key.table()),
                digestPathSegment(String.join("\u001F", key.columns())));
        Files.createDirectories(path);
        return FSDirectory.open(path);
    }

    /**
     * Creates analyzer used for both indexing and querying.
     *
     * @return standard Lucene analyzer
     */
    private static Analyzer createAnalyzer() {
        return new StandardAnalyzer();
    }

    /**
     * Sanitizes path segment to avoid invalid filename characters.
     *
     * @param value raw segment
     * @return filesystem-safe segment
     */
    private static String sanitizePathSegment(String value) {
        return value == null ? "null" : value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Generates deterministic directory suffix from ordered column names.
     * This avoids collisions and keeps path length reasonable.
     *
     * @param value concatenated column names
     * @return SHA-256 hex digest
     */
    private static String digestPathSegment(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
