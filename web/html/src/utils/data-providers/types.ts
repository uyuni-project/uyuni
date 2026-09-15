/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
export type PagedData = {
  items: any[];
  total: number;
};

export type Comparator = (a: any, b: any, key: string, direction: number) => number;
