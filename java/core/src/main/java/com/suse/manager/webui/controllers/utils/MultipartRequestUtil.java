/*
 * Copyright (c) 2018--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.controllers.utils;

import static com.suse.manager.webui.services.SaltConstants.SALT_FILE_GENERATION_TEMP_PATH;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.javax.JavaxServletFileUpload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import spark.Request;

public class MultipartRequestUtil {
    private MultipartRequestUtil() { }

    /**
     * Parse a multipart request
     * @param request the Spark request object
     * @return list of DiskFileItem from the request
     * @throws FileUploadException if parsing fails
     */
    public static List<DiskFileItem> parseMultipartRequest(Request request) throws FileUploadException {
        DiskFileItemFactory fileItemFactory = DiskFileItemFactory.builder()
                .setPath(SALT_FILE_GENERATION_TEMP_PATH)
                .get();
        return new JavaxServletFileUpload<>(fileItemFactory).parseRequest(request.raw());
    }

    /**
     * Find a string parameter value by name
     * @param items the list of file items
     * @param name the field name to search for
     * @return optional of String value if found
     */
    public static Optional<String> findStringParam(List<DiskFileItem> items, String name) {
        return items.stream()
                .filter(FileItem::isFormField)
                .filter(item -> name.equals(item.getFieldName()))
                .findFirst()
                .map(item -> {
                    try {
                        return item.getString(StandardCharsets.UTF_8);
                    }
                    catch (IOException e) {
                        throw new RuntimeException("Failed to read parameter: " + name, e);
                    }
                });
    }
    /**
     * Get a required string parameter value by name
     * @param items the list of file items
     * @param name the field name to search for
     * @return the string value (non-empty)
     * @throws IllegalArgumentException if parameter is missing or empty
     */
    public static String getRequiredString(List<DiskFileItem> items, String name) {
        return findStringParam(items, name)
                .filter(s -> !s.trim().isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Required parameter '" + name + "' is missing"));
    }
    /**
     * Get a required integer parameter value by name
     * @param items the list of file items
     * @param name the field name to search for
     * @return the integer value
     * @throws IllegalArgumentException if parameter is missing or not a valid integer
     */
    public static Integer getRequiredInt(List<DiskFileItem> items, String name) {
        try {
            return Integer.parseInt(getRequiredString(items, name));
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parameter '" + name + "' must be a valid integer");
        }
    }

    /**
     * Find a file upload item by name
     * @param items the list of file items
     * @param name the field name to search for
     * @return optional of DiskFileItem if found and has content
     */
    public static Optional<DiskFileItem> findFileItem(List<DiskFileItem> items, String name) {
        return items.stream()
                .filter(item -> !item.isFormField())         // 1. Only files
                .filter(item -> name.equals(item.getFieldName())) // 2. Match name
                .filter(item -> item.getSize() > 0)          // 3. Ignore empty uploads
                .findFirst();
    }
    /**
     * Find a file upload item by name
     * @param items the list of file items
     * @param name the field name to search for
     * @return optional of DiskFileItem if found and has content
     */
    public static DiskFileItem getRequiredFileItem(List<DiskFileItem> items, String name) {
        return findFileItem(items, name)
                .orElseThrow(() -> new IllegalArgumentException("Required file '" + name + "' is missing"));
    }
}
