/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.common.db.datasource;

import org.hibernate.Session;

import java.util.Collection;
import java.util.List;

/**
 * A {@link WriteMode} that can execute a fully dynamic query generated at runtime.
 */
public class GeneratedSelectMode extends SelectMode {

    /**
     * Creates an instance.
     * @param name the name to be used to identify the dynamic query
     * @param session hibernate database session to be used
     * @param sqlStatement the sql statement to execute
     * @param parameters the parameters needed to peform the quer
     */
    public GeneratedSelectMode(String name, Session session, String sqlStatement, List<String> parameters) {
        super(session, new DynamicParsedMode(name, sqlStatement, parameters));
    }

    private static class DynamicParsedMode implements ParsedMode {

        private final String name;

        private final String sqlStatement;

        private final Collection<String> parameters;

        private DynamicParsedMode(String nameIn, String sqlStatementIn, Collection<String> parametersIn) {
            this.name = nameIn;
            this.sqlStatement = sqlStatementIn;
            this.parameters = parametersIn;
        }

        @Override
        public String getName() {
            return "generated." + name;
        }

        @Override
        public ModeType getType() {
            return ModeType.WRITE;
        }

        @Override
        public ParsedQuery getParsedQuery() {
            return new DynamicQuery(name, sqlStatement, parameters);
        }

        @Override
        public String getClassname() {
            return null;
        }

        @Override
        public List<ParsedQuery> getElaborators() {
            return List.of();
        }
    }

    private static class DynamicQuery implements ParsedQuery {

        private final String name;

        private final String sqlStatement;

        private final Collection<String> parameters;

        private DynamicQuery(String nameIn, String sqlStatementIn, Collection<String> parametersIn) {
            this.name = nameIn;
            this.sqlStatement = sqlStatementIn;
            this.parameters = parametersIn;
        }

        @Override
        public String getName() {
            return "query.generated." + name;
        }

        @Override
        public String getAlias() {
            return "";
        }

        @Override
        public String getSqlStatement() {
            return sqlStatement;
        }

        @Override
        public String getElaboratorJoinColumn() {
            return "";
        }

        @Override
        public List<String> getParameterList() {
            return List.copyOf(parameters);
        }

        @Override
        public boolean isMultiple() {
            return false;
        }
    }
}
