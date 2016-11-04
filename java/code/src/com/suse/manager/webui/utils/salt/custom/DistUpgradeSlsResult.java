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

import com.google.gson.annotations.SerializedName;
import com.suse.manager.webui.utils.salt.ModuleRun;
import com.suse.manager.webui.utils.salt.OldNew;
import com.suse.salt.netapi.results.StateApplyResult;

import java.util.Map;

/**
 * Object representation of the results of a call to state.apply distupgrade.
 */
public class DistUpgradeSlsResult {

   @SerializedName("module_|-spmigration_|-pkg.upgrade_|-run")
   private StateApplyResult<RetOpt<ModuleRun<Map<String, OldNew>>>> spmigration;

   /**
    * constructor
    *
    * @param spm spmigration state apply result
    */
   public DistUpgradeSlsResult(StateApplyResult<RetOpt<ModuleRun<Map<String, OldNew>>>> spm) {
      this.spmigration = spm;
   }

   /**
    * get spmigration state apply result
    *
    * @return spmigration state apply result
    */
   public StateApplyResult<RetOpt<ModuleRun<Map<String, OldNew>>>> getSpmigration() {
      return spmigration;
   }
}
