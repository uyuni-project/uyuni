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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;

import junit.framework.Assert;

/**
 * Sets up mock tag objects in a common configuration.
 * MockHttpServletRequest, MockServletContext and MockHttpSession are attached to MockPageContext
 * @see MockPageContext#setRequest();
 * @see MockPageContext#setServletContext();
 * @see MockPageContext#setSession();
 */
public class TagTestHelper extends AbstractServletTestHelper {
    private final MockPageContext pageContext = new MockPageContext();
    private final MockBodyContent bodyContent = new MockBodyContent();
    private final MockJspWriter outWriter = new MockJspWriter();
    private final MockJspWriter enclosingWriter = new MockJspWriter();
    private final Tag testSubject;

    private final String getReturnValueName(int returnValue) {
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
     * @param testSubject The Tag to be tested
     */
    public TagTestHelper(Tag testSubject) {
        this.testSubject = testSubject;

        pageContext.setRequest(getRequest());
        pageContext.setServletContext(getServletContext());
        pageContext.setSession(getHttpSession());
        pageContext.setJspWriter(outWriter);
        bodyContent.setupGetEnclosingWriter(enclosingWriter);
    }

    /**
     * @return The writer use when making calls to PageContext.getOut
     */
    public MockJspWriter getOutWriter() {
        return outWriter;
    }

    public MockPageContext getPageContext() {
        return pageContext;
    }

    /**
     * Assert that the return value of doStartTag is equal to an expectedValue
     * @param expectedValue value to check against doStartTag
     */
    public void assertDoStartTag(final int expectedValue) throws JspException {
        testSubject.setPageContext(pageContext);

        checkReturnValue("doStartTag", expectedValue, testSubject.doStartTag());
    }

    private final void checkReturnValue(final String methodName, final int expectedValue, final int returnValue) {
        Assert.assertEquals(methodName + " expected value " + getReturnValueName(expectedValue) +
            " but was " + getReturnValueName(returnValue),
            expectedValue, returnValue);
    }

    /**
     * Invoke doInitBody on the test subject
     */
    public void testDoInitBody() throws JspException {
        Assert.assertTrue("doInitBody should not be called as test subject not an instance of BodyTag",
            testSubject instanceof BodyTag);

        ((BodyTag) testSubject).setBodyContent(bodyContent);
        ((BodyTag) testSubject).doInitBody();
    }

    /**
     * Assert that the return value of doAfterBody is equal to an expectedValue
     * @param expectedValue value to check against doAfterBody
     */
    public void assertDoAfterBody(int expectedValue) throws JspException {
        Assert.assertTrue("doAfterTag should not be called as test subject not an instance of IterationTag",
            testSubject instanceof IterationTag);

        checkReturnValue("doAfterTag", expectedValue, ((IterationTag) testSubject).doAfterBody());
    }

    /**
     * Assert that the return value of doEndTag is equal to an expectedValue
     * @param expectedValue value to check against doEndTag
     */
    public void assertDoEndTag(int expectedValue) throws JspException {
        Assert.assertEquals("doEndTag returned unexpected value" + getReturnValueName(expectedValue),
            expectedValue, testSubject.doEndTag());
    }
}
