/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt.modulemd;

/**
 * modulemd API response wrapper class
 */
public class ModulemdApiResponse {
    static final int OK = 0;
    static final int EXCEPTION = 1;
    static final int MODULE_NOT_FOUND = 201;
    static final int DEPENDENCY_RESOLUTION_ERROR = 202;
    static final int CONFLICTING_STREAMS = 203;
    static final int REQUEST_ERROR = 301;

    private int errorCode;
    private String exception;
    private ModulePackagesResponse modulePackages;
    private ListPackagesResponse listPackages;
    private ListModulesResponse listModules;
    private ModulemdExceptionDataResponse data;

    public int getErrorCode() {
        return errorCode;
    }

    public String getException() {
        return exception;
    }

    public ModulePackagesResponse getModulePackages() {
        return modulePackages;
    }

    public ListPackagesResponse getListPackages() {
        return listPackages;
    }

    public ListModulesResponse getListModules() {
        return listModules;
    }

    public ModulemdExceptionDataResponse getData() {
        return data;
    }

    public boolean isError() {
        return errorCode != OK;
    }
}
