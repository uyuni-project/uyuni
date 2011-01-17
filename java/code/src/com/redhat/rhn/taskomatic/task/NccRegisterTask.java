/**
 * Copyright (c) 2010 Novell
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.taskomatic.task;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.List;


/**
 * NccRegisterTask
 * @version $Rev$
 */
public class NccRegisterTask extends RhnJavaJob {

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        List<String> cmd = new ArrayList<String>();

        cmd.add("/usr/sbin/mgr-register");

        String[] args = cmd.toArray(new String[cmd.size()]);
        executeExtCmd(args);
    }
}
