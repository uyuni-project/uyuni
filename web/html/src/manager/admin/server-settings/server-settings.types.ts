/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

export type ServerSettingType = "STRING" | "INTEGER" | "BOOLEAN" | "LIST";

export type ServerSetting = {
  key: string;
  description: string;
  type: ServerSettingType;
  /** The effective value, the default value when no value is set. Booleans are "true" or "false". */
  value: string | null;
  defaultValue: string | null;
  /** True when a value is set, false when the default value is used */
  modified: boolean;
};

export type ServerSettingsModel = Record<string, string | boolean>;
