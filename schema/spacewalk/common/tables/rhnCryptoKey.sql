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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnCryptoKey
(
    id                  NUMBER NOT NULL
                            CONSTRAINT rhn_cryptokey_id_pk PRIMARY KEY
                            USING INDEX TABLESPACE [[2m_tbs]],
    org_id              NUMBER
                            CONSTRAINT rhn_cryptokey_oid_fk
                                REFERENCES web_customer (id)
                                ON DELETE CASCADE,
    description         VARCHAR2(1024) NOT NULL,
    crypto_key_type_id  NUMBER NOT NULL
                            CONSTRAINT rhn_cryptokey_cktid_fk
                                REFERENCES rhnCryptoKeyType (id),
    key                 BLOB
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX rhn_cryptokey_oid_desc_uq
    ON rhnCryptoKey (org_id, description)
    TABLESPACE [[4m_tbs]];

CREATE SEQUENCE rhn_cryptokey_id_seq;

