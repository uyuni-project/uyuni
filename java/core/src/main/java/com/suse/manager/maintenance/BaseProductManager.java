/*
 * Copyright (c) 2022--2023 SUSE LLC
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class BaseProductManager {

    private static final Logger LOGGER = LogManager.getLogger(BaseProductManager.class);

    private static final Period NOTIFICATION_PERIOD = Period.ofMonths(6);

    private static final String BASE_PRODUCT_FILE = "/etc/products.d/baseproduct";

    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private final XPathFactory xPathFactory = XPathFactory.newInstance();

    private final String name;

    private final String version;

    private final String arch;

    private final String summary;

    private final LocalDate endOfLifeDate;

    /**
     * Default constructor.
     */
    public BaseProductManager() {
        this(Path.of(BASE_PRODUCT_FILE).toUri());
    }

    /**
     * Parse the product file and extract the relevant piece of information.
     *
     * @param baseProductURI the base product URI
     */
    public BaseProductManager(URI baseProductURI) {
        Optional<Document> document = openDocument(baseProductURI);

        name = document.map(doc -> extractTextByXPath(doc, "//product/name")).orElse(null);
        version = document.map(doc -> extractTextByXPath(doc, "//product/version")).orElse(null);
        arch = document.map(doc -> extractTextByXPath(doc, "//product/arch")).orElse(null);
        summary = document.map(doc -> extractTextByXPath(doc, "//product/summary")).orElse(null);
        endOfLifeDate = document.map(doc -> extractTextByXPath(doc, "//product/codestream/endoflife"))
            .map(stringDate -> {
                try {
                    return LocalDate.parse(stringDate, DateTimeFormatter.ISO_LOCAL_DATE);
                }
                catch (DateTimeParseException ex) {
                    LOGGER.warn("Unable to parse end of life date {}", stringDate, ex);
                    return null;
                }
            })
            .orElse(null);

        LOGGER.debug("{} parse result: (name, version, arch, summary, endOfLife) => ({}, {}, {}, {}, {})",
            baseProductURI, name, version, arch, summary, endOfLifeDate);
    }

    /**
     * Checks if a given date is inside the notification period for the specified end of life date
     * @param date          the date to check
     * @return true if the given date is in the range we should notify the end of life
     */
    public boolean isNotificationPeriod(LocalDate date) {
        return endOfLifeDate.minus(NOTIFICATION_PERIOD).isBefore(date);
    }

    /**
     * Returns the name for this product.
     * @return the name of the product, or null if not defined.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version for this product.
     * @return the version of the product, or null if not defined.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the architecture for this product.
     * @return the architecture of the product, or null if not defined.
     */
    public String getArch() {
        return arch;
    }

    /**
     * Returns the end of life date for this product.
     *
     * @return the end of life date of the product, or null if not defined.
     */
    public LocalDate getEndOfLifeDate() {
        return endOfLifeDate;
    }

    /**
     * Returns the summary for this product.
     * @return the summary of the product, or null if not defined.
     */
    public String getSummary() {
        return summary;
    }

    private Optional<Document> openDocument(URI baseProductURI) {
        try (InputStream inputStream = baseProductURI.toURL().openStream()) {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return Optional.of(documentBuilder.parse(inputStream));
        }
        catch (ParserConfigurationException | IOException | SAXException ex) {
            LOGGER.warn("Unable to open xml document from {}", baseProductURI, ex);
            return Optional.empty();
        }
    }

    private String extractTextByXPath(Document document, String nodePath) {
        String expression = nodePath + "/text()";

        try {
            XPath xPath = xPathFactory.newXPath();
            return (String) xPath.compile(expression).evaluate(document, XPathConstants.STRING);
        }
        catch (XPathExpressionException ex) {
            LOGGER.warn("Unable to extract value for XPath {}", expression, ex);
            return null;
        }
    }

}
