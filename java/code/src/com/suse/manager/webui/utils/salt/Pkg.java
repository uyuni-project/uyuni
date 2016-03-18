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
     * @param pkgs map of packages (name -> version) to be installed
     * @return the LocalCall object
     */
    public static LocalCall<Map<String, Object>> install(boolean refresh,
            Map<String, String> pkgs) {
        LinkedHashMap<String, Object> kwargs = new LinkedHashMap<>();
        kwargs.put("refresh", refresh);
        kwargs.put("pkgs", preparePkgs(pkgs));
        return new LocalCall<>("pkg.install", Optional.empty(), Optional.of(kwargs),
                new TypeToken<Map<String, Object>>() { });
    }

    /**
     * @param pkgs map of packages (name -> version) to be removed
     * @return the LocalCall object
     */
    public static LocalCall<Map<String, Object>> remove(Map<String, String> pkgs) {
        LinkedHashMap<String, Object> kwargs = new LinkedHashMap<>();
        kwargs.put("pkgs", preparePkgs(pkgs));
        return new LocalCall<>("pkg.remove", Optional.empty(), Optional.of(kwargs),
                new TypeToken<Map<String, Object>>() { });
    }

    /**
     * From a given map (package name -> version), create a list of maps with just one
     * element each. This is how Salt requires us to send the 'pkgs' argument when multiple
     * packages should be installed or reomved.
     *
     * @param pkgs map with packages (name -> version)
     * @return list of maps with one element each
     */
    private static List<Map<String, String>> preparePkgs(Map<String, String> pkgs) {
        List<Map<String, String>> pkgsList = pkgs.entrySet().stream()
                .map(entry -> Collections.unmodifiableMap(Stream.of(entry).collect(
                        Collectors.toMap(e -> e.getKey(), e -> e.getValue()))))
                .collect(Collectors.toList());
        return pkgsList;
    }
}
