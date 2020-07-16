package com.suse.manager.webui.services.test;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.event.EventStream;

import java.util.Optional;

public class TestSaltApi implements SaltApi {
    @Override
    public EventStream getEventStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshPillar(MinionList minionList) {
        throw new UnsupportedOperationException();
    }
}
