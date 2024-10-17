/*
 * Copyright (c) 2018 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.utils.salt;

import com.suse.salt.netapi.calls.LocalCall;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LocalCallWithExecutors<T> extends LocalCall<T> {

    private LocalCall<T> call;
    private List<String> executors;
    private Map<String, ?> executorOpts;

    public LocalCallWithExecutors(LocalCall<T> call, List<String> executors, Map<String, ?> executorOps) {
        super((String) call.getPayload().get("fun"),
                Optional.ofNullable((List<?>) call.getPayload().get("arg")),
                Optional.ofNullable((Map<String, ?>) call.getPayload().get("kwarg")),
                call.getReturnType());
        this.call = call;
        this.executors = executors;
        this.executorOpts = executorOps;
    }

    @Override
    public Map<String, Object> getPayload() {
        Map<String, Object> payPaload = super.getPayload();
        payPaload.put("module_executors", executors);
        payPaload.put("executor_opts", executorOpts);
        return payPaload;
    }
}
