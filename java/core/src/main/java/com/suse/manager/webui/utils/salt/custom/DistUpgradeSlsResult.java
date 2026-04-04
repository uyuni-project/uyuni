/*
 * Copyright (c) 2016--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.utils.salt.custom;

import com.suse.salt.netapi.results.Change;
import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Optional;

/**
 * Object representation of the results of a call to state.apply distupgrade.
 * Returned with salt 2016.11
 */
public class DistUpgradeSlsResult {

   @SerializedName("mgrcompat_|-spmigration_|-pkg.upgrade_|-module_run")
   private StateApplyResult<RetOpt<Map<String, Change<String>>>> spmigration;

   @SerializedName("cmd_|-spmigration_|-/usr/bin/cat /var/log/yum_sles_es_migration.log_|-run")
   private StateApplyResult<CmdResult> liberateYumEs;

   @SerializedName("cmd_|-spmigration_|-/usr/bin/cat /var/log/dnf_sles_es_migration.log_|-run")
   private StateApplyResult<CmdResult> liberateDnfEs;

   @SerializedName("cmd_|-spmigration_|-/usr/bin/cat /var/log/dnf_sll_migration.log_|-run")
   private StateApplyResult<CmdResult> liberateDnfSll;

   @SerializedName("cmd_|-spmigration_liberated_|-/usr/bin/cat /etc/sysconfig/liberated_|-run")
   private StateApplyResult<CmdResult> liberated;

   /**
    * get spmigration state apply result
    *
    * @return spmigration state apply result
    */
   public StateApplyResult<RetOpt<Map<String, Change<String>>>> getSpmigration() {
      return spmigration;
   }

   /**
    * get spmigration state apply result for liberate call
    *
    * @return spmigration state apply result for liberate
    */
   public StateApplyResult<CmdResult> getLiberate() {
       return Optional.ofNullable(liberateYumEs)
               .or(() -> Optional.ofNullable(liberateDnfEs))
               .orElse(liberateDnfSll);

   }

    /**
     * get spmiration_liberated state apply result
     * @return spmiration_liberated state apply result
     */
   public StateApplyResult<CmdResult> getLiberatedResult() {
       return liberated;
   }
}
