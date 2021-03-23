/**
 * Copyright (c) 2016--2021 SUSE LLC
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

import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.calls.modules.Zypper;
import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.salt.netapi.utils.Xor;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Object representation of the results of a call to state.apply
 * packages.profileupdate.
 */
public class PkgProfileUpdateSlsResult {

    public static final String PKG_PROFILE_REDHAT_RELEASE =
            "cmd_|-rhelrelease_|-cat /etc/redhat-release_|-run";
    public static final String PKG_PROFILE_CENTOS_RELEASE =
            "cmd_|-centosrelease_|-cat /etc/centos-release_|-run";
    public static final String PKG_PROFILE_ORACLE_RELEASE =
            "cmd_|-oraclerelease_|-cat /etc/oracle-release_|-run";
    public static final String PKG_PROFILE_ALIBABA_RELEASE =
            "cmd_|-alibabarelease_|-cat /etc/alinux-release_|-run";
    public static final String PKG_PROFILE_WHATPROVIDES_SLES_RELEASE =
            "cmd_|-respkgquery_|-rpm -q --whatprovides 'sles_es-release-server'_|-run";

    @SerializedName("mgrcompat_|-kernel_live_version_|-sumautil.get_kernel_live_version_|" +
            "-module_run")
    private Optional<StateApplyResult<Ret<KernelLiveVersionInfo>>> kernelLiveVersionInfo = Optional.empty();

    @SerializedName("mgrcompat_|-grains_update_|-grains.items_|-module_run")
    private StateApplyResult<Ret<Map<String, Object>>> grains;

    @SerializedName("mgrcompat_|-products_|-pkg.list_products_|-module_run")
    private StateApplyResult<Ret<List<Zypper.ProductInfo>>> listProducts;

    @SerializedName("mgrcompat_|-packages_|-pkg.info_installed_|-module_run")
    private StateApplyResult<Ret<Map<String, Xor<Pkg.Info, List<Pkg.Info>>>>> infoInstalled;

    @SerializedName(PKG_PROFILE_REDHAT_RELEASE)
    private StateApplyResult<CmdResult> rhelReleaseFile;

    @SerializedName(PKG_PROFILE_CENTOS_RELEASE)
    private StateApplyResult<CmdResult> centosReleaseFile;

    @SerializedName(PKG_PROFILE_ORACLE_RELEASE)
    private StateApplyResult<CmdResult> oracleReleaseFile;

    @SerializedName(PKG_PROFILE_ALIBABA_RELEASE)
    private StateApplyResult<CmdResult> alibabaReleaseFile;

    @SerializedName(PKG_PROFILE_WHATPROVIDES_SLES_RELEASE)
    private StateApplyResult<CmdResult> whatProvidesResReleasePkg;

    /**
     * Gets live patching info.
     *
     * @return the live patching info
     */
    public Optional<StateApplyResult<Ret<KernelLiveVersionInfo>>> getKernelLiveVersionInfo() {
        return kernelLiveVersionInfo;
    }

    /**
     * Gets grains.
     *
     * @return the grains
     */
    public Map<String, Object> getGrains() {
        return grains != null ? grains.getChanges().getRet() : new HashMap<>();
    }

    /**
     * @return the list of installed products
     */
    public StateApplyResult<Ret<List<Zypper.ProductInfo>>> getListProducts() {
        return listProducts;
    }

    /**
     * @return information about installed packages
     */
    public StateApplyResult<Ret<Map<String, Xor<Pkg.Info, List<Pkg.Info>>>>> getInfoInstalled() {
        return infoInstalled;
    }

    /**
     * @return the content of the file /etc/redhat-release
     */
    public StateApplyResult<CmdResult> getRhelReleaseFile() {
        return rhelReleaseFile;
    }

    /**
     * @return the content of the file /etc/centos-release
     */
    public StateApplyResult<CmdResult> getCentosReleaseFile() {
        return centosReleaseFile;
    }

    /**
     * @return the content of the file /etc/oracle-release
     */
    public StateApplyResult<CmdResult> getOracleReleaseFile() {
        return oracleReleaseFile;
    }

    /**
     * @return the content of the file /etc/alinux-release
     */
    public StateApplyResult<CmdResult> getAlibabaReleaseFile() { return alibabaReleaseFile; }

    /**
     * @return the package that provides 'sles_es-release-server'
     */
    public StateApplyResult<CmdResult> getWhatProvidesResReleasePkg() {
        return whatProvidesResReleasePkg;
    }
}
