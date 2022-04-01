/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.common;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * TokenFactory
 */
public class ChecksumFactory extends HibernateFactory {

    private static Logger log = LogManager.getLogger(ChecksumFactory.class);

    /**
     * Lookup a checksum by id
     * @param idIn the checksum to search for
     * @return the Checksum or null if none match
     */
    public static Checksum lookupById(Long idIn) {
        Session session = null;
        Checksum c = null;
        try {
            session = HibernateFactory.getSession();
            c = (Checksum) session.getNamedQuery("Checksum.findById")
                .setParameter("id", idIn)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
        }
        catch (HibernateException e) {
            log.error("Hibernate exception: " + e.toString());
            throw e;
        }
        return c;
    }

   /**
     * Lookup a checksum and if not exists, it is created.
     * @param hash to lookup Checksum for
     * @param hashType to lookup Checksum for
     * @return Checksum
     */
    public static Checksum safeCreate(String hash, String hashType) {
        if (hash == null || hashType == null) {
            return null;
        }

        // Lookup existing or create new checksum
        CallableMode m = ModeFactory.getCallableMode("checksum_queries",
            "create_new_checksum");
        Map inParams = new HashMap();
        Map outParams = new HashMap();
        inParams.put("checksum_in", hash);
        inParams.put("checksum_type_in", hashType);
        //Outparam
        outParams.put("checksumId", Types.NUMERIC);
        Map result = m.execute(inParams, outParams);
        Long checksumId = (Long) result.get("checksumId");
        if (checksumId == null) {
            throw new IllegalArgumentException(
                "Unknown checksum type: " + hashType + ")");
        }
        return lookupById(checksumId);
    }

    /**
     * {@inheritDoc}
     */
    protected Logger getLogger() {
        return log;
    }



}
