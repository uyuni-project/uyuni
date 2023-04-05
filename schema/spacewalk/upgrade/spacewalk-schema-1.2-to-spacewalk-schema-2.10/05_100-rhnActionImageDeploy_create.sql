--
-- Copyright (c) 2012 Novell
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

CREATE TABLE rhnActionImageDeploy
(
    id            NUMERIC NOT NULL
                  CONSTRAINT rhn_aid_id_pk PRIMARY KEY
                  ,
    action_id     NUMERIC NOT NULL
                      CONSTRAINT rhn_act_idp_act_fk
                      REFERENCES rhnAction (id)
                      ON DELETE CASCADE,
    vcpus         NUMERIC NOT NULL,
    mem_kb        NUMERIC NOT NULL,
    bridge_device VARCHAR(32),
    download_url  VARCHAR(256) NOT NULL,
    proxy_server  VARCHAR(64),
    proxy_user    VARCHAR(32),
    proxy_pass    VARCHAR(64)
)

;

CREATE SEQUENCE rhn_action_image_deploy_id_seq;
