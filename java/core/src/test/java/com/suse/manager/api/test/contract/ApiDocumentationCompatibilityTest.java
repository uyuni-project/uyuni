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
    private static final Pattern DOCBOOK_PARAMETERS = Pattern.compile("<term>Parameters</term>");
    private static final Pattern DOCBOOK_RETURNS = Pattern.compile("<term>Return Value</term>");
    private static final Pattern DOCBOOK_LIST_OPEN = Pattern.compile("<itemizedlist[^>]*>");
    private static final Pattern DOCBOOK_LIST_TAG = Pattern.compile("<(/)?itemizedlist[^>]*>");
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
                .filter(entry -> !alignArrayElementNesting(entry.getValue().returns())
                        .equals(alignArrayElementNesting(actual.get(entry.getKey()).returns())))
                .forEach(entry -> differences.add(
                        "[%s] Return mismatch for %s: expected %s, actual %s".formatted(
                                namespace,
                                entry.getKey(),
                                alignArrayElementNesting(entry.getValue().returns()),
                                alignArrayElementNesting(actual.get(entry.getKey()).returns())
                        )
                ));

        return differences;
    }

    /**
     * Aligns the nesting of the struct that describes the element type of an array return value.
     *
     * The legacy doclet documents it in two interchangeable ways depending on the namespace: as a
     * sibling of the array marker, or indented one level below it. Both describe the same return
     * value, so the depth difference is an artifact of how each namespace was documented rather
     * than a difference in the documented shape. The indented form is rebased on the sibling form
     * before comparing, which keeps the depth of every other item, and the nesting relative to the
     * element struct, part of the comparison.
     *
     * @param items the parsed return value items
     * @return the items with the element struct and its contents rebased, when indented
     */
    private List<DocItem> alignArrayElementNesting(List<DocItem> items) {
        return alignSerializerStructs(alignNestedElementStructs(alignLeadingArrayElement(items)));
    }

    /**
     * Aligns the nesting of a struct that the doclet expanded from a serializer reference.
     *
     * The doclet renders the bullet level from a template variable that it resets whenever it
     * inlines a {@code $Serializer} reference, so a struct reached through such a reference is
     * always emitted at the top level, however deeply it is nested in the documented value. A
     * struct spelled out inline with {@code #struct_begin} on the very same position is emitted
     * one level below its parent instead. Both spell the same shape, and the OpenAPI schema is a
     * reference either way, so the depth cannot be reproduced from the specification. The flattened
     * form is rebased below the property it follows before comparing; the type, name and order of
     * every item, and the nesting below the struct itself, all stay part of the comparison.
     *
     * @param items the parsed items
     * @return the items with every flattened serializer struct and its contents rebased
     */
    private List<DocItem> alignSerializerStructs(List<DocItem> items) {
        List<DocItem> aligned = new ArrayList<>(items);
        for (int i = 1; i < aligned.size(); i++) {
            DocItem struct = aligned.get(i);
            boolean nestedBefore = aligned.subList(0, i).stream().anyMatch(item -> item.level() > 1);
            if (!"struct".equals(struct.type()) || struct.level() != 1 || !nestedBefore ||
                    "array".equals(aligned.get(i - 1).type())) {
                continue;
            }

            int shift = aligned.get(i - 1).level() - 1;
            aligned.set(i, new DocItem(struct.level() + shift, struct.type(), struct.name()));
            for (int j = i + 1; j < aligned.size() && aligned.get(j).level() > 1; j++) {
                DocItem item = aligned.get(j);
                aligned.set(j, new DocItem(item.level() + shift, item.type(), item.name()));
            }
        }
        return aligned;
    }

    private List<DocItem> alignLeadingArrayElement(List<DocItem> items) {
        if (items.size() < 2) {
            return items;
        }
        DocItem array = items.get(0);
        DocItem elementStruct = items.get(1);
        if (!"array".equals(array.type()) || !"struct".equals(elementStruct.type()) ||
                elementStruct.level() != array.level() + 1) {
            return items;
        }

        List<DocItem> aligned = new ArrayList<>(items.size());
        aligned.add(array);
        items.subList(1, items.size())
                .forEach(item -> aligned.add(new DocItem(item.level() - 1, item.type(), item.name())));
        return aligned;
    }

    /**
     * Aligns the nesting of a struct that describes the element type of an array valued property.
     *
     * This is the same artifact as above, one level in. The doclet indents the element struct below
     * its property when the namespace spells the element out with {@code #struct_begin}, and emits
     * it flat when the namespace refers to a {@code $Serializer} instead. Both spellings describe
     * the same property, and the OpenAPI schema is a reference either way, so the depth cannot be
     * reproduced from the specification. The indented form is rebased on the flat form before
     * comparing; the type, name and order of every item, and the nesting relative to the element
     * struct, all stay part of the comparison.
     *
     * @param items the parsed return value items
     * @return the items with every nested element struct and its contents rebased, when indented
     */
    private List<DocItem> alignNestedElementStructs(List<DocItem> items) {
        List<DocItem> aligned = new ArrayList<>(items);
        for (int i = 0; i + 1 < aligned.size(); i++) {
            DocItem array = aligned.get(i);
            DocItem elementStruct = aligned.get(i + 1);
            if (!"array".equals(array.type()) || !"struct".equals(elementStruct.type()) ||
                    elementStruct.level() <= array.level()) {
                continue;
            }
            int shift = elementStruct.level() - 1;
            aligned.set(i + 1, new DocItem(1, elementStruct.type(), elementStruct.name()));
            for (int j = i + 2; j < aligned.size() && aligned.get(j).level() > array.level(); j++) {
                DocItem item = aligned.get(j);
                aligned.set(j, new DocItem(item.level() - shift, item.type(), item.name()));
            }
        }
        return aligned;
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
        Matcher itemMatcher = DOCBOOK_LIST_ITEM.matcher(itemizedListBody(section, sectionMatcher.start()));
        while (itemMatcher.find()) {
            parseDocBookItem(itemMatcher.group(1)).ifPresent(items::add);
        }
        return items;
    }

    /**
     * Extracts the contents of the first item list of a section, nested lists included.
     *
     * A struct property and an {@code #options()} block are both rendered as an item list inside
     * an item of the enclosing list, so the list that opens a section closes only after the ones
     * it contains. Matching up to the first closing tag would cut the section short at the first
     * nested list and silently drop every item after it, so the closing tag is found by balancing.
     *
     * @param section the method section to read
     * @param from the offset of the section header
     * @return the body of the item list, or an empty string when the section has none
     */
    private String itemizedListBody(String section, int from) {
        Matcher openMatcher = DOCBOOK_LIST_OPEN.matcher(section);
        if (!openMatcher.find(from)) {
            return "";
        }

        int bodyStart = openMatcher.end();
        int depth = 1;
        Matcher tagMatcher = DOCBOOK_LIST_TAG.matcher(section);
        int cursor = bodyStart;
        while (tagMatcher.find(cursor)) {
            depth += tagMatcher.group(1) == null ? 1 : -1;
            if (depth == 0) {
                return section.substring(bodyStart, tagMatcher.start());
            }
            cursor = tagMatcher.end();
        }
        return section.substring(bodyStart);
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
