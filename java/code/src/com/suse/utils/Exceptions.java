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
package com.suse.utils;

import java.util.Optional;

public final class Exceptions {

    /**
     * Represents an operation that does not accept inputs and returns no result, but may throw an exception.
     */
    @FunctionalInterface
    public interface ThrowingOperation {
        /**
         * Performs this operation.
         * @throws Exception when an error occurs during the execution
         */
        void execute() throws Exception;
    }

    /**
     * Represents an operation that does not accept inputs and returns a value, but may throw an exception.
     * @param <T> the type of the return value
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        /**
         * Performs this operation.
         * @return the result of the operation
         * @throws Exception when an error occurs during the execution
         */
        T execute() throws Exception;
    }

    private Exceptions() {
        // Prevent instantiation
    }

    /**
     * Executes an operation and returns the exception if occurred during the execution.
     * @param operation the operation to perform
     * @return an optional wrapping the exception, or empty if the operation completes successfully.
     */
    public static Optional<? extends Exception> handleByReturning(ThrowingOperation operation) {
        try {
            operation.execute();
            return Optional.empty();
        }
        catch (Exception ex) {
            return Optional.of(ex);
        }
    }

    /**
     * Executes an operation and wraps any exception into a runtime exception.
     * @param operation the operation to perform
     * @throws RuntimeException if an exception occurs during the operation.
     */
    public static void handleByWrapping(ThrowingOperation operation) {
        handleByWrapping(() -> {
            operation.execute();
            return null;
        });
    }

    /**
     * Executes an operation and wraps any exception into a runtime exception.
     * @param operation the operation to perform
     * @param <T> the type of the return value of the operation
     * @return the result value of the operation
     * @throws RuntimeException if an exception occurs during the operation.
     */
    public static <T> T handleByWrapping(ThrowingSupplier<T> operation) {
        try {
            return operation.execute();
        }
        catch (Exception ex) {
            throw new RuntimeException("Unable to execute operation", ex);
        }
    }
}
