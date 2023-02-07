/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

package com.redhat.rhn.common.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.util.CSVWriter;
import com.redhat.rhn.common.util.ExportWriter;
import com.redhat.rhn.frontend.dto.BaseDto;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * CSVWriterTest
 */
public class CSVWriterTest {

    @BeforeEach
    public void setUp() {
        TestUtils.disableLocalizationLogging();
    }


    @Test
    public void testMimeType() {
        ExportWriter writer = new CSVWriter(new StringWriter());
        assertEquals("text/csv", writer.getMimeType());
    }

    @Test
    public void testListOutput() {
        ExportWriter writer = new CSVWriter(new StringWriter());
        List values = new LinkedList<>();
        values.add("val1");
        values.add("val2");
        values.add("val3");
        values.add("val4");

        writer.write(values);
        assertEquals("val1,val2,val3,val4\n", writer.getContents());
    }

    @Test
    public void testListofMaps() {

        ExportWriter writer = new CSVWriter(new StringWriter());
        List columns = new LinkedList<>();
        columns.add("column1");
        columns.add("column2");
        columns.add("column3");
        columns.add("nullColumn");

        List<Map<String, String>> values = getTestListOfMaps();
        boolean failed = false;
        try {
            writer.write(values);
        }
        catch (IllegalArgumentException ia) {
            failed = true;
        }
        assertTrue(failed);
        writer.setColumns(columns);
        writer.write(values);

        assertTrue(writer.getContents().
                startsWith("**column1**,**column2**,**column3**,**nullColumn**\n"));
        assertTrue(writer.getContents().
                endsWith("cval1-9,cval2-9,cval3-9,\n"));
    }

    @Test
    public void testListofDtos() {

        ExportWriter writer = new CSVWriter(new StringWriter());
        List columns = new LinkedList<>();
        columns.add("fieldOne");
        columns.add("fieldTwo");
        columns.add("fieldThree");
        writer.setColumns(columns);

        List values = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            TestCsvDto dto = new TestCsvDto();
            dto.setFieldOne("f1 - " + i);
            dto.setFieldTwo("f2 - " + i);
            dto.setFieldThree("f3 - " + i);
            dto.setId((long) i);
            values.add(dto);
        }

        writer.write(values);
        assertTrue(writer.getContents().
                startsWith("**fieldOne**,**fieldTwo**,**fieldThree**\n"));
        assertTrue(writer.getContents().
                endsWith("f1 - 9,f2 - 9,f3 - 9\n"));
    }

    public static List<Map<String, String>> getTestListOfMaps() {
        List<Map<String, String>> values = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            Map<String, String> testmap = new HashMap<>();
            testmap.put("column1", "cval1-" + i);
            testmap.put("column2", "cval2-" + i);
            testmap.put("column3", "cval3-" + i);
            values.add(testmap);
        }
        return values;
    }

    public class TestCsvDto extends BaseDto {
        private Long id;
        private String fieldOne;
        private String fieldTwo;
        private String fieldThree;

        @Override
        public Long getId() {
            return id;
        }


        /**
         * @return Returns the fieldOne.
         */
        public String getFieldOne() {
            return fieldOne;
        }


        /**
         * @param fieldOneIn The fieldOne to set.
         */
        public void setFieldOne(String fieldOneIn) {
            this.fieldOne = fieldOneIn;
        }


        /**
         * @return Returns the fieldThree.
         */
        public String getFieldThree() {
            return fieldThree;
        }


        /**
         * @param fieldThreeIn The fieldThree to set.
         */
        public void setFieldThree(String fieldThreeIn) {
            this.fieldThree = fieldThreeIn;
        }


        /**
         * @return Returns the fieldTwo.
         */
        public String getFieldTwo() {
            return fieldTwo;
        }


        /**
         * @param fieldTwoIn The fieldTwo to set.
         */
        public void setFieldTwo(String fieldTwoIn) {
            this.fieldTwo = fieldTwoIn;
        }


        /**
         * @param idIn The id to set.
         */
        public void setId(Long idIn) {
            this.id = idIn;
        }

    }

}
