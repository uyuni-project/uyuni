/*
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.common.util;

import org.simpleframework.xml.transform.Transform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Parses a datetime using the given {@link DateFormat}.
 */
public class DateFormatTransformer implements Transform<Date> {

    private DateFormat dateFormat;

    /**
     * @param dateFormatIn date format
     */
    public DateFormatTransformer(DateFormat dateFormatIn) {
        this.dateFormat = dateFormatIn;
    }

    @Override
    public Date read(String value) throws Exception {
        return dateFormat.parse(value);
    }

    @Override
    public String write(Date date) throws Exception {
        return dateFormat.format(date);
    }

    /**
     * Parses a datetime from XML format (e.g. 2017-02-22T10:23:55).
     * See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html
     * @return a transformer for parsing yyyy-MM-dd'T'HH:mm:ss
     */
    public static DateFormatTransformer createXmlDateTransformer() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return new DateFormatTransformer(dateFormat);
    }
}
