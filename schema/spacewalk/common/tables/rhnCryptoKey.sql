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


CREATE TABLE rhnCryptoKey
(
    id                  NUMERIC NOT NULL
                            CONSTRAINT rhn_cryptokey_id_pk PRIMARY KEY
                            ,
    org_id              NUMERIC
                            CONSTRAINT rhn_cryptokey_oid_fk
                                REFERENCES web_customer (id)
                                ON DELETE CASCADE,
    description         VARCHAR(1024) NOT NULL,
    crypto_key_type_id  NUMERIC NOT NULL
                            CONSTRAINT rhn_cryptokey_cktid_fk
                                REFERENCES rhnCryptoKeyType (id),
    key                 BYTEA
)

;

CREATE UNIQUE INDEX rhn_cryptokey_oid_desc_uq
    ON rhnCryptoKey (org_id, description)
    ;

CREATE SEQUENCE rhn_cryptokey_id_seq;

