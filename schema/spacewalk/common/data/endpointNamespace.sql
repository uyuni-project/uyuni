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

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/access-control' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/list_custom' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/access-control/show-access-group/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/access-control/create' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/list_namespaces' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/organizations/:orgId/users' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/organizations' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/organizations/:orgId/access-groups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/delete/:id' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/hub-details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals/register' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals/migrate-from-v1' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals/migrate-from-v2' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals/:id/sync-channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/access-tokens' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id/sync-channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/hub/access-tokens' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id/root-ca' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id/root-ca' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id/credentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id/sync-channels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/:id' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/:id/root-ca' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/:id/root-ca' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/migrate/v1' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/migrate/v2' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/access-tokens' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/access-tokens/:id/validity' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/access-tokens/:id' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/sync-bunch' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/audit/cve' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/cve' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/cve.csv' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/ListXccdf.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/ListXccdf.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/scap/Diff.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/scap/DiffSubmit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/scap/DiffSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/scap/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/scap/Search.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Overview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Overview.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Machine.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Machine.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Search.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/audit/confidential-computing' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/confidential-computing/listAttestations' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/contentmanagement/projects' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/contentmanagement/project/:label' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/contentmanagement/project' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/properties' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.sources' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.sources' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/mandatoryChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.sources' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/softwaresources' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/filters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/filters' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.environments' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/environments' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.environments' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/environments' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.environments' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/environments' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/build' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/promote' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/contentmanagement/filters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/filters' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/filters/:filterId' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/filters/:filterId' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/appstreams/:channelId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channels/modular' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/livepatching/products' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/livepatching/systems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/livepatching/kernels/product/:productId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/livepatching/kernels/system/:systemId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/Overview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/GlobalConfigChannelList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/GlobalConfigChannelList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/ChannelOverview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/ChannelFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/ChannelFilesSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/channel/ChannelSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/channel/ChannelSystemsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/channel/TargetSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/channel/TargetSystemsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/Copy2Systems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/Copy2Systems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/Copy2Channels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/Copy2Channels.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelUploadFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelUploadFiles.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelImportFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelImportFilesSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelCreateFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelCreateFiles.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/ChooseFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/ChooseFilesSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/ChooseSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/ChooseSystemsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/DeployConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/DeployConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/ChannelDeployTasks.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/ChannelDeployTasks.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/DeleteChannel.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/DeleteChannel.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/GlobalConfigFileList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/GlobalConfigFileList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/FileDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/FileDownload.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/ManageRevision.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/ManageRevisionSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareRevision.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareCopy.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareChannel.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareFile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareDeployed.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareDeployedSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/Diff.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/DownloadDiff.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/LocalConfigFileList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/LocalConfigFileList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/FileDetails.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/DeleteFile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/DeleteFile.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/DeleteRevision.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/DeleteRevision.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/CopyFileCentral.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/CopyFileCentralSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/CopyFileLocal.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/CopyFileSandbox.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/RevisionDeploy.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/GlobalRevisionDeploy.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/GlobalRevisionDeploySubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/GlobalRevisionDeployConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/GlobalRevisionDeployConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/system/ManagedSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/system/ManagedSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/system/TargetSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/system/TargetSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/system/TargetSystemsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/system/Summary.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/system/Summary.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.main' AND ns.access_mode = 'R'
    AND ep.endpoint = '/YourRhn.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.main' AND ns.access_mode = 'R'
    AND ep.endpoint = '/YourRhnClips.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.main' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/subscription-warning' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.tasks' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/tasks' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.inactive_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/inactive-systems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.critical_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/critical-systems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.pending_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/pending-actions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.latest_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/latest-errata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.system_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/systems-groups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.recent_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/recent-systems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/notification-messages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/notification-messages/data-unread' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/notification-messages/data-all' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/notification-messages/update-messages-status' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/notification-messages/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications.retry' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/notification-messages/retry/:notificationId' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/account/UserDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/UserDetailsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.address' AND ns.access_mode = 'R'
    AND ep.endpoint = '/account/Addresses.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.address' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/EditAddress.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.address' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/EditAddressSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.email' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/ChangeEmail.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.email' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/ChangeEmailSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.deactivate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/AccountDeactivation.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.deactivate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/AccountDeactivationSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.deactivate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/AccountDeactivationConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.deactivate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/AccountDeactivationConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.preferences' AND ns.access_mode = 'R'
    AND ep.endpoint = '/account/UserPreferences.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.preferences' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/PrefSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/OrgConfigDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/multiorg/OrgConfigDetails.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/Organizations.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/OrgTrustDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/channels/Provided.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/channels/Consumed.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/channels/Provided.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/channels/Consumed.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/yourorg/recurring-actions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions/:type/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/summary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/states' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/:id/delete' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/custom/execute' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/targets/:type/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/yourorg/custom' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/match' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/:channelId/content' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/apply' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/cm/images' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/build/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/cm/rebuild/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/inspect/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/inspect/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/patches/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/packages/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/buildlog/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/patches/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/packages/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/buildlog/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/cm/import' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/import' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/upload/image' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/type/:type' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/build/hosts/:type' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/activationkeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/cm/imageprofiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imageprofiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/cm/imageprofiles/create' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/find/:label' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/update/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/find/:label' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/find/' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/cm/imageprofiles/edit/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/channels/:token' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imagestores/type/:type' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/cm/imagestores' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imagestores' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/cm/imagestores/create' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/find/:label' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/find/' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/find' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/update/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/cm/imagestores/edit/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imagestores/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/cm/build' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/build/hosts/:type' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/build/:id' AND ep.http_method = 'POST'
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
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantBugErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantBugErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantEnhancementErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantEnhancementErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantSecurityErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantSecurityErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllBugErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllBugErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllEnhancementErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllEnhancementErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllSecurityErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllSecurityErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/details/Details.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/details/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/details/SystemsAffected.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/details/SystemsAffected.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/details/ErrataConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/details/ErrataConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/Search.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/Errata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/Errata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/Create.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/CreateSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/Edit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/ErrataChannelIntersection.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/ErrataChannelIntersection.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/SelectChannels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/SelectChannels.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/SelectChannelsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/PackagePush.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/PackagePush.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/PackagePushSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/DeleteBug.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackagesConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackagesConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackagePackagePush.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackagePackagePushSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/RemovePackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/RemovePackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddChannelPackagePush.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddChannelPackagePushSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/Edit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/Channels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/ChannelsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/ListPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/ListPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.notify' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/Notify.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.notify' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/NotifySubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/Delete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/PublishedDeleteConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/PublishedDeleteConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/CloneErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/CloneErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/CloneConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/CloneConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/keys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/keys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/keys/:target/accept' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/keys/:target/reject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/keys/:target/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/cmd' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/formula-catalog' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula-catalog/data' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/formula-catalog/formula/:name' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula-catalog/formula/:name/data' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.pending' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/PendingActions.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.pending' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/PendingActions.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.pending' AND ns.access_mode = 'W'
    AND ep.endpoint = '/schedule/PendingActionsDeleteConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.pending' AND ns.access_mode = 'W'
    AND ep.endpoint = '/schedule/PendingActionsDeleteConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.failed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/FailedActions.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.failed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/FailedActions.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.completed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/CompletedActions.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.completed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/CompletedActions.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.archived' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ArchivedActions.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.archived' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ArchivedActions.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ActionDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/CompletedSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/CompletedSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/InProgressSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/InProgressSystemsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/FailedSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/FailedSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ActionChains.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ActionChains.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ActionChain.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/action-chain-entries' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'W'
    AND ep.endpoint = '/schedule/ActionChain.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ajax/action-chain-save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/schedule/recurring-actions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/schedule/maintenance/schedules' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/schedule/list' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/calendar/names' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/schedule/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/schedule/:id/systems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/schedule/:id/setsystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/schedule/systems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/schedule/maintenance/calendars' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/calendar/list' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/calendar/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/events/:operation/:type/:startOfWeek/:date/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/schedule/:id/setsystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/schedule/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/schedule/delete' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/calendar/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/calendar/delete' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/calendar/refresh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/All.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/All.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Vendor.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Vendor.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Popular.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Popular.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Custom.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Custom.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Shared.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Shared.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Retired.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Retired.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/Details.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/Dependencies.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/ChangeLog.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/FileList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/NewVersions.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/InstalledSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/InstalledSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/TargetSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/TargetSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/software/packages/TargetSystemsConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/software/packages/TargetSystemsConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelDetail.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/ChannelDetail.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.managers' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/Managers.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.managers' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/Managers.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelSubscribers.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelSubscribers.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/TargetSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/TargetSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/ConfirmTargetSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/ConfirmTargetSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.appstreams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/AppStreams.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.appstreams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/AppStreams.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/software/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/software/Search.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/NameOverview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/NameOverview.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Manage.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Manage.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Edit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Edit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Delete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Delete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.managers' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Managers.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.managers' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Managers.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Clone.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Clone.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/Errata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ListRemove.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ListRemove.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ConfirmRemove.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ConfirmRemove.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/Add.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/AddRedHatErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/AddCustomErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/AddCustomErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/Clone.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/Clone.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ConfirmErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ConfirmErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/AddErrataToChannel.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/SyncErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/SyncErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ConfirmSyncPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ConfirmSyncPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/manage/errata/AddRedHatErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackageMenu.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesRemove.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesRemove.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesAdd.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesAdd.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesAddConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesAddConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompare.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompare.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompareMerge.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompareMerge.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompareMergeConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompareMergeConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/Subscribers.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/Subscribers.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/packages/list' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channels/owned' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/list/:binary/:kind' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/list/:binary/channel/:cid' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/packages/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Repositories.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Repositories.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Sync.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Sync.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/AssociatedChannels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/AssociatedChannels.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/manage/DistChannelMap.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/manage/DistChannelMap.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/DistChannelMapEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/DistChannelMapEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/DistChannelMapDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/DistChannelMapDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/index.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewAllLog.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewAllLog.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewLog.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewLog.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewCompletedLog.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewCompletedLog.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ListSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ListSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/list/all' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/list/virtual' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/list/all' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/list/virtual' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/BootstrapSystemList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateIPList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateIPList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateIPv6List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateIPv6List.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateHostName.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateHostName.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateMacAddress.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateMacAddress.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateSystemsCompare.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateSystemsCompare.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemCurrency.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemCurrency.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemEntitlements.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemEntitlementsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/csv/all' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/csv/virtualSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ListErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ListErrata.do' AND ep.http_method = 'POST'
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
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ErrataSystemsAffected.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ErrataSystemsAffected.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/ErrataList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/ErrataList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/PackageList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/PackageList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/ExtraPackagesList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/ExtraPackagesList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/profiles/ShowProfiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/profiles/ShowProfiles.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/profiles/CompareProfiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageUpgrade.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageUpgrade.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
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
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageInstall.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageInstall.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageSchedule.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageSchedule.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageRemove.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageRemove.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageRemoveSchedule.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageRemoveSchedule.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/RemoveConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/RemoveConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/UpgradableList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/UpgradableList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/UpgradeConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/UpgradeConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/InstallPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/InstallPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/InstallConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/InstallConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/VerifyPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/VerifyPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/VerifyConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/VerifyConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/LockPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/LockPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/RemoveExtraConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/RemoveExtraConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/profiles/Create.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/profiles/Create.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/profiles/DeleteProfile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/profiles/DeleteProfile.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/groups/Manage.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/groups/Manage.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemGroupList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemGroupList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/groups/ListRemove.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/groups/ListRemove.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.target_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/groups/Confirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.target_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/groups/Confirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.target_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/AddSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.target_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/AddSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/groups/Create.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/groups/Create.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/EditGroup.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/EditGroup.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channel/ssm/ChannelSubscriptions.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/channels/bases' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/channels/allowed-changes' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/channels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/upcoming-windows' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/channels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Deploy.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DeploySubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DeployConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DeployConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Diff.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DiffSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DiffConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DiffConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DeployFile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DeployFileSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DeployFileConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DeployFileConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DiffFile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DiffFileSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DiffFileConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DiffFileConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/ViewDiffResult.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Subscribe.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/SubscribeSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Rank.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Rank.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/SubscribeConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/SubscribeConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Unsubscribe.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/UnsubscribeSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/UnsubscribeConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/UnsubscribeConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Enable.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/EnableSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/EnableSummary.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/EnableSummary.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/SubscriptionsSetup.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/SubscriptionsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/RankChannels.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/KickstartableSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/KickstartableSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/ScheduleByProfile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/ScheduleByProfile.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/ScheduleByIp.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/ScheduleByIp.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/PowerManagementConfiguration.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/PowerManagementConfiguration.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/PowerManagementOperations.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/PowerManagementOperations.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/CreateProfileWizard.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/CreateProfileWizard.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/AdvancedModeCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/AdvancedModeCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/AdvancedModeEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartDeleteAdvanced.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartDeleteAdvanced.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartCloneAdvanced.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartCloneAdvanced.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartIpRangeEditAdvanced.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/EditVariables.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartClone.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartClone.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartIpRangeEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartIpRangeDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartIpRangeDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartDetailsEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartSoftwareEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartOptionsEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/SystemDetailsEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/Locale.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartPartitionEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartFilePreservationListsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartCryptoKeysListSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/Troubleshooting.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartPackagesEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/ActivationKeysSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartScriptCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartScriptCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartScriptEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartScriptEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartScriptDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartScriptDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartScriptOrder.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartScriptOrder.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/keys/CryptoKeyCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/keys/CryptoKeyCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/keys/CryptoKeyEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/keys/CryptoKeyEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/keys/CryptoKeyDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/keys/CryptoKeyDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/TreeCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/TreeCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/TreeEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/tree/EditVariables.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/TreeDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/TreeDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationListDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/audit/ScheduleXccdf.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/audit/ScheduleXccdf.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/audit/ScheduleXccdfConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/audit/XccdfDeleteConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/audit/XccdfDeleteConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/audit/ScheduleXccdf.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/audit/ScheduleXccdf.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/coco/settings' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/coco/settings' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/coco/schedule' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/coco/scheduleAction' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/coco/settings' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/coco/scheduleAction' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/coco/listAttestations' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/ssm/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/summary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/Index.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/Index.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/ConfirmSystemPreferences.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/Edit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/Edit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/EditNote.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/EditNote.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.hardware' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/HardwareRefresh.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.hardware' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/HardwareRefresh.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.hardware' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SystemHardware.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.support' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/support' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.support' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/uploadSupportData' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.refresh' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/SoftwareRefresh.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.refresh' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/SoftwareRefresh.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.refresh' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/Packages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.maintenance' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/maintenance' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.maintenance' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/schedule/:id/assign' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.maintenance' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/schedule/unassign' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/RemoteCommand.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/RemoteCommand.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SystemRemoteCommand.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SystemRemoteCommand.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/misc/CustomValue.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/misc/CustomValue.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/ListCustomData.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/SetCustomValue.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/SetCustomValue.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/CreateCustomData.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/UpdateCustomData.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/UpdateCustomData.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/DeleteCustomData.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/DeleteCustomData.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/RebootSystem.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/RebootSystem.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/RebootSystemConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/RebootSystemConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/RebootSystem.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/RebootSystem.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.connection' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/proxy' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.connection' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/details/proxy' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.connection' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/proxy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.transfer' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/MigrateSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.transfer' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/MigrateSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.transfer' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SystemMigrate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.transfer' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SystemMigrate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/DeleteConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/DeleteConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/DeleteConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/DeleteConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Index.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Index.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/SnapshotTags.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/SnapshotTags.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Groups.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Groups.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Channels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Channels.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Packages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/ConfigChannels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/ConfigChannels.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/ConfigFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/ConfigFiles.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/UnservablePackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/UnservablePackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Tags.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Tags.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/SnapshotTagCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/SnapshotTagCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/Rollback.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/Rollback.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/TagCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/TagCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/TagsDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/TagsDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/WorkWithGroup.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/GroupDetail.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/Delete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/Delete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/ListRemoveSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/ListRemoveSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/ListErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/ListErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/SystemsAffected.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/SystemsAffected.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.admins' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/AdminList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.admins' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/AdminList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/groups/details/custom' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/match' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/groups/details/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/summary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.highstate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/applyall' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/apply' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/groups/details/formulas' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formulas/list/:targetType/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/groups/details/formula/:formula_id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formulas/form/:targetType/:id/:formula_id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formulas/select' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formulas/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/groups/details/recurring-actions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions/:type/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/summary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/states' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/targets/:type/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/custom/execute' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/:id/delete' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/Overview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/Notes.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/RemoveFromSSM.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/AddToSSM.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.connection' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/Connection.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.proxy' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/ProxyClients.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.proxy' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/ProxyClients.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.proxy' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/proxy-config' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
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
    WHERE ns.namespace = 'systems.details.peripheral' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/mgr-server-info/:sid' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.peripheral' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/mgr-server-reportdb-newpw' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.activation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/Activation.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.activation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/Activation.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.hardware' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/SystemHardware.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/ErrataConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/ErrataConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/SystemChannels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/channels-available-base' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/channels/:channelId/accessible-children' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/mandatoryChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SPMigration.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SPMigration.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.appstreams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/appstreams' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.appstreams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/appstreams/:channelId/:appstream/packages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/appstreams/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/ptf/overview' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/ptf/list' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/details/ptf/allowedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/details/ptf/installed' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/details/ptf/install' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/ptf/available' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/ptf/scheduleAction' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/Overview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewCentralPaths.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewCentralPaths.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifyCentralPaths.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifyCentralPaths.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifyLocalPaths.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifyLocalPaths.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifySandboxPaths.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifySandboxPaths.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ConfigChannelList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ConfigChannelListUnsubscribeSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/RankChannels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.join' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/groups/Add.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.join' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/groups/Add.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/audit/ListScap.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/audit/ListScap.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/audit/XccdfDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/audit/ScapResultDownload.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/audit/RuleDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/coco/settings' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/details/coco/settings' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/coco/list' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.highstate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/applyall' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/packages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/packages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/packages/match' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/packages/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/apply' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/custom' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/match' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/apply' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/formulas' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formulas/list/:targetType/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/formula/:formula_id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formulas/form/:targetType/:id/:formula_id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formulas/select' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formulas/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/ansible/control-node' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/details/ansible/paths/:minionServerId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/ansible/playbooks' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/details/ansible/paths/:pathType/:minionServerId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/details/ansible/discover-playbooks/:pathId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/details/ansible/paths/playbook-contents' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/ansible/inventories' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/details/ansible/introspect-inventory/:pathId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/details/ansible/paths/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/details/ansible/paths/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/details/ansible/schedule-playbook' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/recurring-actions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions/:type/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/summary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/states' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/custom/execute' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/:id/delete' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/Pending.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/History.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/History.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/Event.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/Pending.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
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
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/Event.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/FailEventConfirmation.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/FailEventConfirmation.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.bootstrap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/bootstrap' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.bootstrap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/bootstrap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.bootstrap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/bootstrap-ssh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/proxy/container-config' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/container-config' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/container-config/:filename' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/Search.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/List.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Create.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Create.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Edit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Clone.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Clone.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/Edit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activation-keys/:tid/channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activation-keys/base-channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activation-keys/base-channels/:cid/child-channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/mandatoryChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/systems/List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/systems/List.do' AND ep.http_method = 'POST'
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
    WHERE ns.namespace = 'systems.activation_keys.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/packages/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/packages/Packages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/configuration/List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/configuration/List.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/configuration/Subscribe.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/configuration/Subscribe.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/configuration/Rank.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/configuration/Rank.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/groups/List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/groups/List.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/groups/Add.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/groups/Add.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Delete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Delete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/profiles/List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/profiles/Details.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/profiles/PackageList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/profiles/PackageList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/profiles/Details.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/profiles/Delete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/profiles/Delete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/customdata/CustomDataList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/customdata/CustomDataList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/customdata/CreateCustomKey.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/customdata/CreateCustomKey.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/customdata/UpdateCustomKey.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/customdata/UpdateCustomKey.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/customdata/DeleteCustomKey.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/customdata/DeleteCustomKey.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartOverview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Kickstarts.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Kickstarts.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/AdvancedModeEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartIpRangeEditAdvanced.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/EditVariables.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartFileDownloadAdvanced.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartIpRanges.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartIpRanges.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartIpRangeEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartDetailsEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartSoftwareEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartOptionsEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/SystemDetailsEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Locale.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartPartitionEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartFilePreservationLists.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartFilePreservationLists.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartCryptoKeysList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartCryptoKeysList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Troubleshooting.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartPackagesEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartPackageProfileEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartPackageProfileEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ActivationKeys.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ActivationKeys.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ActivationKeysList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ActivationKeysList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Scripts.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Scripts.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartFileDownload.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartFileDownload.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/keys/CryptoKeysList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ViewTrees.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ViewTrees.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/TreeEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/tree/EditVariables.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationListCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationListCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationListDeleteSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationListDeleteSingle.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationListConfirmDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationListConfirmDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/CustomSnippetList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/CustomSnippetList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/DefaultSnippetList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/DefaultSnippetList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetView.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/ScheduleWizard.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/ScheduleWizard.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/SessionCancel.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/SessionCancel.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/Variables.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/PowerManagement.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/PowerManagement.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/kickstart/SessionStatus.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/kickstart/SessionStatus.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/kickstart/Variables.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/vhms' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/vhms' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/vhms/modules' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/vhms/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/vhms/:id/nodes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/:id/refresh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/module/:name/params' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/update/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/delete/:id' AND ep.http_method = 'DELETE'
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
    WHERE ns.namespace = 'systems.software.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/product-migration' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/product-migration/dry-run/:actionId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/migration/computeChannels' AND http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/migration/schedule' AND http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id
    FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/rhn/manager/systems/ssm/appstreams' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id
    FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/rhn/manager/systems/ssm/appstreams/configure/:channelId' AND ep.http_method = 'GET'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id
    FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/rhn/manager/api/ssm/appstreams/save' AND ep.http_method = 'POST'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id
    FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/rhn/manager/api/system/appstreams/ssmEnable' AND ep.http_method = 'POST'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id
    FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/rhn/manager/api/system/appstreams/ssmDisable' AND ep.http_method = 'POST'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.active' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ActiveList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.active' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ActiveList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.disabled' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/DisabledList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.disabled' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/DisabledList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.all' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/UserList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.disabled' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EnableConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.disabled' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EnableConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/UserDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/Addresses.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/UserDetailsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/ChangeEmail.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/ChangeEmailSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/DisableUser.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/DisableUserSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EnableUser.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EnableUserSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/DeleteUser.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/DeleteUserSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/CreateUser.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EditAddress.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EditAddressSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/AssignedSystemGroups.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/AssignedSystemGroups.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/SystemsAdmined.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/SystemsAdminedSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ChannelPerms.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ChannelManagementPerms.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ChannelPermsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.preferences' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/UserPreferences.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.preferences' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/PrefSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/SystemGroupConfig.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ExtAuthSgMapping.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ExtAuthSgMapping.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/SystemGroupConfig.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/ExtAuthSgDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/ExtAuthSgDetails.do' AND ep.http_method = 'POST'
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
    WHERE ns.namespace = 'patches.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.get_all_peripheral_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/hub/getAllPeripheralChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.get_manager_info' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/hub/getManagerInfo' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.is_iss_peripheral' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/hub/isISSPeripheral' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.get_all_peripheral_orgs' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/hub/getAllPeripheralOrgs' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.list_peripheral_channels_to_sync' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/hub/listPeripheralChannelsToSync' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;


INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.migrate_from_iss_v1' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/migrateFromISSv1' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.sync_peripheral_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/syncPeripheralChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.migrate_from_iss_v2' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/migrateFromISSv2' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.remove_peripheral_channels_to_sync' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/removePeripheralChannelsToSync' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.add_peripheral_channels_to_sync' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/addPeripheralChannelsToSync' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.regenerate_scc_credentials' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/regenerateSCCCredentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.create_role' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/access/createRole' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.delete_role' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/access/deleteRole' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.grant_access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/access/grantAccess' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.list_namespaces' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/access/listNamespaces' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.list_permissions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/access/listPermissions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.list_roles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/access/listRoles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.revoke_access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/access/revokeAccess' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_configuration_deployment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addConfigurationDeployment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_errata_update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addErrataUpdate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_package_install' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addPackageInstall' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_package_removal' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addPackageRemoval' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_package_upgrade' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addPackageUpgrade' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_package_verify' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addPackageVerify' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_script_run' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addScriptRun' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_system_reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addSystemReboot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.create_chain' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/createChain' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.delete_chain' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/deleteChain' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.list_chain_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/actionchain/listChainActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.list_chains' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/actionchain/listChains' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.remove_action' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/removeAction' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.rename_chain' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/renameChain' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.schedule_chain' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/scheduleChain' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_apply_highstate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addApplyHighstate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_app_streams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addAppStreams' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_child_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addChildChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_config_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addConfigChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_entitlements' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addEntitlements' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addPackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_server_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addServerGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.check_config_deployment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/checkConfigDeployment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/clone' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.disable_config_deployment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/disableConfigDeployment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.enable_config_deployment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/enableConfigDeployment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activationkey/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.list_activated_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activationkey/listActivatedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.list_activation_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activationkey/listActivationKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.list_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activationkey/listChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.list_config_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activationkey/listConfigChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_app_streams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removeAppStreams' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_child_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removeChildChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_config_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removeConfigChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_entitlements' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removeEntitlements' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_server_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removeServerGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.set_config_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/setConfigChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.configuration.configure' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/configuration/configure' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.monitoring.disable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/monitoring/disable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.monitoring.enable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/monitoring/enable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.monitoring.get_status' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/monitoring/getStatus' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.payg.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/payg/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.payg.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/payg/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.payg.get_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/payg/getDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.payg.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/payg/list' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.payg.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/payg/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.create_ansible_path' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/createAnsiblePath' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.discover_playbooks' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/discoverPlaybooks' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.fetch_playbook_contents' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/fetchPlaybookContents' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.introspect_inventory' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/introspectInventory' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.list_ansible_paths' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/ansible/listAnsiblePaths' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.lookup_ansible_path_by_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/ansible/lookupAnsiblePathById' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.remove_ansible_path' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/removeAnsiblePath' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.schedule_playbook' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/schedulePlaybook' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.update_ansible_path' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/updateAnsiblePath' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.access.disable_user_restrictions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/access/disableUserRestrictions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.access.enable_user_restrictions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/access/enableUserRestrictions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.access.get_org_sharing' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/access/getOrgSharing' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.access.set_org_sharing' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/access/setOrgSharing' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.appstreams.is_modular' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/appstreams/isModular' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.appstreams.list_modular' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/appstreams/listModular' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.appstreams.list_module_streams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/appstreams/listModuleStreams' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_all_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listAllChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_manageable_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listManageableChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_my_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listMyChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_popular_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listPopularChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_retired_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listRetiredChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_shared_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listSharedChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_software_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listSoftwareChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_vendor_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listVendorChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.org.disable_access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/org/disableAccess' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.org.enable_access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/org/enableAccess' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.org.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/org/list' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.add_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/addPackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.add_repo_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/addRepoFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.align_metadata' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/alignMetadata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.apply_channel_state' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/applyChannelState' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.associate_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/associateRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.clear_repo_filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/clearRepoFilters' AND ep.http_method = 'POST'
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
    WHERE ns.namespace = 'api.channel.software.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/clone' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.create_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/createRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.disassociate_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/disassociateRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.get_channel_last_build_by_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/getChannelLastBuildById' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.get_repo_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/getRepoDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.get_repo_sync_cron_expression' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/getRepoSyncCronExpression' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.is_existing' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/isExisting' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.is_globally_subscribable' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/isGloballySubscribable' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.is_user_manageable' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/isUserManageable' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.is_user_subscribable' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/isUserSubscribable' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_all_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listAllPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_arches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listArches' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_channel_repos' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listChannelRepos' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_children' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listChildren' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listErrata' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_errata_by_type' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listErrataByType' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_errata_needing_sync' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listErrataNeedingSync' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_latest_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listLatestPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_packages_without_channel' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listPackagesWithoutChannel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_repo_filters' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listRepoFilters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_subscribed_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listSubscribedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_system_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listSystemChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_user_repos' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listUserRepos' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.merge_errata' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/mergeErrata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.merge_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/mergePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.regenerate_needed_cache' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/regenerateNeededCache' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.regenerate_yum_cache' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/regenerateYumCache' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.remove_errata' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/removeErrata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.remove_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/removePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.remove_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/removeRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.remove_repo_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/removeRepoFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_contact_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setContactDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_globally_subscribable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setGloballySubscribable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_repo_filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setRepoFilters' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_user_manageable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setUserManageable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_user_subscribable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setUserSubscribable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.sync_errata' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/syncErrata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.sync_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/syncRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.update_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/updateRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.update_repo_label' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/updateRepoLabel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.update_repo_ssl' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/updateRepoSsl' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.update_repo_url' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/updateRepoUrl' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.channel_exists' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/channelExists' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.create_or_update_path' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/createOrUpdatePath' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.create_or_update_symlink' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/createOrUpdateSymlink' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.delete_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/deleteChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.delete_file_revisions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/deleteFileRevisions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.delete_files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/deleteFiles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.deploy_all_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/deployAllSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.get_encoded_file_revision' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/getEncodedFileRevision' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.get_file_revision' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/getFileRevision' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.get_file_revisions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/getFileRevisions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.list_assigned_system_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/listAssignedSystemGroups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.list_files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/listFiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.list_globals' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/listGlobals' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.list_subscribed_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/listSubscribedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.lookup_channel_info' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/lookupChannelInfo' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.lookup_file_info' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/lookupFileInfo' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.schedule_file_comparisons' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/scheduleFileComparisons' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.sync_salt_files_on_disk' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/syncSaltFilesOnDisk' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.update_init_sls' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/updateInitSls' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.attach_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/attachFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.attach_source' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/attachSource' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.build_project' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/buildProject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.generate_project_difference' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/generateProjectDifference' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.generate_environment_difference' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/generateEnvironmentDifference' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.create_app_stream_filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/createAppStreamFilters' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.create_environment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/createEnvironment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.create_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/createFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.create_project' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/createProject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.detach_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/detachFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.detach_source' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/detachSource' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_environment_difference' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listEnvironmentDifference' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_filter_criteria' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listFilterCriteria' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_filters' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listFilters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_project_environments' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listProjectEnvironments' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_project_filters' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listProjectFilters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_project_sources' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listProjectSources' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_projects' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listProjects' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.lookup_environment' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/lookupEnvironment' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.lookup_filter' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/lookupFilter' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.lookup_project' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/lookupProject' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.lookup_source' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/lookupSource' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.promote_project' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/promoteProject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.remove_environment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/removeEnvironment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.remove_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/removeFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.remove_project' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/removeProject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.update_environment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/updateEnvironment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.update_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/updateFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.update_project' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/updateProject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.add_channel' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/addChannel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.add_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/addChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.add_credentials' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/addCredentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.delete_credentials' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/deleteCredentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.list_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/content/listChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.list_credentials' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/content/listCredentials' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.list_products' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/content/listProducts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.synchronize_channel_families' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/synchronizeChannelFamilies' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.synchronize_products' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/synchronizeProducts' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.synchronize_repositories' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/synchronizeRepositories' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.synchronize_subscriptions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/synchronizeSubscriptions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.keys.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/keys/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.keys.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/keys/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.keys.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/keys/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.keys.list_all_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/keys/listAllKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.keys.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/keys/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.custominfo.create_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/custominfo/createKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.custominfo.delete_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/custominfo/deleteKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.custominfo.list_all_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/custominfo/listAllKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.custominfo.update_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/custominfo/updateKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.audit.list_images_by_patch_status' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/listImagesByPatchStatus' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.audit.list_systems_by_patch_status' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/listSystemsByPatchStatus' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.delta.create_delta_image' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/delta/createDeltaImage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.delta.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/delta/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.delta.list_deltas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/delta/listDeltas' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.distchannel.list_default_maps' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/distchannel/listDefaultMaps' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.distchannel.list_maps_for_org' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/distchannel/listMapsForOrg' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.distchannel.set_map_for_org' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/distchannel/setMapForOrg' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.add_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/addPackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.applicable_to_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/applicableToChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.bugzilla_fixes' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/bugzillaFixes' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/clone' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.clone_as_original' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/cloneAsOriginal' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.clone_as_original_async' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/cloneAsOriginalAsync' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.clone_async' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/cloneAsync' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.find_by_cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/findByCve' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.list_affected_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/listAffectedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.list_cves' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/listCves' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.list_keywords' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/listKeywords' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.list_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/listPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.publish' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/publish' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.publish_as_original' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/publishAsOriginal' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.remove_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/removePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.filepreservation.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/filepreservation/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.filepreservation.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/filepreservation/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.filepreservation.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/filepreservation/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.filepreservation.list_all_file_preservations' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/filepreservation/listAllFilePreservations' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_combined_formula_data_by_server_ids' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getCombinedFormulaDataByServerIds' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_combined_formulas_by_server_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getCombinedFormulasByServerId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_formulas_by_group_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getFormulasByGroupId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_formulas_by_server_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getFormulasByServerId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_group_formula_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getGroupFormulaData' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_system_formula_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getSystemFormulaData' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.list_formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/listFormulas' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.set_formulas_of_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formula/setFormulasOfGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.set_formulas_of_server' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formula/setFormulasOfServer' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.set_group_formula_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formula/setGroupFormulaData' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.set_system_formula_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formula/setSystemFormulaData' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.deregister' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/deregister' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.generate_access_token' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/generateAccessToken' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.register_peripheral' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/registerPeripheral' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.register_peripheral_with_token' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/registerPeripheralWithToken' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.replace_tokens' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/replaceTokens' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.store_access_token' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/storeAccessToken' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.add_image_file' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/addImageFile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.delete_image_file' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/deleteImageFile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.get_custom_values' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/getCustomValues' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.get_pillar' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/getPillar' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.get_relevant_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/getRelevantErrata' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.import_container_image' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/importContainerImage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.import_image' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/importImage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.import_o_s_image' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/importOSImage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.list_images' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/listImages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.list_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/listPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.schedule_image_build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/scheduleImageBuild' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.set_pillar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/setPillar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/profile/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/profile/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.delete_custom_values' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/profile/deleteCustomValues' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.get_custom_values' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/profile/getCustomValues' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/profile/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.list_image_profile_types' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/profile/listImageProfileTypes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.list_image_profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/profile/listImageProfiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.set_custom_values' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/profile/setCustomValues' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/profile/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/store/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/store/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/store/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.list_image_store_types' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/store/listImageStoreTypes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.list_image_stores' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/store/listImageStores' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/store/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.keys.add_activation_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/keys/addActivationKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.keys.get_activation_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/keys/getActivationKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.keys.remove_activation_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/keys/removeActivationKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.clone_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/cloneProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.create_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/createProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.create_profile_with_custom_url' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/createProfileWithCustomUrl' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.delete_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/deleteProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.disable_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/disableProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.find_kickstart_for_ip' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/findKickstartForIp' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.import_file' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/importFile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.import_raw_file' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/importRawFile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.is_profile_disabled' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/isProfileDisabled' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.list_all_ip_ranges' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/listAllIpRanges' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.list_autoinstallable_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/listAutoinstallableChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.list_kickstartable_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/listKickstartableChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.list_kickstarts' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/listKickstarts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.rename_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/renameProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.delete_tree_and_profiles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/deleteTreeAndProfiles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/tree/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/list' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.list_install_types' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/tree/listInstallTypes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.rename' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/rename' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.assign_schedule_to_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/assignScheduleToSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.create_calendar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/createCalendar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.create_calendar_with_url' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/createCalendarWithUrl' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.create_schedule' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/createSchedule' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.delete_calendar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/deleteCalendar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.delete_schedule' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/deleteSchedule' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.get_calendar_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/getCalendarDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.get_schedule_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/getScheduleDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.list_calendar_labels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/listCalendarLabels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.list_schedule_names' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/listScheduleNames' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.list_systems_with_schedule' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/listSystemsWithSchedule' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.refresh_calendar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/refreshCalendar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.retract_schedule_from_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/retractScheduleFromSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.update_calendar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/updateCalendar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.update_schedule' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/updateSchedule' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.add_to_master' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/addToMaster' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.get_default_master' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/master/getDefaultMaster' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.get_master' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/master/getMaster' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.get_master_by_label' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/master/getMasterByLabel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.get_master_orgs' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/master/getMasterOrgs' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.get_masters' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/master/getMasters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.make_default' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/makeDefault' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.map_to_local' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/mapToLocal' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.set_ca_cert' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/setCaCert' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.set_master_orgs' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/setMasterOrgs' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.unset_default_master' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/unsetDefaultMaster' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.create_first' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/createFirst' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.get_clm_sync_patches_config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/getClmSyncPatchesConfig' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.get_policy_for_scap_file_upload' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/getPolicyForScapFileUpload' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.get_policy_for_scap_result_deletion' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/getPolicyForScapResultDeletion' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.is_content_staging_enabled' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/isContentStagingEnabled' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.is_errata_email_notifs_for_org' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/isErrataEmailNotifsForOrg' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.is_org_config_managed_by_org_admin' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/isOrgConfigManagedByOrgAdmin' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.list_orgs' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/listOrgs' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.list_users' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/listUsers' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.migrate_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/migrateSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_clm_sync_patches_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setClmSyncPatchesConfig' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_content_staging' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setContentStaging' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_errata_email_notifs_for_org' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setErrataEmailNotifsForOrg' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_org_config_managed_by_org_admin' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setOrgConfigManagedByOrgAdmin' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_policy_for_scap_file_upload' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setPolicyForScapFileUpload' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_policy_for_scap_result_deletion' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setPolicyForScapResultDeletion' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.transfer_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/transferSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.update_name' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/updateName' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.add_trust' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/trusts/addTrust' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.list_channels_consumed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/listChannelsConsumed' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.list_channels_provided' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/listChannelsProvided' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.list_orgs' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/listOrgs' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.list_systems_affected' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/listSystemsAffected' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.list_trusts' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/listTrusts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.remove_trust' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/trusts/removeTrust' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.find_by_nvrea' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/findByNvrea' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.get_package' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/getPackage' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.get_package_url' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/getPackageUrl' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_changelog' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listChangelog' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_dependencies' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listDependencies' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listFiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_providing_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listProvidingChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_providing_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listProvidingErrata' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_source_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listSourcePackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.remove_package' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/packages/removePackage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.remove_source_package' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/packages/removeSourcePackage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.provider.associate_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/packages/provider/associateKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.provider.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/packages/provider/list' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.provider.list_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/provider/listKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.advanced' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/advanced' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.advanced_with_act_key' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/advancedWithActKey' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.advanced_with_channel' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/advancedWithChannel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.name' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/name' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.name_and_description' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/nameAndDescription' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.name_and_summary' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/nameAndSummary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.subscriptionmatching.pinnedsubscription.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/subscriptionmatching/pinnedsubscription/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.subscriptionmatching.pinnedsubscription.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/subscriptionmatching/pinnedsubscription/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.subscriptionmatching.pinnedsubscription.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/subscriptionmatching/pinnedsubscription/list' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.get_status' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/getStatus' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.list_types' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/listTypes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.power_off' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/powerOff' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.power_on' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/powerOn' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/reboot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.preferences.locale.list_locales' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/preferences/locale/listLocales' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.preferences.locale.list_time_zones' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/preferences/locale/listTimeZones' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.preferences.locale.set_locale' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/preferences/locale/setLocale' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.preferences.locale.set_time_zone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/preferences/locale/setTimeZone' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.add_ip_range' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/addIpRange' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.add_script' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/addScript' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.compare_activation_keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/compareActivationKeys' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.compare_advanced_options' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/compareAdvancedOptions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.compare_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/comparePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.download_kickstart' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/downloadKickstart' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.download_rendered_kickstart' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/downloadRenderedKickstart' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_advanced_options' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getAdvancedOptions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_available_repositories' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getAvailableRepositories' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_cfg_preservation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getCfgPreservation' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_child_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getChildChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_custom_options' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getCustomOptions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_kickstart_tree' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getKickstartTree' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_repositories' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getRepositories' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_update_type' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getUpdateType' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_variables' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getVariables' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_virtualization_type' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getVirtualizationType' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.list_ip_ranges' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/listIpRanges' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.list_scripts' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/listScripts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.order_scripts' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/orderScripts' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.remove_ip_range' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/removeIpRange' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.remove_script' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/removeScript' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_advanced_options' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setAdvancedOptions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_cfg_preservation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setCfgPreservation' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_child_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setChildChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_custom_options' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setCustomOptions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_kickstart_tree' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setKickstartTree' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_logging' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setLogging' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_repositories' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setRepositories' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_update_type' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setUpdateType' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_variables' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setVariables' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_virtualization_type' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setVirtualizationType' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.activate_proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/activateProxy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.bootstrap_proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/bootstrapProxy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.container_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/containerConfig' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.create_monitoring_scout' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/createMonitoringScout' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.deactivate_proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/deactivateProxy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.is_proxy' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/proxy/isProxy' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.list_available_proxy_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/proxy/listAvailableProxyChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.list_proxies' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/proxy/listProxies' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.list_proxy_clients' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/proxy/listProxyClients' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.list_by_entity' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurring/listByEntity' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.lookup_by_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurring/lookupById' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.custom.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/custom/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.custom.list_available' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurring/custom/listAvailable' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.custom.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/custom/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.highstate.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/highstate/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.highstate.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/highstate/update' AND ep.http_method = 'POST'
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
    WHERE ns.namespace = 'api.saltkey.accept' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/saltkey/accept' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.accepted_list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/saltkey/acceptedList' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/saltkey/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.denied_list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/saltkey/deniedList' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.pending_list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/saltkey/pendingList' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.reject' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/saltkey/reject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.rejected_list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/saltkey/rejectedList' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.archive_actions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/schedule/archiveActions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.cancel_actions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/schedule/cancelActions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.delete_actions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/schedule/deleteActions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.fail_system_action' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/schedule/failSystemAction' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_all_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listAllActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_all_archived_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listAllArchivedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_all_completed_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listAllCompletedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_archived_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listArchivedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_completed_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listCompletedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_completed_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listCompletedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_failed_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listFailedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_failed_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listFailedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_in_progress_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listInProgressActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_in_progress_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listInProgressSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.reschedule_actions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/schedule/rescheduleActions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.add_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/addChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.create_or_update_path' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/createOrUpdatePath' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.create_or_update_symlink' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/createOrUpdateSymlink' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.delete_files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/deleteFiles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.deploy_all' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/deployAll' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.list_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/config/listChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.list_files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/config/listFiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.lookup_file_info' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/config/lookupFileInfo' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.remove_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/removeChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.schedule_apply_config_channel' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/scheduleApplyConfigChannel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.set_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/setChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.add_or_remove_admins' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/addOrRemoveAdmins' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.add_or_remove_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/addOrRemoveSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_active_systems_in_group' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listActiveSystemsInGroup' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_administrators' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listAdministrators' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_all_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listAllGroups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_assigned_config_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listAssignedConfigChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_assigned_formuals' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listAssignedFormuals' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_groups_with_no_associated_admins' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listGroupsWithNoAssociatedAdmins' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_inactive_systems_in_group' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listInactiveSystemsInGroup' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_systems_minimal' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listSystemsMinimal' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.schedule_apply_errata_to_active' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/scheduleApplyErrataToActive' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.subscribe_config_channel' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/subscribeConfigChannel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.unsubscribe_config_channel' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/unsubscribeConfigChannel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/slave/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/slave/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.get_allowed_orgs' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/slave/getAllowedOrgs' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.get_slave' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/slave/getSlave' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.get_slave_by_name' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/slave/getSlaveByName' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.get_slaves' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/slave/getSlaves' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.set_allowed_orgs' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/slave/setAllowedOrgs' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/slave/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.add_tag_to_snapshot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/addTagToSnapshot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.delete_snapshot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/deleteSnapshot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.delete_snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/deleteSnapshots' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.list_snapshot_config_files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/listSnapshotConfigFiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.list_snapshot_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/listSnapshotPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.list_snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/listSnapshots' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.rollback_to_snapshot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/rollbackToSnapshot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.rollback_to_tag' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/rollbackToTag' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.snippet.create_or_update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/snippet/createOrUpdate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.snippet.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/snippet/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.snippet.list_all' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/snippet/listAll' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.snippet.list_custom' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/snippet/listCustom' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.snippet.list_default' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/snippet/listDefault' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.software.append_to_software_list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/software/appendToSoftwareList' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.software.get_software_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/software/getSoftwareDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.software.get_software_list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/software/getSoftwareList' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.software.set_software_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/software/setSoftwareDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.software.set_software_list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/software/setSoftwareList' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.appstreams.disable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/appstreams/disable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.appstreams.enable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/appstreams/enable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.appstreams.list_module_streams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/appstreams/listModuleStreams' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.add_file_preservations' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/addFilePreservations' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.add_keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/addKeys' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.check_config_management' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/checkConfigManagement' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.check_remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/checkRemoteCommands' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.disable_config_management' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/disableConfigManagement' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.disable_remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/disableRemoteCommands' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.enable_config_management' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/enableConfigManagement' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.enable_remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/enableRemoteCommands' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.get_locale' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/getLocale' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.get_partitioning_scheme' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/getPartitioningScheme' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.get_registration_type' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/getRegistrationType' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.get_selinux' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/getSELinux' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.list_file_preservations' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/listFilePreservations' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.list_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/listKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.remove_file_preservations' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/removeFilePreservations' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.remove_keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/removeKeys' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.set_locale' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/setLocale' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.set_partitioning_scheme' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/setPartitioningScheme' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.set_registration_type' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/setRegistrationType' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.set_selinux' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/setSELinux' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.add_entitlements' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/addEntitlements' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.add_note' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/addNote' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.bootstrap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/bootstrap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.bootstrap_with_private_ssh_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/bootstrapWithPrivateSshKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.change_proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/changeProxy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.compare_package_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/comparePackageProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.compare_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/comparePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.create_package_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/createPackageProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.create_system_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/createSystemProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.create_system_record' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/createSystemRecord' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_custom_values' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteCustomValues' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_guest_profiles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteGuestProfiles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_note' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteNote' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_notes' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteNotes' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_package_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deletePackageProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_system' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteSystem' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_tag_from_snapshot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteTagFromSnapshot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.download_system_id' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/downloadSystemId' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_co_co_attestation_config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getCoCoAttestationConfig' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_co_co_attestation_result_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getCoCoAttestationResultDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_connection_path' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getConnectionPath' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_cpu' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getCpu' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_custom_values' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getCustomValues' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_devices' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getDevices' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_dmi' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getDmi' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_entitlements' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getEntitlements' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_event_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getEventDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_event_history' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getEventHistory' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_installed_products' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getInstalledProducts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_kernel_live_patch' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getKernelLivePatch' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_latest_co_co_attestation_report' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getLatestCoCoAttestationReport' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_memory' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getMemory' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_minion_id_map' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getMinionIdMap' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_name' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getName' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_network' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getNetwork' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_network_devices' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getNetworkDevices' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_network_for_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getNetworkForSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_osa_ping' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getOsaPing' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_pillar' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getPillar' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_registration_date' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getRegistrationDate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_relevant_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getRelevantErrata' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_relevant_errata_by_type' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getRelevantErrataByType' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_running_kernel' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getRunningKernel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_script_action_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getScriptActionDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_script_results' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/getScriptResults' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_subscribed_base_channel' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getSubscribedBaseChannel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_system_currency_multipliers' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getSystemCurrencyMultipliers' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_system_currency_scores' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getSystemCurrencyScores' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_unscheduled_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getUnscheduledErrata' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_uuid' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getUuid' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_variables' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getVariables' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.has_traditional_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/hasTraditionalSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.is_nvre_installed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/isNvreInstalled' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_activation_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listActivationKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_active_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listActiveSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_active_systems_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listActiveSystemsDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_administrators' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listAdministrators' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_all_installable_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listAllInstallablePackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_co_co_attestation_reports' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listCoCoAttestationReports' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_duplicates_by_hostname' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listDuplicatesByHostname' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_duplicates_by_ip' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listDuplicatesByIp' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_duplicates_by_mac' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listDuplicatesByMac' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_empty_system_profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listEmptySystemProfiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_extra_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listExtraPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_fqdns' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listFqdns' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listGroups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_inactive_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listInactiveSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_installed_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listInstalledPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_latest_available_package' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listLatestAvailablePackage' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_latest_installable_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listLatestInstallablePackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_latest_upgradable_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listLatestUpgradablePackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_migration_targets' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listMigrationTargets' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_newer_installed_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listNewerInstalledPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_notes' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listNotes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_older_installed_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listOlderInstalledPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_out_of_date_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listOutOfDateSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_package_profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPackageProfiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_package_state' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPackageState' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_packages_from_channel' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPackagesFromChannel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_packages_lock_status' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPackagesLockStatus' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_physical_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPhysicalSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_subscribable_base_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSubscribableBaseChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_subscribable_child_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSubscribableChildChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_subscribed_child_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSubscribedChildChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_suggested_reboot' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSuggestedReboot' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_system_events' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystemEvents' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_system_groups_for_systems_with_entitlement' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystemGroupsForSystemsWithEntitlement' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_systems_with_entitlement' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystemsWithEntitlement' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_systems_with_extra_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystemsWithExtraPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_systems_with_package' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystemsWithPackage' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_ungrouped_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listUngroupedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_user_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listUserSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_virtual_guests' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listVirtualGuests' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_virtual_hosts' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listVirtualHosts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.obtain_reactivation_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/obtainReactivationKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provision_system' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisionSystem' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provision_virtual_guest' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisionVirtualGuest' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.refresh_pillar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/refreshPillar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.register_peripheral_server' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/registerPeripheralServer' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.remove_entitlements' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/removeEntitlements' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_apply_errata' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleApplyErrata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_apply_highstate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleApplyHighstate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_apply_states' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleApplyStates' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_certificate_update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleCertificateUpdate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_change_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleChangeChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_co_co_attestation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleCoCoAttestation' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_dist_upgrade' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleDistUpgrade' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_hardware_refresh' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleHardwareRefresh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_install' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageInstall' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_install_by_nevra' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageInstallByNevra' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_lock_change' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageLockChange' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_refresh' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageRefresh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_remove' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageRemove' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_remove_by_nevra' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageRemoveByNevra' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageUpdate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_product_migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleProductMigration' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleReboot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_s_p_migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleSPMigration' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_script_run' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleScriptRun' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_support_data_upload' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleSupportDataUpload' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_sync_packages_with_system' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleSyncPackagesWithSystem' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search_by_name' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/searchByName' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.send_osa_ping' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/sendOsaPing' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_base_channel' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setBaseChannel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_child_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setChildChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_co_co_attestation_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setCoCoAttestationConfig' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_custom_values' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setCustomValues' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_group_membership' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setGroupMembership' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_lock_status' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setLockStatus' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_pillar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setPillar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_primary_fqdn' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setPrimaryFqdn' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_primary_interface' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setPrimaryInterface' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_profile_name' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setProfileName' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_variables' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setVariables' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.tag_latest_snapshot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/tagLatestSnapshot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.unentitle' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/unentitle' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.update_package_state' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/updatePackageState' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.update_peripheral_server_info' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/updatePeripheralServerInfo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.upgrade_entitlement' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/upgradeEntitlement' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.who_registered' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/whoRegistered' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.monitoring.list_endpoints' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/monitoring/listEndpoints' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.delete_xccdf_scan' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scap/deleteXccdfScan' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.get_xccdf_scan_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/scap/getXccdfScanDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.get_xccdf_scan_rule_results' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/scap/getXccdfScanRuleResults' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.list_xccdf_scans' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/scap/listXccdfScans' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.schedule_xccdf_scan' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scap/scheduleXccdfScan' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.device_description' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/deviceDescription' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.device_driver' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/deviceDriver' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.device_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/deviceId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.device_vendor_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/deviceVendorId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.hostname' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/hostname' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.ip' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/ip' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.name_and_description' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/nameAndDescription' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.uuid' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/uuid' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.create_external_group_to_role_map' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/createExternalGroupToRoleMap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.create_external_group_to_system_group_map' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/createExternalGroupToSystemGroupMap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.delete_external_group_to_role_map' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/deleteExternalGroupToRoleMap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.delete_external_group_to_system_group_map' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/deleteExternalGroupToSystemGroupMap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.get_default_org' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/getDefaultOrg' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.get_external_group_to_role_map' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/getExternalGroupToRoleMap' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.get_external_group_to_system_group_map' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/getExternalGroupToSystemGroupMap' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.get_keep_temporary_roles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/getKeepTemporaryRoles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.get_use_org_unit' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/getUseOrgUnit' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.list_external_group_to_role_maps' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/listExternalGroupToRoleMaps' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.list_external_group_to_system_group_maps' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/listExternalGroupToSystemGroupMaps' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.set_default_org' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/setDefaultOrg' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.set_external_group_roles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/setExternalGroupRoles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.set_external_group_system_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/setExternalGroupSystemGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.set_keep_temporary_roles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/setKeepTemporaryRoles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.set_use_org_unit' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/setUseOrgUnit' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.add_assigned_system_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/addAssignedSystemGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.add_assigned_system_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/addAssignedSystemGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.add_default_system_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/addDefaultSystemGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.add_default_system_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/addDefaultSystemGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.add_role' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/addRole' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.disable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/disable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.enable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/enable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.get_create_default_system_group' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/getCreateDefaultSystemGroup' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_assignable_roles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listAssignableRoles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_assigned_system_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listAssignedSystemGroups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_default_system_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listDefaultSystemGroups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_permissions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listPermissions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_roles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listRoles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_users' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listUsers' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.remove_assigned_system_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/removeAssignedSystemGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.remove_assigned_system_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/removeAssignedSystemGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.remove_default_system_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/removeDefaultSystemGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.remove_default_system_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/removeDefaultSystemGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.remove_role' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/removeRole' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.set_create_default_system_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/setCreateDefaultSystemGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.set_errata_notifications' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/setErrataNotifications' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.set_read_only' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/setReadOnly' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.use_pam_authentication' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/usePamAuthentication' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.notifications.delete_notifications' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/notifications/deleteNotifications' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.notifications.get_notifications' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/notifications/getNotifications' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.notifications.set_all_notifications_read' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/notifications/setAllNotificationsRead' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.notifications.set_notifications_read' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/notifications/setNotificationsRead' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/virtualhostmanager/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/virtualhostmanager/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.get_detail' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/virtualhostmanager/getDetail' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.get_module_parameters' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/virtualhostmanager/getModuleParameters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.list_available_virtual_host_gatherer_modules' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/virtualhostmanager/listAvailableVirtualHostGathererModules' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.list_virtual_host_managers' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/virtualhostmanager/listVirtualHostManagers' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.backup_configuration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/backupConfiguration' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
