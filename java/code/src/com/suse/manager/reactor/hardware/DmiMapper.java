/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor.hardware;

import com.redhat.rhn.domain.server.Dmi;
import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.utils.salt.Smbios;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Objects;

/**
 * Retrieve the DMI information from a minion an store it in our db.
 */
public class DmiMapper extends AbstractHardwareMapper<Dmi> {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(DmiMapper.class);

    private ValueMap bios;

    private ValueMap system;

    /**
     * The constructor.
     * @param saltServiceInvoker a {@link SaltServiceInvoker} instance
     */
    public DmiMapper(SaltServiceInvoker saltServiceInvoker) {
        super(saltServiceInvoker);
    }

    @Override
    public Dmi doMap(MinionServer server, ValueMap grains) {
        String biosVendor = null, biosVersion = null, biosReleseDate = null,
                productName = null, systemVersion = null, systemSerial = null,
                chassisSerial = null, chassisTag = null, boardSerial = null;
        String minionId = server.getMinionId();

        try {
            // TODO get all records at once? less roundtrips but larger response
            // TODO there is a 1MB limit to message size
            bios = new ValueMap(saltInvoker.getDmiRecords(minionId,
                    Smbios.RecordType.BIOS));
            system = new ValueMap(saltInvoker.getDmiRecords(minionId,
                    Smbios.RecordType.SYSTEM));
            ValueMap chassis = new ValueMap(saltInvoker.getDmiRecords(minionId,
                    Smbios.RecordType.CHASSIS));
            ValueMap board = new ValueMap(saltInvoker.getDmiRecords(minionId,
                    Smbios.RecordType.BASEBOARD));

            biosVendor = bios.getValueAsString("vendor");
            biosVersion = bios.getValueAsString("version");
            biosReleseDate = bios.getValueAsString("release_date");

            productName = system.getValueAsString("product_name");
            systemVersion = system.getValueAsString("version");
            systemSerial = system.getValueAsString("serial_number");

            chassisSerial = chassis.getValueAsString("serial_number");
            chassisTag = chassis.getValueAsString("asset_tag");

            boardSerial = board.getValueAsString("serial_number");
        }
        catch (com.google.gson.JsonSyntaxException e) {
            LOG.warn("Could not retrieve DMI info from minion '" + minionId +
                    "'. JSON syntax error.");
            // In order to behave like the "old style" registration
            // go on and persist an empty Dmi bean.
        }

        Dmi dmi = new Dmi();
        StringBuilder dmiSystem = new StringBuilder();
        if (StringUtils.isNotBlank(productName)) {
            dmiSystem.append(productName);
        }
        if (StringUtils.isNotBlank(systemVersion)) {
            if (dmiSystem.length() > 0) {
                dmiSystem.append(" ");
            }
            dmiSystem.append(systemVersion);
        }
        dmi.setSystem(dmiSystem.length() > 0 ? dmiSystem.toString().trim() : null);
        dmi.setProduct(productName);
        dmi.setBios(biosVendor, biosVersion, biosReleseDate);
        dmi.setVendor(biosVendor);

        dmi.setAsset(String.format("(chassis: %s) (chassis: %s) (board: %s) (system: %s)",
                Objects.toString(chassisSerial, ""), Objects.toString(chassisTag, ""),
                Objects.toString(boardSerial, ""), Objects.toString(systemSerial, "")));

        dmi.setServer(server);
        server.setDmi(dmi);

        return dmi;
    }

}
