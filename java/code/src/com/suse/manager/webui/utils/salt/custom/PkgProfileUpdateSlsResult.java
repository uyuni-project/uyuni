/**
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

import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.calls.modules.Zypper;
import com.suse.salt.netapi.results.CmdExecCodeAllResult;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Object representation of the results of a call to state.apply packages.profileupdate.
 */
public class PkgProfileUpdateSlsResult {

   @SerializedName("module_|-products_|-pkg.list_products_|-run")
   private StateApplyResult<Ret<List<Zypper.ProductInfo>>> listProducts;

   @SerializedName("module_|-packages_|-pkg.info_installed_|-run")
   private StateApplyResult<Ret<Map<String, Pkg.Info>>> infoInstalled;

   @SerializedName("cmd_|-rhelrelease_|-cat /etc/redhat-release_|-run")
   private StateApplyResult<CmdExecCodeAllResult> rhelReleaseFile;

   @SerializedName("cmd_|-centosrelease_|-cat /etc/centos-release_|-run")
   private StateApplyResult<CmdExecCodeAllResult> centosReleaseFile;

   @SerializedName("cmd_|-respkgquery_|-rpm -q --whatprovides 'sles_es-release-server'_|-run")
   private StateApplyResult<CmdExecCodeAllResult> whatProvidesResReleasePkg;

   /**
    * @return the list of installed products
    */
   public StateApplyResult<Ret<List<Zypper.ProductInfo>>> getListProducts() {
      return listProducts;
   }

   /**
    * @return information about installed packages
    */
   public StateApplyResult<Ret<Map<String, Pkg.Info>>> getInfoInstalled() {
      return infoInstalled;
   }

   /**
    * @return the content of the file /etc/redhat-release
    */
   public StateApplyResult<CmdExecCodeAllResult> getRhelReleaseFile() {
      return rhelReleaseFile;
   }

   /**
    * @return the content of the file /etc/centos-release
    */
   public StateApplyResult<CmdExecCodeAllResult> getCentosReleaseFile() {
      return centosReleaseFile;
   }

   /**
    * @return the package that provides 'sles_es-release-server'
    */
   public StateApplyResult<CmdExecCodeAllResult> getWhatProvidesResReleasePkg() {
      return whatProvidesResReleasePkg;
   }
}
