/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jacoco;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.*;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static es.iti.wakamiti.api.util.StringUtils.format;


@Extension(provider = "es.iti.wakamiti", name = "jacoco-reporter", version = "2.6", priority = 6)
public class JacocoReporter implements EventObserver {

    private static final Logger LOGGER = WakamitiLogger.forClass(JacocoReporter.class);

    private String host;
    private String port;
    private int retries;
    private Path output;
    private Path xml;
    private Path csv;
    private Path html;
    private Path classes;
    private Path sources;
    private int tabwidth;
    private String name;

    private ExecFileLoader fileLoader;
    private ExecDumpClient dumpClient;

    public void setHost(
            String host
    ) {
        this.host = host;
    }

    public void setPort(
            String port
    ) {
        this.port = port;
    }

    public void setRetries(
            int retries
    ) {
        this.retries = retries;
    }

    public void setOutput(
            Path output
    ) {
        this.output = output;
    }

    public void setXml(
            Path xml
    ) {
        this.xml = xml;
    }

    public void setCsv(
            Path csv
    ) {
        this.csv = csv;
    }

    public void setHtml(
            Path html
    ) {
        this.html = html;
    }

    public void setClasses(
            Path classes
    ) {
        this.classes = classes;
    }

    public void setSources(
            Path sources
    ) {
        this.sources = sources;
    }

    public void setTabwidth(
            int tabwidth
    ) {
        this.tabwidth = tabwidth;
    }

    public void setName(
            String name
    ) {
        this.name = name;
    }

    @Override
    public void eventReceived(
            Event event
    ) {
        if (event.data() != null) {
            PlanNodeSnapshot snapshot = (PlanNodeSnapshot) event.data();
            if (snapshot.getNodeType().isAnyOf(NodeType.TEST_CASE)) {
                dump(snapshot.getId());
                if (xml != null || csv != null) {
                    executeSingle(snapshot.getId());
                }
            }
        }

        if (Event.AFTER_WRITE_OUTPUT_FILES.equals(event.type())) {
            Optional.ofNullable(html).ifPresent(x -> executeFinal());
        }

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
        if (fileLoader == null) {
            fileLoader = new ExecFileLoader();
        }
        return fileLoader;
    }

    private void dump(
            String id
    ) {
        final ExecDumpClient client = dumpClient();
        client.setReset(true);
        client.setRetryCount(retries);

        try {
            final ExecFileLoader loader = client.dump(host, Integer.parseInt(port));
            File file = output.resolve(format("{}.exec", id)).toFile();
            LOGGER.info("Writing execution data to {}", file);
            loader.save(file, true);
        } catch (IOException e) {
            throw new WakamitiException("Cannot dump jacoco coverage of test case '{}'", id, e);
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
        for (final File f : searchFiles(sources, ".java")) {
            multi.add(new DirectorySourceFileLocator(f, Charset.defaultCharset().name(), tabwidth));
        }
        return multi;
    }

    private IBundleCoverage analyze(
            String name,
            final ExecutionDataStore data
    ) throws IOException {
        final CoverageBuilder builder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(data, builder);
        for (final File f : searchFiles(classes, ".class")) {
            analyzer.analyzeAll(f);
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

        final ExecFileLoader loader = fileLoader();
        if (!exec.exists()) {
            LOGGER.warn("No execution data file provided of test case '{}'", id);
        } else {
            LOGGER.info("Loading execution data file {}", exec.getAbsolutePath());
            try {
                loader.load(exec);
                IBundleCoverage bundle = analyze(format("{} - {}", name, id), loader.getExecutionDataStore());

                LOGGER.info("Analyzing {} classes.", bundle.getClassCounter().getTotalCount());
                final IReportVisitor visitor = createReportVisitor(id);
                visitor.visitInfo(loader.getSessionInfoStore().getInfos(), loader.getExecutionDataStore().getContents());
                visitor.visitBundle(bundle, getSourceLocator());
                visitor.visitEnd();
            } catch (IOException e) {
                throw new WakamitiException("Cannot process execution file '{}'", exec.getAbsolutePath(), e);
            }
        }
    }

    private void executeFinal() {
        final ExecFileLoader loader = fileLoader();
        try {
            loader.load(html.toFile());
            IBundleCoverage bundle = analyze(name, loader.getExecutionDataStore());
            LOGGER.info("Analyzing {} classes.", bundle.getClassCounter().getTotalCount());

            final IReportVisitor visitor = new MultiReportVisitor(List.of(
                    new HTMLFormatter().createVisitor(new FileMultiReportOutput(html.toFile()))));
            visitor.visitInfo(loader.getSessionInfoStore().getInfos(), loader.getExecutionDataStore().getContents());
            visitor.visitBundle(bundle, getSourceLocator());
            visitor.visitEnd();
        } catch (IOException e) {
            throw new WakamitiException("Cannot process execution file '{}'", html.toFile().getAbsolutePath(), e);
        }
    }

    private IReportVisitor createReportVisitor(String id) throws IOException {
        final List<IReportVisitor> visitors = new ArrayList<>();
        if (xml != null) {
            final XMLFormatter formatter = new XMLFormatter();
            visitors.add(formatter.createVisitor(new FileOutputStream(
                    xml.resolve(format("{}.xml", id)).toFile())));
        }
        if (csv != null) {
            final CSVFormatter formatter = new CSVFormatter();
            visitors.add(formatter.createVisitor(new FileOutputStream(
                    csv.resolve(format("{}.csv", id)).toFile())));
        }
        return new MultiReportVisitor(visitors);
    }

}
