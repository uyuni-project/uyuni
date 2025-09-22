export interface ProjectHistoryEntry {
  version: number;
  message: string;
}

export interface ProjectPropertiesType {
  label: string;
  name: string;
  description?: string;
  historyEntries: ProjectHistoryEntry[];
}

export interface ProjectSoftwareSourceType {
  channelId: number;
  name: string;
  label: string;
  state: string;
  type: string;
  hasUnsyncedPatches: boolean;
  targetChannelId: number | null | undefined;
}

export interface ProjectEnvironmentType {
  id: number;
  projectLabel: string;
  label: string;
  name: string;
  description: string;
  status: string;
  version: number;
  hasProfiles: boolean;
  builtTime: string | null | undefined;
}

export interface ProjectFilterServerType {
  id: number;
  name: string;
  criteriaKey: string;
  criteriaValue: string;
  entityType: string;
  rule: "deny" | "allow";
  state: string;
}

export interface ProjectMessageType {
  text: string;
  type: "info" | "warning" | "error";
  entity: "properties" | "softwareSources" | "filters" | "environments";
}

export interface ProjectType {
  properties: ProjectPropertiesType;
  softwareSources: ProjectSoftwareSourceType[];
  filters: any[];
  environments: ProjectEnvironmentType[];
  messages: ProjectMessageType[];
}
