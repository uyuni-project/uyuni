/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

export type AnsiblePath = {
  id: number;
  minionServerId: number;
  type: string;
  path: string;
};

export function createNewAnsiblePath(fieldsToUpdate: Partial<AnsiblePath>) {
  const newDefaultEntity: AnsiblePath = { id: -1, minionServerId: -1, type: "", path: "" };
  return { ...newDefaultEntity, ...fieldsToUpdate };
}
