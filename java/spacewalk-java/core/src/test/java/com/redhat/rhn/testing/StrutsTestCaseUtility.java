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

package com.redhat.rhn.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.RequestProcessor;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.TilesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

public class StrutsTestCaseUtility {
    private static final String ERROR_MESSAGE_LABEL = "error";
    private static final String ACTION_MESSAGE_LABEL = "action";

    protected StrutsTestCaseUtility() {
        //empty protected constructor
    }

    public static void testActionHasNoErrors(HttpServletRequest request) {
        testActionNoMessages(request, Globals.ERROR_KEY, ERROR_MESSAGE_LABEL);
    }

    public static void testActionNoMessages(HttpServletRequest request) {
        testActionNoMessages(request, Globals.MESSAGE_KEY, ACTION_MESSAGE_LABEL);
    }

    private static void testActionNoMessages(HttpServletRequest request, String key, String messageLabel) {
        if (!(request.getAttribute(key) instanceof ActionMessages actionMessages)) {
            return;
        }

        Iterator<?> iterator = actionMessages.get();
        List<String> messageKeys = new ArrayList<>();
        iterator.forEachRemaining(m -> {
            if (m instanceof ActionMessage actionMessage) {
                messageKeys.add(actionMessage.getKey());
            }
        });

        if (!messageKeys.isEmpty()) {
            fail("Got message(s) [%s] while no action messages expected of type [%s]"
                    .formatted(String.join(" ", messageKeys), messageLabel));
        }
    }

    public static void testActionHasErrors(HttpServletRequest request, String[] expectedErrors) {
        testActionMessages(request, expectedErrors, Globals.ERROR_KEY, ERROR_MESSAGE_LABEL);
    }

    public static void testActionHasMessages(HttpServletRequest request, String[] expectedMessages) {
        testActionMessages(request, expectedMessages, Globals.MESSAGE_KEY, ACTION_MESSAGE_LABEL);
    }

    private static void testActionMessages(HttpServletRequest request, String[] expectedMessages,
                                           String key, String messageLabel) {

        if (!(request.getAttribute(key) instanceof ActionMessages actionMessages)) {
            fail("Expected messages of type [%s], but received none.".formatted(messageLabel));
            return;
        }

        if (actionMessages.size() != expectedMessages.length) {
            fail("Expected %d messages of type [%s], but received %d."
                    .formatted(expectedMessages.length, messageLabel, actionMessages.size()));
        }

        Iterator<?> iterator = actionMessages.get();
        List<String> actualList = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false)
                .flatMap(m -> {
                    if (m instanceof ActionMessage actionMessage) {
                        return Stream.of(actionMessage.getKey());
                    }
                    return Stream.empty();
                })
                .sorted().toList();

        List<String> expectedList = Arrays.stream(expectedMessages).sorted().toList();
        assertEquals(expectedList, actualList,
                () -> "Messages not matching: expected: %s, actual: %s".formatted(expectedList, actualList));
    }

    public static void testForwardPath(HttpServletRequest request, String forwardPath, String actualForwardPath) {
        if ((null == actualForwardPath) && (null == forwardPath)) {
            return;
        }

        forwardPath = request.getContextPath() + forwardPath;

        if (null == actualForwardPath) {
            fail("Found null actual forward path, when expected is [%s]".formatted(forwardPath));
        }

        assertEquals(forwardPath, actualForwardPath, "Expected and actual forward path not matching");
    }

    private static String getModuleName(HttpServletRequest request) {
        String moduleName = "";
        if (null != request.getServletPath()) {
            moduleName = request.getServletPath();
        }

        if (StringUtils.isBlank(moduleName)) {
            if (null != request.getAttribute(RequestProcessor.INCLUDE_SERVLET_PATH)) {
                moduleName = (String) request.getAttribute(RequestProcessor.INCLUDE_SERVLET_PATH);
            }
        }

        if (StringUtils.isNotEmpty(moduleName)) {
            moduleName = moduleName.substring(moduleName.indexOf('/'), moduleName.lastIndexOf('/'));
        }
        return moduleName;
    }

    private static Optional<String> getForwardNameWhenTiles(ForwardConfig expectedForward, HttpServletRequest request,
                                                            ServletContext context) {
        String tilesForward = null;
        ComponentDefinition definition = getTilesForward(expectedForward.getPath(), request, context);
        if (definition != null) {
            tilesForward = definition.getPath();
        }
        if (tilesForward != null) {
            return Optional.of(request.getContextPath() +
                    Strings.CS.prependIfMissing(tilesForward, "/"));
        }
        return Optional.empty();
    }

    private static Optional<String> getForwardNameWhenModules(ForwardConfig expectedForward,
                                                              HttpServletRequest request) {
        if (null == expectedForward.getModule()) {
            return Optional.of(request.getContextPath() + getModuleName(request) +
                    Strings.CS.prependIfMissing(expectedForward.getPath(), "/"));
        }
        return Optional.empty();
    }

    private static String getForwardNameDefault(ForwardConfig expectedForward, HttpServletRequest request) {
        return request.getContextPath() +
                Strings.CS.prependIfMissing(expectedForward.getPath(), "/");
    }

    public static void verifyForward(String actionPath, String forwardName, String actualForwardPath,
                                     HttpServletRequest request, ServletContext context) {
        if (null == forwardName) {
            if (null == actualForwardPath) {
                return;
            }
            else {
                fail("Expected a null forward from action, but received [%s]".formatted(actualForwardPath));
            }
        }

        ForwardConfig expectedForward = findForward(actionPath, forwardName, request, context);
        if (null == expectedForward) {
            fail("Cannot find forward [%s]".formatted(forwardName));
        }

        forwardName = getForwardNameWhenTiles(expectedForward, request, context)
                .orElseGet(() -> getForwardNameWhenModules(expectedForward, request)
                        .orElseGet(() -> getForwardNameDefault(expectedForward, request)));

        if (null == actualForwardPath) {
            fail("Expected forward [%s] from action, but received null one".formatted(forwardName));
        }

        if (!forwardName.equals(removeSessionId(actualForwardPath))) {
            fail("Expected forward [%s] from action, but received [%s]".formatted(forwardName, actualForwardPath));
        }
    }

    protected static ForwardConfig findForward(String mappingName, String forwardName,
                                               HttpServletRequest request, ServletContext context) {
        ForwardConfig forward = null;
        if (null != mappingName) {
            ActionConfig actionConfig = getActionConfig(mappingName, request, context);
            if (null != actionConfig) {
                forward = actionConfig.findForwardConfig(forwardName);
            }
        }

        if (null == forward) {
            ModuleConfig moduleConfig = getModuleConfig(request, context);
            forward = moduleConfig.findForwardConfig(forwardName);
        }
        return forward;
    }

    protected static ModuleConfig getModuleConfig(HttpServletRequest request, ServletContext context) {
        ModuleConfig config = (ModuleConfig) request.getAttribute(Globals.MODULE_KEY);
        if (null == config) {
            config = (ModuleConfig) context.getAttribute(Globals.MODULE_KEY);
        }
        return config;
    }

    protected static ActionConfig getActionConfig(String mappingName, HttpServletRequest request,
                                                  ServletContext context) {
        ModuleConfig config = getModuleConfig(request, context);
        return config.findActionConfig(mappingName);
    }

    protected static ComponentDefinition getTilesForward(String forwardPath, HttpServletRequest request,
                                                         ServletContext context) {
        try {
            ComponentDefinition actionDefinition =
                    (ComponentDefinition) request.getAttribute("org.apache.struts.tiles.ACTION_DEFINITION");

            if (actionDefinition != null) {
                return actionDefinition;
            }
            else {
                return TilesUtil.getDefinition(forwardPath, request, context);
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String removeSessionId(String urlString) {
        if (null == urlString) {
            return null;
        }

        // Remove string ";jsessionid=<sessionid>" from url
        int removeIndex = Strings.CI.indexOf(urlString, ";jsessionid=");
        if (removeIndex == -1) {
            // No string to remove
            return urlString;
        }

        // Search for parameter start after jsessionid, if any, to avoid removing part of the query string
        int queryIndex = Strings.CI.indexOf(urlString, "?", removeIndex);
        if (queryIndex == -1) {
            return urlString.substring(0, removeIndex);
        }

        StringBuilder buffer = new StringBuilder(urlString);
        return buffer.delete(removeIndex, queryIndex).toString();
    }

}
