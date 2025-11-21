/*
 * Copyright (c) 2010--2012 Red Hat, Inc.
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
package com.redhat.rhn.common.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;

import java.io.Serializable;

/**
 * Hibernate interceptor that searches all objects being saved and checks if all
 * varchar fields are not empty. It can either print a warning in the log or
 * convert empty varchar to null automatically. It depends on the setting of the
 * interceptor.
 */
public class EmptyVarcharInterceptor implements Interceptor, Serializable {

    private static final long serialVersionUID = 5351605245345217308L;

    private static final Logger LOG = LogManager.getLogger(EmptyVarcharInterceptor.class);

    private boolean autoConvert;

    /**
     * Default constructor. Auto conversion is disabled by default.
     */
    public EmptyVarcharInterceptor() {
        this(false);
    }

    /**
     * Build a new instance specifying if empty varchar will be automatically converted to null.
     *
     * @param autoConvertIn if true automatically convert all empty varchar fields to null.
     */
    public EmptyVarcharInterceptor(boolean autoConvertIn) {
        this.autoConvert = autoConvertIn;
    }

    private static boolean emptyStringToNull(Object entity, Object id,
            Object[] state, String[] propertyNames, Type[] types,
            boolean autoConvert) {

        boolean modified = false;

        for (int i = 0; i < types.length; i++) {
            if ("".equals(state[i])) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Object {} is setting empty string {}", entity.getClass().getCanonicalName(),
                            propertyNames[i]);
                }
                if (autoConvert) {
                    state[i] = null;
                    modified = true;
                }
            }
        }
        return modified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onPersist(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        return emptyStringToNull(entity, id, state, propertyNames, types, autoConvert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState,
            String[] propertyNames, Type[] types) {
        return emptyStringToNull(entity, id, currentState, propertyNames,
                types, autoConvert);
    }

    /**
     * Flag indicating if the interceptor correct the varchar errors automatically
     *
     * @return boolean
     */
    public boolean isAutoConvert() {
        return autoConvert;
    }

    /**
     * Flag indicating if the interceptor correct the varchar errors automatically
     *
     * @param autoConvertIn true - convert automatically
     */
    public void setAutoConvert(boolean autoConvertIn) {
        this.autoConvert = autoConvertIn;
    }

}
