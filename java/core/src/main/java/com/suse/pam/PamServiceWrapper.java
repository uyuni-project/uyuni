/*
 * Copyright (c) 2011--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.pam;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Wrap around 'unix2_chkpwd' for authenticating a user against PAM.
 */
public class PamServiceWrapper implements PamService {

    /* Path to the command, can be made configurable as well */
    private static final String COMMAND = "/sbin/unix2_chkpwd";
    /* The specific authentication service to use for this instance */
    private final String authService;
    /** The {@link Runtime} instance to execute commands */
    private final Runtime runtime;

    /**
     * Default constructor.
     *
     * @param authServiceIn The auth service to use for this instance
     */
    public PamServiceWrapper(String authServiceIn) {
        this(authServiceIn, Runtime.getRuntime());
    }

    /**
     * Full constructor.
     *
     * @param authServiceIn The auth service to use for this instance
     * @param runtimeIn the {@link Runtime} instance to execute commands
     */
    public PamServiceWrapper(String authServiceIn, Runtime runtimeIn) {
        this.authService = authServiceIn;
        this.runtime = runtimeIn;
    }

    /**
     * Perform the actual authentication by calling the 'unix2_chkpwd' binary.
     *
     * @param user The user to authenticate
     * @param passwd The password
     * @return A {@link PamReturnValue} representing the result.
     */
    public PamReturnValue authenticate(String user, String passwd) {
        PamReturnValue ret = PamReturnValue.PAM_FAILURE;
        if (user == null || passwd == null) {
            return ret;
        }

        try {
            // Execute the command
            String[] command = {COMMAND, this.authService, user};
            Process pr = runtime.exec(command);

            // Write the password to the stdin of the program
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
            output.write(passwd);
            output.flush();

            // Determine the exit value
            int exitVal = pr.waitFor();
            if (exitVal == 0) {
                ret = PamReturnValue.PAM_SUCCESS;
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return ret;
    }
}
