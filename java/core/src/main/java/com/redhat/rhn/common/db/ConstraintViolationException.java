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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

/*
 * AUTOMATICALLY GENERATED FILE, DO NOT EDIT.
 */
package com.redhat.rhn.common.db;

/**
 * Thrown when a DB constraint is violated
 * <p>

 *
 */
public class ConstraintViolationException extends DatabaseException  {

    private static final long serialVersionUID = 8280712690008736370L;
    protected final int constraintType;
    protected final String constraint;

    /////////////////////////
    // Constructors
    /////////////////////////
    /**
     * Constructor
     * @param type constraint type
     * @param name constraint name
     * @param message exception message
     */
    public ConstraintViolationException(int type, String name,
            String message) {
        super(message);
        // begin member variable initialization
        this.constraintType =  type;
        this.constraint =  name;
    }

    /**
     * Constructor
     * @param type constraint type
     * @param name constraint name
     * @param message exception message
     * @param cause the cause (which is saved for later retrieval
     * by the Throwable.getCause() method). (A null value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     */
    public ConstraintViolationException(int type, String name,
            String message, Throwable cause) {
        super(message, cause);
        // begin member variable initialization
        this.constraintType =  type;
        this.constraint =  name;
    }

    /////////////////////////
    // Getters/Setters
    /////////////////////////
    /**
     * Returns the value of constraintType
     * @return int constraintType
     */
    public int getConstraintType() {
        return constraintType;
    }

    /**
     * Returns the value of constraint
     * @return String constraint
     */
    public String getConstraint() {
        return constraint;
    }
}
