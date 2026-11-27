/*
 * Copyright (c) 2014 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.taglibs;

import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.frontend.html.HtmlTag;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DecimalStyle;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * &lt;rhn:datepicker data="${picker}"/&gt;
 *
 * Where picker is a com.redhat.rhn.common.util.DatePicker
 *
 * The date-picker.jsp fragment is kept for backwards compatibility
 *
 * It generates backward compatibility input tags with date_hour,
 * date_minutes, date_am_pm...,
 * using Javascript to be backwards compatible with the old tag,
 * so it should work in all pages.
 *
 * The date is displayed in a localized format when the calendar is not open.
 * The calendar is localized using the month names from the DatePicker class
 * and related classes.
 *
 */
public class DateTimePickerTag extends TagSupport {

    private DatePicker data;

    /**
     * @return the date picker object for this tag
     * @see com.redhat.rhn.common.util.DatePicker
     */
    public DatePicker getData() {
        return data;
    }

    /**
     * Sets the date picker for this tag
     * @param pData the date picker object
     */
    public void setData(DatePicker pData) {
        this.data = pData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        this.data = null;
        super.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int doEndTag() throws JspException {
       try {
          writePickerHtml(pageContext.getOut());
       }
       catch (IOException e) {
           throw new JspException(e);
       }
       return super.doEndTag();
    }

    private HtmlTag createInputAddonTag(String type, String icon) {
        HtmlTag dateAddon = new HtmlTag("span");
        dateAddon.setAttribute("class", "input-group-addon input-group-text text");
        dateAddon.setAttribute("id", data.getName() + "_" +
                type + "picker_widget_input_addon");
        dateAddon.setAttribute("data-picker-name", data.getName());
        dateAddon.setAttribute("data-picker-type", type);
        IconTag dateAddonIcon = new IconTag(icon);
        dateAddon.addBody("&nbsp;");
        dateAddon.addBody(dateAddonIcon.render());
        return dateAddon;
    }

    /**
     * Convert day java.util.Calendar constants
     * to an index usable by the javascript picker.
     *
     * @return the equivalent index for the javascript picker
     */
    private String getJavascriptPickerDayIndex(int calIndex) {
        return String.valueOf(calIndex - 1);
    }

    private void writePickerHtml(Writer out) throws IOException {

        // This is a mounting point for the React-based picker
        HtmlTag group = new HtmlTag("div");
        group.setAttribute("id", data.getName() + "_datepicker_widget");
        group.setAttribute("class", "legacy-date-time-picker");

        group.setAttribute("data-name", data.getName());
        if (!data.getDisableDate()) {
            group.setAttribute("data-has-date", "");
        }
        if (!data.getDisableTime()) {
            group.setAttribute("data-has-time", "");
        }
        if (data.isLatin()) {
            group.setAttribute("data-is-am-pm", "");
        }
        DateTimeFormatter isoFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmZ")
                .withDecimalStyle(DecimalStyle.ofDefaultLocale()).withZone(ZoneId.systemDefault());
        group.setAttribute("data-value", isoFmt.format(Instant.ofEpochMilli(data.getDate().getTime())));

        out.append(group.render());

        // compatibility with the old struts form
        // these values are updated when the picker changes using javascript
        //
        // if you are tempted to not write out these fields in case
        // date or time are disabled for the picker, mind that
        // DatePicker::readMap resets the date to now() if not all fields
        // are present.
        out.append(createHiddenInput("day", String.valueOf(data.getDay())).render());
        out.append(createHiddenInput("month", String.valueOf(data.getMonth())).render());
        out.append(createHiddenInput("year", String.valueOf(data.getYear())).render());

        out.append(createHiddenInput("hour", String.valueOf(data.getHour())).render());
        out.append(createHiddenInput("minute", String.valueOf(data.getMinute())).render());
        if (data.isLatin()) {
            out.append(createHiddenInput("am_pm",
                    String.valueOf((data.getHourOfDay() >= 12) ? 1 : 0)).render());
        }
    }

    private HtmlTag createHiddenInput(String type, String value) {
        HtmlTag input = new HtmlTag("input");
        input.setAttribute("id", data.getName() + "_" + type);
        input.setAttribute("name", data.getName() + "_" + type);
        input.setAttribute("type", "hidden");
        input.setAttribute("value", value);
        return input;
    }
}
