/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.action.kickstart;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.frontend.dto.CryptoKeyDto;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.kickstart.KickstartLister;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * KickstartCryptoKeysSetupAction - setup action to show the list of CryptoKeys
 * associated with this KickstartData
 */
public class KickstartCryptoKeysSetupAction extends BaseKickstartListSetupAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public RhnSetDecl getSetDecl() {
       return RhnSetDecl.GPGSSL_KEYS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataResult<CryptoKeyDto> getDataResult(RequestContext rctx, PageControl pc) {
        return KickstartLister.getInstance().
            cryptoKeysInOrg(rctx.getCurrentUser().getOrg());
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected Iterator<CryptoKey> getCurrentItemsIterator(KickstartData ksdata) {
        List<CryptoKey> keys = new ArrayList<>();
        if (ksdata.getCryptoKeys() != null) {
            keys.addAll(ksdata.getCryptoKeys());
        }
        return keys.iterator();
    }

}
