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

CREATE TABLE rhnActionImageBuildResult
(
    server_id              NUMERIC NOT NULL
                               CONSTRAINT rhn_image_build_result_sid_fk
                                   REFERENCES rhnServer (id)
                                   ON DELETE CASCADE,
    action_image_build_id NUMERIC NOT NULL
                               CONSTRAINT rhn_image_build_result_aid_fk
                                   REFERENCES rhnActionImageBuild (id)
                                   ON DELETE CASCADE
)

;

CREATE UNIQUE INDEX rhn_image_build_result_sa_uq
    ON rhnActionImageBuildResult (server_id, action_image_build_id);

CREATE INDEX rhn_image_build_result_ad_idx
    ON rhnActionImageBuildResult (action_image_build_id)
    ;

