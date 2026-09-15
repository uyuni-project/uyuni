/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
export const AccessMode = {
  NONE: "",
  READ: "R",
  WRITE: "W",
  READ_WRITE: "RW",
} as const;

export type AccessModeValue = (typeof AccessMode)[keyof typeof AccessMode];

export const AccessModeByPermissionType = {
  view: AccessMode.READ,
  modify: AccessMode.WRITE,
} as const;

export type PermissionType = keyof typeof AccessModeByPermissionType;
