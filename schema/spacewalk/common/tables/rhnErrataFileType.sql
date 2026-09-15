--
-- Copyright (c) 2008 Red Hat, Inc.
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


CREATE TABLE rhnErrataFileType
(
    id     NUMERIC NOT NULL,
    label  VARCHAR(128) NOT NULL
)

;

CREATE INDEX rhn_erratafile_type_id_idx
    ON rhnErrataFileType (id)
    ;

CREATE INDEX rhn_erratafile_type_label_idx
    ON rhnErrataFileType (label)
    ;

CREATE SEQUENCE rhn_erratafile_type_id_seq;

ALTER TABLE rhnErrataFileType
    ADD CONSTRAINT rhn_erratafile_type_id_pk PRIMARY KEY (id);

ALTER TABLE rhnErrataFileType
    ADD CONSTRAINT rhn_erratafile_type_label_uq UNIQUE (label);

