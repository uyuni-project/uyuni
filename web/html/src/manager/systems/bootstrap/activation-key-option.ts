/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

export type ActivationKeyOption = {
  key: string;
  description?: string;
};

export function formatActivationKeyLabel(activationKey: ActivationKeyOption): string {
  const description = (activationKey.description ?? "").trim();
  if (description) {
    return `${description} (${activationKey.key})`;
  }
  return activationKey.key;
}
