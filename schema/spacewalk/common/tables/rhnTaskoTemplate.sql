--
-- Copyright (c) 2010--2012 Red Hat, Inc.
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


CREATE TABLE rhnTaskoTemplate
(
    id          NUMERIC NOT NULL
                    CONSTRAINT rhn_tasko_template_id_pk PRIMARY KEY,
    bunch_id    NUMERIC NOT NULL
                    CONSTRAINT rhn_tasko_template_bunch_fk
                    REFERENCES rhnTaskoBunch (id),
    task_id     NUMERIC NOT NULL
                    CONSTRAINT rhn_tasko_template_task_fk
                    REFERENCES rhnTaskoTask (id),
    ordering    NUMERIC NOT NULL,
    start_if    VARCHAR(10),
    created     TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL,
    modified    TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL
)
;

CREATE SEQUENCE rhn_tasko_template_id_seq;

ALTER TABLE rhnTaskoTemplate
    ADD CONSTRAINT tasko_template_ordering_uq UNIQUE (bunch_id, ordering);
