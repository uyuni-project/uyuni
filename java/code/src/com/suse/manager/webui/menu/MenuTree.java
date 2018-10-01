/**
 * Copyright (c) 2017 SUSE LLC
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
package com.suse.manager.webui.menu;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import com.redhat.rhn.common.security.acl.Access;
import com.redhat.rhn.common.security.acl.Acl;
import com.redhat.rhn.common.security.acl.AclFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;

import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The UI Menu Tree.
 */
public class MenuTree {
    /**
     * Generate a List of {@link MenuItem}.
     *
     * @param pageContext the PageContext object
     * @return the full menu tree as a List of {@link MenuItem}
     */
    public static List<MenuItem> getMenuTree(PageContext pageContext) {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        User user = new RequestContext(request).getCurrentUser();
        String url = request.getRequestURI();

        Map<String, Boolean> adminRoles = new HashMap<>();
        adminRoles.put("org", checkAcl(user, "user_role(org_admin)"));
        adminRoles.put("config", checkAcl(user, "user_role(config_admin)"));
        adminRoles.put("satellite", checkAcl(user, "user_role(satellite_admin)"));
        adminRoles.put("activationKey", checkAcl(user, "user_role(activation_key_admin)"));
        adminRoles.put("image", checkAcl(user, "user_role(image_admin)"));

        MenuItemList nodes = new MenuItemList();

        if (checkAcl(user, "user_authenticated()")) {
            // Home
            nodes.add(new MenuItem("Home").withIcon("fa-home")
                .addChild(new MenuItem("Overview").withPrimaryUrl("/rhn/YourRhn.do"))
                .addChild(new MenuItem("Notification Messages").withPrimaryUrl("/rhn/manager/notification-messages"))
                .addChild(new MenuItem("User Account")
                    .addChild(new MenuItem("Your Account").withPrimaryUrl("/rhn/account/UserDetails.do"))
                    .addChild(new MenuItem("Addresses")
                        .withPrimaryUrl("/rhn/account/Addresses.do").withAltUrl("/rhn/account/EditAddress.do"))
                    .addChild(new MenuItem("Change Email").withPrimaryUrl("/rhn/account/ChangeEmail.do"))
                    .addChild(new MenuItem("Account Deactivation").withPrimaryUrl("/rhn/account/AccountDeactivation.do")))
                .addChild(new MenuItem("Your Preferences").withPrimaryUrl("/rhn/account/UserPreferences.do"))
                .addChild(new MenuItem("Your Organization")
                    .addChild(new MenuItem("Configuration")
                        .withPrimaryUrl("/rhn/multiorg/OrgConfigDetails.do").withVisibility(adminRoles.get("org")))
                    .addChild(new MenuItem("Organization Trusts").withPrimaryUrl("/rhn/multiorg/Organizations.do")
                        .withAltUrl("/rhn/multiorg/OrgTrustDetails.do").withAltUrl("/rhn/multiorg/channels/Consumed.do")
                        .withAltUrl("/rhn/multiorg/channels/Provided.do").withVisibility(adminRoles.get("org")))
                    .addChild(new MenuItem("Configuration Channels").withPrimaryUrl("/rhn/manager/yourorg/custom").withVisibility(adminRoles.get("org")))
                    .withVisibility(adminRoles.get("org"))));

            // Systems
            nodes.add(new MenuItem("Systems").withIcon("fa-desktop").withDir("/rhn/systems/details").withDir("/rhn/manager/systems/details")
                .addChild(new MenuItem("Overview").withPrimaryUrl("/rhn/systems/Overview.do"))
                .addChild(new MenuItem("System List").addChild(new MenuItem("All").withPrimaryUrl("/rhn/systems/SystemList.do"))
                    .addChild(new MenuItem("Physical Systems").withPrimaryUrl("/rhn/systems/PhysicalList.do"))
                    .addChild(new MenuItem("Virtual Systems").withPrimaryUrl("/rhn/systems/VirtualList.do"))
                    .addChild(new MenuItem("Bare Metal Systems").withPrimaryUrl("/rhn/systems/BootstrapSystemList.do"))
                    .addChild(new MenuItem("Out of Date").withPrimaryUrl("/rhn/systems/OutOfDate.do"))
                    .addChild(new MenuItem("Requiring Reboot").withPrimaryUrl("/rhn/systems/RequiringReboot.do"))
                    .addChild(new MenuItem("Extra Packages").withPrimaryUrl("/rhn/systems/ExtraPackagesSystems.do"))
                    .addChild(new MenuItem("Unentitled").withPrimaryUrl("/rhn/systems/Unentitled.do"))
                    .addChild(new MenuItem("Ungrouped").withPrimaryUrl("/rhn/systems/Ungrouped.do").withVisibility(adminRoles.get("org")))
                    .addChild(new MenuItem("Inactive").withPrimaryUrl("/rhn/systems/Inactive.do"))
                    .addChild(new MenuItem("Recently Registered").withPrimaryUrl("/rhn/systems/Registered.do"))
                    .addChild(new MenuItem("Proxy").withPrimaryUrl("/rhn/systems/ProxyList.do")
                        .withVisibility(checkAcl(user, "org_channel_family(SMP) or not is_satellite()") && adminRoles.get("org")))
                    .addChild(new MenuItem("Duplicate Systems").withPrimaryUrl("/rhn/systems/DuplicateIPList.do")
                        .withAltUrl("/rhn/systems/DuplicateIPv6List.do").withAltUrl("/rhn/systems/DuplicateHostName.do")
                        .withAltUrl("/rhn/systems/DuplicateMacAddress.do").withAltUrl("/rhn/systems/DuplicateSystemsCompare.do")
                        .withAltUrl("/rhn/systems/DuplicateSystemsDeleteConfirm.do"))
                    .addChild(new MenuItem("System Currency").withPrimaryUrl("/rhn/systems/SystemCurrency.do"))
                    .addChild(new MenuItem("System Entitlements").withPrimaryUrl("/rhn/systems/SystemEntitlements.do")
                        .withVisibility(adminRoles.get("org"))))
                .addChild(new MenuItem("System Groups").withPrimaryUrl("/rhn/systems/SystemGroupList.do").withDir("/rhn/groups")
                    .withDir("/rhn/systems/groups").withDir("/rhn/manager/groups"))
                .addChild(new MenuItem("System Set Manager").withDir("/rhn/systems/ssm").withDir("/rhn/ssm")
                        .withDir("/rhn/channel/ssm").withDir("/rhn/manager/systems/ssm")
                    .addChild(new MenuItem("Overview").withPrimaryUrl("/rhn/ssm/index.do"))
                    .addChild(new MenuItem("ssm.nav.status").withPrimaryUrl("/rhn/ssm/ViewAllLog.do")
                        .withAltUrl("/rhn/ssm/ViewLog.do").withAltUrl("/rhn/ssm/ViewCompletedLog.do")))
                .addChild(new MenuItem("Bootstrapping").withPrimaryUrl("/rhn/manager/systems/bootstrap")
                    .withVisibility(adminRoles.get("org")))
                .addChild(new MenuItem("visualization.nav.title")
                        .withVisibility(adminRoles.get("org"))
                        .addChild(new MenuItem("Virtualization Hierarchy")
                                .withPrimaryUrl("/rhn/manager/visualization/virtualization-hierarchy")
                                .withVisibility(adminRoles.get("org")))
                        .addChild(new MenuItem("Proxy Hierarchy")
                                .withPrimaryUrl("/rhn/manager/visualization/proxy-hierarchy")
                                .withVisibility(adminRoles.get("org")))
                        .addChild(new MenuItem("Systems Grouping")
                                .withPrimaryUrl("/rhn/manager/visualization/systems-with-managed-groups")
                                .withVisibility(adminRoles.get("org"))))
                .addChild(new MenuItem("Advanced Search").withPrimaryUrl("/rhn/systems/Search.do"))
                .addChild(new MenuItem("Activation Keys").withPrimaryUrl("/rhn/activationkeys/List.do")
                    .withAltUrl("/rhn/activationkeys/Create.do").withAltUrl("/rhn/activationkeys/Edit.do")
                    .withAltUrl("/rhn/activationkeys/Delete.do").withAltUrl("/rhn/activationkeys/Clone.do")
                    .withDir("/rhn/activationkeys/channels").withDir("/rhn/activationkeys/configuration")
                    .withDir("/rhn/activationkeys/groups").withDir("/rhn/activationkeys/packages")
                    .withDir("/rhn/activationkeys/systems").withVisibility(adminRoles.get("activationKey")))
                .addChild(new MenuItem("Stored Profiles").withPrimaryUrl("/rhn/profiles/List.do").withDir("/rhn/profiles"))
                .addChild(new MenuItem("Custom System Info").withPrimaryUrl("/rhn/systems/customdata/CustomDataList.do")
                    .withDir("/rhn/systems/customdata"))
                .addChild(new MenuItem("Kickstart")
                    .addChild(new MenuItem("Overview").withPrimaryUrl("/rhn/kickstart/KickstartOverview.do")
                        .withVisibility(adminRoles.get("config")))
                    .addChild(new MenuItem("Profiles").withPrimaryUrl("/rhn/kickstart/Kickstarts.do").withDir("/rhn/kickstart")
                        .withVisibility(adminRoles.get("config")))
                    .addChild(new MenuItem("Bare Metal").withPrimaryUrl("/rhn/kickstart/KickstartIpRanges.do")
                        .withVisibility(adminRoles.get("config")))
                    .addChild(new MenuItem("GPG and SSL Keys").withPrimaryUrl("/rhn/keys/CryptoKeysList.do").withDir("/rhn/keys")
                        .withVisibility(adminRoles.get("config")))
                    .addChild(new MenuItem("Distributions").withPrimaryUrl("/rhn/kickstart/ViewTrees.do")
                        .withAltUrl("/rhn/kickstart/TreeEdit.do")
                        .withAltUrl("/rhn/kickstart/TreeCreate.do").withAltUrl("/rhn/kickstart/TreeDelete.do")
                        .withAltUrl("/rhn/kickstart/tree/EditVariables.do").withVisibility(adminRoles.get("config")))
                    .addChild(new MenuItem("File Preservation")
                        .withPrimaryUrl("/rhn/systems/provisioning/preservation/PreservationList.do")
                        .withAltUrl("/rhn/systems/provisioning/preservation/PreservationListEdit.do")
                        .withAltUrl("/rhn/systems/provisioning/preservation/PreservationListCreate.do")
                        .withAltUrl("/rhn/systems/provisioning/preservation/PreservationListDeleteSubmit.do")
                        .withAltUrl("/rhn/systems/provisioning/preservation/PreservationListDelete.do")
                        .withVisibility(adminRoles.get("config")))
                    .addChild(new MenuItem("snippets.jsp.toolbar")
                        .withPrimaryUrl("/rhn/kickstart/cobbler/CustomSnippetList.do")
                        .withAltUrl("/rhn/kickstart/cobbler/DefaultSnippetList.do")
                        .withAltUrl("/rhn/kickstart/cobbler/CobblerSnippetList.do")
                        .withAltUrl("/rhn/kickstart/cobbler/CustomSnippetList.do")
                        .withAltUrl("/rhn/kickstart/cobbler/CobblerSnippetEdit.do")
                        .withAltUrl("/rhn/kickstart/cobbler/CobblerSnippetCreate.do")
                        .withAltUrl("/rhn/kickstart/cobbler/CobblerSnippetDelete.do")
                        .withAltUrl("/rhn/kickstart/cobbler/CobblerSnippetView.do")
                        .withAltUrl("/rhn/kickstart/cobbler/DefaultSnippetView.do")
                        .withVisibility(adminRoles.get("config")))
                    .withVisibility(adminRoles.get("config")))
                .addChild(new MenuItem("software.crashes").withPrimaryUrl("/rhn/systems/SoftwareCrashesOverview.do")
                    .withAltUrl("/rhn/systems/SoftwareCrashUuidDetails.do"))
                .addChild(new MenuItem("Virtual Host Managers").withPrimaryUrl("/rhn/manager/vhms")
                    .withDir("/rhn/manager/vhms")
                    .withVisibility(adminRoles.get("org"))));

            // Salt
            nodes.add(new MenuItem("Salt").withIcon("spacewalk-icon-salt")
                .addChild(new MenuItem("Keys").withPrimaryUrl("/rhn/manager/systems/keys"))
                .addChild(new MenuItem("Remote Commands").withPrimaryUrl("/rhn/manager/systems/cmd"))
                .addChild(new MenuItem("Formula Catalog").withPrimaryUrl("/rhn/manager/formula-catalog")
                    .withVisibility(adminRoles.get("org") && checkAcl(user, "salt_formulas_installed()"))));

            // Images
            nodes.add(new MenuItem("Images").withIcon("spacewalk-icon-manage-configuration-files")
                .addChild(new MenuItem("Image List").withPrimaryUrl("/rhn/manager/cm/images")
                    .withDir("/rhn/manager/cm/images"))
                .addChild(new MenuItem("Build").withPrimaryUrl("/rhn/manager/cm/build")
                    .withDir("/rhn/manager/cm/build").withVisibility(adminRoles.get("image")))
                .addChild(new MenuItem("Profiles").withPrimaryUrl("/rhn/manager/cm/imageprofiles")
                    .withDir("/rhn/manager/cm/imageprofiles"))
                .addChild(new MenuItem("Stores").withPrimaryUrl("/rhn/manager/cm/imagestores")
                    .withDir("/rhn/manager/cm/imagestores")));

            // Patches
            nodes.add(new MenuItem("Patches").withIcon("spacewalk-icon-patches")
                .addChild(new MenuItem("Patch List").withPrimaryUrl("/rhn/errata/RelevantErrata.do").withDir("/rhn/errata")
                    .addChild(new MenuItem("Relevant").withPrimaryUrl("/rhn/errata/RelevantErrata.do")
                        .withAltUrl("/rhn/errata/RelevantBugErrata.do").withAltUrl("/rhn/errata/RelevantEnhancementErrata.do")
                        .withAltUrl("/rhn/errata/RelevantSecurityErrata.do"))
                    .addChild(new MenuItem("All").withPrimaryUrl("/rhn/errata/AllErrata.do").withAltUrl("/rhn/errata/AllBugErrata.do")
                        .withAltUrl("/rhn/errata/AllEnhancementErrata.do").withAltUrl("/rhn/errata/AllSecurityErrata.do")))
                .addChild(new MenuItem("Advanced Search").withPrimaryUrl("/rhn/errata/Search.do"))
                .addChild(
                    new MenuItem("Manage Errata").withVisibility(checkAcl(user, "user_role(channel_admin)")).withDir("/rhn/errata/manage")
                        .addChild(new MenuItem("Published").withPrimaryUrl("/rhn/errata/manage/PublishedErrata.do")
                            .withVisibility(checkAcl(user, "user_role(channel_admin)")))
                        .addChild(new MenuItem("Unpublished").withPrimaryUrl("/rhn/errata/manage/UnpublishedErrata.do")
                            .withVisibility(checkAcl(user, "user_role(channel_admin)"))))
                .addChild(new MenuItem("Clone Errata").withPrimaryUrl("/rhn/errata/manage/CloneErrata.do")
                    .withDir("/rhn/errata/manage/clone").withVisibility(checkAcl(user, "user_role(channel_admin)"))));

            // (Software) Channels
            nodes.add(new MenuItem("Software").withIcon("spacewalk-icon-software-channels")
                .addChild(new MenuItem("Channel List").withDir("/rhn/channels")
                    .addChild(new MenuItem("channel.nav.all").withPrimaryUrl("/rhn/software/channels/All.do"))
                    .addChild(new MenuItem("channel.nav.vendor").withPrimaryUrl("/rhn/software/channels/Vendor.do")
                        .withVisibility(checkAcl(user, "is_satellite()")))
                    .addChild(new MenuItem("channel.nav.popular").withPrimaryUrl("/rhn/software/channels/Popular.do"))
                    .addChild(new MenuItem("channel.nav.custom").withPrimaryUrl("/rhn/software/channels/Custom.do"))
                    .addChild(new MenuItem("channel.nav.shared").withPrimaryUrl("/rhn/software/channels/Shared.do"))
                    .addChild(new MenuItem("channel.nav.retired").withPrimaryUrl("/rhn/software/channels/Retired.do")))
                .addChild(
                    new MenuItem("Package Search").withPrimaryUrl("/rhn/channels/software/Search.do").withDir("/rhn/software/packages"))
                .addChild(new MenuItem("Manage").withDir("/rhn/channels/manage")
                    .addChild(new MenuItem("Channels").withPrimaryUrl("/rhn/channels/manage/Manage.do")
                        .withAltUrl("/rhn/channels/manage/errata/Add.do").withAltUrl("/rhn/channels/manage/errata/AddRedHatErrata.do")
                        .withAltUrl("/rhn/channels/manage/errata/AddCustomErrata.do")
                        .withAltUrl("/rhn/channels/manage/errata/ConfirmErrataAdd.do"))
                    .addChild(new MenuItem("Packages").withPrimaryUrl("/rhn/software/manage/packages/PackageList.do")
                        .withDir("/rhn/software/manage/packages").withVisibility(checkAcl(user, "user_role(channel_admin)")))
                    .addChild(new MenuItem("Repositories").withPrimaryUrl("/rhn/channels/manage/repos/RepoList.do")
                        .withAltUrl("/rhn/channels/manage/repos/RepoEdit.do").withAltUrl("/rhn/channels/manage/repos/RepoCreate.do")
                        .withDir("/rhn/channels/manage/repos").withVisibility(checkAcl(user, "user_role(channel_admin)"))))
                .addChild(new MenuItem("Distribution Channel Mapping").withPrimaryUrl("/rhn/channels/manage/DistChannelMap.do")
                    .withAltUrl("/rhn/channels/manage/DistChannelMapEdit.do").withAltUrl("/rhn/channels/manage/DistChannelMapDelete.do")
                    .withVisibility(adminRoles.get("org"))));

            // Audit
            nodes.add(new MenuItem("Audit").withIcon("fa-search").withDir("/rhn/audit")
                .addChild(new MenuItem("cveaudit.nav.title").withPrimaryUrl("/rhn/manager/audit/cve"))
                .addChild(new MenuItem("subscriptionmatching.nav.title").withPrimaryUrl("/rhn/manager/subscription-matching")
                    .withVisibility(adminRoles.get("satellite")))
                .addChild(new MenuItem("OpenSCAP").addChild(new MenuItem("All Scans").withPrimaryUrl("/rhn/audit/ListXccdf.do"))
                    .addChild(
                        new MenuItem("XCCDF Diff").withPrimaryUrl("/rhn/audit/scap/Diff.do").withAltUrl("/rhn/audit/scap/DiffSubmit.do"))
                    .addChild(new MenuItem("Advanced Search").withPrimaryUrl("/rhn/audit/scap/Search.do"))
                    .addChild(new MenuItem("audit.nav.logreview").withVisibility(checkAcl(user, "not is_satellite()"))
                        .addChild(new MenuItem("Overview").withPrimaryUrl("/rhn/audit/Overview.do"))
                        .addChild(new MenuItem("Reviews").withPrimaryUrl("/rhn/audit/Machine.do"))
                        .addChild(new MenuItem("Search").withPrimaryUrl("/rhn/audit/Search.do")))));

            // Configuration
            nodes.add(new MenuItem("config.nav.config").withIcon("spacewalk-icon-software-channel-management")
                .withVisibility(adminRoles.get("config"))
                .withDir("/rhn/configuration")
                .addChild(new MenuItem("common.nav.overview").withPrimaryUrl("/rhn/configuration/Overview.do")
                    .withVisibility(adminRoles.get("config")))
                .addChild(new MenuItem("config.nav.channels").withPrimaryUrl("/rhn/configuration/GlobalConfigChannelList.do")
                    .withVisibility(adminRoles.get("config")))
                .addChild(new MenuItem("config.nav.files").withDir("/rhn/configuration/file")
                    .withVisibility(adminRoles.get("config"))
                    .addChild(new MenuItem("config.nav.globalfiles").withPrimaryUrl("/rhn/configuration/file/GlobalConfigFileList.do")
                        .withVisibility(adminRoles.get("config")))
                    .addChild(new MenuItem("config.nav.localfiles").withPrimaryUrl("/rhn/configuration/file/LocalConfigFileList.do")
                        .withVisibility(adminRoles.get("config")))
                    )
                .addChild(new MenuItem("common.nav.systems").withDir("/rhn/configuration/system")
                    .addChild(new MenuItem("config.nav.managed").withPrimaryUrl("/rhn/configuration/system/ManagedSystems.do"))
                    .addChild(new MenuItem("config.nav.target").withPrimaryUrl("/rhn/configuration/system/TargetSystems.do")
                        .withAltUrl("/rhn/configuration/system/TargetSystemsSubmit.do")
                        .withAltUrl("/rhn/configuration/system/EnableSystemsConfirm.do")
                        .withAltUrl("/rhn/configuration/system/EnableSystemsConfirmSubmit.do")
                        .withAltUrl("/rhn/configuration/system/Summary.do")))
                );

            // Schedule
            nodes.add(new MenuItem("Schedule").withIcon("fa-clock-o").withDir("/rhn/schedule")
                .addChild(new MenuItem("Pending Actions").withPrimaryUrl("/rhn/schedule/PendingActions.do"))
                .addChild(new MenuItem("Failed Actions").withPrimaryUrl("/rhn/schedule/FailedActions.do"))
                .addChild(new MenuItem("Completed Actions").withPrimaryUrl("/rhn/schedule/CompletedActions.do"))
                .addChild(new MenuItem("Archived Actions").withPrimaryUrl("/rhn/schedule/ArchivedActions.do"))
                .addChild(new MenuItem("Action Chains").withPrimaryUrl("/rhn/schedule/ActionChains.do")
                    .withAltUrl("/rhn/schedule/ActionChain.do"))
                );

            // Users
            nodes.add(new MenuItem("Users").withIcon("fa-users").withVisibility(adminRoles.get("org"))
                .addChild(new MenuItem("User List")
                    .withDir("/rhn/users")
                    .withVisibility(adminRoles.get("org"))
                    .addChild(new MenuItem("users.nav.active").withPrimaryUrl("/rhn/users/ActiveList.do")
                        .withVisibility(adminRoles.get("org")))
                    .addChild(new MenuItem("users.nav.disabled").withPrimaryUrl("/rhn/users/DisabledList.do")
                        .withVisibility(adminRoles.get("org")))
                    .addChild(new MenuItem("users.nav.all").withPrimaryUrl("/rhn/users/UserList.do")
                        .withVisibility(adminRoles.get("org"))))
                .addChild(new MenuItem("System Group Configuration")
                    .withPrimaryUrl("/rhn/users/SystemGroupConfig.do")
                    .withAltUrl("/rhn/users/ExtAuthSgMapping.do")
                    .withAltUrl("/rhn/users/ExtAuthSgDetails.do")
                    .withAltUrl("/rhn/users/ExtAuthSgDelete.do")
                    .withVisibility(adminRoles.get("org")))
                );

            // Admin
            nodes.add(new MenuItem("Admin").withIcon("fa-user").withVisibility(adminRoles.get("satellite"))
                .addChild(new MenuItem("Setup Wizard")
                    .withPrimaryUrl("/rhn/admin/setup/ProxySettings.do")
                    .withAltUrl("/rhn/admin/setup/MirrorCredentials.do")
                    .withAltUrl("/rhn/manager/admin/setup/products")
                    .withVisibility(adminRoles.get("satellite")))
                .addChild(new MenuItem("Organizations")
                    .withPrimaryUrl("/rhn/admin/multiorg/Organizations.do")
                    .withAltUrl("/rhn/admin/multiorg/OrgDetails.do")
                    .withAltUrl("/rhn/admin/multiorg/OrgConfigDetails.do")
                    .withAltUrl("/rhn/admin/multiorg/OrgUsers.do")
                    .withAltUrl("/rhn/admin/multiorg/OrgTrusts.do")
                    .withAltUrl("/rhn/admin/multiorg/DeleteOrg.do")
                    .withAltUrl("/rhn/admin/multiorg/OrgCreate.do")
                    .withAltUrl("/rhn/manager/multiorg/details/custom")
                    .withVisibility(adminRoles.get("satellite")))
                .addChild(new MenuItem("Users")
                    .withPrimaryUrl("/rhn/admin/multiorg/Users.do")
                    .withAltUrl("/rhn/admin/multiorg/SatRoleConfirm.do")
                    .withVisibility(adminRoles.get("satellite")))
                .addChild(new MenuItem("Spacewalk Configuration")
                    .withVisibility(adminRoles.get("satellite"))
                    .addChild(new MenuItem("General").withPrimaryUrl("/rhn/admin/config/GeneralConfig.do")
                        .withVisibility(adminRoles.get("satellite")))
                    .addChild(new MenuItem("Bootstrap Script").withPrimaryUrl("/rhn/admin/config/BootstrapConfig.do")
                        .withVisibility(adminRoles.get("satellite")))
                    .addChild(new MenuItem("Organizations").withPrimaryUrl("/rhn/admin/config/Orgs.do")
                        .withVisibility(adminRoles.get("satellite")))
                    .addChild(new MenuItem("Restart").withPrimaryUrl("/rhn/admin/config/Restart.do")
                        .withVisibility(adminRoles.get("satellite")))
                    .addChild(new MenuItem("Cobbler").withPrimaryUrl("/rhn/admin/config/Cobbler.do")
                        .withVisibility(adminRoles.get("satellite")))
                    .addChild(new MenuItem("Bare-metal systems").withPrimaryUrl("/rhn/admin/config/BootstrapSystems.do")
                        .withVisibility(adminRoles.get("satellite"))))
                .addChild(new MenuItem("ISS Configuration")
                    .withVisibility(adminRoles.get("satellite"))
                    .addChild(new MenuItem("Master Setup").withPrimaryUrl("/rhn/admin/iss/Master.do")
                        .withVisibility(adminRoles.get("satellite")))
                    .addChild(new MenuItem("Slave Setup").withPrimaryUrl("/rhn/admin/iss/Slave.do")
                        .withVisibility(adminRoles.get("satellite"))))
                .addChild(new MenuItem("Task Schedules")
                    .withPrimaryUrl("/rhn/admin/SatSchedules.do")
                    .withAltUrl("/rhn/admin/BunchDetail.do")
                    .withAltUrl("/rhn/admin/ScheduleDetail.do")
                    .withAltUrl("/rhn/admin/DeleteSchedule.do")
                    .withVisibility(adminRoles.get("satellite")))
                .addChild(new MenuItem("Task Engine Status").withPrimaryUrl("/rhn/admin/TaskStatus.do")
                    .withAltUrl("/rhn/manager/admin/runtime-status")
                    .withVisibility(adminRoles.get("satellite")))
                .addChild(new MenuItem("Show Tomcat Logs").withPrimaryUrl("/rhn/admin/Catalina.do")
                    .withVisibility(adminRoles.get("satellite")))
                );

            // Help
            nodes.add(new MenuItem("Help").withIcon("fa-book")
                .addChild(new MenuItem("Overview").withPrimaryUrl("/rhn/help/index.do")
                    .withDir("/rhn/help"))
                .addChild(new MenuItem("Getting Started Guide").withPrimaryUrl("/rhn/help/dispatcher/getting_started_guide")
                    .withDir("/rhn/help/getting-started"))
                .addChild(new MenuItem("Reference Guide").withPrimaryUrl("/rhn/help/dispatcher/reference_guide")
                    .withDir("/rhn/help/reference"))
                .addChild(new MenuItem("Best Practices Guide").withPrimaryUrl("/rhn/help/dispatcher/best_practices_guide")
                    .withDir("/rhn/help/best-practices"))
                .addChild(new MenuItem("Advanced Topics Guide").withPrimaryUrl("/rhn/help/dispatcher/advanced_topics_guide")
                    .withDir("/rhn/help/advanced-topics"))
                .addChild(new MenuItem("Release Notes").withPrimaryUrl("/rhn/help/dispatcher/release_notes")
                    .withDir("/rhn/help/release-notes/manager"))
                .addChild(new MenuItem("API")
                    .addChild(new MenuItem("Overview").withPrimaryUrl("/rhn/apidoc/index.jsp")
                        .withDir("/rhn/apidoc"))
                    .addChild(new MenuItem("FAQ").withPrimaryUrl("/rhn/apidoc/faqs.jsp"))
                    .addChild(new MenuItem("Sample Scripts").withPrimaryUrl("/rhn/apidoc/scripts.jsp"))
                    )
                .addChild(new MenuItem("Search").withPrimaryUrl("/rhn/help/Search.do"))
                );

            // External Links
            nodes.add(new MenuItem("External Links").withIcon("fa-link")
                .addChild(new MenuItem("header.jsp.knowledgebase")
                    .withPrimaryUrl("https://www.suse.com/support/kb/product.php?id=SUSE_Manager")
                    .withTarget("blank"))
                .addChild(new MenuItem("header.jsp.documentation")
                    .withPrimaryUrl("https://www.suse.com/documentation/suse_manager/")
                    .withTarget("blank"))
                );
        }
        else {
            // Create First User
            if (checkAcl(user, "need_first_user()")) {
                nodes.add(new MenuItem("Create First User").withPrimaryUrl("/rhn/newlogin/CreateFirstUser.do"));
            }

            // About Spacewalk
            nodes.add(new MenuItem("About Spacewalk").withIcon("fa-question-circle")
                .addChild(new MenuItem("Overview").withPrimaryUrl("/rhn/help/about.do"))
                .addChild(new MenuItem("Sign In").withPrimaryUrl("/rhn/Login.do"))
                .addChild(new MenuItem("Help Desk").withPrimaryUrl("/rhn/help/index.do"))
                .addChild(new MenuItem("Lookup Login/Password").withPrimaryUrl("/rhn/help/ForgotCredentials.do"))
                .addChild(new MenuItem("Getting Started Guide").withPrimaryUrl("/rhn/help/dispatcher/getting_started_guide")
                    .withDir("/rhn/help/getting-started"))
                .addChild(new MenuItem("Reference Guide").withPrimaryUrl("/rhn/help/dispatcher/reference_guide")
                    .withDir("/rhn/help/reference"))
                .addChild(new MenuItem("Best Practices Guide").withPrimaryUrl("/rhn/help/dispatcher/best_practices_guide")
                    .withDir("/rhn/help/best-practices"))
                .addChild(new MenuItem("Advanced Topics Guide").withPrimaryUrl("/rhn/help/dispatcher/advanced_topics_guide")
                    .withDir("/rhn/help/advanced-topics"))
                .addChild(new MenuItem("Release Notes").withPrimaryUrl("/rhn/help/dispatcher/release_notes")
                    .withDir("/rhn/help/release-notes/manager"))
                .addChild(new MenuItem("API")
                    .addChild(new MenuItem("Overview").withPrimaryUrl("/rhn/apidoc/index.jsp")
                        .withDir("/rhn/apidoc"))
                    .addChild(new MenuItem("FAQ").withPrimaryUrl("/rhn/apidoc/faqs.jsp"))
                    .addChild(new MenuItem("Sample Scripts").withPrimaryUrl("/rhn/apidoc/scripts.jsp"))
                    )
                .addChild(new MenuItem("Search").withPrimaryUrl("/rhn/help/Search.do"))
                );

            // External Links
            nodes.add(new MenuItem("External Links").withIcon("fa-link")
                .addChild(new MenuItem("header.jsp.knowledgebase")
                    .withPrimaryUrl("https://www.suse.com/support/kb/product.php?id=SUSE_Manager")
                    .withTarget("blank"))
                .addChild(new MenuItem("header.jsp.documentation")
                    .withPrimaryUrl("https://www.suse.com/documentation/suse_manager/")
                    .withTarget("blank"))
                );
        }

        if (getActiveNode(nodes, url) == null) {
            getBestActiveDirs(nodes, url);
        }
        return nodes;
    }

    /**
     * Evaluate acl conditions for the current {@link User}
     *
     * @param user the current User
     * @param aclMixin acls to evaluate
     * @return the acl evaluated result
     */
    public static boolean checkAcl(User user, String aclMixin) {
        Map<String, Object> aclContext = new HashMap<>();
        aclContext.put("user", user);
        Acl acl = AclFactory.getInstance().getAcl(Access.class.getName());
        return acl.evalAcl(aclContext, aclMixin);
    }

    /**
     * Decode which is the active {@link MenuItem} from the current URL
     * based on the list of urls of the link
     *
     * @param nodes the list of nodes of the menu
     * @param url the current URL
     * @return the current active {@link MenuItem}
     */
    public static MenuItem getActiveNode(List<MenuItem> nodes, String url) {
        MenuItem activeItem = null;
        for (MenuItem item : nodes) {
            if (item.getSubmenu() != null) {
                // recursive call to iterate in the submenu
                activeItem = getActiveNode(item.getSubmenu(), url);
            }
            else {
                if (url.equalsIgnoreCase(item.getPrimaryUrl())) {
                    activeItem = item;
                    return activeItem;
                }
                if (item.getUrls() != null && item.getUrls().size() > 0) {
                    for (String link : item.getUrls()) {
                        if (url.equalsIgnoreCase(link)) {
                            activeItem = item;
                            return activeItem;
                        }
                    }
                }
            }

            if (activeItem != null) {
                activeItem.setActive(true);
                item.setActive(true);
                return activeItem;
            }
        }
        return null;
    }

    /**
     * Decode which is the active {@link MenuItem} from the current URL
     * based on the list of directories of the link
     *
     * @param nodes the list of nodes of the menu
     * @param url the current URL
     * @return the current active {@link MenuItem}
     */
    public static List<MenuItem> getBestActiveDirs(List<MenuItem> nodes, String url) {
        Integer depthPrecision = 0;
        List<MenuItem> bestActiveItems = new LinkedList<>();
        for (MenuItem item1 : nodes) {
            for (String dir : item1.getDirectories()) {
                if (url.contains(dir) && dir.length() > depthPrecision) {
                    bestActiveItems = new LinkedList<>();
                    bestActiveItems.add(item1);
                    depthPrecision = dir.length();
                }
            }
            if (item1.getSubmenu() != null) {
                for (MenuItem item2 : item1.getSubmenu()) {
                    for (String dir2 : item2.getDirectories()) {
                        if (url.contains(dir2) && dir2.length() > depthPrecision) {
                            bestActiveItems = new LinkedList<>();
                            bestActiveItems.add(item1);
                            bestActiveItems.add(item2);
                            depthPrecision = dir2.length();
                        }
                    }
                    if (item2.getSubmenu() != null) {
                        for (MenuItem item3 : item2.getSubmenu()) {
                            for (String dir3 : item3.getDirectories()) {
                                if (url.contains(dir3) && dir3.length() > depthPrecision) {
                                    bestActiveItems = new LinkedList<>();
                                    bestActiveItems.add(item1);
                                    bestActiveItems.add(item2);
                                    bestActiveItems.add(item3);
                                    depthPrecision = dir3.length();
                                }
                            }
                        }
                    }
                }
            }
        }

        for (MenuItem menuItem : bestActiveItems) {
            menuItem.setActive(true);
        }
        return bestActiveItems;
    }

    /**
     * Calculate the web page title on the active flagged MenuItems
     *
     * @param pageContext the context of the request
     * @return the page title
     */
    public static String getTitlePage(PageContext pageContext) {
        String title = "";
        Optional<MenuItem> activeItem = getMenuTree(pageContext).stream()
                .filter(node -> node.getActive()).findFirst();
        while (activeItem.isPresent()) {
          title += " - " + activeItem.get().getLabel();
          activeItem = activeItem.get().getSubmenu() == null ?
                  Optional.empty() :
                  activeItem.get().getSubmenu().stream()
                      .filter(node -> node.getActive()).findFirst();
        }
        return title;
    }

    /**
     * Convert the {@link MenuTree} to a JSON String
     *
     * @param pageContext the current PageContext
     * @return the JSON String as for output
     */
    public static String getJsonMenu(PageContext pageContext) {
        return new GsonBuilder().create().toJson(getMenuTree(pageContext));
    }

    /**
     * Special implementation of a LinkedList that adds only visible MenuItems.
     */
    public static class MenuItemList extends LinkedList<MenuItem> {

        @Override
        public boolean add(MenuItem e) {
            return e.getIsVisible() ? super.add(e) : false;
        }
    }
}
