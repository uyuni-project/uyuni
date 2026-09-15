--
-- Copyright (c) 2023 SUSE
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

CREATE TABLE susePaygProduct
(
    id                          NUMERIC NOT NULL
                                CONSTRAINT susePaygProduct_pk PRIMARY KEY,
    credentials_id              NUMERIC NOT NULL
                                CONSTRAINT susePaygProduct_credentials_id_fk
                                REFERENCES suseCredentials (id)
                                ON DELETE CASCADE,
    name                        VARCHAR(256),
    version                     VARCHAR(256),
    arch                        VARCHAR(256),
    created                     TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified                    TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE susePaygProduct_id_seq;

CREATE UNIQUE INDEX susePaygProduct_credentials_product_uq
    ON susePaygProduct (credentials_id, name, version, arch);
