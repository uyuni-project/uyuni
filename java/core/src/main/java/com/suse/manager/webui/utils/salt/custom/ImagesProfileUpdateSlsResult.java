/*
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.utils.salt.custom;

import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

/**
 * Result structure from images.profileupdate
 */
public class ImagesProfileUpdateSlsResult {

    // used for old salt 2016.11 dockerng module
    @SerializedName("mgrcompat_|-mgr_image_profileupdate_|-dockerng.sls_build_|-module_run")
    private Optional<StateApplyResult<Ret<PkgProfileUpdateSlsResult>>> dockerngSlsBuild = Optional.empty();

    // used for old salt 2016.11 dockerng module
    @SerializedName("mgrcompat_|-mgr_image_inspect_|-dockerng.inspect_|-module_run")
    private Optional<StateApplyResult<Ret<ImageInspectSlsResult>>> dockerngInspect = Optional.empty();

    // used for new salt 2018.3 docker module
    @SerializedName("mgrcompat_|-mgr_image_profileupdate_|-docker.sls_build_|-module_run")
    private Optional<StateApplyResult<Ret<PkgProfileUpdateSlsResult>>> dockerSlsBuild = Optional.empty();

    // used for new salt 2018.3 docker module
    @SerializedName("mgrcompat_|-mgr_image_inspect_|-docker.inspect_image_|-module_run")
    private Optional<StateApplyResult<Ret<ImageInspectSlsResult>>> dockerInspect = Optional.empty();

    @SerializedName("mgrcompat_|-mgr_inspect_kiwi_image_|-kiwi_info.inspect_image_|-module_run")
    private Optional<StateApplyResult<Ret<OSImageInspectSlsResult>>> kiwiInspect = Optional.empty();

    /**
     * @return getter
     */
    public StateApplyResult<Ret<PkgProfileUpdateSlsResult>> getDockerSlsBuild() {
        if (dockerSlsBuild.isPresent()) {
            return dockerSlsBuild.get();
        }
        return dockerngSlsBuild.get();
    }

    /**
     * @return getter
     */
    public StateApplyResult<Ret<ImageInspectSlsResult>> getDockerInspect() {
        if (dockerInspect.isPresent()) {
            return dockerInspect.get();
        }
        return dockerngInspect.get();
    }

    /**
     * @return getter
     */
    public StateApplyResult<Ret<OSImageInspectSlsResult>> getKiwiInspect() {
        return kiwiInspect.get();
    }
}
