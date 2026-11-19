/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.common.errors;

/**
 * Protocol-neutral category of a {@link UyuniError}.
 * Each entry point maps it to its own format (HTTP status, XMLRPC fault code, ...).
 * Names and meanings follow the canonical gRPC status codes (google.rpc.Code).
 */
public enum UyuniErrorCategory {
    /** The error was not classified: no information on what kind of error it is */
    UNKNOWN,
    /** An argument is invalid regardless of the state of the system, e.g. a malformed name */
    INVALID_ARGUMENT,
    /** A referenced entity does not exist */
    NOT_FOUND,
    /** The entity the caller tried to create already exists */
    ALREADY_EXISTS,
    /** The system is not in the state the operation requires, e.g. deleting a channel that has children */
    FAILED_PRECONDITION
}
