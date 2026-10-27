--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
CREATE TABLE suseRecurringContentBuild
(
  rec_id            NUMERIC NOT NULL
                    CONSTRAINT suse_recurring_ct_bld_id_pk PRIMARY KEY
                    CONSTRAINT suse_rec_ct_bld_rec_fk
                      REFERENCES suseRecurringAction(id)
                      ON DELETE CASCADE,
  project_id        NUMERIC NOT NULL
                    CONSTRAINT suse_rec_ct_bld_prj_fk
                      REFERENCES suseContentProject(id)
                      ON DELETE CASCADE
);
