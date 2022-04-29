/*
 * Copyright (c) 2020 SUSE LLC
 * Copyright (c) 2009--2012 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.internal.doclet;

import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.IdentifierTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.util.DocTreeScanner;
import com.sun.source.util.DocTrees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor9;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;


/**
 * Base doclet class
 */
public abstract class ApiDoclet implements Doclet {

    private static final String XMLRPC_DOC = "xmlrpc.doc";
    private static final String XMLRPC_PARAM = "xmlrpc.param";
    private static final String XMLRPC_RETURN = "xmlrpc.returntype";
    private static final String XMLRPC_NAMESPACE = "xmlrpc.namespace";
    private static final String XMLRPC_IGNORE = "xmlrpc.ignore";

    public static final String API_MACROS_FILE = "macros.txt";
    public static final String API_HANDLER_FILE = "handler.txt";
    public static final String API_INDEX_FILE = "apiindex.txt";
    public static final String API_FOOTER_FILE = "api_index_ftr.txt";
    public static final String API_HEADER_FILE = "api_index_hdr.txt";

    private Reporter reporter;
    private boolean debug = false;
    private String outputFolder;
    private String templateFolder;
    private String productName;
    private String apiVersion;

    protected final Set<Option> getOptions() {
        return Set.of(
            new AbstractOption("-debug", false,
                    "output debug messages", null) {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    debug = true;
                    return true;
                }
            },
            new AbstractOption("-d", true,
                    "output folder", "path") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    outputFolder = arguments.get(0);
                    if (!outputFolder.endsWith("/")) {
                        outputFolder += "/";
                    }
                    return true;
                }
            },
            new AbstractOption("-templates", true,
                    "JSP templates folder", "path") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    templateFolder = arguments.get(0);
                    if (!templateFolder.endsWith("/")) {
                        templateFolder += "/";
                    }
                    return true;
                }
            },
            new AbstractOption("-product", true,
                    "name of the product to write in the generated docs", "name") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    productName = arguments.get(0);
                    return true;
                }
            },
            new AbstractOption("-apiversion", true,
                    "version of the API to write in the generated docs", "version") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    apiVersion = arguments.get(0);
                    return true;
                }
            }
        );
    }

    protected ApiDoclet() {
    }

    /**
     * @return The documentation writer for the Doclet implementation
     * @param outputIn the folder where the result needs to be written
     * @param templateIn the folder where the templates are located
     * @param productNameIn the name of the product to write in the docs
     * @param apiVersionIn the version of the API to write in the docs
     * @param debugIn whether to show debug infos
     */
    public abstract DocWriter getWriter(String outputIn, String templateIn, String productNameIn,
                                        String apiVersionIn, boolean debugIn);

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return getOptions();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public void init(Locale locale, Reporter reporterIn) {
        reporter = reporterIn;
    }

    /**
     * start the doclet
     * @param docEnv the doclet environment
     * @param docType 'jsp' or 'wiki'
     * @return boolean
     */
    public boolean run(DocletEnvironment docEnv, String docType) {
        Set<TypeElement> classes = docEnv.getIncludedElements().stream()
                .filter(element -> element instanceof TypeElement && docEnv.isIncluded(element))
                .map(element -> (TypeElement)element)
                .collect(Collectors.toSet());

        List<TypeElement> serializers = getSerializers(classes, docEnv.getTypeUtils());
        List<TypeElement> handlers = getHandlers(classes, docEnv.getTypeUtils());
        Map<String, String> serialMap = getSerialMap(serializers, docEnv.getDocTrees());
        List<Handler> handlerList = new ArrayList<>();

        for (TypeElement clas : handlers) {
            DocCommentTree docTree = docEnv.getDocTrees().getDocCommentTree(clas);
            Handler handler = new Handler();

            handler.setClassName(clas.getSimpleName().toString());

            log("Processing handler: " + clas.getSimpleName());

            new HandlerDoctreeScanner().scan(docTree.getBlockTags(), handler);

            if (handler.isIgnored()) {
                continue;
            }
            if (handler.getName() == null) {
                String error =
                        "Someone didn't set " + XMLRPC_NAMESPACE + " correctly." +
                        "  If you really did not want this handler to appear in " +
                        "the API docs.  Add @xmlrpc.ignore to the class javadoc.";
                reporter.print(Diagnostic.Kind.ERROR, clas, error);
                return false;
            }

            handler.setCalls(
                clas.getEnclosedElements().stream()
                    .filter(enclosed -> enclosed.getKind().equals(ElementKind.METHOD) &&
                                        enclosed.getModifiers().contains(Modifier.PUBLIC))
                    .map(element -> new SimpleElementVisitor9<ApiCall, Void>() {
                            @Override
                            public ApiCall visitExecutable(ExecutableElement executable, Void p) {
                                log("Visiting executable: " + executable.getSimpleName());
                                ApiCall call = new ApiCall(executable);
                                call.setName(executable.getSimpleName().toString());

                                new CallDoctreeScanner(docType).
                                        scan(docEnv.getDocTrees().getDocCommentTree(executable).getBlockTags(), call);

                                return call;
                            }
                        }.visit(element)
                    )
                .filter(call -> !call.isIgnored())
                .collect(Collectors.toList()));

            //Then simply sort the apicalls and add the handler to our List
            Collections.sort(handler.getCalls());
            handlerList.add(handler);
        }
        Collections.sort(handlerList);
        DocWriter writer = getWriter(outputFolder, templateFolder, productName, apiVersion, debug);
        try {
            writer.write(handlerList, serialMap);
        }
        catch (Exception e) {
            reporter.print(Diagnostic.Kind.ERROR, e.getMessage());
            return false;
        }

        return true;
    }

    private List<TypeElement> getSerializers(Set<TypeElement> classes, Types types) {
        return classes.stream()
            .filter(element -> {
                // Less robust that the old implementation but should still do the job
                if (element.getSuperclass() != null) {
                    Element parent = types.asElement(element.getSuperclass());
                    return parent.getSimpleName().contentEquals("RhnXmlRpcCustomSerializer") ||
                            parent.getSimpleName().contentEquals("ApiResponseSerializer");
                }
                return false;
            })
            .collect(Collectors.toList());
    }

    private Map<String, String> getSerialMap(List<TypeElement> classes, DocTrees docTrees) {
        Map<String, String> map  = new HashMap<>();

        for (TypeElement clas : classes) {
            String doc = new DocTreeScanner<String, Void>() {
                @Override
                public String visitUnknownBlockTag(UnknownBlockTagTree node, Void ignore) {
                    if (node.getTagName().equals(XMLRPC_DOC)) {
                        String text = new TextExtractor().scan(node.getContent(), null);
                        log("Serial Doc content: " + text);
                        return text;
                    }
                    return null;
                }

                @Override
                public String reduce(String r1, String r2) {
                    return (r1 == null ? "" : r1) + (r2 == null ? "" : r2);
                }
            }.scan(docTrees.getDocCommentTree(clas).getBlockTags(), null);

            if (doc != null) {
                map.put(clas.getSimpleName().toString(), doc);
            }
        }

        return map;
    }

    private List<TypeElement> getHandlers(Set<TypeElement> classes, Types types) {
        return classes.stream()
            .filter(clazz -> clazz.getSuperclass() != null &&
                    types.asElement(clazz.getSuperclass()).getSimpleName().contentEquals("BaseHandler"))
            .sorted(Comparator.comparing(oIn -> oIn.getSimpleName().toString()))
            .collect(Collectors.toList());
    }

    @SuppressWarnings("java:S106")
    private void log(String msg) {
        if (debug) {
            System.err.println(msg);
        }
    }

    /**
     * DocTree scanner extracting everything as on string
     */
    static class TextExtractor extends DocTreeScanner<String, Void> {
        @Override
        public String visitText(TextTree node, Void p) {
            return node.getBody();
        }

        @Override
        public String visitEntity(EntityTree node, Void p) {
            return "&" + node.getName().toString() + ";";
        }

        @Override
        public String visitIdentifier(IdentifierTree node, Void p) {
            return node.getName().toString();
        }

        @Override
        public String reduce(String r1, String r2) {
            return (r2 == null ? "" : r2) + (r1 == null ? "" : r1);
        }
    }

    private class HandlerDoctreeScanner extends DocTreeScanner<Void, Handler> {
        @Override
        public Void visitUnknownBlockTag(UnknownBlockTagTree node, Handler handler) {
            log("Visiting unknown tag: " + node.getTagName());
            if (node.getTagName().equals(XMLRPC_IGNORE)) {
                handler.setIgnored();
                return null;
            }

            String text = new TextExtractor().scan(node.getContent(), null);
            if (node.getTagName().equals(XMLRPC_NAMESPACE)) {
                log("Handler Namespace content: " + text);
                handler.setName(text);
            }
            else if (node.getTagName().equals(XMLRPC_DOC)) {
                log("Handler Doc content: " + text);
                handler.setDesc(text);
            }
            return null;
        }
    }

    private class CallDoctreeScanner extends DocTreeScanner<Void, ApiCall> {

        private final String docType;

        CallDoctreeScanner(String docTypeIn) {
            docType = docTypeIn;
        }

        @Override
        public Void visitDeprecated(DeprecatedTree node, ApiCall call) {
            call.setDeprecated(true);
            String text = new TextExtractor().scan(node.getBody(), null);
            call.setDeprecatedReason(text);
            return null;
        }

        @Override
        public Void visitSince(SinceTree node, ApiCall call) {
            call.setSinceAvailable(true);
            String text = new TextExtractor().scan(node.getBody(), null);
            call.setSinceVersion(text);
            return null;
        }

        @Override
        public Void visitUnknownBlockTag(UnknownBlockTagTree node, ApiCall call) {
            log("Visiting unknown tag: " + node.getTagName());
            if (node.getTagName().equals(XMLRPC_IGNORE)) {
                call.setIgnored();
                return null;
            }

            String rawText = new TextExtractor().scan(node.getContent(), null);
            String text = rawText;
            if (docType.equals("docbook")) {
                text = DocBookWriter.transcode(rawText);
            }

            switch (node.getTagName()) {
                case XMLRPC_DOC:
                    log("Call Doc content: " + text);
                    call.setDoc(text);
                    break;
                case XMLRPC_PARAM:
                    log("Call Param content: " + text);
                    call.addParam(text);
                    break;
                case XMLRPC_RETURN:
                    log("Call Return content: " + rawText);
                    call.setReturnDoc(rawText);
                    break;
                default:
            }
            return null;
        }
    }
}
