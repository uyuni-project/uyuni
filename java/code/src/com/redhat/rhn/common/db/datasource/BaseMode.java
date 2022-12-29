/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

/**
 * A cached set of query/elaborator strings and the parameterMap hash maps.
 *
 */
public abstract class BaseMode implements Mode {

    private ParsedMode parsed;
    private String name;
    private CachedStatement query;

    /**
     * This constructor is only to support DataListTest
     */
    protected BaseMode() { }

    /**
     * Construct a new BaseMode instance.
     * @param session hibernate database session
     * @param parsedMode the mode
     */
    /*package*/ BaseMode(Session session, ParsedMode parsedMode) {
        this.parsed = parsedMode;
        this.name = parsedMode.getName();
        this.query = new CachedStatement(session, parsedMode.getParsedQuery());
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String n) {
        name = n;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setQuery(CachedStatement q) {
        query = q;
    }

    /** {@inheritDoc} */
    @Override
    public CachedStatement getQuery() {
        return query;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[ Name = " + getName() + "  query = " + (query == null ? "null" : query.toString());
    }

    /**
     * Returns the arity of the mode
     * @return number of parameters the mode accepts
     */
    public int getArity() {
        return query.getArity();
    }


    /**
     * @return the parsed mode to be used to replicate the mode
     */
    public ParsedMode getParsedMode() {
        return parsed;
    }
}

