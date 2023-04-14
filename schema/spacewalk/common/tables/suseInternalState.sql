--
-- Copyright (c) 2023 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
CREATE TABLE suseInternalState
(
  id                NUMERIC NOT NULL
                    CONSTRAINT suse_internal_state_id_pk PRIMARY KEY,
  name              VARCHAR(128) NOT NULL,
  label             VARCHAR(128) NOT NULL
);
