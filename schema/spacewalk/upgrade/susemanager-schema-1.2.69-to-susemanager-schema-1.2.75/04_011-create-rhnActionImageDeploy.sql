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
--

CREATE TABLE rhnActionImageDeploy
(
    id            NUMBER NOT NULL PRIMARY KEY,
    action_id     NUMBER NOT NULL
                   CONSTRAINT rhn_act_idp_act_fk
                       REFERENCES rhnAction (id)
                       ON DELETE CASCADE,
    vcpus         NUMBER DEFAULT(1) NOT NULL,
    mem_kb        NUMBER DEFAULT(524288) NOT NULL,
    bridge_device VARCHAR2(32)  DEFAULT('br0') NOT NULL,
    image_type    VARCHAR2(32)  NOT NULL,
    download_url  VARCHAR2(256) NOT NULL,
    proxy_server  VARCHAR2(64),
    proxy_user    VARCHAR2(32),
    proxy_pass    VARCHAR2(64)
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE rhn_action_image_deploy_id_seq;

