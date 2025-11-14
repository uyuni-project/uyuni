/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.access;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;

/**
 * Factory class for RBAC's {@link Namespace} entities
 */
public class NamespaceFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(NamespaceFactory.class);

    private NamespaceFactory() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Lists all namespaces defined in MLM.
     * @return the list of all namespaces
     */
    public static List<Namespace> list() {
        return getSession()
                .createQuery("SELECT n FROM Namespace n", Namespace.class)
                .getResultList();
    }

    /**
     * Finds namespaces matching a query string.
     * <p>
     * Asterisk (*) character can be used as a wildcard.
     * @param namespace the string to search for
     * @return the list of namespaces matching the string
     */
    public static List<Namespace> find(String namespace) {
        return getSession()
                .createQuery("SELECT n FROM Namespace n WHERE n.namespace LIKE :namespace", Namespace.class)
                .setParameter("namespace", sanitizeNamespacePattern(namespace))
                .getResultList();
    }

    /**
     * Finds namespaces matching a query string, filtered by specified access modes.
     * <p>
     * The asterisk (*) character can be used as a wildcard in the namespace string.
     * @param namespace the string to search for
     * @param modes the access modes to filter the namespaces by
     * @return the list of namespaces matching the string and the specified access modes
     */
    public static List<Namespace> find(String namespace, Set<Namespace.AccessMode> modes) {
        return getSession()
                .createQuery("SELECT n FROM Namespace n WHERE n.namespace LIKE :namespace AND n.accessMode IN :modes",
                        Namespace.class)
                .setParameter("namespace", sanitizeNamespacePattern(namespace))
                .setParameter("modes", modes)
                .getResultList();
    }

    /**
     * Sanitizes a namespace pattern with wildcards to be used in SQL's LIKE expression.
     * @param input the namespace pattern to sanitize
     * @return the sanitized string
     */
    protected static String sanitizeNamespacePattern(String input) {
        if (input == null) {
            return "%";
        }

        String escaped = StringEscapeUtils.escapeJava(input);
        StringBuilder result = new StringBuilder(escaped.length());

        for (int i = 0; i < escaped.length(); i++) {
            char c = escaped.charAt(i);
            if (c == '%' || c == '_') {
                result.append('\\'); // Escape SQL LIKE special characters
            }
            result.append(c);
        }

        // Translate wildcard '*' to '%' to be used with LIKE
        return result.toString().replace("*", "%");
    }
}
