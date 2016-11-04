package com.suse.manager.webui.utils.salt.custom;

import java.util.Optional;

/**
 * Wrapper object representing a "ret" element to be parsed from event data.
 *
 * @param <T> the type that is wrapped
 */
public class RetOpt<T> {

    private Optional<T> retOpt = Optional.empty();

    public Optional<T> getRetOpt() {
        return retOpt;
    }
}
