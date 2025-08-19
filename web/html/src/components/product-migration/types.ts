import { ChannelTreeType } from "core/channels/type/channels.type";

import { SystemData } from "components/target-systems";

export interface MigrationProduct {
  id: number;
  name: string;
  addons: MigrationProduct[];
}

export interface MigrationTarget {
  id: string;
  targetProduct: MigrationProduct;
  missingChannels: string[];
}

export interface MigrationSystemData extends SystemData {
  installedProduct: MigrationProduct;
  eligible: boolean;
  reason: string | null;
  details: string | null;
}

export interface MigrationChannelsSelection {
  baseChannelTrees: ChannelTreeType[];
  mandatoryMap: Record<string, number[]>;
  systemsData: MigrationSystemData[];
}

export interface MigrationTargetSelection {
  commonBaseProduct: boolean;
  migrationSource: MigrationProduct;
  migrationTargets: MigrationTarget[];
  systemsData: MigrationSystemData[];
}

export interface MigrationDryRunConfirmation {
  targetProduct: MigrationProduct;
  selectedChannels: ChannelTreeType;
  systemsData: MigrationSystemData[];
  allowVendorChange: boolean;
}

export interface MigrationScheduleRequest {
  serverIds: number[];
  targetProduct: MigrationProduct;
  targetChannelTree: ChannelTreeType;
  dryRun: boolean;
  allowVendorChange: boolean;
  earliest: moment.Moment | null;
  actionChain: string | null;
}
