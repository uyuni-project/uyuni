-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

UPDATE rhnServerGroupTypeFeature
  SET server_group_type_id = (
    SELECT id
      FROM rhnServerGroupType
      WHERE label = 'enterprise_entitled'
  )
  WHERE server_group_type_id = (
    SELECT id
      FROM rhnServerGroupType
      WHERE label = 'provisioning_entitled'
  );
