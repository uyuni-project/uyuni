--
-- Copyright (c) 2008--2017 Red Hat, Inc.
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


CREATE TABLE rhnErrata
(
    id                NUMERIC NOT NULL
                          CONSTRAINT rhn_errata_id_pk PRIMARY KEY
                          ,
    advisory          VARCHAR(100) NOT NULL,
    advisory_type     VARCHAR(32) NOT NULL
                          CONSTRAINT rhn_errata_adv_type_ck
                              CHECK (advisory_type in ('Bug Fix Advisory',
                                                       'Product Enhancement Advisory',
                                                       'Security Advisory')),
    advisory_name     VARCHAR(100) NOT NULL,
    advisory_rel      NUMERIC NOT NULL,
    advisory_status   VARCHAR(32) NOT NULL DEFAULT('final')
                          CONSTRAINT rhn_errata_adv_status_ck
                              CHECK (advisory_status in ('final', 'stable', 'testing',
                                                         'pending', 'retracted')),
    product           VARCHAR(64) NOT NULL,
    description       VARCHAR(4000),
    synopsis          VARCHAR(4000) NOT NULL,
    topic             VARCHAR(4000),
    solution          VARCHAR(4000) NOT NULL,
    rights            VARCHAR(100),
    issue_date        TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    update_date       TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    refers_to         VARCHAR(4000),
    notes             VARCHAR(4000),
    org_id            NUMERIC
                          CONSTRAINT rhn_errata_oid_fk
                              REFERENCES web_customer (id)
                              ON DELETE CASCADE,
    locally_modified  CHAR(1)
                          CONSTRAINT rhn_errata_lm_ck
                              CHECK (locally_modified in ('Y','N')),
    errata_from       VARCHAR(127),
    created           TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    modified          TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    last_modified     TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    severity_id       NUMERIC
                          CONSTRAINT rhn_errata_sevid_fk
                              REFERENCES rhnErrataSeverity (id)
)

;

-- unique index definitions has been moved to
-- {oracle,postgres}/tables/rhnErrata_index.sql

CREATE INDEX rhn_errata_udate_index
    ON rhnErrata (update_date)
    ;

CREATE INDEX rhn_errata_syn_index
    ON rhnErrata ( synopsis )
    ;

CREATE SEQUENCE rhn_errata_id_seq;

