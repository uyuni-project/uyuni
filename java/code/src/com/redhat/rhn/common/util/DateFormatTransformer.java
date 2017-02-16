package com.redhat.rhn.common.util;

import org.simpleframework.xml.transform.Transform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by matei on 2/15/17.
 */
public class DateFormatTransformer implements Transform<Date> {

    private DateFormat dateFormat;

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

    public static DateFormatTransformer createXmlDateTransformer() {
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm");
        return new DateFormatTransformer(dateFormat);
    }
}
