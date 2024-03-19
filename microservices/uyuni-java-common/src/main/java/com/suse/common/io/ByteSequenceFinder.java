/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.common.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Finds a byte sequence in a stream of bytes. Uses the Knuth–Morris–Pratt search algorithm for better performance.
 * <p>
 * The algorithm remembers the past matched characters in order to avoid restarting from the beginning of the pattern
 * when a mismatch is found. This is obtained by building a lookup of a partial match table called failure function.
 * <p>
 * <a href="https://en.wikipedia.org/wiki/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm">Algorithm reference</a>.
 */
public class ByteSequenceFinder {

    private byte[] sequence;

    private int[] failure;


    /**
     * Default constructor
     */
    public ByteSequenceFinder() {
        this(new byte[0]);
    }

    /**
     * Create a new instance.
     * @param sequenceIn the sequence to find
     */
    public ByteSequenceFinder(byte[] sequenceIn) {
        setSequence(sequenceIn);
    }

    /**
     * Finds the first occurrence of the pattern in the specified byte array.
     * @param bytes the byte array where the sequence is searched
     * @return the index where the sequence starts within the array, <code>-1</code> if the sequence was not found.
     * @throws IOException when an error processing the stream of bytes occurs
     */
    public int search(byte[] bytes) throws IOException {
        return search(new ByteArrayInputStream(bytes));
    }

    /**
     * Finds the first occurrence of the pattern in the specified stream. The stream will be consumed up to end
     * of the sequence matched. It will be fully consumed if no match is found.
     * @param stream the input stream to search
     * @return the position where the sequence starts within the stream and the stream will be positioned AFTER the
     * end of the sequence. Otherwise, <code>-1</code> if the sequence was not found.
     * @throws IOException when an error processing the stream occurs
     */
    public int search(InputStream stream) throws IOException {
        int data = stream.read();

        // If the sequence is empty and the stream is empty as well, return that the sequence is found
        // This case is similar to "".indexOf("") and "".contains("")
        if (sequence.length == 0 && data == -1) {
            return 0;
        }

        int matchedLength = 0;
        int bytesRead = 1;

        while (data != -1) {
            // If the byte is correct
            if (sequence[matchedLength] == (byte) data) {
                // Increment the length of current match
                matchedLength++;

                if (matchedLength == sequence.length) {
                    // All sequence matched
                    return bytesRead - sequence.length;
                }

                data = stream.read();
                bytesRead++;
            }
            else {
                // Mismatch: check the failure function to see from where to restart
                matchedLength = failure[matchedLength];

                // If we hit -1 there is no change for a partial match, we restart from zero and read the next byte
                if (matchedLength == -1) {
                    matchedLength = 0;

                    data = stream.read();
                    bytesRead++;
                }
            }
        }

        return -1;
    }

    /**
     * Retrieves the current sequence used by a search method.
     * @return the current sequence to be searched
     */
    public byte[] getSequence() {
        return sequence;
    }

    /**
     * Sets the sequence to be used by the next invocation of a search method.
     * @param sequenceIn the new sequence to search
     */
    public void setSequence(byte[] sequenceIn) {
        this.sequence = sequenceIn;
        this.failure = updateFailureFunction(sequence);
    }

    /**
     * Computes the failure function. Evaluates the sequence and find repeating prefixes.
     */
    private static int[] updateFailureFunction(byte[] sequence) {
        int[] failure = new int[sequence.length + 1];

        failure[0] = -1;

        int prefixLength = -1;
        int i = 0;

        while (i < sequence.length) {
            while (prefixLength >= 0 && sequence[prefixLength] != sequence[i]) {
                prefixLength = failure[prefixLength];
            }

            failure[++i] = ++prefixLength;
        }

        return failure;
    }
}
