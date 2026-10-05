-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- Delete all credentials of type SUSE Studio
delete from suseCredentials where type_id =
    (select id from suseCredentialsType where label = 'susestudio');

-- Delete the credentials type
delete from suseCredentialsType where label = 'susestudio';

