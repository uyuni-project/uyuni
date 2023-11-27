--
-- Copyright (c) 2008--2012 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnTaskQueue
(
    id         NUMERIC NOT NULL
                   CONSTRAINT rhn_task_queue_id_pk PRIMARY KEY,
    org_id     NUMERIC NOT NULL
                   CONSTRAINT rhn_task_queue_org_id_fk
                       REFERENCES web_customer (id)
                       ON DELETE CASCADE,
    task_name      VARCHAR(64) NOT NULL,
    task_data      NUMERIC,
    priority       NUMERIC
                   DEFAULT (0),
    earliest       TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL
)
;

CREATE INDEX rhn_task_queue_org_task_idx
    ON rhnTaskQueue (org_id, task_name);

CREATE INDEX rhn_task_queue_earliest
    ON rhnTaskQueue (earliest);

CREATE SEQUENCE rhn_task_queue_id_seq START WITH 1;
