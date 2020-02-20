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

CREATE TABLE suseImageRepoDigest
(
    id             NUMERIC NOT NULL
                     CONSTRAINT suse_rdigest_id_pk PRIMARY KEY,
    image_history_id    NUMERIC NOT NULL,
    repo_digest    VARCHAR(255) NOT NULL,
    created        TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    CONSTRAINT suse_rdigest_bldhst_fk FOREIGN KEY (image_history_id)
        REFERENCES suseImageBuildHistory (id) ON DELETE CASCADE
)

;

CREATE UNIQUE INDEX suse_img_repodigest_idx
    ON suseImageRepoDigest(repo_digest, image_history_id)
    
    ;

CREATE SEQUENCE suse_img_repodigest_id_seq;
