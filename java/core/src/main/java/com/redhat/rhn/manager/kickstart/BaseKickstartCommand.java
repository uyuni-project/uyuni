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
package com.redhat.rhn.manager.kickstart;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.kickstart.KickstartCommandName;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.builder.KickstartBuilder;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.PersistOperation;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerProfileEditCommand;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * BaseKickstartCommand - baseclass for editing a FileList class.
 */
public abstract class BaseKickstartCommand implements PersistOperation {

    private static Logger logger = LogManager.getLogger(BaseKickstartCommand.class);

    protected KickstartData ksdata;
    protected User user;
    protected boolean rebuildPartitionCommands = false;

    /**
     * Construct a command with a Kickstart ksid.
     * @param ksidIn to use.
     * @param userIn Logged in User
     *
     */
    protected BaseKickstartCommand(Long ksidIn, User userIn) {
        this(KickstartFactory
                .lookupKickstartDataByIdAndOrg(userIn.getOrg(), ksidIn), userIn);

    }


    /**
     * Construct a command with a KSdata provided.
     * @param data the kickstart data
     * @param userIn Logged in User
     *
     */
    protected BaseKickstartCommand(KickstartData data, User userIn) {
        super();
        this.ksdata = data;
        this.user = userIn;
    }

    /**
     *
     * @return KickstartData
     */
    public KickstartData getKickstartData() {
        return this.ksdata;
    }

    /**
     * Save the Kickstart Data to DB
     * @return ValdiatorError if there was an error.  Currently always returns null
     */
    @Override
    public ValidatorError store() {
        KickstartData ksData = getKickstartData();
        if (rebuildPartitionCommands) {
            ksData.removeCommand("bootloader", false);
            ksData.removeCommand("partitions", false);
            ksData.removeCommand("volgroups", false);
            ksData.removeCommand("logvols", false);

            KickstartWizardHelper helper = new KickstartWizardHelper(user);
            KickstartBuilder.setBootloader(helper, ksData);
            KickstartBuilder.setPartitionScheme(ksData);
        }


        KickstartFactory.saveKickstartData(ksData);

        CobblerProfileEditCommand cmd = new CobblerProfileEditCommand(ksdata, user, false);
        ValidatorError err = cmd.store();
        logger.debug("Did we get an error storing to cobbler: {}", err);
        return err;
    }


    /**
     * @return the user
     */
    @Override
    public User getUser() {
        return user;
    }


    /**
     * Looks up a KickstartCommandName by name
     * @param commandName name of the KickstartCommandName
     * @return found instance, if any
     */
    public  KickstartCommandName findCommandName(String commandName) {
        return KickstartFactory.lookupKickstartCommandName(commandName);
    }
}
