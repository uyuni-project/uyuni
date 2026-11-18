/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { Utils } from "utils/functions";
import Network from "utils/network";
import { mockNetwork } from "utils/storybook/mock-network";

export type PackageFixture = {
  idCombo: string;
  nvre: string;
  nvrea: string;
  packageId: number | null;
  selectable: boolean;
  summary: string;
  arch: string;
};

const packages: PackageFixture[] = [
  {
    idCombo: "101|201|301",
    nvre: "ptf-18102-1-1",
    nvrea: "ptf-18102-1-1.x86_64",
    packageId: 501,
    selectable: true,
    summary: "Fixes a kernel live-patching issue",
    arch: "x86_64",
  },
  {
    idCombo: "102|202|302",
    nvre: "ptf-18134-2-1",
    nvrea: "ptf-18134-2-1.x86_64",
    packageId: 502,
    selectable: true,
    summary: "Corrects package dependency resolution",
    arch: "x86_64",
  },
  {
    idCombo: "103|203|303",
    nvre: "ptf-18201-1-2",
    nvrea: "ptf-18201-1-2.aarch64",
    packageId: null,
    selectable: false,
    summary: "Requires a newer product service pack",
    arch: "aarch64",
  },
];

const selectionKey = (item: PackageFixture) => `${item.idCombo}~*~${item.nvrea}`;

export const installPackageMocks = () => {
  const selectedIds = new Set([selectionKey(packages[0])]);
  const get = ((url: string) => {
    const params = new URL(url, window.location.origin).searchParams;
    const criteria = params.get("q")?.toLocaleLowerCase() ?? "";
    const items = packages
      .filter((item) => item.nvre.toLocaleLowerCase().includes(criteria))
      .map((item) => ({ ...item }));
    if (params.get("f") === "id") {
      return Utils.cancelable(Promise.resolve(items.filter((item) => item.selectable).map(selectionKey)));
    }
    return Utils.cancelable(Promise.resolve({ items, total: items.length, selectedIds: [...selectedIds] }));
  }) as typeof Network.get;
  const post = ((url: string, data?: Record<string, boolean>) => {
    if (url.endsWith("/maintenance/upcoming-windows")) {
      return Utils.cancelable(
        Promise.resolve({
          data: { maintenanceWindowsMultiSchedules: false, maintenanceWindows: null },
        })
      );
    }
    if (url.endsWith("/scheduleAction")) return Utils.cancelable(Promise.resolve(731));
    if (url.includes("/api/sets/")) {
      if (url.endsWith("/clear")) selectedIds.clear();
      else
        Object.entries(data ?? {}).forEach(([id, selected]) => {
          if (selected) selectedIds.add(id);
          else selectedIds.delete(id);
        });
    }
    return Utils.cancelable(Promise.resolve({}));
  }) as typeof Network.post;
  return mockNetwork({ get, post });
};
