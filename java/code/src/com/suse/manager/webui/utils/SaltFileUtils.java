package com.suse.manager.webui.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Utilities for working with Salt files.
 */
public class SaltFileUtils {

    public SaltFileUtils() {}

    public static boolean hasExtension(String fileName) {
        return fileName.endsWith(".sls");
    }

    public static String stripExtension(String fileName) {
        return StringUtils.removeEnd(fileName, ".sls");
    }

    public static String defaultExtension(String fileName) {
        return SaltFileUtils.hasExtension(fileName) ? fileName : fileName + ".sls";
    }
}
