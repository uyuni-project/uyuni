--
-- Copyright (c) 2011 Novell
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
    id         NUMBER        NOT NULL PRIMARY KEY,
    action_id  NUMBER NOT NULL
                   CONSTRAINT rhn_act_idp_act_fk
                       REFERENCES rhnAction (id)
                       ON DELETE CASCADE,
    image_id  NUMBER NOT NULL
                   CONSTRAINT rhn_act_idp_img_fk
                       REFERENCES suseImages (id)
                       ON DELETE CASCADE,
    vcpus     NUMBER DEFAULT(1) NOT NULL,
    mem_kb    NUMBER DEFAULT(524288) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE rhn_action_image_deploy_id_seq;

CREATE INDEX rhn_act_idp_aid_iid_idx
    ON rhnActionImageDeploy (action_id, image_id)
    TABLESPACE [[8m_tbs]];

CREATE INDEX rhn_act_idp_iid_idx
    ON rhnActionImageDeploy (image_id)
    TABLESPACE [[8m_tbs]];
