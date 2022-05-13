/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.maintenance;

import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class ProductEndOfLifeManager {

    private static final String END_OF_LIFE_ELEMENT_PATH = "product/codestream/endoflife";

    private static final Period NOTIFICATION_PERIOD = Period.ofMonths(6);

    private static final String BASE_PRODUCT_FILE = "/etc/products.d/baseproduct";

    private final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();


    /**
     * Checks if a given date is inside the notification period for the specified end of life date
     * @param endOfLifeDate the end of life date
     * @param date          the date to check
     * @return true if the given date is in the range we should notify the end of life
     */
    public boolean isNotificationPeriod(LocalDate endOfLifeDate, LocalDate date) {
        return endOfLifeDate.minus(NOTIFICATION_PERIOD).isBefore(date);
    }

    /**
     * Retrieves the end of life date for the given product. It parses the product xml file contained
     * in the /etc/products.d/ directory using StAX extracting the element containing the eol date. If the
     * file does not exist or the date cannot be parsed, an exception will be thrown.
     *
     * @return the end of life date of the product, null if no end of life is defined.
     * @throws IOException if there is an error accessing the product file
     * @throws XMLStreamException if an error happens during the parsing of the xml
     */
    public LocalDate getEndOfLifeDate() throws XMLStreamException, IOException {
        final Path productFile = Path.of(BASE_PRODUCT_FILE);
        if (!Files.exists(productFile)) {
            throw new FileNotFoundException("Unable to locate product file " + BASE_PRODUCT_FILE);
        }

        try (InputStream inputStream = Files.newInputStream(productFile)) {
            XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(inputStream);
            final String value = StringUtils.trimToNull(extractElementText(streamReader, END_OF_LIFE_ELEMENT_PATH));
            if (value == null) {
                return null;
            }

            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }

    /**
     * Extract the text content of the element specified by the given path. This method does not support XPath,
     * it is just using a tree path to identify the tag of interest.
     *
     * @param reader the StAX stream reader
     * @param elementPath the path of the element, as in /root/tag1/tag2
     * @return The text content of the element, or null if the element does not exist. If more the one element exists
     * only the first one is returned.
     *
     * @throws XMLStreamException when an XML exception happens during the process or the element path does not match
 *     any element.
     */
    private static String extractElementText(XMLStreamReader reader, String elementPath) throws XMLStreamException {
        final Iterator<String> elementsIterator = Arrays.asList(elementPath.split("/")).iterator();
        if (!elementsIterator.hasNext()) {
            return null;
        }

        String elementToFind = elementsIterator.next();
        String parent = null;

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals(elementToFind)) {
                if (!elementsIterator.hasNext()) {
                    // we reached the leaf, let's return the text
                    return reader.getElementText();
                }

                parent = elementToFind;
                elementToFind = elementsIterator.next();
            }
            else if (event == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals(parent)) {
                // The parent element is closed, and we did not find the element we were looking for
                throw new XMLStreamException("Unable to find element " + elementToFind + " within parent " + parent);
            }
        }

        throw new XMLStreamException("Unable to find element " + elementToFind);
    }

}
