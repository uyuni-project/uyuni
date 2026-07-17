/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.frontend.action.systems;

class SPMigrationActionParameterHolder {

    // attributes
    private boolean isMinion;
    private boolean isSuseMinion;
    private boolean isRedHatMinion;
    private String saltPackageOnMinion;
    private boolean isSaltPackageUpToDateOnMinion;
    private boolean isTradCliUpgradesViaCapabilitySupported;
    private boolean isTradCliZyppPluginInstalled;
    private boolean isTradCliUpdateStackUpdateNeeded;

    // request parameters
    private Long targetBaseProduct;
    private Long[] targetAddonProducts;
    private Long targetBaseChannel;
    private Long[] targetChildChannels;
    private boolean dryRun;
    private boolean hasDryRun;
    private boolean goBack;
    private boolean targetProductSelectedEmpty;
    private boolean allowVendorChange;

    SPMigrationActionParameterHolder() {
        isMinion = false;
        isSuseMinion = false;
        isRedHatMinion = false;
        saltPackageOnMinion = "";
        isSaltPackageUpToDateOnMinion = false;
        isTradCliUpgradesViaCapabilitySupported = false;
        isTradCliZyppPluginInstalled = false;
        isTradCliUpdateStackUpdateNeeded = false;

        targetBaseProduct = null;
        targetAddonProducts = null;
        targetBaseChannel = null;
        targetChildChannels = null;
        dryRun = false;
        hasDryRun = true;
        goBack = false;
        targetProductSelectedEmpty = false;
        allowVendorChange = false;
    }

    public boolean isMinion() {
        return isMinion;
    }

    public void setMinion(boolean minionIn) {
        isMinion = minionIn;
    }

    public boolean isSuseMinion() {
        return isSuseMinion;
    }

    public void setSuseMinion(boolean suseMinionIn) {
        isSuseMinion = suseMinionIn;
    }

    public boolean isRedHatMinion() {
        return isRedHatMinion;
    }

    public void setRedHatMinion(boolean redHatMinionIn) {
        isRedHatMinion = redHatMinionIn;
    }

    public String getSaltPackageOnMinion() {
        return saltPackageOnMinion;
    }

    public void setSaltPackageOnMinion(String saltPackageOnMinionIn) {
        saltPackageOnMinion = saltPackageOnMinionIn;
    }

    public boolean isSaltPackageUpToDateOnMinion() {
        return isSaltPackageUpToDateOnMinion;
    }

    public void setSaltPackageUpToDateOnMinion(boolean saltPackageUpToDateOnMinionIn) {
        isSaltPackageUpToDateOnMinion = saltPackageUpToDateOnMinionIn;
    }

    public boolean isTradCliUpgradesViaCapabilitySupported() {
        return isTradCliUpgradesViaCapabilitySupported;
    }

    public void setTradCliUpgradesViaCapabilitySupported(boolean tradCliUpgradesViaCapabilitySupportedIn) {
        isTradCliUpgradesViaCapabilitySupported = tradCliUpgradesViaCapabilitySupportedIn;
    }

    public boolean isTradCliZyppPluginInstalled() {
        return isTradCliZyppPluginInstalled;
    }

    public void setTradCliZyppPluginInstalled(boolean tradCliZyppPluginInstalledIn) {
        isTradCliZyppPluginInstalled = tradCliZyppPluginInstalledIn;
    }

    public boolean isTradCliUpdateStackUpdateNeeded() {
        return isTradCliUpdateStackUpdateNeeded;
    }

    public void setTradCliUpdateStackUpdateNeeded(boolean tradCliUpdateStackUpdateNeededIn) {
        isTradCliUpdateStackUpdateNeeded = tradCliUpdateStackUpdateNeededIn;
    }

    public Long getTargetBaseProduct() {
        return targetBaseProduct;
    }

    public void setTargetBaseProduct(Long targetBaseProductIn) {
        targetBaseProduct = targetBaseProductIn;
    }

    public Long[] getTargetAddonProducts() {
        return targetAddonProducts;
    }

    public void setTargetAddonProducts(Long[] targetAddonProductsIn) {
        targetAddonProducts = targetAddonProductsIn;
    }

    public Long getTargetBaseChannel() {
        return targetBaseChannel;
    }

    public void setTargetBaseChannel(Long targetBaseChannelIn) {
        targetBaseChannel = targetBaseChannelIn;
    }

    public Long[] getTargetChildChannels() {
        return targetChildChannels;
    }

    public void setTargetChildChannels(Long[] targetChildChannelsIn) {
        targetChildChannels = targetChildChannelsIn;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRunIn) {
        dryRun = dryRunIn;
    }

    public boolean isHasDryRun() {
        return hasDryRun;
    }

    public void setHasDryRun(boolean hasDryRunIn) {
        hasDryRun = hasDryRunIn;
    }

    public boolean isGoBack() {
        return goBack;
    }

    public void setGoBack(boolean goBackIn) {
        goBack = goBackIn;
    }

    public boolean isTargetProductSelectedEmpty() {
        return targetProductSelectedEmpty;
    }

    public void setTargetProductSelectedEmpty(boolean targetProductSelectedEmptyIn) {
        targetProductSelectedEmpty = targetProductSelectedEmptyIn;
    }

    public boolean isAllowVendorChange() {
        return allowVendorChange;
    }

    public void setAllowVendorChange(boolean allowVendorChangeIn) {
        allowVendorChange = allowVendorChangeIn;
    }
}
