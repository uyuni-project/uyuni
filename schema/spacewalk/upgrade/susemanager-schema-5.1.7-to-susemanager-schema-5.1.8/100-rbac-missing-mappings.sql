--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--

-- New namespace definitions

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'admin.config', 'R', 'View configuration and setup parameters'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'admin.config' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'admin.config', 'W', 'Modify configuration and setup parameters'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'admin.config' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.runtime', 'R', 'View container runtime status'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.runtime' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.runtime', 'W', 'Modify container runtime definitions'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.runtime' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.proxy', 'W', 'Modify configuration of a proxy system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.proxy' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.appstreams', 'R', 'View AppStream modules assigned to an activation key'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.appstreams' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.appstreams', 'W', 'Modify AppStream modules assigned to an activation key'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.appstreams' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.add_apply_highstate', 'W', 'Adds an action to apply highstate on the system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.add_apply_highstate' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_vendor_repo_filters', 'W', 'Lists the filters for a vendor repo'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_vendor_repo_filters' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.clear_vendor_repo_filters', 'W', 'Clears the filters for a repo'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.clear_vendor_repo_filters' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.set_vendor_repo_filters', 'W', 'Replaces the existing set of filters for a given repo'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.set_vendor_repo_filters' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.remove_vendor_repo_filter', 'W', 'Removes a filter from a given vendor repo'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.remove_vendor_repo_filter' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.add_vendor_repo_filter', 'W', 'Adds a filter to a given vendor repo'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.add_vendor_repo_filter' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.proxy.bootstrap_proxy', 'W', 'Deploy a proxy container on given Salt minion'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.proxy.bootstrap_proxy' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.recurring.playbook.create', 'W', 'Create a new recurring playbook action'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.recurring.playbook.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.recurring.playbook.update', 'W', 'Update a recurring Ansible playbook action'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.recurring.playbook.update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.has_traditional_systems', 'R', 'Returns whether there are traditional systems registered'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.has_traditional_systems' AND access_mode = 'R');


-- New endpoints

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/cm/rebuild/:id', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/cm/rebuild/:id' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/cm/clusters', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/cm/clusters' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/cm/runtime/:clusterId', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/cm/runtime/:clusterId' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/cm/runtime/:clusterId/:id', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/cm/runtime/:clusterId/:id' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/cm/runtime/details/:clusterId/:id', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/cm/runtime/details/:clusterId/:id' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/maintenance/schedule/:id/assign', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/maintenance/schedule/:id/assign' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/maintenance/calendar/refresh', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/maintenance/calendar/refresh' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/activationkeys/appstreams', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/activationkeys/appstreams' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/activationkeys/appstreams/save', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/activationkeys/appstreams/save' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/vhms/kubeconfig/validate', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/vhms/kubeconfig/validate' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/vhms/kubeconfig/:id/contexts', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/vhms/kubeconfig/:id/contexts' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/vhms/create/kubernetes', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/vhms/create/kubernetes' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/vhms/update/kubernetes', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/vhms/update/kubernetes' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/download/saltssh/pubkey', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/download/saltssh/pubkey' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/saltboot/*', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/saltboot/*' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/storybook', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/storybook' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/taskomatic/invoke', 'POST', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/taskomatic/invoke' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/proxy', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/proxy' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/ajax/save-proxy-settings', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/ajax/save-proxy-settings' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/ajax/verify-proxy-settings', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/ajax/verify-proxy-settings' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/ajax/render-mirror-credentials', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/ajax/render-mirror-credentials' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/ajax/verify-mirror-credentials', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/ajax/verify-mirror-credentials' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/ajax/save-mirror-credentials', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/ajax/save-mirror-credentials' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/ajax/delete-mirror-credentials', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/ajax/delete-mirror-credentials' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/ajax/make-primary-mirror-credentials', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/ajax/make-primary-mirror-credentials' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/ajax/list-mirror-subscriptions', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/ajax/list-mirror-subscriptions' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/products', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/products' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/products', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/products' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/products', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/products' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/products/metadata', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/products/metadata' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/sync/products', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/sync/products' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/sync/channelfamilies', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/sync/channelfamilies' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/sync/subscriptions', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/sync/subscriptions' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/sync/repositories', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/sync/repositories' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/channels/optional', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/channels/optional' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/payg', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/payg' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/payg/create', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/payg/create' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/payg', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/payg' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/payg/:id', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/payg/:id' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/payg/:id', 'DELETE', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/payg/:id' AND http_method = 'DELETE');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/payg/:id', 'PUT', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/payg/:id' AND http_method = 'PUT');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/config/monitoring', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/config/monitoring' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/config/password-policy', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/config/password-policy' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/monitoring', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/monitoring' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/monitoring', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/monitoring' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/password-policy', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/password-policy' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/password-policy/default', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/password-policy/default' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/password-policy', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/password-policy' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/password-policy/validate-password', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/password-policy/validate-password' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/enable-scc-data-forwarding', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/enable-scc-data-forwarding' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/runtime-status', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/runtime-status' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/runtime-status/data', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/runtime-status/data' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/subscription-matching', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/subscription-matching' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/subscription-matching/:filename', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/subscription-matching/:filename' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/subscription-matching/data', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/subscription-matching/data' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/subscription-matching/schedule-matcher-run', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/subscription-matching/schedule-matcher-run' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/subscription-matching/pins', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/subscription-matching/pins' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/subscription-matching/pins/:id/delete', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/subscription-matching/pins/:id/delete' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/multiorg/details/custom', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/multiorg/details/custom' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/multiorg/recurring-actions', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/multiorg/recurring-actions' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.logout', '/manager/api/auth/logout', 'POST', 'A', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/auth/logout' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/sso/metadata', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/sso/metadata' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/sso/acs', 'POST', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/sso/acs' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/sso/logout', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/sso/logout' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/sso/sls', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/sso/sls' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addApplyHighstate', '/manager/api/actionchain/addApplyHighstate', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/actionchain/addApplyHighstate' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.addVendorRepoFilter', '/manager/api/channel/software/addVendorRepoFilter', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/channel/software/addVendorRepoFilter' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.clearVendorRepoFilters', '/manager/api/channel/software/clearVendorRepoFilters', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/channel/software/clearVendorRepoFilters' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listVendorRepoFilters', '/manager/api/channel/software/listVendorRepoFilters', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/channel/software/listVendorRepoFilters' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.removeVendorRepoFilter', '/manager/api/channel/software/removeVendorRepoFilter', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/channel/software/removeVendorRepoFilter' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setVendorRepoFilters', '/manager/api/channel/software/setVendorRepoFilters', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/channel/software/setVendorRepoFilters' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.bootstrapProxy', '/manager/api/proxy/bootstrapProxy', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/proxy/bootstrapProxy' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringPlaybookHandler.create', '/manager/api/recurring/playbook/create', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/recurring/playbook/create' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringPlaybookHandler.update', '/manager/api/recurring/playbook/update', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/recurring/playbook/update' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.hasTraditionalSystems', '/manager/api/system/hasTraditionalSystems', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/system/hasTraditionalSystems' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setupStaticNetwork', '/manager/api/system/setupStaticNetwork', 'POST', 'A', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/system/setupStaticNetwork' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.transitionDataForSystem', '/manager/api/system/transitionDataForSystem', 'POST', 'A', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/system/transitionDataForSystem' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listMigrationTargets', '/manager/api/system/listMigrationTargets', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/system/listMigrationTargets' AND http_method = 'GET');


-- Namespace assignments of the new endpoints

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/details/proxy-config' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/details/proxy-config/get-registry-tags' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/cm/rebuild/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.runtime' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/clusters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.runtime' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/runtime/:clusterId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.runtime' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/runtime/:clusterId/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.runtime' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/runtime/details/:clusterId/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/kubeconfig/validate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/create/kubernetes' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/update/kubernetes' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/vhms/kubeconfig/:id/contexts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.appstreams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/activationkeys/appstreams' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkeys/appstreams/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.maintenance' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/schedule/:id/assign' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/calendar/refresh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/proxy' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ajax/save-proxy-settings' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ajax/verify-proxy-settings' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ajax/render-mirror-credentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ajax/verify-mirror-credentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ajax/save-mirror-credentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ajax/delete-mirror-credentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ajax/make-primary-mirror-credentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ajax/list-mirror-subscriptions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/products' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/products' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/products' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/products/metadata' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/sync/products' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/sync/channelfamilies' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/sync/subscriptions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/sync/repositories' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/channels/optional' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/payg' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/payg/create' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/payg' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/payg/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/payg/:id' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/payg/:id' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/config/monitoring' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/config/password-policy' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/monitoring' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/monitoring' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/password-policy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/password-policy/default' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/password-policy' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/password-policy/validate-password' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/enable-scc-data-forwarding' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/runtime-status' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/runtime-status/data' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/subscription-matching' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/subscription-matching/:filename' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/subscription-matching/data' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/subscription-matching/schedule-matcher-run' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/subscription-matching/pins' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/subscription-matching/pins/:id/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/multiorg/details/custom' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/multiorg/recurring-actions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_vendor_repo_filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/listVendorRepoFilters' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.clear_vendor_repo_filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/clearVendorRepoFilters' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_vendor_repo_filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setVendorRepoFilters' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.remove_vendor_repo_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/removeVendorRepoFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.add_vendor_repo_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/addVendorRepoFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.bootstrap_proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/bootstrapProxy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.has_traditional_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/hasTraditionalSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.playbook.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/playbook/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.playbook.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/playbook/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_apply_highstate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addApplyHighstate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;


-- Missing SSM endpoints

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/ssm/PackageUpgradeSchedule.do', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/ssm/PackageUpgradeSchedule.do' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/ssm/PackageUpgradeSchedule.do', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/ssm/PackageUpgradeSchedule.do' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/systems/ssm/ListErrataConfirm.do', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/systems/ssm/ListErrataConfirm.do' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/systems/ssm/ListErrataConfirm.do', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/systems/ssm/ListErrataConfirm.do' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageUpgradeSchedule.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageUpgradeSchedule.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/ListErrataConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/ListErrataConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;


-- Missing event history endpoints

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/systems/details/history/PendingDelete.do', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/systems/details/history/PendingDelete.do' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/systems/details/history/PendingDelete.do', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/systems/details/history/PendingDelete.do' AND http_method = 'POST');
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/PendingDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/PendingDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;


-- Permissions for the new namespaces

-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.actionchain.add_apply_highstate',
        'api.proxy.bootstrap_proxy',
        'api.recurring.playbook.create',
        'api.recurring.playbook.update',
        'api.system.has_traditional_systems',
        'systems.details.proxy'
    )
    ON CONFLICT DO NOTHING;

-- Permit to Activation Key Admin
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'activation_key_admin'
    AND ns.namespace = 'systems.activation_keys.appstreams'
    ON CONFLICT DO NOTHING;

-- Permit to Channel Admin
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'channel_admin'
    AND ns.namespace IN (
        'api.channel.software.list_vendor_repo_filters',
        'api.channel.software.clear_vendor_repo_filters',
        'api.channel.software.set_vendor_repo_filters',
        'api.channel.software.remove_vendor_repo_filter',
        'api.channel.software.add_vendor_repo_filter'
    )
    ON CONFLICT DO NOTHING;

-- Permit view to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'cm.runtime' AND ns.access_mode = 'R'
    ON CONFLICT DO NOTHING;

-- Permit modify to Image Admin
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'image_admin'
    AND ns.namespace = 'cm.runtime' AND ns.access_mode = 'W'
    ON CONFLICT DO NOTHING;
