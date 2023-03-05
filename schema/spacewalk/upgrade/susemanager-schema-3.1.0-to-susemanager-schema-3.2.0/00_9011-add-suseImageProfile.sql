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

CREATE TABLE suseImageProfile
(
    profile_id     NUMERIC NOT NULL
                     CONSTRAINT suse_imgprof_prid_pk PRIMARY KEY,
    label          VARCHAR(128) NOT NULL,
    org_id         NUMERIC NOT NULL
                     CONSTRAINT suse_imgprof_oid_fk
                       REFERENCES web_customer (id)
                       ON DELETE CASCADE,
    token_id       NUMERIC
                     CONSTRAINT suse_imgprof_tk_fk
                       REFERENCES rhnRegToken (id)
                       ON DELETE SET NULL,
    image_type     VARCHAR(32) NOT NULL,
    target_store_id NUMERIC NOT NULL
                      CONSTRAINT suse_imgprof_tsid_fk
                         REFERENCES suseImageStore (id)
                         ON DELETE CASCADE,
    created        TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX suse_imgprof_label_uq
    ON suseImageProfile (label)
        ;

CREATE SEQUENCE suse_imgprof_prid_seq;
