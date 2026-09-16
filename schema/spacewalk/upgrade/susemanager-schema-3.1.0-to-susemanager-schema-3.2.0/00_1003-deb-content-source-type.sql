-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

insert into rhnContentSourceType (id, label) values
(sequence_nextval('rhn_content_source_type_id_seq'), 'deb');
