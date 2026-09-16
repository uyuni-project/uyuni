-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- Adding the column 'gpg_check' to rhnChannel

DO $$
    BEGIN
        BEGIN
            ALTER TABLE rhnChannel ADD gpg_check CHAR(1) DEFAULT ('Y') NOT NULL CONSTRAINT rhn_channel_gc_ck CHECK (gpg_check in ('Y', 'N'));
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column rhnChannel already exists in rhnChannel';
        END;
    END;
$$;
