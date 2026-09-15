/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
export type OptionalValue = number | undefined;

export type Package = {
  arch: string;
  name: string;
  packageStateId: OptionalValue;
  versionConstraintId: OptionalValue;
  epoch?: string;
  release?: string;
  version?: string;
};

export type PackagesObject = {
  original: Package;
  value?: Package;
};

export type ChangesMapObject = Record<string, PackagesObject>;
