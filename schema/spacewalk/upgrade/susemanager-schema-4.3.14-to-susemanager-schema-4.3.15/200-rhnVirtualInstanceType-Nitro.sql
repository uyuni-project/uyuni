-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

UPDATE rhnVirtualInstanceType SET name='Amazon EC2/Nitro', label='aws_nitro' WHERE label='aws_kvm';
