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


CREATE TABLE rhnChannelProduct
(
    id        NUMERIC NOT NULL
                  CONSTRAINT rhn_channelprod_id_pk PRIMARY KEY
                  ,
    product   VARCHAR(256) NOT NULL,
    version   VARCHAR(64) NOT NULL,
    beta      CHAR(1)
                  DEFAULT ('N') NOT NULL
                  CONSTRAINT rhn_channelprod_beta_ck
                      CHECK (beta in ('Y', 'N')),
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_channelprod_p_v_b_uq
    ON rhnChannelProduct (product, version, beta)
    ;

CREATE SEQUENCE rhn_channelprod_id_seq;

