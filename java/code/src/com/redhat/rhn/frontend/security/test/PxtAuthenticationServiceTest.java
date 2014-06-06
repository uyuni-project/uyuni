/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.security.test;

import com.redhat.rhn.common.util.ServletUtils;
import com.redhat.rhn.frontend.security.PxtAuthenticationService;

import org.jmock.core.Constraint;

import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * PxtAuthenticationServiceTest
 * @version $Rev$
 */
// TODO Review Test classes in package to factor out common code
public class PxtAuthenticationServiceTest extends AuthenticationServiceAbstractTestCase {

    private class PxtAuthenticationServiceStub extends PxtAuthenticationService {
    }

    private PxtAuthenticationService service;

    public PxtAuthenticationServiceTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mock(RequestDispatcher.class);

        mockRequest.stubs().method("getRequestURI").will(returnValue("/rhn/YourRhn.do"));

        service = new PxtAuthenticationServiceStub();
        service.setPxtSessionDelegate(getPxtDelegate());

        setUpValidate();
    }

    private void setUpValidate() {
        mockPxtDelegate.stubs().method("isPxtSessionKeyValid").with(
                isA(HttpServletRequest.class)).will(returnValue(true));

        mockPxtDelegate.stubs().method("isPxtSessionExpired").with(
                isA(HttpServletRequest.class)).will(returnValue(false));

        mockPxtDelegate.stubs().method("getWebUserId").with(
                isA(HttpServletRequest.class)).will(returnValue(new Long(1234)));
    }

    private void runValidateFailsTest() {
        mockPxtDelegate.expects(atLeastOnce()).method("invalidatePxtSession").with(
                requestResponseArgs);

        assertFalse(service.validate(getRequest(), getResponse()));
    }

    private void runValidateSucceedsTest() {
        mockPxtDelegate.expects(atLeastOnce()).method("refreshPxtSession").with(
                requestResponseArgs);

        assertTrue(service.validate(getRequest(), getResponse()));
    }

    public final void testValidateFailsWhenPxtSessionKeyIsInvalid() {
        mockPxtDelegate.stubs().method("isPxtSessionKeyValid").with(
                isA(HttpServletRequest.class)).will(returnValue(false));

        runValidateFailsTest();
    }

    public final void testValidateFailsWhenPxtSessionExpired() {
        mockPxtDelegate.stubs().method("isPxtSessionExpired").with(
                isA(HttpServletRequest.class)).will(returnValue(true));

        runValidateFailsTest();
    }

    public final void testValidateFailsWhenWebUserIdIsNull() {
        mockPxtDelegate.stubs().method("getWebUserId").with(
                isA(HttpServletRequest.class)).will(returnValue(null));

        runValidateFailsTest();
    }

    public final void testValidateSucceedsWhenRequestURIUnprotected() {
        mockPxtDelegate.stubs().method("isPxtSessionKeyValid").with(
                isA(HttpServletRequest.class)).will(returnValue(false));

        mockRequest.stubs().method("getRequestURI").will(returnValue("/rhn/Login"));
        assertTrue(service.validate(getRequest(), getResponse()));
    }

    public final void testValidateSucceeds() {
        runValidateSucceedsTest();
    }

    public final void testInvalidate() {
        mockPxtDelegate.expects(atLeastOnce()).method("invalidatePxtSession").with(
                requestResponseArgs);

        service.invalidate(getRequest(), getResponse());
    }

    private void runRedirectToLoginTest() throws Exception {
        service.redirectToLogin(getRequest(), getResponse());
    }

    protected void setUpRedirectToLogin() {
        super.setUpRedirectToLogin();

        mockResponse.stubs().method("sendRedirect").will(returnValue(null));
    }

    /**
     *
     */
    public final void testRedirectoToLoginForwardsRequest() throws Exception {
        setUpRedirectToLogin();

        mockResponse.expects(once()).method("sendRedirect").with(
                new Constraint[] {eq("/rhn/Login.do")}).will(returnValue(null));

        mockRequest.stubs().method("getParameterNames").will(
returnValue(new Vector<String>().elements()));

        mockRequest.stubs().method("setAttribute").with(
                new Constraint[] {eq("url_bounce"), eq(getRequest().getRequestURI())});

        runRedirectToLoginTest();
    }

    /**
     * @throws Exception
     */
    public final void testRedirectToLoginSetsURLBounceRequestAttribute() throws Exception {
        setUpRedirectToLogin();

        createRequestURIWithParams(requestParamNames,
                requestParamValues);

        mockResponse.expects(once()).method("sendRedirect").with(
                new Constraint[] {eq("/rhn/Login.do")}).will(returnValue(null));

        runRedirectToLoginTest();
    }

    private String createRequestURIWithParams(String[] paramNames, String[] paramValues)
        throws Exception {

        StringBuffer uri = new StringBuffer(getRequest().getRequestURI()).append("?");

        uri.append(ServletUtils.requestParamsToQueryString(getRequest()));

        return uri.toString();
    }
}
