package com.suse.manager.webui.services.iface;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.utils.Opt;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Interface for interacting with salt.
 */
public interface SaltApi {

    /**
     * Return the stream of events happening in salt.
     *
     * @return the event stream
     */
    EventStream getEventStream();

    /**
     * Synchronously executes a salt function on a single minion.
     * If a SaltException is thrown, re-throw a RuntimeException.
     *
     * @param call salt function to call
     * @param minionId minion id to target
     * @param <R> result type of the salt function
     * @return Optional holding the result of the function
     * or empty if the minion did not respond.
     */
    <R> Optional<R> callSync(LocalCall<R> call, String minionId);

}
