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

package com.redhat.rhn.frontend.taglibs.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

/**
 * Tests for ListTag column filter behaviour.
 */
public class ListTagFilterTest extends MockObjectTestCase {

    /**
     * Test subclass that exposes private ListTag members via reflection.
     */
    static class TesterListTag extends ListTag {
        public ListFilter getFilter() throws Exception {
            var field = ListTag.class.getDeclaredField("filter");
            field.setAccessible(true);
            return (ListFilter) field.get(this);
        }

        public void setManipulator(DataSetManipulator m) throws Exception {
            var field = ListTag.class.getDeclaredField("manip");
            field.setAccessible(true);
            field.set(this, m);
        }
    }

    private PageContext pageContext;
    private HttpServletRequest req;

    @BeforeEach
    public void setUp() throws Exception {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        TestUtils.disableLocalizationLogging();

        req = mock(HttpServletRequest.class);
        pageContext = mock(PageContext.class);

        // Stub all pageContext interactions that may arise from tag lifecycle
        // methods (setPageContext, setColumnFilter, release).
        context().checking(new Expectations() { {
            allowing(pageContext).getAttribute(with(any(String.class)));
            will(returnValue(null));
            allowing(pageContext).removeAttribute(with(any(String.class)));
            allowing(pageContext).getRequest();
            will(returnValue(req));
        } });
    }

    private TesterListTag createMinimalListTag() throws Exception {
        TesterListTag testTag = new TesterListTag();
        testTag.setPageContext(pageContext);
        testTag.setName("testFilterList");

        DataSetManipulator manipulator = mock(DataSetManipulator.class);
        context().checking(new Expectations() { {
            ignoring(manipulator);
        } });
        testTag.setManipulator(manipulator);

        return testTag;
    }

    /**
     * Registering the same ColumnFilter twice must be a no-op.
     * Fix for bsc#1269192: JSP containers reuse pooled tag instances, which causes
     * ColumnTag to call setColumnFilter() again during the ENUMERATE phase on a tag
     * whose filter field is already set.
     */
    @Test
    public void testSetColumnFilterIdempotentSameFilter() throws Exception {
        TesterListTag testTag = createMinimalListTag();
        ColumnFilter filter = new ColumnFilter("actions.jsp.action", "actionName");

        testTag.setColumnFilter(filter);
        assertEquals(filter, testTag.getFilter(), "Filter should be set after first registration");

        testTag.setColumnFilter(filter);
        assertEquals(filter, testTag.getFilter(), "Filter should remain unchanged after duplicate registration");
    }

    /**
     * Registering a different ColumnFilter on the same tag must still
     * throw a JspException, preserving the original conflict-detection behaviour.
     */
    @Test
    public void testSetColumnFilterConflictDifferentFilter() throws Exception {
        TesterListTag testTag = createMinimalListTag();
        ColumnFilter filter1 = new ColumnFilter("actions.jsp.action", "actionName");
        ColumnFilter filter2 = new ColumnFilter("actions.jsp.status", "statusName");

        testTag.setColumnFilter(filter1);

        assertThrows(JspException.class, () -> testTag.setColumnFilter(filter2),
                "Setting a different filter on an already-filtered tag must throw JspException");
    }

    /**
     * After release(), the filter field must be null so that a subsequent render
     * cycle (as happens with pooled tags) can register a fresh filter.
     */
    @Test
    public void testReleaseResetsFilterState() throws Exception {
        TesterListTag testTag = createMinimalListTag();
        ColumnFilter filter1 = new ColumnFilter("actions.jsp.action", "actionName");

        testTag.setColumnFilter(filter1);
        assertEquals(filter1, testTag.getFilter(), "Filter should be set before release");

        testTag.release();
        assertNull(testTag.getFilter(), "Filter must be null after release");

        ColumnFilter filter2 = new ColumnFilter("actions.jsp.status", "statusName");
        testTag.setColumnFilter(filter2);
        assertEquals(filter2, testTag.getFilter(), "New filter should be accepted after release");
    }
}
