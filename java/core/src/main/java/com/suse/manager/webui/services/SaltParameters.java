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

package com.suse.manager.webui.services;

public class SaltParameters {
    private SaltParameters() { }

    public static final String PACKAGES_PKGINSTALL = "packages.pkginstall";
    public static final String PACKAGES_PKGUPDATE = "packages.pkgupdate";
    public static final String PACKAGES_PKGDOWNLOAD = "packages.pkgdownload";
    public static final String PACKAGES_PATCHINSTALL = "packages.patchinstall";
    public static final String PACKAGES_PATCHDOWNLOAD = "packages.patchdownload";
    public static final String PACKAGES_PKGREMOVE = "packages.pkgremove";
    public static final String PACKAGES_PKGLOCK = "packages.pkglock";
    public static final String CONFIG_DEPLOY_FILES = "configuration.deploy_files";
    public static final String CONFIG_DIFF_FILES = "configuration.diff_files";
    public static final String PARAM_PKGS = "param_pkgs";
    public static final String PARAM_PATCHES = "param_patches";
    public static final String PARAM_FILES = "param_files";
    public static final String REMOTE_COMMANDS = "remotecommands";
    public static final String SYSTEM_REBOOT = "system.reboot";
    public static final String KICKSTART_INITIATE = "bootloader.autoinstall";
    public static final String ANSIBLE_RUNPLAYBOOK = "ansible.runplaybook";
    public static final String ANSIBLE_INVENTORIES = "ansible.targets";
    public static final String COCOATTEST_REQUESTDATA = "cocoattest.requestdata";
    public static final String APPSTREAMS_CONFIGURE = "appstreams.configure";
    public static final String PARAM_APPSTREAMS_ENABLE = "param_appstreams_enable";
    public static final String PARAM_APPSTREAMS_DISABLE = "param_appstreams_disable";

    /** SLS pillar parameter name for the list of update stack patch names. */
    public static final String PARAM_UPDATE_STACK_PATCHES = "param_update_stack_patches";

    /** SLS pillar parameter name for the list of regular patch names. */
    public static final String PARAM_REGULAR_PATCHES = "param_regular_patches";
    public static final String ALLOW_VENDOR_CHANGE = "allow_vendor_change";
    public static final String INVENTORY_PATH = "/etc/ansible/hosts";
}
