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
// TEST
import java.util.Optional;

public final class Exceptions {

    /**
     * Represents an operation that does not accept inputs and returns no result, but may throw an exception.
     * @param <E> type of exception
     */
    @FunctionalInterface
    public interface ThrowingRunnable<E extends Exception> {
        /**
         * Performs this operation.
         * @throws E when an error occurs during the execution
         */
        void run() throws E;
    }

    /**
     * Represents an operation that does not accept inputs and returns a value, but may throw an exception.
     * @param <T> the type of the return value
     * @param <E> type of exception
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Exception>  {
        /**
         * Performs this operation.
         * @return the result of the operation
         * @throws E when an error occurs during the execution
         */
        T get() throws E;
    }

    /**
     * Represents an operation that accepts a single input argument and returns no result, but may throw an exception.
     * @param <T> the type of the return value
     * @param <E> type of exception
     */
    @FunctionalInterface
    public interface ThrowingConsumer<T, E extends Exception>  {
        /**
         * Performs this operation.
         * @param value the value to consume
         * @throws E when an error occurs during the execution
         */
        void accept(T value) throws E;
    }

    private Exceptions() {
        // Prevent instantiation
    }

    /**
     * Executes an operation and returns the exception if occurred during the execution.
     * @param operation the operation to perform
     * @param <E> type of exception
     * @return an optional wrapping the exception, or empty if the operation completes successfully.
     */
    public static <E extends Exception> Optional<Exception> handleByReturning(ThrowingRunnable<E> operation) {
        try {
            operation.run();
            return Optional.empty();
        }
        catch (Exception ex) {
            return Optional.of(ex);
        }
    }

    /**
     * Executes an operation and wraps any exception into a runtime exception.
     * @param operation the operation to perform
     * @param <E> type of exception
     * @throws RuntimeException if an exception occurs during the operation.
     */
    public static <E extends Exception> void handleByWrapping(ThrowingRunnable<E> operation) {
        handleByWrapping(() -> {
            operation.run();
            return null;
        });
    }

    /**
     * Executes an operation and wraps any exception into a runtime exception.
     * @param operation the operation to perform
     * @param <T> the type of the return value of the operation
     * @param <E> type of exception
     * @return the result value of the operation
     * @throws RuntimeException if an exception occurs during the operation.
     */
    public static <T, E extends Exception> T handleByWrapping(ThrowingSupplier<T, E> operation) {
        try {
            return operation.get();
        }
        catch (Exception ex) {
            throw new RuntimeException("Unable to execute operation", ex);
        }
    }
}
