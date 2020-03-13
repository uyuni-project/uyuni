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


CREATE TABLE rhnConfigChannel
(
    id                NUMERIC NOT NULL
                          CONSTRAINT rhn_confchan_id_pk PRIMARY KEY
                          ,
    org_id            NUMERIC NOT NULL
                          CONSTRAINT rhn_confchan_oid_fk
                              REFERENCES web_customer (id),
    confchan_type_id  NUMERIC NOT NULL
                          CONSTRAINT rhn_confchan_ctid_fk
                              REFERENCES rhnConfigChannelType (id),
    name              VARCHAR(128) NOT NULL,
    label             VARCHAR(64) NOT NULL,
    description       VARCHAR(1024) NOT NULL,
    created           TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    modified          TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_confchan_oid_label_type_uq
    ON rhnConfigChannel (org_id, label, confchan_type_id)
    ;

CREATE SEQUENCE rhn_confchan_id_seq;

