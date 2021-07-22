--
-- Copyright (c) 2020 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

DO $$
    BEGIN
        IF EXISTS
            (
                SELECT 1
                FROM information_schema.columns
                WHERE table_name='rhnactionvirtcreate' AND column_name='vm_type'
            )
        THEN
            ALTER TABLE rhnActionVirtCreate
                ADD COLUMN IF NOT EXISTS cobbler_system VARCHAR(256),
                ADD COLUMN IF NOT EXISTS kickstart_host VARCHAR(256),
                ADD COLUMN IF NOT EXISTS kernel_options VARCHAR(256);
        END IF;
    END
$$ LANGUAGE plpgsql;
