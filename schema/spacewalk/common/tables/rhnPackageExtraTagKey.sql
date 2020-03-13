--
-- Copyright (c) 2019 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE rhnPackageExtraTagKey
(
    id          NUMERIC NOT NULL
                    CONSTRAINT rhn_pkg_extra_tags_keys_id_pk PRIMARY KEY,
    name        VARCHAR(256) NOT NULL,
    created     TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL
)
;

CREATE SEQUENCE rhn_package_extra_tags_keys_id_seq;

CREATE UNIQUE INDEX rhn_pkg_extra_tag_key_idx
    ON rhnPackageExtraTagKey (name);