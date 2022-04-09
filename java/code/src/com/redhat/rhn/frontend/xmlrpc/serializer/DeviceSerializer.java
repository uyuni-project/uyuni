/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.domain.server.Device;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * DeviceSerializer
 *
 * @xmlrpc.doc
 *  #struct_begin("device")
 *      #prop_desc("string", "device", "optional")
 *      #prop_desc("string", "device_class",  "Includes CDROM, FIREWIRE, HD, USB, VIDEO,
 *                  OTHER, etc.")
 *      #prop("string", "driver")
 *      #prop("string", "description")
 *      #prop("string", "bus")
 *      #prop("string", "pcitype")
 *   #struct_end()
 */
public class DeviceSerializer extends ApiResponseSerializer<Device> {

    @Override
    public Class<Device> getSupportedClass() {
        return Device.class;
    }

    @Override
    public SerializedApiResponse serialize(Device src) {
        return new SerializationBuilder()
                .add("device", src.getDevice())
                .add("device_class", src.getDeviceClass())
                .add("driver", src.getDriver())
                .add("description", src.getDescription())
                .add("pcitype", src.getPcitype())
                .add("bus", src.getBus())
                .add("prop1", src.getProp1())
                .add("prop2", src.getProp2())
                .add("prop3", src.getProp3())
                .add("prop4", src.getProp4())
                .build();
    }
}
