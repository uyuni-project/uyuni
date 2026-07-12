/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.internal.doclet.AsciidocDoclet;
import com.redhat.rhn.internal.doclet.AsciidocWriter;
import com.redhat.rhn.internal.doclet.DocBookDoclet;
import com.redhat.rhn.internal.doclet.DocBookWriter;

import com.suse.manager.api.OpenApiConfig;
import com.suse.manager.api.docs.OpenApiToAsciidocParser;
import com.suse.manager.api.docs.OpenApiToDocBookParser;
import com.suse.manager.api.docs.UyuniSwaggerReader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DocumentationTool;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Verifies that the OpenAPI generated API documentation keeps the same functional
 * API documentation surface as the legacy doclet for all migrated handlers.
 */
public class ApiDocumentationCompatibilityTest {

    private static final Pattern ANCHOR = Pattern.compile("^\\[#([^]]+)]\\s*$");
    private static final Pattern DOCBOOK_SECTION =
            Pattern.compile("(?s)<sect1>.*?</sect1>|<section[^>]*xml:id=\"[^\"]+\".*?</section>");
    private static final Pattern DOCBOOK_METHOD =
            Pattern.compile("(?s)<title>(?:<function>)?(?:Method: )?([^<]+)(?:</function>)?</title>");
    private static final Pattern DOCBOOK_HTTP =
            Pattern.compile("(?s)HTTP\\s+(?:<function>|<literal>)([^<]+)(?:</function>|</literal>)");
    private static final Pattern DOCBOOK_PARAMETERS =
            Pattern.compile("(?s)<term>Parameters</term>.*?<itemizedlist[^>]*>(.*?)</itemizedlist>");
    private static final Pattern DOCBOOK_RETURNS =
            Pattern.compile("(?s)<term>Return Value</term>.*?<itemizedlist[^>]*>(.*?)</itemizedlist>");
    private static final Pattern DOCBOOK_LIST_ITEM =
            Pattern.compile("(?s)<listitem>\\s*<para>(.*?)</para>");
    private static final Pattern METHOD_TITLE = Pattern.compile("^== Method:\\s+(.+?)\\s*$");
    private static final Pattern HTTP_METHOD = Pattern.compile("^HTTP\\s+`([^`]+)`\\s*$");
    private static final Pattern LIST_ITEM =
            Pattern.compile("^\\s*(\\*+)\\s+(?:\\*\\s+)?\\[\\.([^]]+)]#[^#]+#\\s*(.*)$");

    @Test
    public void openApiAsciidocIsCompatibleWithLegacyDoclet() throws Exception {
        Map<String, String> legacyDocs = generateLegacyAsciidoc();
        Map<String, String> openApiDocs = generateOpenApiAsciidoc();

        List<String> differences = new ArrayList<>();
        OpenApiConfig.getHandlerClasses().keySet().forEach(namespace -> {
            String legacy = legacyDocs.get(namespace + ".adoc");
            String openApi = openApiDocs.get(namespace);
            if (legacy == null) {
                differences.add("Missing legacy doclet output for namespace: " + namespace);
                return;
            }
            if (openApi == null) {
                differences.add("Missing OpenAPI AsciiDoc output for namespace: " + namespace);
                return;
            }
            differences.addAll(compare(namespace, parse(legacy), parse(openApi)));
        });

        if (!differences.isEmpty()) {
            Assertions.fail("Generated OpenAPI AsciiDoc is not functionally compatible with the legacy doclet:\n" +
                    String.join("\n", differences));
        }
    }

    @Test
    public void openApiDocBookIsCompatibleWithLegacyDoclet() throws Exception {
        Map<String, String> legacyDocs = generateLegacyDocBook();
        Map<String, String> openApiDocs = generateOpenApiDocBook();

        List<String> differences = new ArrayList<>();
        OpenApiConfig.getHandlerClasses().forEach((namespace, handlerClass) -> {
            String legacy = legacyDocs.get(handlerClass.getSimpleName() + ".xml");
            String openApi = openApiDocs.get(namespace + ".xml");
            if (legacy == null) {
                differences.add("Missing legacy DocBook output for namespace: " + namespace);
                return;
            }
            if (openApi == null) {
                differences.add("Missing OpenAPI DocBook output for namespace: " + namespace);
                return;
            }
            differences.addAll(compare(namespace, parseDocBook(legacy), parseDocBook(openApi)));
        });

        if (!differences.isEmpty()) {
            Assertions.fail("Generated OpenAPI DocBook is not functionally compatible with the legacy doclet:\n" +
                    String.join("\n", differences));
        }
    }

    private Map<String, String> generateOpenApiAsciidoc() {
        UyuniSwaggerReader reader = new UyuniSwaggerReader();
        OpenApiConfig.getHandlerClasses().forEach((namespace, handlerClass) -> reader.read(handlerClass, namespace));
        OpenAPI spec = reader.getSpec();
        return new OpenApiToAsciidocParser(spec).generateDocumentation();
    }

    private Map<String, String> generateOpenApiDocBook() {
        UyuniSwaggerReader reader = new UyuniSwaggerReader();
        OpenApiConfig.getHandlerClasses().forEach((namespace, handlerClass) -> reader.read(handlerClass, namespace));
        OpenAPI spec = reader.getSpec();
        return new OpenApiToDocBookParser(spec).generateDocumentation();
    }

    private Map<String, String> generateLegacyAsciidoc() throws IOException {
        DocumentationTool documentationTool = ToolProvider.getSystemDocumentationTool();
        Assertions.assertNotNull(documentationTool, "Javadoc tool is not available. Run tests with a JDK, not a JRE.");

        CapturingAsciidocDoclet.clear();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        try (StandardJavaFileManager fileManager = documentationTool.getStandardFileManager(
                diagnostics, Locale.ROOT, null)) {
            Iterable<? extends JavaFileObject> sources = fileManager.getJavaFileObjectsFromFiles(
                    sourceFiles().stream().map(Path::toFile).toList()
            );
            DocumentationTool.DocumentationTask task = documentationTool.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    CapturingAsciidocDoclet.class,
                    javadocOptions(),
                    sources
            );

            Boolean success = task.call();
            if (!Boolean.TRUE.equals(success)) {
                Assertions.fail("Legacy AsciiDoc doclet failed:\n" + formatDiagnostics(diagnostics));
            }
        }

        return CapturingAsciidocDoclet.generatedFiles();
    }

    private Map<String, String> generateLegacyDocBook() throws IOException {
        DocumentationTool documentationTool = ToolProvider.getSystemDocumentationTool();
        Assertions.assertNotNull(documentationTool, "Javadoc tool is not available. Run tests with a JDK, not a JRE.");

        CapturingDocBookDoclet.clear();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        try (StandardJavaFileManager fileManager = documentationTool.getStandardFileManager(
                diagnostics, Locale.ROOT, null)) {
            Iterable<? extends JavaFileObject> sources = fileManager.getJavaFileObjectsFromFiles(
                    sourceFiles().stream().map(Path::toFile).toList()
            );
            DocumentationTool.DocumentationTask task = documentationTool.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    CapturingDocBookDoclet.class,
                    javadocOptions("docbook"),
                    sources
            );

            Boolean success = task.call();
            if (!Boolean.TRUE.equals(success)) {
                Assertions.fail("Legacy DocBook doclet failed:\n" + formatDiagnostics(diagnostics));
            }
        }

        return CapturingDocBookDoclet.generatedFiles();
    }

    private List<String> javadocOptions() {
        return javadocOptions("asciidoc");
    }

    private List<String> javadocOptions(String template) {
        Path root = projectRoot();
        return List.of(
                "-classpath", System.getProperty("java.class.path"),
                "-sourcepath", root.resolve("core/src/main/java").toString(),
                "-quiet",
                "-d", "memory",
                "-templates", root.resolve("webapp/src/apidoc/" + template).toString(),
                "-product", "Uyuni",
                "-apiversion", "test"
        );
    }

    private List<Path> sourceFiles() throws IOException {
        Path sourceRoot = projectRoot().resolve("core/src/main/java");
        List<Path> files = new ArrayList<>();

        OpenApiConfig.getHandlerClasses().values().forEach(handlerClass ->
                files.add(sourceRoot.resolve(handlerClass.getName().replace('.', '/') + ".java"))
        );

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith("Serializer.java"))
                    .forEach(files::add);
        }

        return files.stream()
                .distinct()
                .sorted()
                .toList();
    }

    private Path projectRoot() {
        Path current = Path.of("").toAbsolutePath();
        if (Files.isDirectory(current.resolve("core/src/main/java"))) {
            return current;
        }
        if (Files.isDirectory(current.resolve("java/core/src/main/java"))) {
            return current.resolve("java");
        }
        throw new IllegalStateException("Unable to locate java project root from " + current);
    }

    private String formatDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
        return diagnostics.getDiagnostics().stream()
                .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
                .map(Diagnostic::toString)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("No diagnostics reported.");
    }

    private List<String> compare(String namespace, Map<MethodKey, ApiMethodDoc> expected,
                                 Map<MethodKey, ApiMethodDoc> actual) {
        List<String> differences = new ArrayList<>();

        expected.keySet().stream()
                .filter(key -> !actual.containsKey(key))
                .sorted(Comparator.comparing(MethodKey::toString))
                .forEach(key -> differences.add("[%s] Missing method: %s".formatted(namespace, key)));

        actual.keySet().stream()
                .filter(key -> !expected.containsKey(key))
                .sorted(Comparator.comparing(MethodKey::toString))
                .forEach(key -> differences.add("[%s] Unexpected method: %s".formatted(namespace, key)));

        expected.entrySet().stream()
                .filter(entry -> actual.containsKey(entry.getKey()))
                .filter(entry -> !entry.getValue().returns().equals(actual.get(entry.getKey()).returns()))
                .forEach(entry -> differences.add(
                        "[%s] Return mismatch for %s: expected %s, actual %s".formatted(
                                namespace,
                                entry.getKey(),
                                entry.getValue().returns(),
                                actual.get(entry.getKey()).returns()
                        )
                ));

        return differences;
    }

    private Map<MethodKey, ApiMethodDoc> parse(String content) {
        Map<MethodKey, ApiMethodDoc> methods = new LinkedHashMap<>();
        List<String> block = new ArrayList<>();

        for (String line : content.split("\\R")) {
            if (ANCHOR.matcher(line).matches()) {
                parseMethod(block).ifPresent(method -> methods.put(method.key(), method));
                block.clear();
            }
            block.add(line);
        }
        parseMethod(block).ifPresent(method -> methods.put(method.key(), method));

        return methods;
    }

    private Map<MethodKey, ApiMethodDoc> parseDocBook(String content) {
        Map<MethodKey, ApiMethodDoc> methods = new LinkedHashMap<>();
        Matcher sectionMatcher = DOCBOOK_SECTION.matcher(content);
        while (sectionMatcher.find()) {
            parseDocBookMethod(sectionMatcher.group()).ifPresent(method -> methods.put(method.key(), method));
        }
        return methods;
    }

    private Optional<ApiMethodDoc> parseDocBookMethod(String section) {
        Matcher methodMatcher = DOCBOOK_METHOD.matcher(section);
        if (!methodMatcher.find()) {
            return Optional.empty();
        }

        Matcher httpMatcher = DOCBOOK_HTTP.matcher(section);
        String httpMethod = httpMatcher.find() ? normalize(httpMatcher.group(1)) : "";

        return Optional.of(new ApiMethodDoc(
                normalize(stripXml(methodMatcher.group(1)).replaceFirst("^Method:\\s*", "")),
                httpMethod,
                parseDocBookItems(section, DOCBOOK_PARAMETERS),
                parseDocBookItems(section, DOCBOOK_RETURNS)
        ));
    }

    private List<DocItem> parseDocBookItems(String section, Pattern sectionPattern) {
        Matcher sectionMatcher = sectionPattern.matcher(section);
        if (!sectionMatcher.find()) {
            return List.of();
        }

        List<DocItem> items = new ArrayList<>();
        Matcher itemMatcher = DOCBOOK_LIST_ITEM.matcher(sectionMatcher.group(1));
        while (itemMatcher.find()) {
            parseDocBookItem(itemMatcher.group(1)).ifPresent(items::add);
        }
        return items;
    }

    private Optional<DocItem> parseDocBookItem(String item) {
        String text = normalize(stripXml(item));
        if (text.isEmpty() || "None".equals(text)) {
            return Optional.empty();
        }

        String normalized = text.replaceFirst("^array\\(([^)]+)\\)", "array($1)");
        String type;
        String name;
        int level = 1;

        if (normalized.startsWith("struct ")) {
            type = "struct";
            name = normalized.substring("struct ".length());
        }
        else if (normalized.startsWith("array(")) {
            int end = normalized.indexOf(')');
            type = "array";
            name = normalized.substring(end + 1).trim();
        }
        else if (normalized.startsWith("array")) {
            type = "array";
            name = normalized.substring("array".length()).trim();
        }
        else {
            int firstSpace = normalized.indexOf(' ');
            if (firstSpace < 0) {
                return Optional.of(new DocItem(level, normalized, ""));
            }
            type = normalized.substring(0, firstSpace);
            name = normalized.substring(firstSpace + 1);
        }

        if (name.startsWith("\"")) {
            level = 2;
        }
        return Optional.of(new DocItem(level, normalize(type), normalizeLabel(name)));
    }

    private String stripXml(String value) {
        return value
                .replaceAll("<[^>]+>", "")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&");
    }

    private Optional<ApiMethodDoc> parseMethod(List<String> lines) {
        String method = "";
        String httpMethod = "";
        List<DocItem> parameters = new ArrayList<>();
        List<DocItem> returns = new ArrayList<>();
        Section section = Section.NONE;

        for (String line : lines) {
            Matcher methodMatcher = METHOD_TITLE.matcher(line);
            if (methodMatcher.matches()) {
                method = methodMatcher.group(1).trim();
                continue;
            }

            Matcher httpMatcher = HTTP_METHOD.matcher(line);
            if (httpMatcher.matches()) {
                httpMethod = httpMatcher.group(1).trim();
                continue;
            }

            String trimmed = line.trim();
            if ("Parameters:".equals(trimmed)) {
                section = Section.PARAMETERS;
                continue;
            }
            if ("Returns:".equals(trimmed)) {
                section = Section.RETURNS;
                continue;
            }

            Matcher itemMatcher = LIST_ITEM.matcher(line);
            if (itemMatcher.matches() && section != Section.NONE) {
                DocItem item = new DocItem(
                        itemMatcher.group(1).length(),
                        normalize(itemMatcher.group(2)),
                        normalizeLabel(itemMatcher.group(3))
                );
                if (section == Section.PARAMETERS) {
                    parameters.add(item);
                }
                else {
                    returns.add(item);
                }
            }
        }

        if (method.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ApiMethodDoc(method, httpMethod, parameters, returns));
    }

    private String normalizeLabel(String value) {
        String normalized = normalize(value).replaceFirst("^[-:]\\s*", "");
        int descriptionStart = normalized.indexOf(" - ");
        if (descriptionStart >= 0) {
            normalized = normalized.substring(0, descriptionStart);
        }
        return normalize(normalized.replaceAll("^\"|\"$", ""));
    }

    private String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private enum Section {
        NONE,
        PARAMETERS,
        RETURNS
    }

    private record ApiMethodDoc(String method, String httpMethod, List<DocItem> parameters, List<DocItem> returns) {
        MethodKey key() {
            return new MethodKey(method, httpMethod, parameters);
        }
    }

    private record MethodKey(String method, String httpMethod, List<DocItem> parameters) { }

    private record DocItem(int level, String type, String name) { }

    /**
     * Test doclet that uses the real legacy AsciiDoc doclet implementation but captures
     * file contents in memory.
     */
    public static class CapturingAsciidocDoclet extends AsciidocDoclet {

        private static final Map<String, String> GENERATED_FILES = new LinkedHashMap<>();

        static void clear() {
            GENERATED_FILES.clear();
        }

        static Map<String, String> generatedFiles() {
            return Map.copyOf(GENERATED_FILES);
        }

        @Override
        public AsciidocWriter getWriter(String outputIn, String templateIn, String productNameIn,
                                        String apiVersionIn, boolean debugIn) {
            return new CapturingAsciidocWriter(outputIn, templateIn, productNameIn, apiVersionIn, debugIn);
        }
    }

    private static class CapturingAsciidocWriter extends AsciidocWriter {

        CapturingAsciidocWriter(String outputIn, String templatesIn, String productIn,
                                String apiVersionIn, boolean debugIn) {
            super(outputIn, templatesIn, productIn, apiVersionIn, debugIn);
        }

        @Override
        protected void writeFile(String filePath, String contents) {
            CapturingAsciidocDoclet.GENERATED_FILES.put(Path.of(filePath).getFileName().toString(), contents);
        }
    }

    /**
     * Test doclet that uses the real legacy DocBook doclet implementation but captures
     * file contents in memory.
     */
    public static class CapturingDocBookDoclet extends DocBookDoclet {

        private static final Map<String, String> GENERATED_FILES = new LinkedHashMap<>();

        static void clear() {
            GENERATED_FILES.clear();
        }

        static Map<String, String> generatedFiles() {
            return Map.copyOf(GENERATED_FILES);
        }

        @Override
        public DocBookWriter getWriter(String outputIn, String templateIn, String productNameIn,
                                       String apiVersionIn, boolean debugIn) {
            return new CapturingDocBookWriter(outputIn, templateIn, productNameIn, apiVersionIn, debugIn);
        }
    }

    private static class CapturingDocBookWriter extends DocBookWriter {

        CapturingDocBookWriter(String outputIn, String templatesIn, String productIn,
                               String apiVersionIn, boolean debugIn) {
            super(outputIn, templatesIn, productIn, apiVersionIn, debugIn);
        }

        @Override
        protected void writeFile(String filePath, String contents) {
            CapturingDocBookDoclet.GENERATED_FILES.put(Path.of(filePath).getFileName().toString(), contents);
        }
    }
}
