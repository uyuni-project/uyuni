-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE TABLE web_contact_all
(
    id                 NUMERIC
                           CONSTRAINT web_contact_all_pk PRIMARY KEY
                           ,
    org_id             NUMERIC,
    login              VARCHAR(64) NOT NULL
)
;
