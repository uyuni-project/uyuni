/**
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
package com.redhat.rhn.frontend.events;


import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Offers utility methods to handle database transactions.
 */
public abstract class TransactionHelper {

    private static Logger log = Logger.getLogger(TransactionHelper.class);

    /**
     * Runs the runnable and handles the closing of the transaction and Hibernate session upon completion,
     * rolling back in case of unexpected Exceptions.
     *
     * @param runnable code to wrap
     * @param errorHandler called in case of unexpected Exceptions
     */
    public static void handlingTransaction(Runnable runnable, Consumer<Exception> errorHandler) {
        Optional<Exception> exception = empty();
        try {
            runnable.run();
        }
        catch (Exception e) {
            log.error("Unexpected error executing action", e);
            exception = of(e);
        }
        finally {
            handleTransactions(!exception.isPresent());
        }

        exception.ifPresent(e -> {
            handlingTransaction(
                    () -> errorHandler.accept(e),
                    f -> log.error("Additional Exception during handling", f)
            );
        });
    }

    private static void handleTransactions(boolean commit) {
        boolean committed = false;

        try {
            if (commit) {
                HibernateFactory.commitTransaction();
                committed = true;

                if (log.isDebugEnabled()) {
                    log.debug("Transaction committed");
                }
            }
        }
        catch (HibernateException e) {
            log.error("Error during transaction. Rolling back.", e);
        }
        finally {
            try {
                if (!committed) {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Rolling back transaction");
                        }
                        HibernateFactory.rollbackTransaction();
                    }
                    catch (HibernateException e) {
                        final String msg = "Additional error during rollback";
                        log.warn(msg, e);
                    }
                }
            }
            finally {
                // cleanup the session
                HibernateFactory.closeSession();
            }
        }
    }
}
