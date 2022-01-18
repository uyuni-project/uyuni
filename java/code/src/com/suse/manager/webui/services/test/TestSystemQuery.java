/*
 * Copyright (c) 2020--2021 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.services.test;

import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.webui.services.iface.RedhatProductInfo;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Zypper;
import com.suse.salt.netapi.exception.SaltException;

import java.util.List;
import java.util.Optional;

public class TestSystemQuery implements SystemQuery {

    @Override
    public Optional<String> getMachineId(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notifySystemIdGenerated(MinionServer minion) throws InstantiationException, SaltException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<List<Zypper.ProductInfo>> getProducts(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
        return Optional.empty();
    }

    @Override
    public Optional<MgrUtilRunner.ExecResult> collectKiwiImage(
            MinionServer minion, String filepath, String imageStore) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<RedhatProductInfo> redhatProductInfo(String minionId) {
        throw new UnsupportedOperationException();
    }
}
