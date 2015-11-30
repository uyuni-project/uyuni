package com.suse.manager.webui.utils.salt;

import com.suse.saltstack.netapi.datatypes.target.Target;

/**
 * Matcher based on salt grains
 */
public class Grains implements Target<String> {

    private final String grain;
    private final String value;
    private final String target;

    /**
     * Creates a grains matcher
     *
     * @param grain the grain name
     * @param value the value to match
     */
    public Grains(String grain, String value) {
        this.grain = grain;
        this.value = value;
        this.target = grain + ":" + value;
    }

    /**
     * @return the grain name of this matcher
     */
    public String getGrain() {
        return grain;
    }

    /**
     * @return the value to match the grain
     */
    public String getValue() {
        return value;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public String getType() {
        return "grain";
    }
}
