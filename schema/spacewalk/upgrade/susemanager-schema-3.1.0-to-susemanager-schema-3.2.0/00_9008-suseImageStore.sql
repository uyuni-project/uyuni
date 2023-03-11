--
-- Copyright (c) 2017 SUSE LLC
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

CREATE TABLE suseImageStoreType
(
    id     NUMERIC NOT NULL
                 CONSTRAINT suse_imgstore_type_id_pk PRIMARY KEY,
    label  VARCHAR(128) NOT NULL,
    name   VARCHAR(128) NOT NULL
)

;

CREATE UNIQUE INDEX suse_imgstore_type_label_uq
    ON suseImageStoreType (label)
        ;

CREATE UNIQUE INDEX suse_imgstore_type_name_uq
    ON suseImageStoreType (name)
        ;

CREATE SEQUENCE suse_imgstore_type_id_seq;

-----------------------------------------------------------------

CREATE TABLE suseImageStore
(
    id             NUMERIC NOT NULL
                     CONSTRAINT suse_imgstore_id_pk PRIMARY KEY,
    label          VARCHAR(128) NOT NULL,
    uri            VARCHAR(512) NOT NULL,
    store_type_id  NUMERIC NOT NULL
                     CONSTRAINT suse_imgstore_type_fk
                       REFERENCES suseImageStoreType (id)
                       ON DELETE CASCADE,
    org_id         NUMERIC NOT NULL
                     CONSTRAINT suse_imgstore_oid_fk
                       REFERENCES web_customer (id)
                       ON DELETE CASCADE,
    creds_id       NUMERIC
                     CONSTRAINT suse_imgstore_creds_fk
                       REFERENCES suseCredentials (id)
                       ON DELETE SET NULL,
    created        TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX suse_imgstore_label_uq
    ON suseImageStore (label)
        ;

CREATE SEQUENCE suse_imgstore_id_seq;

-----------------------------------------------------------------

insert into suseImageStoreType (id, label, name) values
	(sequence_nextval('suse_imgstore_type_id_seq'), 'registry', 'Registry');
