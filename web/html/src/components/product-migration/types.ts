import { ChannelTreeType } from "core/channels/type/channels.type";

import { SystemData } from "components/target-systems";

export type MigrationProduct = {
  id: number;
  name: string;
  addons: MigrationProduct[];
};

export type MigrationTarget = {
  id: string;
  targetProduct: MigrationProduct;
  missingChannels: string[];
};

export type MigrationSystemData = {
  installedProduct: MigrationProduct;
  eligible: boolean;
  reason: string | null;
  details: string | null;
} & SystemData;

export type MigrationChannelsSelection = {
  baseChannelTrees: ChannelTreeType[];
  mandatoryMap: Array<[number, number[]]>;
  reversedMandatoryMap: Array<[number, number[]]>;
  systemsData: MigrationSystemData[];
};

export type MigrationTargetSelection = {
  commonBaseProduct: boolean;
  migrationSource: MigrationProduct;
  migrationTargets: MigrationTarget[];
  systemsData: MigrationSystemData[];
};

export type MigrationDryRunConfirmation = {
  targetProduct: MigrationProduct;
  selectedChannels: ChannelTreeType;
  systemsData: MigrationSystemData[];
  allowVendorChange: boolean;
};

export type MigrationScheduleRequest = {
  serverIds: number[];
  targetProduct: MigrationProduct;
  targetChannelTree: ChannelTreeType;
  dryRun: boolean;
  allowVendorChange: boolean;
  earliest: moment.Moment | null;
  actionChain: string | null;
};
