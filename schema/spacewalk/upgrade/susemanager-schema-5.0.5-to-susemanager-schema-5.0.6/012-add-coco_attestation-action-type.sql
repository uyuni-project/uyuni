-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

insert into rhnActionType
  select 523, 'coco.attestation', 'Confidential Compute Attestation', 'N', 'N', 'N'
  where not exists(select 1  from rhnActionType where id = 523);
