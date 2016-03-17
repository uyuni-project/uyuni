/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * salt.modules.pkg
 */
public class Pkg {

    /**
     * @param refresh set true to perform a refresh before the installation
     * @param pkgs a map of package names to versions
     * @return the LocalCall object
     */
    public static LocalCall<Map<String, Object>> install(boolean refresh,
            Map<String, String> pkgs) {
        LinkedHashMap<String, Object> kwargs = new LinkedHashMap<>();
        kwargs.put("refresh", refresh);

        // Convert map into a list of maps with a single entry each as expected by Salt
        List<Map<String, String>> pkgsList = pkgs.entrySet().stream()
                .map(entry -> Collections.unmodifiableMap(Stream.of(entry).collect(
                        Collectors.toMap(e -> e.getKey(), e -> e.getValue()))))
                .collect(Collectors.toList());
        kwargs.put("pkgs", pkgsList);

        return new LocalCall<>("pkg.install", Optional.empty(), Optional.of(kwargs),
                new TypeToken<Map<String, Object>>() { });
    }

    public static LocalCall<Map<String, Object>> remove(Map<String, String> pkgs) {
        LinkedHashMap<String, Object> kwargs = new LinkedHashMap<>();

        // Convert map into a list of maps with a single entry each as expected by Salt
        List<Map<String, String>> pkgsList = pkgs.entrySet().stream()
                .map(entry -> Collections.unmodifiableMap(Stream.of(entry).collect(
                        Collectors.toMap(e -> e.getKey(), e -> e.getValue()))))
                .collect(Collectors.toList());
        kwargs.put("pkgs", pkgsList);

        return new LocalCall<>("pkg.remove", Optional.empty(), Optional.of(kwargs),
                new TypeToken<Map<String, Object>>() { });
    }
}
