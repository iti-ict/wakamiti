/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jacoco;


import static es.iti.wakamiti.api.util.StringUtils.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;
import org.slf4j.Logger;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.WakamitiLogger;


/**
 * Reports Jacoco execution information.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "jacoco-reporter",
        version = "3.1",
        priority = Extension.NORMAL_PRIORITY + 1
)
public class JacocoReporter implements EventObserver {

    private static final Logger LOGGER = WakamitiLogger.forClass(JacocoReporter.class);

    private List<String> hosts;
    private int retries;
    private Path output;
    private Path xml;
    private Path csv;
    private Path html;
    private List<Path> classes;
    private List<Path> sources;
    private int tabwidth;
    private String name;
    private boolean merge;

    private ExecFileLoader fileLoader;
    private ExecDumpClient dumpClient;
    private final Set<String> initializedSegments = new LinkedHashSet<>();
    private final Set<File> executionFiles = new LinkedHashSet<>();
    private boolean aggregateInitialized;

    /**
     * @param hosts host and port endpoints of the JaCoCo TCP dump agents
     */
    public void setHosts(
            List<String> hosts
    ) {
        this.hosts = hosts;
    }

    /**
     * @param retries connection retries allowed when dumping execution data
     */
    public void setRetries(
            int retries
    ) {
        this.retries = retries;
    }

    /**
     * @param output directory receiving per-scenario {@code .exec} files and base path for the aggregate file
     */
    public void setOutput(
            Path output
    ) {
        this.output = output;
    }

    /**
     * @param xml directory receiving per-scenario XML reports and base path for the aggregate report
     */
    public void setXml(
            Path xml
    ) {
        this.xml = xml;
    }

    /**
     * @param csv directory receiving per-scenario CSV reports and base path for the aggregate report
     */
    public void setCsv(
            Path csv
    ) {
        this.csv = csv;
    }

    /**
     * Sets the path used to produce the final aggregate HTML report.
     *
     * @param html directory receiving the aggregate HTML report
     */
    public void setHtml(
            Path html
    ) {
        this.html = html;
    }

    /**
     * @param classes root directories searched recursively for analyzed {@code .class} files
     */
    public void setClasses(
            List<Path> classes
    ) {
        this.classes = classes;
    }

    /**
     * @param sources root directory searched for Java sources linked in reports
     */
    public void setSources(
            List<Path> sources
    ) {
        this.sources = sources;
    }

    /**
     * @param tabwidth tab width used to calculate source-report columns
     */
    public void setTabwidth(
            int tabwidth
    ) {
        this.tabwidth = tabwidth;
    }

    /**
     * @param name display name assigned to generated JaCoCo bundles
     */
    public void setName(
            String name
    ) {
        this.name = name;
    }

    /**
     * @param merge whether to merge all execution data into a single report
     */
    public void setMerge(
            boolean merge
    ) {
        this.merge = merge;
    }

    @Override
    public void eventReceived(
            Event event
    ) {
        if (event.data() != null) {
            PlanNodeSnapshot snapshot = (PlanNodeSnapshot) event.data();
            if (snapshot.getNodeType() == NodeType.TEST_CASE) {
                String segmentId = snapshot.getId();
                dump(segmentId, true);
                if (xml != null || csv != null) {
                    executeSingle(segmentId);
                }
            } else if (merge && isExecutedLifecycleHook(snapshot)) {
                dump(snapshot.getId(), false);
            }
        }

        if (Event.AFTER_WRITE_OUTPUT_FILES.equals(event.type())) {
            executeFinal();
        }
    }

    private boolean isExecutedLifecycleHook(
            PlanNodeSnapshot snapshot
    ) {
        return snapshot.getNodeType() == NodeType.LIFECYCLE_HOOK
                && snapshot.getResult() != null
                && snapshot.getResult() != Result.SKIPPED;
    }

    @Override
    public boolean acceptType(
            String eventType
    ) {
        return List.of(Event.NODE_RUN_FINISHED, Event.AFTER_WRITE_OUTPUT_FILES).contains(eventType);
    }

    private ExecDumpClient dumpClient() {
        if (dumpClient == null) {
            dumpClient = new ExecDumpClient() {

                @Override
                protected void onConnecting(
                        final InetAddress address,
                        final int port
                ) {
                    LOGGER.info("Connecting to {}:{}...", address, port);
                }

                @Override
                protected void onConnectionFailure(
                        final IOException exception
                ) {
                    LOGGER.warn(exception.getMessage());
                }
            };
        }
        return dumpClient;
    }

    private ExecFileLoader fileLoader() {
        return fileLoader == null ? new ExecFileLoader() : fileLoader;
    }

    private void dump(
            String id,
            boolean saveSegment
    ) {
        try {
            ensureDirectory(output);
        } catch (IOException e) {
            throw new WakamitiException("Cannot create jacoco execution-data directory '{}'", output, e);
        }

        final ExecDumpClient client = dumpClient();
        client.setReset(true);
        client.setRetryCount(retries);

        File file = saveSegment ? output.resolve(format("{}.exec", id)).toFile() : null;
        List<IOException> failures = new ArrayList<>();
        for (String host : hosts) {
            String[] parts = host.split(":", 2);
            try {
                final ExecFileLoader loader = client.dump(parts[0], Integer.parseInt(parts[1]));
                if (saveSegment) {
                    LOGGER.info("Writing execution data from {} to {}", host, file);
                    loader.save(file, initializedSegments.contains(id));
                    initializedSegments.add(id);
                    executionFiles.add(file);
                }
                if (merge) {
                    File aggregate = aggregateFile(output, ".exec");
                    LOGGER.info("Writing aggregate execution data to {}", aggregate);
                    loader.save(aggregate, aggregateInitialized);
                    aggregateInitialized = true;
                }
            } catch (IOException e) {
                failures.add(new IOException(format("Cannot dump execution data from '{}'", host), e));
            }
        }

        if (!failures.isEmpty()) {
            WakamitiException exception = new WakamitiException(
                    format("Cannot dump jacoco coverage data for '{}' from {} endpoint(s)", id, failures.size()),
                    failures.get(0)
            );
            failures.stream().skip(1).forEach(exception::addSuppressed);
            throw exception;
        }
    }

    private List<File> searchFiles(
            Path root,
            String extension
    ) {
        final List<File> foundFiles = new ArrayList<>();
        try (Stream<Path> walkStream = Files.walk(root)) {
            walkStream.filter(p -> p.toFile().isFile())
                    .filter(p -> p.getFileName().toString().endsWith(extension))
                    .forEach(f -> foundFiles.add(f.toFile()));
        } catch (IOException e) {
            throw new WakamitiException("Cannot search file with extension '{}'", extension, e);
        }
        return foundFiles;
    }

    private ISourceFileLocator getSourceLocator() {
        final MultiSourceFileLocator multi = new MultiSourceFileLocator(tabwidth);
        for (Path root : sources) {
            for (final File f : searchFiles(root, ".java")) {
                multi.add(new DirectorySourceFileLocator(f, Charset.defaultCharset().name(), tabwidth));
            }
        }
        return multi;
    }

    private IBundleCoverage analyze(
            String name,
            final ExecutionDataStore data
    ) throws IOException {
        final CoverageBuilder builder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(data, builder);
        for (Path root : classes) {
            for (final File f : searchFiles(root, ".class")) {
                analyzer.analyzeAll(f);
            }
        }
        printNoMatchWarning(builder.getNoMatchClasses());
        return builder.getBundle(name);
    }

    private void printNoMatchWarning(
            final Collection<IClassCoverage> nomatch
    ) {
        if (!nomatch.isEmpty()) {
            LOGGER.warn("Some classes do not match with execution data.");
            LOGGER.warn("For report generation the same class files must be used as at runtime.");
            for (final IClassCoverage c : nomatch) {
                LOGGER.warn("Execution data for class '{}' does not match.", c.getName());
            }
        }
    }

    private void executeSingle(
            String id
    ) {
        File exec = this.output.resolve(format("{}.exec", id)).toFile();
        if (!exec.exists()) {
            LOGGER.warn("No execution data file provided for coverage segment '{}'", id);
            return;
        }
        executeReport(
                List.of(exec),
                format("{} - {}", name, id),
                xml == null ? null : xml.resolve(format("{}.xml", id)),
                csv == null ? null : csv.resolve(format("{}.csv", id)),
                null
        );
    }

    private void executeFinal() {
        if (!merge && html == null) {
            return;
        }
        if (merge && xml == null && csv == null && html == null) {
            return;
        }

        Collection<File> files = merge
                ? List.of(aggregateFile(output, ".exec"))
                : executionFiles;
        if (files.isEmpty() || files.stream().noneMatch(File::exists)) {
            LOGGER.warn("No execution data files provided for aggregate coverage report");
            return;
        }

        executeReport(
                files,
                name,
                merge && xml != null ? aggregatePath(xml, ".xml") : null,
                merge && csv != null ? aggregatePath(csv, ".csv") : null,
                html
        );
    }

    private void executeReport(
            Collection<File> files,
            String bundleName,
            Path xmlOutput,
            Path csvOutput,
            Path htmlOutput
    ) {
        final ExecFileLoader loader = fileLoader();
        try {
            for (File file : files) {
                if (file.exists()) {
                    LOGGER.info("Loading execution data file {}", file.getAbsolutePath());
                    loader.load(file);
                }
            }
            IBundleCoverage bundle = analyze(bundleName, loader.getExecutionDataStore());
            LOGGER.info("Analyzing {} classes.", bundle.getClassCounter().getTotalCount());

            final IReportVisitor visitor = createReportVisitor(xmlOutput, csvOutput, htmlOutput);
            visitor.visitInfo(loader.getSessionInfoStore().getInfos(), loader.getExecutionDataStore().getContents());
            visitor.visitBundle(bundle, getSourceLocator());
            visitor.visitEnd();
        } catch (IOException e) {
            throw new WakamitiException("Cannot generate jacoco coverage report '{}'", bundleName, e);
        }
    }

    private IReportVisitor createReportVisitor(
            Path xmlOutput,
            Path csvOutput,
            Path htmlOutput
    ) throws IOException {
        final List<IReportVisitor> visitors = new ArrayList<>();
        if (xmlOutput != null) {
            ensureParentDirectory(xmlOutput);
            final XMLFormatter formatter = new XMLFormatter();
            visitors.add(formatter.createVisitor(new FileOutputStream(xmlOutput.toFile())));
        }
        if (csvOutput != null) {
            ensureParentDirectory(csvOutput);
            final CSVFormatter formatter = new CSVFormatter();
            visitors.add(formatter.createVisitor(new FileOutputStream(csvOutput.toFile())));
        }
        if (htmlOutput != null) {
            ensureDirectory(htmlOutput);
            visitors.add(new HTMLFormatter().createVisitor(new FileMultiReportOutput(htmlOutput.toFile())));
        }
        return new MultiReportVisitor(visitors);
    }

    private void ensureParentDirectory(
            Path path
    ) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            ensureDirectory(parent);
        }
    }

    private void ensureDirectory(
            Path directory
    ) throws IOException {
        Files.createDirectories(directory);
    }

    private File aggregateFile(
            Path base,
            String extension
    ) {
        return aggregatePath(base, extension).toFile();
    }

    private Path aggregatePath(
            Path base,
            String extension
    ) {
        return Path.of(base.toString() + extension);
    }

}
