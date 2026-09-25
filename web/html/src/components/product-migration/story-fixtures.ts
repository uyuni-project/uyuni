import type { ChannelTreeType } from "core/channels/type/channels.type";

import type { SystemData } from "components/target-systems";

import type { MigrationProduct, MigrationTarget } from "./types";

export const migrationSource: MigrationProduct = {
  id: 1506,
  name: "SUSE Linux Enterprise Server 15 SP6",
  addons: [
    { id: 151, name: "Basesystem Module 15 SP6", addons: [] },
    { id: 152, name: "Server Applications Module 15 SP6", addons: [] },
  ],
};

export const migrationTargetProduct: MigrationProduct = {
  id: 1600,
  name: "SUSE Linux Enterprise Server 16.0",
  addons: [
    { id: 161, name: "Basesystem 16.0", addons: [] },
    { id: 162, name: "Server Applications 16.0", addons: [] },
  ],
};

const unavailableTargetProduct: MigrationProduct = {
  id: 1507,
  name: "SUSE Linux Enterprise Server 15 SP7",
  addons: [{ id: 157, name: "Basesystem Module 15 SP7", addons: [] }],
};

export const migrationTargets: MigrationTarget[] = [
  {
    id: "sles-16",
    targetProduct: migrationTargetProduct,
    missingChannels: [],
    hasDryRunCapability: true,
  },
  {
    id: "sles-15-sp7",
    targetProduct: unavailableTargetProduct,
    missingChannels: ["SLE-Product-SLES15-SP7-Pool", "SLE-Module-Basesystem15-SP7-Pool"],
    hasDryRunCapability: false,
  },
];

export const baseChannelTrees: ChannelTreeType[] = [
  {
    base: {
      id: 1000,
      archLabel: "x86_64",
      custom: false,
      isCloned: false,
      label: "sles16-pool-x86_64",
      name: "SLES 16.0 Pool for x86_64",
      recommended: true,
      subscribable: true,
      standardizedName: "sles 16.0 pool for x86_64",
      recommendedChildrenIds: [1010, 1020],
    },
    children: [
      {
        id: 1010,
        archLabel: "x86_64",
        custom: false,
        isCloned: false,
        label: "sles16-basesystem-x86_64",
        name: "SLES 16.0 Basesystem",
        recommended: true,
        subscribable: true,
        standardizedName: "sles 16.0 basesystem",
        parentId: 1000,
      },
      {
        id: 1020,
        archLabel: "x86_64",
        custom: false,
        isCloned: false,
        label: "sles16-server-applications-x86_64",
        name: "SLES 16.0 Server Applications",
        recommended: true,
        subscribable: true,
        standardizedName: "sles 16.0 server applications",
        parentId: 1000,
      },
      {
        id: 1030,
        archLabel: "x86_64",
        custom: false,
        isCloned: false,
        label: "sles16-containers-x86_64",
        name: "SLES 16.0 Containers",
        recommended: false,
        subscribable: true,
        standardizedName: "sles 16.0 containers",
        parentId: 1000,
      },
    ],
  },
  {
    base: {
      id: 2000,
      archLabel: "x86_64",
      custom: true,
      isCloned: true,
      label: "sles16-production-x86_64",
      name: "SLES 16.0 Production Clone",
      recommended: false,
      subscribable: true,
      standardizedName: "sles 16.0 production clone",
      recommendedChildrenIds: [2010],
    },
    children: [
      {
        id: 2010,
        archLabel: "x86_64",
        custom: true,
        isCloned: true,
        label: "sles16-production-basesystem-x86_64",
        name: "SLES 16.0 Production Basesystem",
        recommended: true,
        subscribable: true,
        standardizedName: "sles 16.0 production basesystem",
        parentId: 2000,
      },
    ],
  },
];

export const mandatoryMap: [number, number[]][] = [
  [1000, [1010, 1020]],
  [1030, [1020]],
  [2000, [2010]],
];

export const reversedMandatoryMap: [number, number[]][] = [
  [1010, [1000]],
  [1020, [1000, 1030]],
  [2010, [2000]],
];

export const selectedChannelTree: ChannelTreeType = {
  base: baseChannelTrees[0].base,
  children: baseChannelTrees[0].children.slice(0, 2),
};

export const systemsData: SystemData[] = [
  { id: 1000010001, name: "sles15-web.example.com" },
  { id: 1000010002, name: "sles15-db.example.com" },
];
