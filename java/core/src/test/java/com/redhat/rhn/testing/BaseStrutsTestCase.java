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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.RequestProcessor;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.util.ModuleUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Optional;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BaseStrutsTestCase extends BaseTestCase {

    private static final Logger LOGGER = LogManager.getLogger(BaseStrutsTestCase.class);

    private static final String LOCATION_KEY = "Location";

    protected static class StrutsTestHttpServletResponse extends RhnMockHttpServletResponse {
        @Override
        public void sendRedirect(String aURL) {
            reset();
            setStatus(SC_MOVED_TEMPORARILY);
            setHeader(LOCATION_KEY, aURL);
        }
    }

    protected static class StrutsTestHttpServletRequest extends RhnMockHttpServletRequest {
        private final ServletContext innerServletContext;

        StrutsTestHttpServletRequest(ServletContext innerServletContextIn) {
            innerServletContext = innerServletContextIn;
        }

        @Override
        public String[] getParameterValues(String name) {
            return getParameterMap().get(name);
        }

        @Override
        public void addParameter(String name, String value) {
            parameters.put(name, new String[]{value});
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String uri) {
            return innerServletContext.getRequestDispatcher(uri);
        }

        @Override
        public ServletContext getServletContext() {
            return innerServletContext;
        }
    }

    private static class StrutsTestServletConfig implements ServletConfig {
        private final ServletContext innerServletContext;
        private final HashMap<String, String> innerInitParameters;

        StrutsTestServletConfig(ServletContext innerServletContextIn, HashMap<String, String> innerInitParametersIn) {
            innerServletContext = innerServletContextIn;
            innerInitParameters = innerInitParametersIn;
        }

        @Override
        public String getServletName() {
            return "ServletName";
        }

        @Override
        public ServletContext getServletContext() {
            return innerServletContext;
        }

        @Override
        public String getInitParameter(String name) {
            return innerInitParameters.get(name);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return Collections.enumeration(innerInitParameters.keySet());
        }
    }

    private static class StrutsTestServletContext extends MockServletContext {
        private final StrutsTestRequestDispatcher innerRequestDispatcher;

        StrutsTestServletContext(StrutsTestRequestDispatcher innerRequestDispatcherIn) {
            innerRequestDispatcher = innerRequestDispatcherIn;
            setRequestDispatcher(innerRequestDispatcher);
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String sIn) {
            innerRequestDispatcher.setDispatchedResource(sIn);
            return innerRequestDispatcher;
        }

        @Override
        public InputStream getResourceAsStream(String string) {
            try {
                File file = new File(string);

                if (!file.exists()) {
                    file = new File(new File(".").getAbsolutePath() +
                            Strings.CS.prependIfMissing(string, "/"));
                }

                if (file.exists()) {
                    return new FileInputStream(file);
                }

                return getClass().getResourceAsStream(Strings.CS.prependIfMissing(string, "/"));
            }
            catch (Exception e) {
                LOGGER.error(e.getMessage());
                return null;
            }
        }
    }

    private static class StrutsTestRequestDispatcher implements RequestDispatcher {
        protected Object innerDispatchedResource = null;

        @Override
        public void forward(ServletRequest requestIn, ServletResponse responseIn) throws ServletException, IOException {
            if (innerDispatchedResource instanceof Servlet servletResource) {
                servletResource.service(requestIn, responseIn);
            }
        }

        @Override
        public void include(ServletRequest requestIn, ServletResponse responseIn) throws ServletException, IOException {
            //empty
        }

        private void setDispatchedResource(Object dispatchedResourceIn) {
            innerDispatchedResource = dispatchedResourceIn;
        }

        private String getForward() {
            if (null == innerDispatchedResource) {
                return null;
            }

            if (innerDispatchedResource instanceof String) {
                return (String) innerDispatchedResource;
            }
            else {
                return innerDispatchedResource.getClass().toString();
            }
        }
    }

    private ActionServlet actionServlet;
    protected StrutsTestHttpServletRequest request;
    protected StrutsTestHttpServletResponse response;
    private StrutsTestServletContext servletContext;
    private StrutsTestServletConfig servletConfig;
    private StrutsTestRequestDispatcher requestDispatcher;
    private HashMap<String, String> initParameters;
    private Optional<String> actionPath;

    @BeforeEach
    public void setUpMockStrutsTestCase() throws Exception {
        initParameters = new HashMap<>();
        requestDispatcher = new StrutsTestRequestDispatcher();
        servletContext = new StrutsTestServletContext(requestDispatcher);
        servletConfig = new StrutsTestServletConfig(servletContext, initParameters);
        request = new StrutsTestHttpServletRequest(servletContext);
        response = new StrutsTestHttpServletResponse();

        actionServlet = new ActionServlet();
        actionServlet.init(servletConfig);
    }

    @AfterEach
    public void tearDownMockStrutsTestCase() {
        actionServlet.destroy();
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void actionPerform() {
        if (actionPath.isEmpty()) {
            throw new RuntimeException("actionPath must be set before calling actionPerform()");
        }

        try {
            actionServlet.doPost(request, response);
        }
        catch (Exception e) {
            throw new RuntimeException("Exception thrown during actionPerform()", e);
        }
    }

    public void addRequestParameter(String parameterName, String parameterValue) {
        request.addParameter(parameterName, parameterValue);
    }

    public void addRequestParameter(String parameterName, String[] parameterValues) {
        request.addParameter(parameterName, parameterValues);
    }

    public void setRequestPathInfo(String pathInfo) {
        setRequestPathInfo("", pathInfo);
    }

    public void setRequestPathInfo(String moduleName, String pathInfo) {
        actionPath = Optional.ofNullable(stripActionPath(pathInfo));

        if (moduleName != null) {
            if (!moduleName.isEmpty()) {
                moduleName = Strings.CS.prependIfMissing(moduleName, "/");
                moduleName = Strings.CS.appendIfMissing(moduleName, "/");
            }
            request.setAttribute(RequestProcessor.INCLUDE_SERVLET_PATH, moduleName);
        }
        request.setPathInfo(actionPath.orElse(null));
    }

    public void testForwardName(String forwardName) {
        StrutsTestCaseUtility.verifyForward(actionPath.orElse(null), forwardName, getActualForward(),
                request, servletContext);
    }

    public void testForwardPath(String forwardPath) {
        StrutsTestCaseUtility.testForwardPath(request, forwardPath, getActualForward());
    }

    public void testActionHasErrors(String[] errorNames) {
        StrutsTestCaseUtility.testActionHasErrors(request, errorNames);
    }

    public void testActionHasNoErrors() {
        StrutsTestCaseUtility.testActionHasNoErrors(request);
    }

    public void testActionHasMessages(String[] expectedMessages) {
        StrutsTestCaseUtility.testActionHasMessages(request, expectedMessages);
    }

    public void testActionHasNoMessages() {
        StrutsTestCaseUtility.testActionNoMessages(request);
    }

    public ActionForm getActionForm() {
        ActionConfig actionConfig = StrutsTestCaseUtility.getActionConfig(actionPath.orElse(null),
                request, servletContext);
        if (isRequest(actionConfig)) {
            return (ActionForm) request.getAttribute(actionConfig.getAttribute());
        }

        return (ActionForm) request.getSession().getAttribute(actionConfig.getAttribute());
    }

    public void setActionForm(ActionForm form) {
        if (actionPath.isEmpty()) {
            throw new RuntimeException("actionPath must be set before calling setActionForm()");
        }

        ActionConfig actionConfig = StrutsTestCaseUtility.getActionConfig(actionPath.get(), request, servletContext);
        if (null == actionConfig) {
            ModuleUtils moduleUtils = ModuleUtils.getInstance();
            moduleUtils.selectModule(request, servletContext);
            actionConfig = StrutsTestCaseUtility.getActionConfig(actionPath.get(), request, servletContext);
        }

        if (isRequest(actionConfig)) {
            request.setAttribute(actionConfig.getAttribute(), form);
        }
        else {
            request.getSession().setAttribute(actionConfig.getAttribute(), form);
        }
    }

    public void clearRequestParameters() {
        request.getParameterMap().clear();
        response.removeHeader(LOCATION_KEY);
    }

    protected String getActualForward() {
        if (response.containsHeader(LOCATION_KEY)) {
            return StrutsTestCaseUtility.removeSessionId(response.getHeader(LOCATION_KEY));
        }

        if (null == request) {
            return null;
        }

        if (null == getForward()) {
            return null;
        }

        return StringUtils.defaultString(request.getContextPath()) +
                StringUtils.defaultString(StrutsTestCaseUtility.removeSessionId(getForward()));
    }

    private String getForward() {
        return requestDispatcher.getForward();
    }

    private static boolean isRequest(ActionConfig actionConfig) {
        return actionConfig.getScope().equals("request");
    }

    private static String stripActionPath(String pathString) {
        if (null == pathString) {
            return null;
        }

        int lastSlashIndex = pathString.lastIndexOf("/");
        int lastDotIndex = pathString.lastIndexOf(".");
        if ((lastDotIndex >= 0) && (lastDotIndex > lastSlashIndex)) {
            return pathString.substring(0, lastDotIndex);
        }

        return pathString;
    }
}

