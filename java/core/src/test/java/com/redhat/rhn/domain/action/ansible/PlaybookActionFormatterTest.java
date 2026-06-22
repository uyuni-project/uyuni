/*
 * Copyright (c) 2026 SUSE LLC
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
package com.redhat.rhn.domain.action.ansible;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PlaybookActionFormatter#getRelatedObjectDescription()}.
 *
 * The Action Chain UI shows the formatter output as the {0} placeholder of
 * the {@code actionchain.jsp.ansible.playbook} message, so distinct playbook
 * names need to make it through the formatter unambiguously.
 */
public class PlaybookActionFormatterTest {

    @Test
    public void testReturnsBasenameOfPlaybookPath() {
        String result = formatter("/etc/ansible/playbooks/deploy.yml").getRelatedObjectDescription();
        assertEquals("deploy.yml", result);
    }

    @Test
    public void testHandlesPathWithoutDirectory() {
        String result = formatter("update.yml").getRelatedObjectDescription();
        assertEquals("update.yml", result);
    }

    @Test
    public void testEscapesHtmlSpecialCharacters() {
        // The Action Chain JSP renders relatedObjectDescription as raw HTML
        // through <bean:message arg0="..."/>, so any HTML metacharacters in
        // the file name must be escaped (matching ErrataActionFormatter /
        // PackageActionFormatter behavior).
        String result = formatter("/playbooks/<evil>.yml").getRelatedObjectDescription();
        assertEquals("&lt;evil&gt;.yml", result);
    }

    @Test
    public void testReturnsEmptyStringWhenDetailsMissing() {
        PlaybookAction action = new PlaybookAction();
        PlaybookActionFormatter formatter = new PlaybookActionFormatter(action);
        assertEquals("", formatter.getRelatedObjectDescription());
    }

    @Test
    public void testReturnsEmptyStringWhenPathBlank() {
        assertEquals("", formatter("").getRelatedObjectDescription());
        assertEquals("", formatter("   ").getRelatedObjectDescription());
        assertEquals("", formatter(null).getRelatedObjectDescription());
    }

    private static PlaybookActionFormatter formatter(String playbookPath) {
        PlaybookActionDetails details = new PlaybookActionDetails();
        details.setPlaybookPath(playbookPath);
        PlaybookAction action = new PlaybookAction();
        action.setDetails(details);
        return new PlaybookActionFormatter(action);
    }
}
