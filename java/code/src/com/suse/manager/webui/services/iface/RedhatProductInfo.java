package com.suse.manager.webui.services.iface;

import java.util.Optional;

public class RedhatProductInfo {

    private final Optional<String> centosReleaseContent;
    private final Optional<String> rhelReleaseContent;
    private final Optional<String> whatProvidesRes;

    public RedhatProductInfo(Optional<String> centosReleaseContent, Optional<String> rhelReleaseContent,
                             Optional<String> whatProvidesRes) {
        this.centosReleaseContent = centosReleaseContent;
        this.rhelReleaseContent = rhelReleaseContent;
        this.whatProvidesRes = whatProvidesRes;
    }


    public Optional<String> getCentosReleaseContent() {
        return centosReleaseContent;
    }

    public Optional<String> getRhelReleaseContent() {
        return rhelReleaseContent;
    }

    public Optional<String> getWhatProvidesRes() {
        return whatProvidesRes;
    }
}
