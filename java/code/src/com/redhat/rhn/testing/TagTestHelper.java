/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;

/**
 * Sets up mock tag objects in a common configuration.
 * RhnMockHttpServletRequest, MockServletContext and MockHttpSession are attached to MockPageContext
 *
 * @see RhnMockPageContext#setRequest(ServletRequest) ();
 * @see RhnMockPageContext#setServletContext(ServletContext) ();
 * @see RhnMockPageContext#setSession(HttpSession) ();
 */
public class TagTestHelper extends AbstractServletTestHelper {
    private final RhnMockPageContext pageContext = new RhnMockPageContext();
    private final MockBodyContent bodyContent = new MockBodyContent();
    private final RhnMockJspWriter outWriter = new RhnMockJspWriter();
    private final RhnMockJspWriter enclosingWriter = new RhnMockJspWriter();
    private final Tag testSubject;

    /**
     * @param testSubjectIn The Tag to be tested
     */
    public TagTestHelper(Tag testSubjectIn) {
        this.testSubject = testSubjectIn;

        pageContext.setRequest(getRequest());
        pageContext.setServletContext(getServletContext());
        pageContext.setSession(getHttpSession());
        pageContext.setJspWriter(outWriter);
        bodyContent.setupGetEnclosingWriter(enclosingWriter);
    }

    private String getReturnValueName(int returnValue) {
        switch (returnValue) {
            case BodyTag.EVAL_BODY_INCLUDE:
                return "EVAL_BODY_INCLUDE";
            case BodyTag.EVAL_PAGE:
                return "EVAL_PAGE";
            case BodyTag.SKIP_BODY:
                return "SKIP_BODY";
            case BodyTag.SKIP_PAGE:
                return "SKIP_PAGE";
            case BodyTag.EVAL_BODY_BUFFERED:
                return "EVAL_BODY_BUFFERED|EVAL_BODY_AGAIN";
            default:
                return "Unknown return value (" + returnValue + ")";
        }
    }

    /**
     * @return The writer use when making calls to PageContext.getOut
     */
    public RhnMockJspWriter getOutWriter() {
        return outWriter;
    }

    public RhnMockPageContext getPageContext() {
        return pageContext;
    }

    /**
     * Assert that the return value of doStartTag is equal to an expectedValue
     *
     * @param expectedValue value to check against doStartTag
     */
    public void assertDoStartTag(final int expectedValue) throws JspException {
        testSubject.setPageContext(pageContext);

        checkReturnValue("doStartTag", expectedValue, testSubject.doStartTag());
    }

    private void checkReturnValue(final String methodName, final int expectedValue, final int returnValue) {
        assertEquals(expectedValue, returnValue, methodName + " expected value " +
                getReturnValueName(expectedValue) + " but was " + getReturnValueName(returnValue));
    }

    /**
     * Invoke doInitBody on the test subject
     */
    public void testDoInitBody() throws JspException {
        assertTrue(testSubject instanceof BodyTag,
                "doInitBody should not be called as test subject not an instance of BodyTag");

        ((BodyTag) testSubject).setBodyContent(bodyContent);
        ((BodyTag) testSubject).doInitBody();
    }

    /**
     * Assert that the return value of doAfterBody is equal to an expectedValue
     *
     * @param expectedValue value to check against doAfterBody
     */
    public void assertDoAfterBody(int expectedValue) throws JspException {
        assertTrue(testSubject instanceof IterationTag,
                "doAfterTag should not be called as test subject not an instance of IterationTag");

        checkReturnValue("doAfterTag", expectedValue, ((IterationTag) testSubject).doAfterBody());
    }

    /**
     * Assert that the return value of doEndTag is equal to an expectedValue
     *
     * @param expectedValue value to check against doEndTag
     */
    public void assertDoEndTag(int expectedValue) throws JspException {
        assertEquals(expectedValue, testSubject.doEndTag(),
                "doEndTag returned unexpected value" + getReturnValueName(expectedValue));
    }
}
