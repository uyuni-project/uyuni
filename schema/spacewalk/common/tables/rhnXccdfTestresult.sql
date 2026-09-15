--
-- Copyright (c) 2012 Red Hat, Inc.
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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE rhnXccdfTestresult
(
    id             NUMERIC NOT NULL
                       CONSTRAINT rhn_xccdf_tresult_id_pk PRIMARY KEY
                       ,
    server_id      NUMERIC NOT NULL
                       CONSTRAINT rhn_xccdf_tresult_srvr_fk
                           REFERENCES rhnServer (id)
                           ON DELETE CASCADE,
    action_scap_id NUMERIC NOT NULL
                       CONSTRAINT rhn_xccdf_tresult_act_fk
                           REFERENCES rhnActionScap (id)
                           ON DELETE CASCADE,
    benchmark_id   NUMERIC NOT NULL
                       CONSTRAINT rhn_xccdf_tresult_bench_fk
                           REFERENCES rhnXccdfBenchmark (id),
    profile_id     NUMERIC NOT NULL
                       CONSTRAINT rhn_xccdf_tresult_profile_fk
                           REFERENCES rhnXccdfProfile (id),
    identifier     VARCHAR(120) NOT NULL,
    start_time     TIMESTAMPTZ,
    end_time       TIMESTAMPTZ NOT NULL,
    errors         BYTEA
)


;

CREATE UNIQUE INDEX rhn_xccdf_tresult_sa_uq
    ON rhnXccdfTestresult (server_id, action_scap_id)
    
    ;

CREATE SEQUENCE rhn_xccdf_tresult_id_seq;
