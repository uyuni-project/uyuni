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

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/ping', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/sync/deregister', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/sync/registerHub', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/serverInfo', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/sync/replaceTokens', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/sync/storeCredentials', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/sync/setHubDetails', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/managerinfo', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/scheduleProductRefresh', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/storeReportDbCredentials', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/removeReportDbCredentials', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/listAllPeripheralOrgs', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/listAllPeripheralChannels', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/addVendorChannels', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/addCustomChannels', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/modifyCustomChannels', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/sync/channelfamilies', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/sync/products', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/sync/repositories', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/sync/subscriptions', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/syncChannels', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/sync/migrate/v1/deleteMaster', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/scc/connect/organizations/products/unscoped', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/scc/connect/organizations/repositories', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/scc/connect/organizations/subscriptions', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/scc/connect/organizations/orders', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/scc/suma/product_tree.json', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/scc/connect/organizations/systems', 'PUT', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/scc/connect/organizations/systems/:id', 'DELETE', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/hub/scc/connect/organizations/virtualization_hosts', 'PUT', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES('', '/manager/admin/access-control', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES('', '/manager/admin/access-control/create', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES('', '/manager/admin/access-control/show-access-group/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES('', '/manager/api/admin/access-control/access-group/list_custom', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES('', '/manager/api/admin/access-control/access-group/list_namespaces', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES('', '/manager/api/admin/access-control/access-group/organizations/:orgId/users', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES('', '/manager/api/admin/access-control/access-group/organizations', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES('', '/manager/api/admin/access-control/access-group/organizations/:orgId/access-groups', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES('', '/manager/api/admin/access-control/access-group/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES('', '/manager/api/admin/access-control/access-group/delete/:id', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/hub/hub-details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/hub/peripherals', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/hub/peripherals/register', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/hub/peripherals/migrate-from-v1', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/hub/peripherals/migrate-from-v2', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/hub/peripherals/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/hub/peripherals/:id/sync-channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/hub/access-tokens', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/support', 'GET', 'W', True)
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/uploadSupportData', 'POST', 'W', True)
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/proxy-config', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/proxy-config', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/proxy-config/get-registry-tags', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/audit/cve', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/audit/cve', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/audit/cve.csv', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/ListXccdf.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/ListXccdf.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/scap/Diff.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/scap/DiffSubmit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/scap/DiffSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/scap/Search.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/scap/Search.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Overview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Overview.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Machine.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Machine.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Search.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Search.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/audit/confidential-computing', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/audit/confidential-computing/listAttestations', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/contentmanagement/projects', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/contentmanagement/project/:label', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/contentmanagement/project', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/properties', 'PUT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/channels/owned', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/peripherals', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/peripherals', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/peripherals/:id', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/peripherals/:id/root-ca', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/peripherals/:id/root-ca', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/peripherals/:id/credentials', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/peripherals/:id/sync-channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/peripherals/:id/sync-channels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/:id', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/:id/root-ca', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/:id/root-ca', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/migrate/v1', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/migrate/v2', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/access-tokens', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/access-tokens', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/access-tokens/:id/validity', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/access-tokens/:id', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/hub/sync-bunch', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;


INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/mandatoryChannels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/softwaresources', 'PUT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/filters', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/filters', 'PUT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/environments', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/environments', 'PUT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/environments', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/build', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/promote', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/contentmanagement/filters', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/filters', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/filters/:filterId', 'PUT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/filters/:filterId', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/appstreams/:channelId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/channels/modular', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/livepatching/products', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/livepatching/systems', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/livepatching/kernels/product/:productId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/livepatching/kernels/system/:systemId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/Overview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/GlobalConfigChannelList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/GlobalConfigChannelList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelOverview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelFilesSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChannelSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChannelSystemsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/TargetSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/TargetSystemsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/Copy2Systems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/Copy2Systems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/Copy2Channels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/Copy2Channels.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelUploadFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelUploadFiles.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelImportFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelImportFilesSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelCreateFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelCreateFiles.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChooseFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChooseFilesSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChooseSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChooseSystemsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/DeployConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/DeployConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChannelDeployTasks.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChannelDeployTasks.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/DeleteChannel.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/DeleteChannel.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalConfigFileList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalConfigFileList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/FileDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/FileDownload.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/ManageRevision.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/ManageRevisionSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareRevision.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareCopy.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareChannel.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareFile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareDeployed.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareDeployedSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/Diff.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/DownloadDiff.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/LocalConfigFileList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/LocalConfigFileList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/FileDetails.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/DeleteFile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/DeleteFile.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/DeleteRevision.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/DeleteRevision.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CopyFileCentral.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CopyFileCentralSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CopyFileLocal.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CopyFileSandbox.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/RevisionDeploy.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalRevisionDeploy.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalRevisionDeploySubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalRevisionDeployConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalRevisionDeployConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/system/ManagedSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/system/ManagedSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/system/TargetSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/system/TargetSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/system/TargetSystemsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/system/Summary.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/system/Summary.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/YourRhn.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/YourRhnClips.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/subscription-warning', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/tasks', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/inactive-systems', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/critical-systems', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/pending-actions', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/latest-errata', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/systems-groups', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/recent-systems', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages/data-unread', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages/data-all', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages/update-messages-status', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages/retry/:notificationId', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/UserDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/UserDetailsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/Addresses.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/EditAddress.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/EditAddressSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/ChangeEmail.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/ChangeEmailSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/AccountDeactivation.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/AccountDeactivationSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/AccountDeactivationConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/AccountDeactivationConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/UserPreferences.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/PrefSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/OrgConfigDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/OrgConfigDetails.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/Organizations.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/OrgTrustDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/channels/Provided.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/channels/Consumed.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/channels/Provided.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/channels/Consumed.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/yourorg/recurring-actions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:type/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/summary', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/states', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/delete', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/custom/execute', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/targets/:type/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/yourorg/custom', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/match', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/:channelId/content', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/apply', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/images', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/build/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/rebuild/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/inspect/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/inspect/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/patches/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/packages/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/buildlog/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/patches/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/packages/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/buildlog/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/import', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/import', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/upload/image', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/type/:type', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/build/hosts/:type', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/activationkeys', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imageprofiles', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imageprofiles/create', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/find/:label', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/create', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/update/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/find/:label', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imageprofiles/edit/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/channels/:token', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/type/:type', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imagestores', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imagestores/create', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/create', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/find/', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/find', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/update/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imagestores/edit/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/build', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/build/hosts/:type', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/build/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/clusters', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/runtime/:clusterId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/runtime/:clusterId/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/runtime/details/:clusterId/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantBugErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantBugErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantEnhancementErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantEnhancementErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantSecurityErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantSecurityErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllBugErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllBugErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllEnhancementErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllEnhancementErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllSecurityErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllSecurityErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/Details.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/SystemsAffected.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/SystemsAffected.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/ErrataConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/ErrataConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/Search.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/Search.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Errata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Errata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Create.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/CreateSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Edit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/ErrataChannelIntersection.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/ErrataChannelIntersection.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/SelectChannels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/SelectChannels.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/SelectChannelsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/PackagePush.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/PackagePush.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/PackagePushSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/DeleteBug.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackagesConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackagesConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackagePackagePush.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackagePackagePushSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/RemovePackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/RemovePackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddChannelPackagePush.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddChannelPackagePushSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Edit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Channels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/ChannelsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/ListPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/ListPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Notify.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/NotifySubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Delete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/PublishedDeleteConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/PublishedDeleteConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/CloneErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/CloneErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/CloneConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/CloneConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/keys', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/keys', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/keys/:target/accept', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/keys/:target/reject', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/keys/:target/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/cmd', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/formula-catalog', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formula-catalog/data', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/formula-catalog/formula/:name', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formula-catalog/formula/:name/data', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/PendingActions.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/PendingActions.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/PendingActionsDeleteConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/PendingActionsDeleteConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/FailedActions.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/FailedActions.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/CompletedActions.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/CompletedActions.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ArchivedActions.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ArchivedActions.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ActionDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/CompletedSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/CompletedSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/InProgressSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/InProgressSystemsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/FailedSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/FailedSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ActionChains.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ActionChains.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ActionChain.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/action-chain-entries', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ActionChain.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/action-chain-save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/schedule/recurring-actions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/schedule/maintenance/schedules', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/list', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/calendar/names', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/:id/systems', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/:id/setsystems', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/:id/assign', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/systems', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/schedule/maintenance/calendars', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/calendar/list', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/calendar/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/events/:operation/:type/:startOfWeek/:date/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/:id/setsystems', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/delete', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/calendar/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/calendar/delete', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/calendar/refresh', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/All.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/All.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Vendor.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Vendor.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Popular.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Popular.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Custom.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Custom.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Shared.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Shared.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Retired.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Retired.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/Details.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/Dependencies.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/ChangeLog.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/FileList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/NewVersions.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/InstalledSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/InstalledSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/TargetSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/TargetSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/TargetSystemsConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/TargetSystemsConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/NameOverview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/packages/NameOverview.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelDetail.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelDetail.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/Managers.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/Managers.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelSubscribers.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelSubscribers.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/TargetSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/TargetSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ConfirmTargetSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ConfirmTargetSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/AppStreams.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/AppStreams.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/software/Search.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/software/Search.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Manage.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Manage.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Edit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Edit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Delete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Delete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Managers.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Managers.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Clone.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Clone.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/Errata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ListRemove.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ListRemove.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ConfirmRemove.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ConfirmRemove.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/Add.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/AddRedHatErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/AddCustomErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/AddCustomErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/Clone.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/Clone.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ConfirmErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ConfirmErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/AddErrataToChannel.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/SyncErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/SyncErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ConfirmSyncPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ConfirmSyncPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/AddRedHatErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackageMenu.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesRemove.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesRemove.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesAdd.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesAdd.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesAddConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesAddConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompare.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompare.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompareMerge.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompareMerge.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompareMergeConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompareMergeConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/Subscribers.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/Subscribers.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/packages/list', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/packages/list/:binary/:kind', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/packages/list/:binary/channel/:cid', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/packages/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Repositories.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Repositories.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Sync.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Sync.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/AssociatedChannels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/AssociatedChannels.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMap.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMap.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMapEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMapEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMapDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMapDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/index.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewAllLog.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewAllLog.do', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewLog.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewLog.do', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewCompletedLog.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewCompletedLog.do', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ListSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ListSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/list/all', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/list/virtual', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/list/all', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/list/virtual', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/BootstrapSystemList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateIPList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateIPList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateIPv6List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateIPv6List.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateHostName.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateHostName.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateMacAddress.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateMacAddress.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateSystemsCompare.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateSystemsCompare.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemCurrency.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemCurrency.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemEntitlements.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemEntitlementsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/csv/all', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/csv/virtualSystems', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ListErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ListErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ListErrataConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ListErrataConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ErrataSystemsAffected.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ErrataSystemsAffected.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ErrataList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ErrataList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/PackageList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/PackageList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/ExtraPackagesList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/ExtraPackagesList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/ShowProfiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/ShowProfiles.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/CompareProfiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageUpgrade.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageUpgrade.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageUpgradeSchedule.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageUpgradeSchedule.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageInstall.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageInstall.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageSchedule.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageSchedule.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageRemove.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageRemove.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageRemoveSchedule.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageRemoveSchedule.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/RemoveConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/RemoveConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/UpgradableList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/UpgradableList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/UpgradeConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/UpgradeConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/InstallPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/InstallPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/InstallConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/InstallConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/VerifyPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/VerifyPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/VerifyConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/VerifyConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/LockPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/LockPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/RemoveExtraConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/RemoveExtraConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/Create.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/Create.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/DeleteProfile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/DeleteProfile.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Manage.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Manage.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemGroupList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemGroupList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/groups/ListRemove.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/groups/ListRemove.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Confirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Confirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/AddSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/AddSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Create.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Create.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/EditGroup.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/EditGroup.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channel/ssm/ChannelSubscriptions.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/channels/bases', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/channels/allowed-changes', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/channels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/upcoming-windows', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/channels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Deploy.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DeploySubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DeployConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DeployConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Diff.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DiffSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DiffConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DiffConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DeployFile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DeployFileSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DeployFileConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DeployFileConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DiffFile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DiffFileSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DiffFileConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DiffFileConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewDiffResult.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Subscribe.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/SubscribeSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Rank.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Rank.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/SubscribeConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/SubscribeConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Unsubscribe.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/UnsubscribeSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/UnsubscribeConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/UnsubscribeConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Enable.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/EnableSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/EnableSummary.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/EnableSummary.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/SubscriptionsSetup.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/SubscriptionsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/RankChannels.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/KickstartableSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/KickstartableSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/ScheduleByProfile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/ScheduleByProfile.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/ScheduleByIp.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/ScheduleByIp.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/PowerManagementConfiguration.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/PowerManagementConfiguration.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/PowerManagementOperations.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/PowerManagementOperations.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/CreateProfileWizard.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/CreateProfileWizard.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/AdvancedModeCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/AdvancedModeCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/AdvancedModeEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDeleteAdvanced.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDeleteAdvanced.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartCloneAdvanced.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartCloneAdvanced.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeEditAdvanced.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/EditVariables.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartClone.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartClone.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDetailsEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartSoftwareEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartOptionsEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/SystemDetailsEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Locale.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPartitionEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFilePreservationListsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartCryptoKeysListSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Troubleshooting.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPackagesEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ActivationKeysSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartScriptCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartScriptCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartScriptEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartScriptEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartScriptDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartScriptDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartScriptOrder.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartScriptOrder.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeyCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeyCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeyEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeyEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeyDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeyDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/tree/EditVariables.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationListDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/audit/ScheduleXccdf.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/audit/ScheduleXccdf.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/audit/ScheduleXccdfConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/XccdfDeleteConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/XccdfDeleteConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/ScheduleXccdf.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/ScheduleXccdf.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/coco/settings', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/coco/settings', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/coco/schedule', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/coco/scheduleAction', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/coco/settings', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/coco/scheduleAction', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/coco/listAttestations', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/summary', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/Index.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/Index.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/ConfirmSystemPreferences.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Edit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Edit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/EditNote.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/EditNote.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/HardwareRefresh.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/HardwareRefresh.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemHardware.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/SoftwareRefresh.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/SoftwareRefresh.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/Packages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/maintenance', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/:id/assign', 'PSOT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/unassign', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/RemoteCommand.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/RemoteCommand.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemRemoteCommand.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemRemoteCommand.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/CustomValue.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/CustomValue.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ListCustomData.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/SetCustomValue.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/SetCustomValue.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/CreateCustomData.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/UpdateCustomData.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/UpdateCustomData.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/DeleteCustomData.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/DeleteCustomData.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/RebootSystem.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/RebootSystem.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/RebootSystemConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/RebootSystemConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/RebootSystem.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/RebootSystem.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/proxy', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/proxy', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/proxy', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/MigrateSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/MigrateSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemMigrate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemMigrate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/DeleteConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/DeleteConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/DeleteConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/DeleteConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Index.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Index.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/SnapshotTags.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/SnapshotTags.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Groups.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Groups.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Channels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Channels.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Packages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/ConfigChannels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/ConfigChannels.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/ConfigFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/ConfigFiles.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/UnservablePackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/UnservablePackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Tags.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Tags.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/SnapshotTagCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/SnapshotTagCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Rollback.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Rollback.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/TagCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/TagCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/TagsDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/TagsDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/WorkWithGroup.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/GroupDetail.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/Delete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/Delete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/ListRemoveSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/ListRemoveSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/ListErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/ListErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/SystemsAffected.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/SystemsAffected.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/AdminList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/AdminList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/groups/details/custom', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/match', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/groups/details/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/summary', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/applyall', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/apply', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/groups/details/formulas', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/list/:targetType/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/groups/details/formula/:formula_id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/form/:targetType/:id/:formula_id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/select', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/groups/details/recurring-actions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:type/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/summary', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/states', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/targets/:type/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/custom/execute', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/delete', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Overview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Notes.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/RemoveFromSSM.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/AddToSSM.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Connection.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ProxyClients.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ProxyClients.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/mgr-server-info/:sid', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/mgr-server-reportdb-newpw', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Activation.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Activation.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemHardware.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ErrataConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ErrataConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemChannels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/channels-available-base', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/channels/:channelId/accessible-children', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/mandatoryChannels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SPMigration.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SPMigration.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/appstreams', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/appstreams/:channelId/:appstream/packages', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/appstreams/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ptf/overview', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ptf/list', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/ptf/allowedActions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/ptf/installed', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ptf/install', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/ptf/available', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/ptf/scheduleAction', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/Overview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewCentralPaths.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewCentralPaths.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifyCentralPaths.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifyCentralPaths.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifyLocalPaths.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifyLocalPaths.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifySandboxPaths.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifySandboxPaths.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ConfigChannelList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ConfigChannelListUnsubscribeSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/RankChannels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/groups/Add.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/groups/Add.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/ListScap.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/ListScap.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/XccdfDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/ScapResultDownload.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/RuleDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/coco/settings', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/coco/settings', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/coco/list', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/applyall', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/packages', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/packages', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/packages/match', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/packages/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/apply', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/custom', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/match', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/apply', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/formulas', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/list/:targetType/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/formula/:formula_id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/form/:targetType/:id/:formula_id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/select', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ansible/control-node', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/paths/:minionServerId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ansible/playbooks', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/paths/:pathType/:minionServerId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/discover-playbooks/:pathId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/paths/playbook-contents', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ansible/inventories', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/introspect-inventory/:pathId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/paths/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/paths/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/schedule-playbook', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/recurring-actions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:type/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/summary', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/states', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/custom/execute', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/delete', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/Pending.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/History.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/History.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/Event.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/Pending.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/PendingDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/PendingDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/Event.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/FailEventConfirmation.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/FailEventConfirmation.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/bootstrap', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/bootstrap', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/bootstrap-ssh', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/proxy/container-config', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/proxy/container-config', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/proxy/container-config/:filename', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/Search.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/Search.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/List.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Create.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Create.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Edit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Clone.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Clone.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Edit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/activation-keys/:tid/channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/activation-keys/base-channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/activation-keys/base-channels/:cid/child-channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/mandatoryChannels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/systems/List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/systems/List.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/packages/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/packages/Packages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/List.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/Subscribe.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/Subscribe.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/Rank.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/Rank.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/groups/List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/groups/List.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/groups/Add.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/groups/Add.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Delete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Delete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/activationkeys/appstreams', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/activationkeys/appstreams/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/Details.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/PackageList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/PackageList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/Details.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/Delete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/Delete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/CustomDataList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/CustomDataList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/CreateCustomKey.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/CreateCustomKey.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/UpdateCustomKey.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/UpdateCustomKey.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/DeleteCustomKey.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/DeleteCustomKey.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartOverview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Kickstarts.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Kickstarts.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/AdvancedModeEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeEditAdvanced.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/EditVariables.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFileDownloadAdvanced.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRanges.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRanges.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDetailsEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartSoftwareEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartOptionsEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/SystemDetailsEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Locale.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPartitionEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFilePreservationLists.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFilePreservationLists.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartCryptoKeysList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartCryptoKeysList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Troubleshooting.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPackagesEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPackageProfileEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPackageProfileEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ActivationKeys.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ActivationKeys.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ActivationKeysList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ActivationKeysList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Scripts.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Scripts.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFileDownload.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFileDownload.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeysList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ViewTrees.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ViewTrees.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/tree/EditVariables.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationListCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationListCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationListDeleteSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationListDeleteSingle.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationListConfirmDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationListConfirmDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CustomSnippetList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CustomSnippetList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/DefaultSnippetList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/DefaultSnippetList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetView.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/ScheduleWizard.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/ScheduleWizard.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/SessionCancel.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/SessionCancel.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/Variables.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/PowerManagement.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/PowerManagement.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/SessionStatus.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/SessionStatus.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/Variables.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/vhms', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/modules', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/:id/nodes', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/:id/refresh', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/module/:name/params', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/create', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/update/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/delete/:id', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/kubeconfig/validate', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/kubeconfig/:id/contexts', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/create/kubernetes', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/update/kubernetes', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ActiveList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ActiveList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DisabledList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DisabledList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/UserList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EnableConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EnableConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/UserDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/Addresses.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/UserDetailsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ChangeEmail.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ChangeEmailSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DisableUser.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DisableUserSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EnableUser.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EnableUserSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DeleteUser.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DeleteUserSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/CreateUser.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EditAddress.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EditAddressSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/AssignedSystemGroups.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/AssignedSystemGroups.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/SystemsAdmined.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/SystemsAdminedSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ChannelPerms.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ChannelManagementPerms.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ChannelPermsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/UserPreferences.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/PrefSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/SystemGroupConfig.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ExtAuthSgMapping.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ExtAuthSgMapping.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/SystemGroupConfig.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ExtAuthSgDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ExtAuthSgDetails.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/hubsync/:sccrepoid/getPackage/:file', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/hubsync/:sccrepoid/getPackage/:org/:checksum/:file', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/hubsync/:sccrepoid/repodata/:file', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/hubsync/:sccrepoid/media.1/:file', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/:channel/getPackage/:file', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/:channel/getPackage/:org/:checksum/:file', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/:channel/repodata/:file', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/:channel/media.1/:file', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/hubsync/:sccrepoid/getPackage/:file', 'HEAD', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/hubsync/:sccrepoid/getPackage/:org/:checksum/:file', 'HEAD', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/hubsync/:sccrepoid/repodata/:file', 'HEAD', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/hubsync/:sccrepoid/media.1/:file', 'HEAD', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/:channel/getPackage/:file', 'HEAD', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/:channel/getPackage/:org/:checksum/:file', 'HEAD', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/:channel/repodata/:file', 'HEAD', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/:channel/media.1/:file', 'HEAD', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/download/saltssh/pubkey', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/saltboot/*', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/storybook', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/login', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/Logout.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/taskomatic/invoke', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/setup/proxy', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/save-proxy-settings', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/verify-proxy-settings', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/admin/setup/MirrorCredentials.do', 'GET/???', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/render-mirror-credentials', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/verify-mirror-credentials', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/save-mirror-credentials', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/delete-mirror-credentials', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/make-primary-mirror-credentials', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/list-mirror-subscriptions', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/setup/products', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/setup/products', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/products', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/products/metadata', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/setup/sync/products', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/setup/sync/channelfamilies', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/setup/sync/subscriptions', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/setup/sync/repositories', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/setup/channels/optional', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/setup/payg', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/setup/payg/create', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/config/payg', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/setup/payg/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/config/payg/:id', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/config/payg/:id', 'PUT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/config/monitoring', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/config/password-policy', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/config/monitoring', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/config/monitoring', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/config/password-policy', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/config/password-policy/default', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/config/password-policy', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/config/password-policy/validate-password', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/enable-scc-data-forwarding', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/admin/runtime-status', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/runtime-status/data', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/subscription-matching', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/subscription-matching/:filename', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/subscription-matching/data', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/subscription-matching/schedule-matcher-run', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/subscription-matching/pins', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/subscription-matching/pins/:id/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/multiorg/details/custom', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/multiorg/recurring-actions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.webui.controllers.login.LoginController', '/manager/api/login', 'POST', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.webui.controllers.login.LoginController', '/manager/api/oidcLogin', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.webui.controllers.login.LoginController', '/manager/api/logout', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.login', '/manager/api/auth/login', 'POST', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.logout', '/manager/api/auth/logout', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.logout', '/manager/api/auth/logout', 'POST', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.isSessionKeyValid', '/manager/api/auth/isSessionKeyValid', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.checkAuthToken', '/manager/api/auth/checkAuthToken', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.getDuration', '/manager/api/auth/getDuration', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.getVersion', '/manager/api/api/getVersion', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.getApiCallList', '/manager/api/api/getApiCallList', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.getApiNamespaceCallList', '/manager/api/api/getApiNamespaceCallList', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.systemVersion', '/manager/api/api/systemVersion', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.getApiNamespaces', '/manager/api/api/getApiNamespaces', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.productName', '/manager/api/api/productName', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.hasMaster', '/manager/api/sync/master/hasMaster', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/item-selector', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/frontend-log', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/sets/:label', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/sets/:label/clear', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/CSVDownloadAction.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/sso/metadata', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/sso/acs', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/sso/logout', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/sso/sls', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/product-migration', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/product-migration/dry-run/:actionId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/migration/computeChannels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/migration/schedule', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/Search.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/rhn/manager/systems/ssm/appstreams', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/rhn/manager/systems/ssm/appstreams/configure/:channelId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/rhn/manager/api/ssm/appstreams/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler.ssmEnable',
        '/rhn/manager/api/system/appstreams/ssmEnable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
VALUES ('com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler.ssmDisable',
    '/rhn/manager/api/system/appstreams/ssmDisable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.createRole', '/manager/api/access/createRole', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.deleteRole', '/manager/api/access/deleteRole', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.grantAccess', '/manager/api/access/grantAccess', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.listNamespaces', '/manager/api/access/listNamespaces', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.listPermissions', '/manager/api/access/listPermissions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.listRoles', '/manager/api/access/listRoles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.revokeAccess', '/manager/api/access/revokeAccess', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addApplyHighstate', '/manager/api/actionchain/addApplyHighstate', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addConfigurationDeployment', '/manager/api/actionchain/addConfigurationDeployment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addErrataUpdate', '/manager/api/actionchain/addErrataUpdate', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addPackageInstall', '/manager/api/actionchain/addPackageInstall', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addPackageRemoval', '/manager/api/actionchain/addPackageRemoval', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addPackageUpgrade', '/manager/api/actionchain/addPackageUpgrade', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addPackageVerify', '/manager/api/actionchain/addPackageVerify', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addScriptRun', '/manager/api/actionchain/addScriptRun', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addSystemReboot', '/manager/api/actionchain/addSystemReboot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.createChain', '/manager/api/actionchain/createChain', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.deleteChain', '/manager/api/actionchain/deleteChain', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.listChainActions', '/manager/api/actionchain/listChainActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.listChains', '/manager/api/actionchain/listChains', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.removeAction', '/manager/api/actionchain/removeAction', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.renameChain', '/manager/api/actionchain/renameChain', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.scheduleChain', '/manager/api/actionchain/scheduleChain', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addAppStreams', '/manager/api/activationkey/addAppStreams', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addChildChannels', '/manager/api/activationkey/addChildChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addConfigChannels', '/manager/api/activationkey/addConfigChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addEntitlements', '/manager/api/activationkey/addEntitlements', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addPackages', '/manager/api/activationkey/addPackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addServerGroups', '/manager/api/activationkey/addServerGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.checkConfigDeployment', '/manager/api/activationkey/checkConfigDeployment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.clone', '/manager/api/activationkey/clone', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.create', '/manager/api/activationkey/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.delete', '/manager/api/activationkey/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.disableConfigDeployment', '/manager/api/activationkey/disableConfigDeployment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.enableConfigDeployment', '/manager/api/activationkey/enableConfigDeployment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.getDetails', '/manager/api/activationkey/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.listActivatedSystems', '/manager/api/activationkey/listActivatedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.listActivationKeys', '/manager/api/activationkey/listActivationKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.listChannels', '/manager/api/activationkey/listChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.listConfigChannels', '/manager/api/activationkey/listConfigChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removeAppStreams', '/manager/api/activationkey/removeAppStreams', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removeChildChannels', '/manager/api/activationkey/removeChildChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removeConfigChannels', '/manager/api/activationkey/removeConfigChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removeEntitlements', '/manager/api/activationkey/removeEntitlements', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removePackages', '/manager/api/activationkey/removePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removeServerGroups', '/manager/api/activationkey/removeServerGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.setConfigChannels', '/manager/api/activationkey/setConfigChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.setDetails', '/manager/api/activationkey/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.admin.configuration.AdminConfigurationHandler.configure', '/manager/api/admin/configuration/configure', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler.disable', '/manager/api/admin/monitoring/disable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler.enable', '/manager/api/admin/monitoring/enable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler.getStatus', '/manager/api/admin/monitoring/getStatus', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.admin.AdminPaygHandler.create', '/manager/api/admin/payg/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.admin.AdminPaygHandler.delete', '/manager/api/admin/payg/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.admin.AdminPaygHandler.getDetails', '/manager/api/admin/payg/getDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.admin.AdminPaygHandler.list', '/manager/api/admin/payg/list', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.admin.AdminPaygHandler.setDetails', '/manager/api/admin/payg/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.createAnsiblePath', '/manager/api/ansible/createAnsiblePath', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.discoverPlaybooks', '/manager/api/ansible/discoverPlaybooks', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.fetchPlaybookContents', '/manager/api/ansible/fetchPlaybookContents', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.introspectInventory', '/manager/api/ansible/introspectInventory', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.listAnsiblePaths', '/manager/api/ansible/listAnsiblePaths', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.lookupAnsiblePathById', '/manager/api/ansible/lookupAnsiblePathById', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.removeAnsiblePath', '/manager/api/ansible/removeAnsiblePath', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.schedulePlaybook', '/manager/api/ansible/schedulePlaybook', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.updateAnsiblePath', '/manager/api/ansible/updateAnsiblePath', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler.disableUserRestrictions', '/manager/api/channel/access/disableUserRestrictions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler.enableUserRestrictions', '/manager/api/channel/access/enableUserRestrictions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler.getOrgSharing', '/manager/api/channel/access/getOrgSharing', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler.setOrgSharing', '/manager/api/channel/access/setOrgSharing', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.appstreams.ChannelAppStreamHandler.isModular', '/manager/api/channel/appstreams/isModular', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.appstreams.ChannelAppStreamHandler.listModular', '/manager/api/channel/appstreams/listModular', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.appstreams.ChannelAppStreamHandler.listModuleStreams', '/manager/api/channel/appstreams/listModuleStreams', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listAllChannels', '/manager/api/channel/listAllChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listManageableChannels', '/manager/api/channel/listManageableChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listMyChannels', '/manager/api/channel/listMyChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listPopularChannels', '/manager/api/channel/listPopularChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listRetiredChannels', '/manager/api/channel/listRetiredChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listSharedChannels', '/manager/api/channel/listSharedChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listSoftwareChannels', '/manager/api/channel/listSoftwareChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listVendorChannels', '/manager/api/channel/listVendorChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.org.ChannelOrgHandler.disableAccess', '/manager/api/channel/org/disableAccess', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.org.ChannelOrgHandler.enableAccess', '/manager/api/channel/org/enableAccess', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.org.ChannelOrgHandler.list', '/manager/api/channel/org/list', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.addPackages', '/manager/api/channel/software/addPackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.addRepoFilter', '/manager/api/channel/software/addRepoFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.addVendorRepoFilter', '/manager/api/channel/software/addVendorRepoFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.alignMetadata', '/manager/api/channel/software/alignMetadata', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.applyChannelState', '/manager/api/channel/software/applyChannelState', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.associateRepo', '/manager/api/channel/software/associateRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.clearRepoFilters', '/manager/api/channel/software/clearRepoFilters', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.clearVendorRepoFilters', '/manager/api/channel/software/clearVendorRepoFilters', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.clone', '/manager/api/channel/software/clone', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.create', '/manager/api/channel/software/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.createRepo', '/manager/api/channel/software/createRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.delete', '/manager/api/channel/software/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.disassociateRepo', '/manager/api/channel/software/disassociateRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.getChannelLastBuildById', '/manager/api/channel/software/getChannelLastBuildById', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.getDetails', '/manager/api/channel/software/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.getRepoDetails', '/manager/api/channel/software/getRepoDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.getRepoSyncCronExpression', '/manager/api/channel/software/getRepoSyncCronExpression', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.isExisting', '/manager/api/channel/software/isExisting', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.isGloballySubscribable', '/manager/api/channel/software/isGloballySubscribable', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.isUserManageable', '/manager/api/channel/software/isUserManageable', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.isUserSubscribable', '/manager/api/channel/software/isUserSubscribable', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listAllPackages', '/manager/api/channel/software/listAllPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listArches', '/manager/api/channel/software/listArches', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listChannelRepos', '/manager/api/channel/software/listChannelRepos', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listChildren', '/manager/api/channel/software/listChildren', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listErrata', '/manager/api/channel/software/listErrata', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listErrataByType', '/manager/api/channel/software/listErrataByType', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listErrataNeedingSync', '/manager/api/channel/software/listErrataNeedingSync', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listLatestPackages', '/manager/api/channel/software/listLatestPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listPackagesWithoutChannel', '/manager/api/channel/software/listPackagesWithoutChannel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listRepoFilters', '/manager/api/channel/software/listRepoFilters', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listSubscribedSystems', '/manager/api/channel/software/listSubscribedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listSystemChannels', '/manager/api/channel/software/listSystemChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listUserRepos', '/manager/api/channel/software/listUserRepos', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listVendorRepoFilters', '/manager/api/channel/software/listVendorRepoFilters', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.mergeErrata', '/manager/api/channel/software/mergeErrata', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.mergePackages', '/manager/api/channel/software/mergePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.regenerateNeededCache', '/manager/api/channel/software/regenerateNeededCache', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.regenerateYumCache', '/manager/api/channel/software/regenerateYumCache', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.removeErrata', '/manager/api/channel/software/removeErrata', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.removePackages', '/manager/api/channel/software/removePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.removeRepo', '/manager/api/channel/software/removeRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.removeRepoFilter', '/manager/api/channel/software/removeRepoFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.removeVendorRepoFilter', '/manager/api/channel/software/removeVendorRepoFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setContactDetails', '/manager/api/channel/software/setContactDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setDetails', '/manager/api/channel/software/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setGloballySubscribable', '/manager/api/channel/software/setGloballySubscribable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setRepoFilters', '/manager/api/channel/software/setRepoFilters', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setUserManageable', '/manager/api/channel/software/setUserManageable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setUserSubscribable', '/manager/api/channel/software/setUserSubscribable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setVendorRepoFilters', '/manager/api/channel/software/setVendorRepoFilters', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.syncErrata', '/manager/api/channel/software/syncErrata', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.syncRepo', '/manager/api/channel/software/syncRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.updateRepo', '/manager/api/channel/software/updateRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.updateRepoLabel', '/manager/api/channel/software/updateRepoLabel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.updateRepoSsl', '/manager/api/channel/software/updateRepoSsl', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.updateRepoUrl', '/manager/api/channel/software/updateRepoUrl', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.channelExists', '/manager/api/configchannel/channelExists', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.create', '/manager/api/configchannel/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.createOrUpdatePath', '/manager/api/configchannel/createOrUpdatePath', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.createOrUpdateSymlink', '/manager/api/configchannel/createOrUpdateSymlink', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.deleteChannels', '/manager/api/configchannel/deleteChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.deleteFileRevisions', '/manager/api/configchannel/deleteFileRevisions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.deleteFiles', '/manager/api/configchannel/deleteFiles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.deployAllSystems', '/manager/api/configchannel/deployAllSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.getDetails', '/manager/api/configchannel/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.getEncodedFileRevision', '/manager/api/configchannel/getEncodedFileRevision', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.getFileRevision', '/manager/api/configchannel/getFileRevision', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.getFileRevisions', '/manager/api/configchannel/getFileRevisions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.listAssignedSystemGroups', '/manager/api/configchannel/listAssignedSystemGroups', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.listFiles', '/manager/api/configchannel/listFiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.listGlobals', '/manager/api/configchannel/listGlobals', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.listSubscribedSystems', '/manager/api/configchannel/listSubscribedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.lookupChannelInfo', '/manager/api/configchannel/lookupChannelInfo', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.lookupFileInfo', '/manager/api/configchannel/lookupFileInfo', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.scheduleFileComparisons', '/manager/api/configchannel/scheduleFileComparisons', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.syncSaltFilesOnDisk', '/manager/api/configchannel/syncSaltFilesOnDisk', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.update', '/manager/api/configchannel/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.updateInitSls', '/manager/api/configchannel/updateInitSls', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.attachFilter', '/manager/api/contentmanagement/attachFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.attachSource', '/manager/api/contentmanagement/attachSource', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.generateProjectDifference', '/manager/api/contentmanagement/generateProjectDifference', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.generateEnvironmentDifference', '/manager/api/contentmanagement/generateEnvironmentDifference', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.buildProject', '/manager/api/contentmanagement/buildProject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.createAppStreamFilters', '/manager/api/contentmanagement/createAppStreamFilters', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.createEnvironment', '/manager/api/contentmanagement/createEnvironment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.createFilter', '/manager/api/contentmanagement/createFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.createProject', '/manager/api/contentmanagement/createProject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.detachFilter', '/manager/api/contentmanagement/detachFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.detachSource', '/manager/api/contentmanagement/detachSource', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listEnvironmentDifference', '/manager/api/contentmanagement/listEnvironmentDifference', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listFilterCriteria', '/manager/api/contentmanagement/listFilterCriteria', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listFilters', '/manager/api/contentmanagement/listFilters', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listProjectEnvironments', '/manager/api/contentmanagement/listProjectEnvironments', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listProjectFilters', '/manager/api/contentmanagement/listProjectFilters', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listProjectSources', '/manager/api/contentmanagement/listProjectSources', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listProjects', '/manager/api/contentmanagement/listProjects', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.lookupEnvironment', '/manager/api/contentmanagement/lookupEnvironment', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.lookupFilter', '/manager/api/contentmanagement/lookupFilter', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.lookupProject', '/manager/api/contentmanagement/lookupProject', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.lookupSource', '/manager/api/contentmanagement/lookupSource', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.promoteProject', '/manager/api/contentmanagement/promoteProject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.removeEnvironment', '/manager/api/contentmanagement/removeEnvironment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.removeFilter', '/manager/api/contentmanagement/removeFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.removeProject', '/manager/api/contentmanagement/removeProject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.updateEnvironment', '/manager/api/contentmanagement/updateEnvironment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.updateFilter', '/manager/api/contentmanagement/updateFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.updateProject', '/manager/api/contentmanagement/updateProject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.addChannel', '/manager/api/sync/content/addChannel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.addChannels', '/manager/api/sync/content/addChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.addCredentials', '/manager/api/sync/content/addCredentials', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.deleteCredentials', '/manager/api/sync/content/deleteCredentials', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.listChannels', '/manager/api/sync/content/listChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.listCredentials', '/manager/api/sync/content/listCredentials', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.listProducts', '/manager/api/sync/content/listProducts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.synchronizeChannelFamilies', '/manager/api/sync/content/synchronizeChannelFamilies', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.synchronizeProducts', '/manager/api/sync/content/synchronizeProducts', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.synchronizeRepositories', '/manager/api/sync/content/synchronizeRepositories', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.synchronizeSubscriptions', '/manager/api/sync/content/synchronizeSubscriptions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler.create', '/manager/api/kickstart/keys/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler.delete', '/manager/api/kickstart/keys/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler.getDetails', '/manager/api/kickstart/keys/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler.listAllKeys', '/manager/api/kickstart/keys/listAllKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler.update', '/manager/api/kickstart/keys/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler.createKey', '/manager/api/system/custominfo/createKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler.deleteKey', '/manager/api/system/custominfo/deleteKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler.listAllKeys', '/manager/api/system/custominfo/listAllKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler.updateKey', '/manager/api/system/custominfo/updateKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.audit.CVEAuditHandler.listImagesByPatchStatus', '/manager/api/audit/listImagesByPatchStatus', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.audit.CVEAuditHandler.listSystemsByPatchStatus', '/manager/api/audit/listSystemsByPatchStatus', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.DeltaImageInfoHandler.createDeltaImage', '/manager/api/image/delta/createDeltaImage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.DeltaImageInfoHandler.getDetails', '/manager/api/image/delta/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.DeltaImageInfoHandler.listDeltas', '/manager/api/image/delta/listDeltas', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.distchannel.DistChannelHandler.listDefaultMaps', '/manager/api/distchannel/listDefaultMaps', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.distchannel.DistChannelHandler.listMapsForOrg', '/manager/api/distchannel/listMapsForOrg', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.distchannel.DistChannelHandler.setMapForOrg', '/manager/api/distchannel/setMapForOrg', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.addPackages', '/manager/api/errata/addPackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.applicableToChannels', '/manager/api/errata/applicableToChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.bugzillaFixes', '/manager/api/errata/bugzillaFixes', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.clone', '/manager/api/errata/clone', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.cloneAsOriginal', '/manager/api/errata/cloneAsOriginal', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.cloneAsOriginalAsync', '/manager/api/errata/cloneAsOriginalAsync', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.cloneAsync', '/manager/api/errata/cloneAsync', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.create', '/manager/api/errata/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.delete', '/manager/api/errata/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.findByCve', '/manager/api/errata/findByCve', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.getDetails', '/manager/api/errata/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.listAffectedSystems', '/manager/api/errata/listAffectedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.listCves', '/manager/api/errata/listCves', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.listKeywords', '/manager/api/errata/listKeywords', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.listPackages', '/manager/api/errata/listPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.publish', '/manager/api/errata/publish', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.publishAsOriginal', '/manager/api/errata/publishAsOriginal', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.removePackages', '/manager/api/errata/removePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.setDetails', '/manager/api/errata/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler.create', '/manager/api/kickstart/filepreservation/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler.delete', '/manager/api/kickstart/filepreservation/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler.getDetails', '/manager/api/kickstart/filepreservation/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler.listAllFilePreservations', '/manager/api/kickstart/filepreservation/listAllFilePreservations', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getCombinedFormulaDataByServerIds', '/manager/api/formula/getCombinedFormulaDataByServerIds', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getCombinedFormulasByServerId', '/manager/api/formula/getCombinedFormulasByServerId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getFormulasByGroupId', '/manager/api/formula/getFormulasByGroupId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getFormulasByServerId', '/manager/api/formula/getFormulasByServerId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getGroupFormulaData', '/manager/api/formula/getGroupFormulaData', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getSystemFormulaData', '/manager/api/formula/getSystemFormulaData', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.listFormulas', '/manager/api/formula/listFormulas', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.setFormulasOfGroup', '/manager/api/formula/setFormulasOfGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.setFormulasOfServer', '/manager/api/formula/setFormulasOfServer', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.setGroupFormulaData', '/manager/api/formula/setGroupFormulaData', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.setSystemFormulaData', '/manager/api/formula/setSystemFormulaData', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.deregister', '/manager/api/sync/hub/deregister', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.generateAccessToken', '/manager/api/sync/hub/generateAccessToken', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.registerPeripheral', '/manager/api/sync/hub/registerPeripheral', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.registerPeripheralWithToken', '/manager/api/sync/hub/registerPeripheralWithToken', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.replaceTokens', '/manager/api/sync/hub/replaceTokens', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.setDetails', '/manager/api/sync/hub/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.storeAccessToken', '/manager/api/sync/hub/storeAccessToken', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.getAllPeripheralChannels', '/manager/api/sync/hub/getAllPeripheralChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.getManagerInfo', '/manager/api/sync/hub/getManagerInfo', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.isISSPeripheral', '/manager/api/sync/hub/isISSPeripheral', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.getAllPeripheralOrgs', '/manager/api/sync/hub/getAllPeripheralOrgs', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.migrateFromISSv1', '/manager/api/sync/hub/migrateFromISSv1', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.syncPeripheralChannels', '/manager/api/sync/hub/syncPeripheralChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.migrateFromISSv2', '/manager/api/sync/hub/migrateFromISSv2', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.removePeripheralChannelsToSync', '/manager/api/sync/hub/removePeripheralChannelsToSync', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.addPeripheralChannelsToSync', '/manager/api/sync/hub/addPeripheralChannelsToSync', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.regenerateSCCCredentials', '/manager/api/sync/hub/regenerateSCCCredentials', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.listPeripheralChannelsToSync', '/manager/api/sync/hub/listPeripheralChannelsToSync', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;


INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.addImageFile', '/manager/api/image/addImageFile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.delete', '/manager/api/image/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.deleteImageFile', '/manager/api/image/deleteImageFile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.getCustomValues', '/manager/api/image/getCustomValues', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.getDetails', '/manager/api/image/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.getPillar', '/manager/api/image/getPillar', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.getRelevantErrata', '/manager/api/image/getRelevantErrata', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.importContainerImage', '/manager/api/image/importContainerImage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.importImage', '/manager/api/image/importImage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.importOSImage', '/manager/api/image/importOSImage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.listImages', '/manager/api/image/listImages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.listPackages', '/manager/api/image/listPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.scheduleImageBuild', '/manager/api/image/scheduleImageBuild', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.setPillar', '/manager/api/image/setPillar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.create', '/manager/api/image/profile/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.delete', '/manager/api/image/profile/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.deleteCustomValues', '/manager/api/image/profile/deleteCustomValues', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.getCustomValues', '/manager/api/image/profile/getCustomValues', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.getDetails', '/manager/api/image/profile/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.listImageProfileTypes', '/manager/api/image/profile/listImageProfileTypes', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.listImageProfiles', '/manager/api/image/profile/listImageProfiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.setCustomValues', '/manager/api/image/profile/setCustomValues', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.setDetails', '/manager/api/image/profile/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.create', '/manager/api/image/store/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.delete', '/manager/api/image/store/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.getDetails', '/manager/api/image/store/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.listImageStoreTypes', '/manager/api/image/store/listImageStoreTypes', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.listImageStores', '/manager/api/image/store/listImageStores', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.setDetails', '/manager/api/image/store/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.keys.KeysHandler.addActivationKey', '/manager/api/kickstart/profile/keys/addActivationKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.keys.KeysHandler.getActivationKeys', '/manager/api/kickstart/profile/keys/getActivationKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.keys.KeysHandler.removeActivationKey', '/manager/api/kickstart/profile/keys/removeActivationKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.cloneProfile', '/manager/api/kickstart/cloneProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.createProfile', '/manager/api/kickstart/createProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.createProfileWithCustomUrl', '/manager/api/kickstart/createProfileWithCustomUrl', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.deleteProfile', '/manager/api/kickstart/deleteProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.disableProfile', '/manager/api/kickstart/disableProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.findKickstartForIp', '/manager/api/kickstart/findKickstartForIp', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.importFile', '/manager/api/kickstart/importFile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.importRawFile', '/manager/api/kickstart/importRawFile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.isProfileDisabled', '/manager/api/kickstart/isProfileDisabled', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.listAllIpRanges', '/manager/api/kickstart/listAllIpRanges', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.listAutoinstallableChannels', '/manager/api/kickstart/listAutoinstallableChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.listKickstartableChannels', '/manager/api/kickstart/listKickstartableChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.listKickstarts', '/manager/api/kickstart/listKickstarts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.renameProfile', '/manager/api/kickstart/renameProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.create', '/manager/api/kickstart/tree/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.delete', '/manager/api/kickstart/tree/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.deleteTreeAndProfiles', '/manager/api/kickstart/tree/deleteTreeAndProfiles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.getDetails', '/manager/api/kickstart/tree/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.list', '/manager/api/kickstart/tree/list', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.listInstallTypes', '/manager/api/kickstart/tree/listInstallTypes', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.rename', '/manager/api/kickstart/tree/rename', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.update', '/manager/api/kickstart/tree/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.assignScheduleToSystems', '/manager/api/maintenance/assignScheduleToSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.createCalendar', '/manager/api/maintenance/createCalendar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.createCalendarWithUrl', '/manager/api/maintenance/createCalendarWithUrl', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.createSchedule', '/manager/api/maintenance/createSchedule', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.deleteCalendar', '/manager/api/maintenance/deleteCalendar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.deleteSchedule', '/manager/api/maintenance/deleteSchedule', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.getCalendarDetails', '/manager/api/maintenance/getCalendarDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.getScheduleDetails', '/manager/api/maintenance/getScheduleDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.listCalendarLabels', '/manager/api/maintenance/listCalendarLabels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.listScheduleNames', '/manager/api/maintenance/listScheduleNames', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.listSystemsWithSchedule', '/manager/api/maintenance/listSystemsWithSchedule', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.refreshCalendar', '/manager/api/maintenance/refreshCalendar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.retractScheduleFromSystems', '/manager/api/maintenance/retractScheduleFromSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.updateCalendar', '/manager/api/maintenance/updateCalendar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.updateSchedule', '/manager/api/maintenance/updateSchedule', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.addToMaster', '/manager/api/sync/master/addToMaster', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.create', '/manager/api/sync/master/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.delete', '/manager/api/sync/master/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.getDefaultMaster', '/manager/api/sync/master/getDefaultMaster', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.getMaster', '/manager/api/sync/master/getMaster', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.getMasterByLabel', '/manager/api/sync/master/getMasterByLabel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.getMasterOrgs', '/manager/api/sync/master/getMasterOrgs', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.getMasters', '/manager/api/sync/master/getMasters', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.makeDefault', '/manager/api/sync/master/makeDefault', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.mapToLocal', '/manager/api/sync/master/mapToLocal', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.setCaCert', '/manager/api/sync/master/setCaCert', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.setMasterOrgs', '/manager/api/sync/master/setMasterOrgs', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.unsetDefaultMaster', '/manager/api/sync/master/unsetDefaultMaster', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.update', '/manager/api/sync/master/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.create', '/manager/api/org/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.createFirst', '/manager/api/org/createFirst', 'POST', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.delete', '/manager/api/org/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.getClmSyncPatchesConfig', '/manager/api/org/getClmSyncPatchesConfig', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.getDetails', '/manager/api/org/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.getPolicyForScapFileUpload', '/manager/api/org/getPolicyForScapFileUpload', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.getPolicyForScapResultDeletion', '/manager/api/org/getPolicyForScapResultDeletion', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.isContentStagingEnabled', '/manager/api/org/isContentStagingEnabled', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.isErrataEmailNotifsForOrg', '/manager/api/org/isErrataEmailNotifsForOrg', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.isOrgConfigManagedByOrgAdmin', '/manager/api/org/isOrgConfigManagedByOrgAdmin', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.listOrgs', '/manager/api/org/listOrgs', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.listUsers', '/manager/api/org/listUsers', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.migrateSystems', '/manager/api/org/migrateSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setClmSyncPatchesConfig', '/manager/api/org/setClmSyncPatchesConfig', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setContentStaging', '/manager/api/org/setContentStaging', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setErrataEmailNotifsForOrg', '/manager/api/org/setErrataEmailNotifsForOrg', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setOrgConfigManagedByOrgAdmin', '/manager/api/org/setOrgConfigManagedByOrgAdmin', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setPolicyForScapFileUpload', '/manager/api/org/setPolicyForScapFileUpload', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setPolicyForScapResultDeletion', '/manager/api/org/setPolicyForScapResultDeletion', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.transferSystems', '/manager/api/org/transferSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.updateName', '/manager/api/org/updateName', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.addTrust', '/manager/api/org/trusts/addTrust', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.getDetails', '/manager/api/org/trusts/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.listChannelsConsumed', '/manager/api/org/trusts/listChannelsConsumed', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.listChannelsProvided', '/manager/api/org/trusts/listChannelsProvided', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.listOrgs', '/manager/api/org/trusts/listOrgs', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.listSystemsAffected', '/manager/api/org/trusts/listSystemsAffected', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.listTrusts', '/manager/api/org/trusts/listTrusts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.removeTrust', '/manager/api/org/trusts/removeTrust', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.findByNvrea', '/manager/api/packages/findByNvrea', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.getDetails', '/manager/api/packages/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.getPackage', '/manager/api/packages/getPackage', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.getPackageUrl', '/manager/api/packages/getPackageUrl', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listChangelog', '/manager/api/packages/listChangelog', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listDependencies', '/manager/api/packages/listDependencies', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listFiles', '/manager/api/packages/listFiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listProvidingChannels', '/manager/api/packages/listProvidingChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listProvidingErrata', '/manager/api/packages/listProvidingErrata', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listSourcePackages', '/manager/api/packages/listSourcePackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.removePackage', '/manager/api/packages/removePackage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.removeSourcePackage', '/manager/api/packages/removeSourcePackage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.provider.PackagesProviderHandler.associateKey', '/manager/api/packages/provider/associateKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.provider.PackagesProviderHandler.list', '/manager/api/packages/provider/list', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.provider.PackagesProviderHandler.listKeys', '/manager/api/packages/provider/listKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.advanced', '/manager/api/packages/search/advanced', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.advancedWithActKey', '/manager/api/packages/search/advancedWithActKey', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.advancedWithChannel', '/manager/api/packages/search/advancedWithChannel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.name', '/manager/api/packages/search/name', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.nameAndDescription', '/manager/api/packages/search/nameAndDescription', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.nameAndSummary', '/manager/api/packages/search/nameAndSummary', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.subscriptionmatching.PinnedSubscriptionHandler.create', '/manager/api/subscriptionmatching/pinnedsubscription/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.subscriptionmatching.PinnedSubscriptionHandler.delete', '/manager/api/subscriptionmatching/pinnedsubscription/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.subscriptionmatching.PinnedSubscriptionHandler.list', '/manager/api/subscriptionmatching/pinnedsubscription/list', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.getDetails', '/manager/api/system/provisioning/powermanagement/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.getStatus', '/manager/api/system/provisioning/powermanagement/getStatus', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.listTypes', '/manager/api/system/provisioning/powermanagement/listTypes', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.powerOff', '/manager/api/system/provisioning/powermanagement/powerOff', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.powerOn', '/manager/api/system/provisioning/powermanagement/powerOn', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.reboot', '/manager/api/system/provisioning/powermanagement/reboot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.setDetails', '/manager/api/system/provisioning/powermanagement/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler.listLocales', '/manager/api/preferences/locale/listLocales', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler.listTimeZones', '/manager/api/preferences/locale/listTimeZones', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler.setLocale', '/manager/api/preferences/locale/setLocale', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler.setTimeZone', '/manager/api/preferences/locale/setTimeZone', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.addIpRange', '/manager/api/kickstart/profile/addIpRange', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.addScript', '/manager/api/kickstart/profile/addScript', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.compareActivationKeys', '/manager/api/kickstart/profile/compareActivationKeys', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.compareAdvancedOptions', '/manager/api/kickstart/profile/compareAdvancedOptions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.comparePackages', '/manager/api/kickstart/profile/comparePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.downloadKickstart', '/manager/api/kickstart/profile/downloadKickstart', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.downloadRenderedKickstart', '/manager/api/kickstart/profile/downloadRenderedKickstart', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getAdvancedOptions', '/manager/api/kickstart/profile/getAdvancedOptions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getAvailableRepositories', '/manager/api/kickstart/profile/getAvailableRepositories', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getCfgPreservation', '/manager/api/kickstart/profile/getCfgPreservation', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getChildChannels', '/manager/api/kickstart/profile/getChildChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getCustomOptions', '/manager/api/kickstart/profile/getCustomOptions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getKickstartTree', '/manager/api/kickstart/profile/getKickstartTree', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getRepositories', '/manager/api/kickstart/profile/getRepositories', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getUpdateType', '/manager/api/kickstart/profile/getUpdateType', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getVariables', '/manager/api/kickstart/profile/getVariables', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getVirtualizationType', '/manager/api/kickstart/profile/getVirtualizationType', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.listIpRanges', '/manager/api/kickstart/profile/listIpRanges', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.listScripts', '/manager/api/kickstart/profile/listScripts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.orderScripts', '/manager/api/kickstart/profile/orderScripts', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.removeIpRange', '/manager/api/kickstart/profile/removeIpRange', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.removeScript', '/manager/api/kickstart/profile/removeScript', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setAdvancedOptions', '/manager/api/kickstart/profile/setAdvancedOptions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setCfgPreservation', '/manager/api/kickstart/profile/setCfgPreservation', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setChildChannels', '/manager/api/kickstart/profile/setChildChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setCustomOptions', '/manager/api/kickstart/profile/setCustomOptions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setKickstartTree', '/manager/api/kickstart/profile/setKickstartTree', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setLogging', '/manager/api/kickstart/profile/setLogging', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setRepositories', '/manager/api/kickstart/profile/setRepositories', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setUpdateType', '/manager/api/kickstart/profile/setUpdateType', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setVariables', '/manager/api/kickstart/profile/setVariables', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setVirtualizationType', '/manager/api/kickstart/profile/setVirtualizationType', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.activateProxy', '/manager/api/proxy/activateProxy', 'POST', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.bootstrapProxy', '/manager/api/proxy/bootstrapProxy', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.containerConfig', '/manager/api/proxy/containerConfig', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.createMonitoringScout', '/manager/api/proxy/createMonitoringScout', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.deactivateProxy', '/manager/api/proxy/deactivateProxy', 'POST', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.isProxy', '/manager/api/proxy/isProxy', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.listAvailableProxyChannels', '/manager/api/proxy/listAvailableProxyChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.listProxies', '/manager/api/proxy/listProxies', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.listProxyClients', '/manager/api/proxy/listProxyClients', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringActionHandler.delete', '/manager/api/recurring/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringActionHandler.listByEntity', '/manager/api/recurring/listByEntity', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringActionHandler.lookupById', '/manager/api/recurring/lookupById', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringCustomStateHandler.create', '/manager/api/recurring/custom/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringCustomStateHandler.listAvailable', '/manager/api/recurring/custom/listAvailable', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringCustomStateHandler.update', '/manager/api/recurring/custom/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringHighstateHandler.create', '/manager/api/recurring/highstate/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringHighstateHandler.update', '/manager/api/recurring/highstate/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringPlaybookHandler.create', '/manager/api/recurring/playbook/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringPlaybookHandler.update', '/manager/api/recurring/playbook/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.accept', '/manager/api/saltkey/accept', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.acceptedList', '/manager/api/saltkey/acceptedList', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.delete', '/manager/api/saltkey/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.deniedList', '/manager/api/saltkey/deniedList', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.pendingList', '/manager/api/saltkey/pendingList', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.reject', '/manager/api/saltkey/reject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.rejectedList', '/manager/api/saltkey/rejectedList', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.archiveActions', '/manager/api/schedule/archiveActions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.cancelActions', '/manager/api/schedule/cancelActions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.deleteActions', '/manager/api/schedule/deleteActions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.failSystemAction', '/manager/api/schedule/failSystemAction', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listAllActions', '/manager/api/schedule/listAllActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listAllArchivedActions', '/manager/api/schedule/listAllArchivedActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listAllCompletedActions', '/manager/api/schedule/listAllCompletedActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listArchivedActions', '/manager/api/schedule/listArchivedActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listCompletedActions', '/manager/api/schedule/listCompletedActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listCompletedSystems', '/manager/api/schedule/listCompletedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listFailedActions', '/manager/api/schedule/listFailedActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listFailedSystems', '/manager/api/schedule/listFailedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listInProgressActions', '/manager/api/schedule/listInProgressActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listInProgressSystems', '/manager/api/schedule/listInProgressSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.rescheduleActions', '/manager/api/schedule/rescheduleActions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.addChannels', '/manager/api/system/config/addChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.createOrUpdatePath', '/manager/api/system/config/createOrUpdatePath', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.createOrUpdateSymlink', '/manager/api/system/config/createOrUpdateSymlink', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.deleteFiles', '/manager/api/system/config/deleteFiles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.deployAll', '/manager/api/system/config/deployAll', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.listChannels', '/manager/api/system/config/listChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.listFiles', '/manager/api/system/config/listFiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.lookupFileInfo', '/manager/api/system/config/lookupFileInfo', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.removeChannels', '/manager/api/system/config/removeChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.scheduleApplyConfigChannel', '/manager/api/system/config/scheduleApplyConfigChannel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.setChannels', '/manager/api/system/config/setChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.addOrRemoveAdmins', '/manager/api/systemgroup/addOrRemoveAdmins', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.addOrRemoveSystems', '/manager/api/systemgroup/addOrRemoveSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.create', '/manager/api/systemgroup/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.delete', '/manager/api/systemgroup/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.getDetails', '/manager/api/systemgroup/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listActiveSystemsInGroup', '/manager/api/systemgroup/listActiveSystemsInGroup', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listAdministrators', '/manager/api/systemgroup/listAdministrators', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listAllGroups', '/manager/api/systemgroup/listAllGroups', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listAssignedConfigChannels', '/manager/api/systemgroup/listAssignedConfigChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listAssignedFormuals', '/manager/api/systemgroup/listAssignedFormuals', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listGroupsWithNoAssociatedAdmins', '/manager/api/systemgroup/listGroupsWithNoAssociatedAdmins', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listInactiveSystemsInGroup', '/manager/api/systemgroup/listInactiveSystemsInGroup', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listSystems', '/manager/api/systemgroup/listSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listSystemsMinimal', '/manager/api/systemgroup/listSystemsMinimal', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.scheduleApplyErrataToActive', '/manager/api/systemgroup/scheduleApplyErrataToActive', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.subscribeConfigChannel', '/manager/api/systemgroup/subscribeConfigChannel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.unsubscribeConfigChannel', '/manager/api/systemgroup/unsubscribeConfigChannel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.update', '/manager/api/systemgroup/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.create', '/manager/api/sync/slave/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.delete', '/manager/api/sync/slave/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.getAllowedOrgs', '/manager/api/sync/slave/getAllowedOrgs', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.getSlave', '/manager/api/sync/slave/getSlave', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.getSlaveByName', '/manager/api/sync/slave/getSlaveByName', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.getSlaves', '/manager/api/sync/slave/getSlaves', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.setAllowedOrgs', '/manager/api/sync/slave/setAllowedOrgs', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.update', '/manager/api/sync/slave/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.addTagToSnapshot', '/manager/api/system/provisioning/snapshot/addTagToSnapshot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.deleteSnapshot', '/manager/api/system/provisioning/snapshot/deleteSnapshot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.deleteSnapshots', '/manager/api/system/provisioning/snapshot/deleteSnapshots', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.listSnapshotConfigFiles', '/manager/api/system/provisioning/snapshot/listSnapshotConfigFiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.listSnapshotPackages', '/manager/api/system/provisioning/snapshot/listSnapshotPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.listSnapshots', '/manager/api/system/provisioning/snapshot/listSnapshots', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.rollbackToSnapshot', '/manager/api/system/provisioning/snapshot/rollbackToSnapshot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.rollbackToTag', '/manager/api/system/provisioning/snapshot/rollbackToTag', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler.createOrUpdate', '/manager/api/kickstart/snippet/createOrUpdate', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler.delete', '/manager/api/kickstart/snippet/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler.listAll', '/manager/api/kickstart/snippet/listAll', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler.listCustom', '/manager/api/kickstart/snippet/listCustom', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler.listDefault', '/manager/api/kickstart/snippet/listDefault', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler.appendToSoftwareList', '/manager/api/kickstart/profile/software/appendToSoftwareList', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler.getSoftwareDetails', '/manager/api/kickstart/profile/software/getSoftwareDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler.getSoftwareList', '/manager/api/kickstart/profile/software/getSoftwareList', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler.setSoftwareDetails', '/manager/api/kickstart/profile/software/setSoftwareDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler.setSoftwareList', '/manager/api/kickstart/profile/software/setSoftwareList', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler.disable', '/manager/api/system/appstreams/disable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler.enable', '/manager/api/system/appstreams/enable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler.listModuleStreams', '/manager/api/system/appstreams/listModuleStreams', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.addFilePreservations', '/manager/api/kickstart/profile/system/addFilePreservations', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.addKeys', '/manager/api/kickstart/profile/system/addKeys', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.checkConfigManagement', '/manager/api/kickstart/profile/system/checkConfigManagement', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.checkRemoteCommands', '/manager/api/kickstart/profile/system/checkRemoteCommands', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.disableConfigManagement', '/manager/api/kickstart/profile/system/disableConfigManagement', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.disableRemoteCommands', '/manager/api/kickstart/profile/system/disableRemoteCommands', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.enableConfigManagement', '/manager/api/kickstart/profile/system/enableConfigManagement', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.enableRemoteCommands', '/manager/api/kickstart/profile/system/enableRemoteCommands', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.getLocale', '/manager/api/kickstart/profile/system/getLocale', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.getPartitioningScheme', '/manager/api/kickstart/profile/system/getPartitioningScheme', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.getRegistrationType', '/manager/api/kickstart/profile/system/getRegistrationType', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.getSELinux', '/manager/api/kickstart/profile/system/getSELinux', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.listFilePreservations', '/manager/api/kickstart/profile/system/listFilePreservations', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.listKeys', '/manager/api/kickstart/profile/system/listKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.removeFilePreservations', '/manager/api/kickstart/profile/system/removeFilePreservations', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.removeKeys', '/manager/api/kickstart/profile/system/removeKeys', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.setLocale', '/manager/api/kickstart/profile/system/setLocale', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.setPartitioningScheme', '/manager/api/kickstart/profile/system/setPartitioningScheme', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.setRegistrationType', '/manager/api/kickstart/profile/system/setRegistrationType', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.setSELinux', '/manager/api/kickstart/profile/system/setSELinux', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.addEntitlements', '/manager/api/system/addEntitlements', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.addNote', '/manager/api/system/addNote', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.bootstrap', '/manager/api/system/bootstrap', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.bootstrapWithPrivateSshKey', '/manager/api/system/bootstrapWithPrivateSshKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.changeProxy', '/manager/api/system/changeProxy', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.comparePackageProfile', '/manager/api/system/comparePackageProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.comparePackages', '/manager/api/system/comparePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.createPackageProfile', '/manager/api/system/createPackageProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.createSystemProfile', '/manager/api/system/createSystemProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.createSystemRecord', '/manager/api/system/createSystemRecord', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteCustomValues', '/manager/api/system/deleteCustomValues', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteGuestProfiles', '/manager/api/system/deleteGuestProfiles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteNote', '/manager/api/system/deleteNote', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteNotes', '/manager/api/system/deleteNotes', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deletePackageProfile', '/manager/api/system/deletePackageProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteSystem', '/manager/api/system/deleteSystem', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteSystems', '/manager/api/system/deleteSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteTagFromSnapshot', '/manager/api/system/deleteTagFromSnapshot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.downloadSystemId', '/manager/api/system/downloadSystemId', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getCoCoAttestationConfig', '/manager/api/system/getCoCoAttestationConfig', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getCoCoAttestationResultDetails', '/manager/api/system/getCoCoAttestationResultDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getConnectionPath', '/manager/api/system/getConnectionPath', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getCpu', '/manager/api/system/getCpu', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getCustomValues', '/manager/api/system/getCustomValues', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getDetails', '/manager/api/system/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getDevices', '/manager/api/system/getDevices', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getDmi', '/manager/api/system/getDmi', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getEntitlements', '/manager/api/system/getEntitlements', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getEventDetails', '/manager/api/system/getEventDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getEventHistory', '/manager/api/system/getEventHistory', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getId', '/manager/api/system/getId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getInstalledProducts', '/manager/api/system/getInstalledProducts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getKernelLivePatch', '/manager/api/system/getKernelLivePatch', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getLatestCoCoAttestationReport', '/manager/api/system/getLatestCoCoAttestationReport', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getMemory', '/manager/api/system/getMemory', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getMinionIdMap', '/manager/api/system/getMinionIdMap', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getName', '/manager/api/system/getName', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getNetwork', '/manager/api/system/getNetwork', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getNetworkDevices', '/manager/api/system/getNetworkDevices', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getNetworkForSystems', '/manager/api/system/getNetworkForSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getOsaPing', '/manager/api/system/getOsaPing', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getPillar', '/manager/api/system/getPillar', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getRegistrationDate', '/manager/api/system/getRegistrationDate', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getRelevantErrata', '/manager/api/system/getRelevantErrata', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getRelevantErrataByType', '/manager/api/system/getRelevantErrataByType', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getRunningKernel', '/manager/api/system/getRunningKernel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getScriptActionDetails', '/manager/api/system/getScriptActionDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getScriptResults', '/manager/api/system/getScriptResults', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getSubscribedBaseChannel', '/manager/api/system/getSubscribedBaseChannel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getSystemCurrencyMultipliers', '/manager/api/system/getSystemCurrencyMultipliers', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getSystemCurrencyScores', '/manager/api/system/getSystemCurrencyScores', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getUnscheduledErrata', '/manager/api/system/getUnscheduledErrata', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getUuid', '/manager/api/system/getUuid', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getVariables', '/manager/api/system/getVariables', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.hasTraditionalSystems', '/manager/api/system/hasTraditionalSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.isNvreInstalled', '/manager/api/system/isNvreInstalled', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listActivationKeys', '/manager/api/system/listActivationKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listActiveSystems', '/manager/api/system/listActiveSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listActiveSystemsDetails', '/manager/api/system/listActiveSystemsDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listAdministrators', '/manager/api/system/listAdministrators', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listAllInstallablePackages', '/manager/api/system/listAllInstallablePackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listCoCoAttestationReports', '/manager/api/system/listCoCoAttestationReports', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listDuplicatesByHostname', '/manager/api/system/listDuplicatesByHostname', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listDuplicatesByIp', '/manager/api/system/listDuplicatesByIp', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listDuplicatesByMac', '/manager/api/system/listDuplicatesByMac', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listEmptySystemProfiles', '/manager/api/system/listEmptySystemProfiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listExtraPackages', '/manager/api/system/listExtraPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listFqdns', '/manager/api/system/listFqdns', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listGroups', '/manager/api/system/listGroups', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listInactiveSystems', '/manager/api/system/listInactiveSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listInstalledPackages', '/manager/api/system/listInstalledPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listLatestAvailablePackage', '/manager/api/system/listLatestAvailablePackage', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listLatestInstallablePackages', '/manager/api/system/listLatestInstallablePackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listLatestUpgradablePackages', '/manager/api/system/listLatestUpgradablePackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listMigrationTargets', '/manager/api/system/listMigrationTargets', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listNewerInstalledPackages', '/manager/api/system/listNewerInstalledPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listNotes', '/manager/api/system/listNotes', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listOlderInstalledPackages', '/manager/api/system/listOlderInstalledPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listOutOfDateSystems', '/manager/api/system/listOutOfDateSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPackageProfiles', '/manager/api/system/listPackageProfiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPackageState', '/manager/api/system/listPackageState', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPackages', '/manager/api/system/listPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPackagesFromChannel', '/manager/api/system/listPackagesFromChannel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPackagesLockStatus', '/manager/api/system/listPackagesLockStatus', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPhysicalSystems', '/manager/api/system/listPhysicalSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSubscribableBaseChannels', '/manager/api/system/listSubscribableBaseChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSubscribableChildChannels', '/manager/api/system/listSubscribableChildChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSubscribedChildChannels', '/manager/api/system/listSubscribedChildChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSuggestedReboot', '/manager/api/system/listSuggestedReboot', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystemEvents', '/manager/api/system/listSystemEvents', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystemGroupsForSystemsWithEntitlement', '/manager/api/system/listSystemGroupsForSystemsWithEntitlement', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystems', '/manager/api/system/listSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystemsWithEntitlement', '/manager/api/system/listSystemsWithEntitlement', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystemsWithExtraPackages', '/manager/api/system/listSystemsWithExtraPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystemsWithPackage', '/manager/api/system/listSystemsWithPackage', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listUngroupedSystems', '/manager/api/system/listUngroupedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listUserSystems', '/manager/api/system/listUserSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listVirtualGuests', '/manager/api/system/listVirtualGuests', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listVirtualHosts', '/manager/api/system/listVirtualHosts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.obtainReactivationKey', '/manager/api/system/obtainReactivationKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.provisionSystem', '/manager/api/system/provisionSystem', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.provisionVirtualGuest', '/manager/api/system/provisionVirtualGuest', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.refreshPillar', '/manager/api/system/refreshPillar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.registerPeripheralServer', '/manager/api/system/registerPeripheralServer', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.removeEntitlements', '/manager/api/system/removeEntitlements', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleApplyErrata', '/manager/api/system/scheduleApplyErrata', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleApplyHighstate', '/manager/api/system/scheduleApplyHighstate', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleApplyStates', '/manager/api/system/scheduleApplyStates', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleCertificateUpdate', '/manager/api/system/scheduleCertificateUpdate', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleChangeChannels', '/manager/api/system/scheduleChangeChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleCoCoAttestation', '/manager/api/system/scheduleCoCoAttestation', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleDistUpgrade', '/manager/api/system/scheduleDistUpgrade', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleHardwareRefresh', '/manager/api/system/scheduleHardwareRefresh', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageInstall', '/manager/api/system/schedulePackageInstall', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageInstallByNevra', '/manager/api/system/schedulePackageInstallByNevra', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageLockChange', '/manager/api/system/schedulePackageLockChange', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageRefresh', '/manager/api/system/schedulePackageRefresh', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageRemove', '/manager/api/system/schedulePackageRemove', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageRemoveByNevra', '/manager/api/system/schedulePackageRemoveByNevra', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageUpdate', '/manager/api/system/schedulePackageUpdate', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleProductMigration', '/manager/api/system/scheduleProductMigration', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleReboot', '/manager/api/system/scheduleReboot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleSPMigration', '/manager/api/system/scheduleSPMigration', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleScriptRun', '/manager/api/system/scheduleScriptRun', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleSupportDataUpload', '/manager/api/system/scheduleSupportDataUpload', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleSyncPackagesWithSystem', '/manager/api/system/scheduleSyncPackagesWithSystem', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.searchByName', '/manager/api/system/searchByName', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.sendOsaPing', '/manager/api/system/sendOsaPing', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setBaseChannel', '/manager/api/system/setBaseChannel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setChildChannels', '/manager/api/system/setChildChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setCoCoAttestationConfig', '/manager/api/system/setCoCoAttestationConfig', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setCustomValues', '/manager/api/system/setCustomValues', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setDetails', '/manager/api/system/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setGroupMembership', '/manager/api/system/setGroupMembership', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setLockStatus', '/manager/api/system/setLockStatus', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setPillar', '/manager/api/system/setPillar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setPrimaryFqdn', '/manager/api/system/setPrimaryFqdn', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setPrimaryInterface', '/manager/api/system/setPrimaryInterface', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setProfileName', '/manager/api/system/setProfileName', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setupStaticNetwork', '/manager/api/system/setupStaticNetwork', 'POST', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setVariables', '/manager/api/system/setVariables', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.tagLatestSnapshot', '/manager/api/system/tagLatestSnapshot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.transitionDataForSystem', '/manager/api/system/transitionDataForSystem', 'POST', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.unentitle', '/manager/api/system/unentitle', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.updatePackageState', '/manager/api/system/updatePackageState', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.updatePeripheralServerInfo', '/manager/api/system/updatePeripheralServerInfo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.upgradeEntitlement', '/manager/api/system/upgradeEntitlement', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.whoRegistered', '/manager/api/system/whoRegistered', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.monitoring.SystemMonitoringHandler.listEndpoints', '/manager/api/system/monitoring/listEndpoints', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.deleteXccdfScan', '/manager/api/system/scap/deleteXccdfScan', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.getXccdfScanDetails', '/manager/api/system/scap/getXccdfScanDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.getXccdfScanRuleResults', '/manager/api/system/scap/getXccdfScanRuleResults', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.listXccdfScans', '/manager/api/system/scap/listXccdfScans', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.scheduleXccdfScan', '/manager/api/system/scap/scheduleXccdfScan', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.deviceDescription', '/manager/api/system/search/deviceDescription', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.deviceDriver', '/manager/api/system/search/deviceDriver', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.deviceId', '/manager/api/system/search/deviceId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.deviceVendorId', '/manager/api/system/search/deviceVendorId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.hostname', '/manager/api/system/search/hostname', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.ip', '/manager/api/system/search/ip', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.nameAndDescription', '/manager/api/system/search/nameAndDescription', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.uuid', '/manager/api/system/search/uuid', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.createExternalGroupToRoleMap', '/manager/api/user/external/createExternalGroupToRoleMap', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.createExternalGroupToSystemGroupMap', '/manager/api/user/external/createExternalGroupToSystemGroupMap', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.deleteExternalGroupToRoleMap', '/manager/api/user/external/deleteExternalGroupToRoleMap', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.deleteExternalGroupToSystemGroupMap', '/manager/api/user/external/deleteExternalGroupToSystemGroupMap', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.getDefaultOrg', '/manager/api/user/external/getDefaultOrg', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.getExternalGroupToRoleMap', '/manager/api/user/external/getExternalGroupToRoleMap', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.getExternalGroupToSystemGroupMap', '/manager/api/user/external/getExternalGroupToSystemGroupMap', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.getKeepTemporaryRoles', '/manager/api/user/external/getKeepTemporaryRoles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.getUseOrgUnit', '/manager/api/user/external/getUseOrgUnit', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.listExternalGroupToRoleMaps', '/manager/api/user/external/listExternalGroupToRoleMaps', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.listExternalGroupToSystemGroupMaps', '/manager/api/user/external/listExternalGroupToSystemGroupMaps', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.setDefaultOrg', '/manager/api/user/external/setDefaultOrg', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.setExternalGroupRoles', '/manager/api/user/external/setExternalGroupRoles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.setExternalGroupSystemGroups', '/manager/api/user/external/setExternalGroupSystemGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.setKeepTemporaryRoles', '/manager/api/user/external/setKeepTemporaryRoles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.setUseOrgUnit', '/manager/api/user/external/setUseOrgUnit', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.addAssignedSystemGroup', '/manager/api/user/addAssignedSystemGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.addAssignedSystemGroups', '/manager/api/user/addAssignedSystemGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.addDefaultSystemGroup', '/manager/api/user/addDefaultSystemGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.addDefaultSystemGroups', '/manager/api/user/addDefaultSystemGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.addRole', '/manager/api/user/addRole', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.create', '/manager/api/user/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.delete', '/manager/api/user/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.disable', '/manager/api/user/disable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.enable', '/manager/api/user/enable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.getCreateDefaultSystemGroup', '/manager/api/user/getCreateDefaultSystemGroup', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.getDetails', '/manager/api/user/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listAssignableRoles', '/manager/api/user/listAssignableRoles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listAssignedSystemGroups', '/manager/api/user/listAssignedSystemGroups', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listDefaultSystemGroups', '/manager/api/user/listDefaultSystemGroups', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listPermissions', '/manager/api/user/listPermissions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listRoles', '/manager/api/user/listRoles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listUsers', '/manager/api/user/listUsers', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.removeAssignedSystemGroup', '/manager/api/user/removeAssignedSystemGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.removeAssignedSystemGroups', '/manager/api/user/removeAssignedSystemGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.removeDefaultSystemGroup', '/manager/api/user/removeDefaultSystemGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.removeDefaultSystemGroups', '/manager/api/user/removeDefaultSystemGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.removeRole', '/manager/api/user/removeRole', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.setCreateDefaultSystemGroup', '/manager/api/user/setCreateDefaultSystemGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.setDetails', '/manager/api/user/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.setErrataNotifications', '/manager/api/user/setErrataNotifications', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.setReadOnly', '/manager/api/user/setReadOnly', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.usePamAuthentication', '/manager/api/user/usePamAuthentication', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserNotificationsHandler.deleteNotifications', '/manager/api/user/notifications/deleteNotifications', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserNotificationsHandler.getNotifications', '/manager/api/user/notifications/getNotifications', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserNotificationsHandler.setAllNotificationsRead', '/manager/api/user/notifications/setAllNotificationsRead', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserNotificationsHandler.setNotificationsRead', '/manager/api/user/notifications/setNotificationsRead', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.create', '/manager/api/virtualhostmanager/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.delete', '/manager/api/virtualhostmanager/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.getDetail', '/manager/api/virtualhostmanager/getDetail', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.getModuleParameters', '/manager/api/virtualhostmanager/getModuleParameters', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.listAvailableVirtualHostGathererModules', '/manager/api/virtualhostmanager/listAvailableVirtualHostGathererModules', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.listVirtualHostManagers', '/manager/api/virtualhostmanager/listVirtualHostManagers', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.backupConfiguration', '/manager/api/proxy/backupConfiguration', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
