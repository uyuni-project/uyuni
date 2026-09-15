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
--

DROP VIEW rhnUserActionOverview;
DROP VIEW rhnActionOverview;

CREATE OR REPLACE VIEW rhnActionOverview (
    org_id,
    action_id,
    type_id,
    type_name,
    name,
    scheduler,
    scheduler_login,
    earliest_action,
    archived
) AS
SELECT
    A.org_id,
    A.id,
    AT.id,
    AT.name,
    A.name,
    A.scheduler,
    U.login,
    A.earliest_action,
    A.archived
FROM
    rhnActionType AT,
    rhnAction A
    LEFT JOIN web_contact U ON A.scheduler = U.id
WHERE
    A.action_type = AT.id
ORDER BY
    A.earliest_action;

CREATE OR REPLACE VIEW rhnUserActionOverview AS
SELECT
    ao.org_id AS org_id,
    usp.user_id AS user_id,
    ao.action_id AS id,
    ao.type_name AS type_name,
    ao.scheduler AS scheduler,
    ao.earliest_action AS earliest_action,
    COALESCE(ao.name, ao.type_name) AS action_name,
    sa.status AS action_status_id,
    astat.name AS action_status,
    COUNT(sa.action_id) AS tally,
    ao.archived AS archived
FROM
    rhnActionOverview ao
    LEFT JOIN rhnServerAction sa ON ao.action_id = sa.action_id
    LEFT JOIN rhnActionStatus astat ON sa.status = astat.id
    LEFT JOIN rhnUserServerPerms usp ON sa.server_id = usp.server_id
GROUP BY
    ao.org_id,
    usp.user_id,
    ao.action_id,
    ao.type_name,
    ao.scheduler,
    ao.earliest_action,
    COALESCE(ao.name, ao.type_name),
    sa.status,
    astat.name,
    ao.archived
ORDER BY
    earliest_action;

