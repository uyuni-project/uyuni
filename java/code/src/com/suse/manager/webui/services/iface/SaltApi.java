package com.suse.manager.webui.services.iface;

import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.results.Result;

import java.util.Map;
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
     * Match the given target expression asynchronously.
     * @param target the target expression
     * @param cancel  a future used to cancel waiting on return events
     * @return a map holding a {@link CompletionStage}s for each minion
     */
    Map<String, CompletionStage<Result<Boolean>>> matchAsync(
            String target, CompletableFuture<GenericError> cancel);

    /**
     * Run a remote command on a given minion asynchronously.
     * @param target the target
     * @param cmd the command to execute
     * @param cancel a future used to cancel waiting on return events
     * @return a map holding a {@link CompletionStage}s for each minion
     */
    Map<String, CompletionStage<Result<String>>> runRemoteCommandAsync(
            MinionList target, String cmd, CompletableFuture<GenericError> cancel);

}
