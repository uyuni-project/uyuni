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

-- RBAC Endpoints for CLM Recurring Actions
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.clm.create_build', 'W', 'Create a new recurring CLM build action.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringContentLifecycleHandler.createBuild',
            '/manager/api/recurring/clm/createBuild', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.clm.create_build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/clm/createBuild' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.clm.update_build', 'W', 'Update a recurring CLM build action.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringContentLifecycleHandler.updateBuild',
            '/manager/api/recurring/clm/updateBuild', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.clm.update_build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/clm/updateBuild' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.clm.create_promote', 'W', 'Create a new recurring CLM promote action.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringContentLifecycleHandler.createPromote',
            '/manager/api/recurring/clm/createPromote', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.clm.create_promote' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/clm/createPromote' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.clm.update_promote', 'W', 'Update a recurring CLM promote action.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringContentLifecycleHandler.updatePromote',
            '/manager/api/recurring/clm/updatePromote', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.clm.update_promote' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/clm/updatePromote' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.clm.list_by_project', 'R', 'List all recurring actions associated with a Content Project.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringContentLifecycleHandler.listByProject',
            '/manager/api/recurring/clm/listByProject', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.clm.list_by_project' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurring/clm/listByProject' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.recurring.clm.create_build',
        'api.recurring.clm.update_build',
        'api.recurring.clm.create_promote',
        'api.recurring.clm.update_promote',
        'api.recurring.clm.list_by_project'
    )
    ON CONFLICT (group_id, namespace_id) DO NOTHING;
