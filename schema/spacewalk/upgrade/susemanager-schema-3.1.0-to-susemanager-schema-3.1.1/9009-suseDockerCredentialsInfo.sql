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

insert into suseCredentialsType (id, label, name) values
        (sequence_nextval('suse_credtype_id_seq'), 'docker', 'Docker Registry');

CREATE TABLE suseDockerCredentialsInfo
(
    creds_id  NUMBER NOT NULL
                 CONSTRAINT suse_dockercredsinf_cid_fk
                     REFERENCES suseCredentials (id)
                     ON DELETE CASCADE,
    email    VARCHAR2(256) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE INDEX suse_dockercredsinf_cid_idx
    ON suseDockerCredentialsInfo (creds_id)
        TABLESPACE [[2m_tbs]];

