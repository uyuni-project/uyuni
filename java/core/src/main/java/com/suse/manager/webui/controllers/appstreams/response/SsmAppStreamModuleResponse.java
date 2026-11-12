/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.controllers.appstreams.response;

import java.util.Objects;


/**
 * A response model (DTO) representing an AppStream module in the SSM context.
 */
public class SsmAppStreamModuleResponse {
    private String name;
    private String stream;
    private int systemCount;

    /**
     * Constructor.
     *
     * @param nameIn the module name
     * @param streamIn the module stream
     * @param systemCountIn the number of SSM systems with this module stream enabled
     */
    public SsmAppStreamModuleResponse(String nameIn, String streamIn, int systemCountIn) {
        name = nameIn;
        stream = streamIn;
        systemCount = systemCountIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String streamIn) {
        stream = streamIn;
    }

    public int getSystemCount() {
        return systemCount;
    }

    public void setSystemCount(int systemCountIn) {
        systemCount = systemCountIn;
    }

    public void setSystemCount(Number systemCountIn) {
        this.systemCount = (systemCountIn != null) ? systemCountIn.intValue() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SsmAppStreamModuleResponse that = (SsmAppStreamModuleResponse) o;
        return name.equals(that.name) && stream.equals(that.stream);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, stream);
    }
}
