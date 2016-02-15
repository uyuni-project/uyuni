/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

import com.suse.salt.netapi.calls.LocalCall;

import java.util.Map;
import java.util.Optional;

/**
 * Add metadata to the Salt LocalCall.
 */
public class LocalCallWithMetadata<T> extends LocalCall<T> {

    private Map<String, ?> metadata;
    private LocalCall<T> call;

    public LocalCallWithMetadata(LocalCall<T> call, Map<String, ?> metadata) {
        super(null, Optional.empty(), Optional.empty(), call.getReturnType());
        this.call = call;
        this.metadata = metadata;
    }

    @Override
    public Map<String, Object> getPayload() {
        Map<String, Object> payPaload = call.getPayload();
        payPaload.put("metadata", metadata);
        return payPaload;
    }
}
