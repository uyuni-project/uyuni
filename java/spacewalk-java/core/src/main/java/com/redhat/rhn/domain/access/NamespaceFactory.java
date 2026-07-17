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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.frontend.listview.PageControl;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.utils.gson.NamespaceJson;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import jakarta.persistence.Tuple;

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
     * Lists namespaces defined in MLM filter by a search parameter.
     * @param filterParam the search parameter to filter by
     * @return the list of all namespaces
     */
    public static List<Namespace> list(String filterParam) {
        return getSession()
                .createNativeQuery("SELECT * FROM search_namespace(:filter)", Namespace.class)
                .addSynchronizedEntityClass(Namespace.class)
                .setParameter("filter", filterParam)
                .getResultList();
    }

    /**
     * Lists s paginated list of namespaces
     * @param pc the page control
     * @param parser the parser for filters when building query
     * @return the list of access groups
     */
    public static DataResult<NamespaceJson> list(
            PageControl pc, Function<Optional<PageControl>, PagedSqlQueryBuilder.FilterWithValue> parser) {
        String from = "(select " +
                "min(id) as id, " +
                "namespace, " +
                "min(description) as description, " +
                "string_agg(access_mode, '') as access_mode " +
                "from access.namespace " +
                "group by namespace " +
                ") ns";

        return new PagedSqlQueryBuilder("ns.id")
                .select("ns.*")
                .from(from)
                .where("true")
                .run(new HashMap<>(), pc, parser, NamespaceJson.class);
    }

    /**
     * List all namespaces assigned to an access group as json object
     * @param groupId the access group id
     * @return the list of namespaces
     */
    public static List<NamespaceJson> getAccessGroupNamespaces(Long groupId) {
        return getSession().createNativeQuery("""
                 SELECT *,
                 CASE
                   WHEN access_mode LIKE '%R%' THEN TRUE ELSE FALSE
                 END AS view,
                 CASE
                   WHEN access_Mode LIKE '%W%' THEN TRUE ELSE FALSE
                 END AS modify
                 FROM (
                   SELECT min(id) AS id,
                   namespace,
                   min(description) AS description,
                   string_agg(access_mode, '') AS access_mode
                   FROM access.namespace ns
                   JOIN access.accessgroupnamespace agn ON ns.id = agn.namespace_id
                   WHERE agn.group_id = :group_id
                   GROUP BY namespace
                 )
                 """, Tuple.class)
                .addSynchronizedEntityClass(Namespace.class)
                .addSynchronizedEntityClass(AccessGroup.class)
                .setParameter("group_id", groupId)
                .stream().map(NamespaceJson::new)
                .toList();

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
     * Lists namespaces matching given list of ids
     * @param ids the list of ids to filter the namespaces by
     * @return the list of namespaces matching the ids
     */
    public static List<Namespace> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return getSession()
                .createQuery("SELECT n FROM Namespace n WHERE n.id IN :ids", Namespace.class)
                .setParameter("ids", ids)
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
