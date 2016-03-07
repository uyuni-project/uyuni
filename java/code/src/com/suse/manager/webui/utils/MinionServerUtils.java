package com.suse.manager.webui.utils;

import com.redhat.rhn.domain.server.Server;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by matei on 3/6/16.
 */
public class MinionServerUtils {

    private MinionServerUtils() { }

    public static <T> List<T> filterSaltMinionIds(List<Server> servers, Function<Server, T> function) {
        return servers.stream()
                .filter(s -> isMinionServer(s))
                .map(function)
                .collect(Collectors.toList());
    }

    public static boolean isMinionServer(Server server) {
        return server.asMinionServer().isPresent();
    }

}
