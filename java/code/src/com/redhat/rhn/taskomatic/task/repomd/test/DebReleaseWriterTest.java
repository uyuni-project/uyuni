/**
 * Copyright (c) 2018 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.taskomatic.task.repomd.test;

import com.redhat.rhn.taskomatic.task.repomd.DebReleaseWriter;
import junit.framework.TestCase;

import java.time.ZonedDateTime;

public class DebReleaseWriterTest extends TestCase {

    public void testDateFormat() {
        ZonedDateTime time = ZonedDateTime.parse("2018-11-22T12:35:40+01:00[Europe/Madrid]");
        assertEquals("Thu, 22 Nov 2018 11:35:40 UTC", DebReleaseWriter.RFC822_DATE_FORMAT.format(time));
    }
}
