/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
export { default as PageControl } from "./page-control";
export { default as SimpleDataProvider } from "./simple-data-provider";
export { default as AsyncDataProvider } from "./async-data-provider";

export type PagedData = {
  items: any[];
  total: number;
  selectedIds?: number[];
};

export type Comparator = (a: any, b: any, key: string, direction: number) => number;
