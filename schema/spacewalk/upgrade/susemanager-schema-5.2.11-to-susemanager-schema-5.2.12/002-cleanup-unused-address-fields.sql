--
-- Copyright (c) 2026 SUSE
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

-- Drop the email_uc normalized email
DROP INDEX IF EXISTS wusi_email_uc_idx;
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS email_uc;

-- Drop alternate names
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS alt_first_names;
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS alt_last_name;

-- Drop extra address lines 3 and 4
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS address3;
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS address4;

-- Drop user url
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS url;

-- Drop oracle crm integration
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS oracle_site_id;

-- Drop old locale fields
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS alt_first_names_ol;
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS alt_last_name_ol;
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS address1_ol;
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS address2_ol;
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS address3_ol;
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS city_ol;
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS state_ol;
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS zip_ol;

-- Drop address type system
ALTER TABLE web_user_site_info DROP COLUMN IF EXISTS type;
-- And drop the actual address type table
DROP TABLE IF EXISTS web_user_site_type;

-- Convert PO Box flag from CHAR(1) to BOOLEAN
-- Drop the value check and defaul constraints
ALTER TABLE web_user_site_info DROP CONSTRAINT IF EXISTS wusi_ipb_ck;
ALTER TABLE web_user_site_info ALTER COLUMN is_po_box DROP DEFAULT;

-- Convert CHAR(1) values ('0'/'1') to BOOLEAN (false/true)
ALTER TABLE web_user_site_info
ALTER COLUMN is_po_box TYPE BOOLEAN
    USING (is_po_box = '1');

-- Set NOT NULL constraint
ALTER TABLE web_user_site_info
    ALTER COLUMN is_po_box SET NOT NULL;

-- Set default value
ALTER TABLE web_user_site_info
    ALTER COLUMN is_po_box SET DEFAULT FALSE;