--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.webui.controllers.SwaggerController.createView', '/manager/swagger', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/swagger' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.webui.controllers.SwaggerController.getNamespaces', '/manager/api/openapi/namespaces', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/openapi/namespaces' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.webui.controllers.SwaggerController.getSpec', '/manager/api/openapi/:namespace', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/openapi/:namespace' AND http_method = 'GET');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'docs.swagger', 'R', 'View the Swagger API documentation UI'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'docs.swagger' AND access_mode = 'R');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'docs.swagger' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/swagger' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'docs.swagger' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/openapi/namespaces' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'docs.swagger' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/openapi/:namespace' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

