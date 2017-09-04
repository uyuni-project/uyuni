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

CREATE TABLE suseImageBuildHistory
(
    id             NUMBER NOT NULL
                     CONSTRAINT suse_bldhst_id_pk PRIMARY KEY,
    image_info_id  NUMBER NOT NULL,
    revision_num   NUMBER NOT NULL,
    created        timestamp with local time zone
                     DEFAULT (current_timestamp) NOT NULL,
    modified       timestamp with local time zone
                     DEFAULT (current_timestamp) NOT NULL,
    CONSTRAINT suse_bldhst_imginfo_fk FOREIGN KEY (image_info_id)
        REFERENCES suseImageInfo (id) ON DELETE CASCADE
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_img_buildhistory_id_seq;
