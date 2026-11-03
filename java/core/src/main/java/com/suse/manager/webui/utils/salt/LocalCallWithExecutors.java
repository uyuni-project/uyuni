/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.suse.manager.webui.utils.salt;

import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * LocalCall with configuration of executor modules
 * @param <T> type
 */
public class LocalCallWithExecutors<T> extends LocalCall<T> {

    private List<String> executors;
    private Map<String, ?> executorOpts;

    /**
     * Constructor
     * @param call a LocalCall
     * @param executorsIn list of executors
     * @param executorOptsIn executor options
     */
    public LocalCallWithExecutors(LocalCall<T> call, List<String> executorsIn, Map<String, ?> executorOptsIn) {
        super((String) call.getPayload().get("fun"),
                Optional.ofNullable((List<?>) call.getPayload().get("arg")),
                Optional.ofNullable((Map<String, ?>) call.getPayload().get("kwarg")),
                call.getReturnType());
        this.executors = executorsIn;
        this.executorOpts = executorOptsIn;
    }

    /**
     * Constructor
     * @param functionName
     * @param arg
     * @param kwarg
     * @param returnType
     * @param metadata
     * @param executorsIn
     * @param executorOptsIn
     */
    public LocalCallWithExecutors(String functionName, Optional<List<?>> arg,
                                  Optional<Map<String, ?>> kwarg, TypeToken<T> returnType, Optional<?> metadata,
                                  List<String> executorsIn, Map<String, ?> executorOptsIn) {
        super(functionName, arg, kwarg, returnType, metadata, Optional.empty(), Optional.empty());
        executors = executorsIn;
        executorOpts = executorOptsIn;
    }


    @Override
    public Map<String, Object> getPayload() {
        Map<String, Object> payPaload = super.getPayload();
        if (executors != null && !executors.isEmpty()) {
            payPaload.put("module_executors", executors);
        }
        if (executorOpts != null && !executorOpts.isEmpty()) {
            payPaload.put("executor_opts", executorOpts);
        }
        return payPaload;
    }

    @Override
    public LocalCallWithExecutors<T> withMetadata(Object metadata) {
        return new LocalCallWithExecutors<>(
                (String) getPayload().get("fun"),
                Optional.ofNullable((List<?>)getPayload().get("arg")),
                Optional.ofNullable((Map<String, ?>) getPayload().get("kwarg")),
                this.getReturnType(),
                Optional.of(metadata),
                this.executors, this.executorOpts);
    }
}
