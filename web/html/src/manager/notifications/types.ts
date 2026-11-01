/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

export type NotificationType = {
  label: string;
  description: string;
};

export enum Severity {
  Info = "info",
  Warning = "warning",
  Error = "error",
}

export type Notification = {
  id: number;
  severity: Severity;
  type: string;
  summary: string;
  details?: string;
  read: boolean;
  actionable: boolean;
  create: Date;
};
