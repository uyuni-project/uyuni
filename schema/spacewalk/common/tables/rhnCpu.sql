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


CREATE TABLE rhnCpu
(
    id           NUMERIC NOT NULL
                     CONSTRAINT rhn_cpu_id_pk PRIMARY KEY
                     ,
    server_id    NUMERIC NOT NULL
                     CONSTRAINT rhn_cpu_server_fk
                         REFERENCES rhnServer (id),
    cpu_arch_id  NUMERIC NOT NULL
                     CONSTRAINT rhn_cpu_caid_fk
                         REFERENCES rhnCpuArch (id),
    bogomips     VARCHAR(16),
    cache        VARCHAR(16),
    family       VARCHAR(32),
    MHz          VARCHAR(16),
    stepping     VARCHAR(16),
    flags        VARCHAR(2048),
    model        VARCHAR(64),
    version      VARCHAR(32),
    vendor       VARCHAR(32),
    nrcpu        NUMERIC
                     DEFAULT (1),
    nrsocket     NUMERIC
                     DEFAULT (1),
    nrcore       NUMERIC
                     DEFAULT (1),
    nrthread     NUMERIC
                     DEFAULT (1),
    acpiVersion  VARCHAR(64),
    apic         VARCHAR(32),
    apmVersion   VARCHAR(32),
    chipset      VARCHAR(64),
    created      TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    modified     TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_cpu_server_id_uq
    ON rhnCpu (server_id)
    
    ;

CREATE SEQUENCE rhn_cpu_id_seq;

