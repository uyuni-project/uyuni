/**
 * Copyright (c) 2011 Novell
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

package com.suse.pam;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Wrap around 'unix2_chkpwd' for authenticating a user against PAM.
 */
public class Pam {

    /* Path to the command, can be made configurable as well */
    private static final String COMMAND = "/sbin/unix2_chkpwd";
    /* The specific authentication service to use for this instance */
    private final String authService;

    /**
     * Public constructor.
     *
     * @param authServiceIn
     *            The auth service to use for this instance
     */
    public Pam(String authServiceIn) {
        this.authService = authServiceIn;
    }

    /**
     * Perform the actual authentication by calling the 'unix2_chkpwd' binary.
     *
     * @param user
     *            The user to authenticate
     * @param passwd
     *            The password
     * @return A {@link PamReturnValue} representing the result.
     */
    public PamReturnValue authenticate(String user, String passwd) {
        PamReturnValue ret = PamReturnValue.PAM_FAILURE;
        if (user == null || passwd == null) {
            return ret;
        }
        try {
            // Execute the command
            Runtime rt = Runtime.getRuntime();
            String[] command = {COMMAND, this.authService, user};
            Process pr = rt.exec(command, new String[]{});

            // Write the password to the stdin of the program
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                    pr.getOutputStream()));
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
            throw new RuntimeException(e);
        }
        return ret;
    }
}
