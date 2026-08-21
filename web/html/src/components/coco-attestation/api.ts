import type { LocalizedMoment } from "utils/datetime";
import Network, { type JsonResult } from "utils/network";

import type { Settings } from "./Utils";

export const COCO_ATTESTATION_ACTION_TYPE = "coco.attestation";

type CurrentSingleCoCoSettingsResponse = {
  supported: boolean;
  settings: Settings;
};

type LegacySingleCoCoSettingsResponse = Pick<Settings, "enabled" | "environmentType" | "attestOnBoot"> & {
  supported: boolean;
  attestOnSchedule?: Settings["attestOnSchedule"];
  inputData?: Settings["inputData"];
};

export type SingleCoCoSettingsResponse = CurrentSingleCoCoSettingsResponse | LegacySingleCoCoSettingsResponse;

export function normalizeSingleCoCoSettingsResponse(
  response: SingleCoCoSettingsResponse
): CurrentSingleCoCoSettingsResponse {
  if ("settings" in response) {
    return response;
  }

  const { supported, attestOnSchedule, inputData = {}, ...settings } = response;
  return { supported, settings: { ...settings, attestOnSchedule, inputData } };
}

export type CoCoScheduleRequest = {
  actionType: typeof COCO_ATTESTATION_ACTION_TYPE;
  earliest: LocalizedMoment;
  actionChain: string | null;
};

export function getSystemCoCoSettings(serverId: number) {
  return Network.get<JsonResult<SingleCoCoSettingsResponse>>(
    `/rhn/manager/api/systems/${serverId}/details/coco/settings`
  );
}

export function saveSystemCoCoSettings(serverId: number, settings: Settings) {
  return Network.post<JsonResult<SingleCoCoSettingsResponse>, Settings>(
    `/rhn/manager/api/systems/${serverId}/details/coco/settings`,
    settings
  );
}

export function saveSsmCoCoSettings(serverIds: number[], settings: Settings) {
  return Network.post<JsonResult<unknown>, { serverIds: number[]; settings: Settings }>(
    "/rhn/manager/api/systems/coco/settings",
    {
      serverIds,
      settings,
    }
  );
}

export function scheduleSystemCoCoAttestation(serverId: number, request: CoCoScheduleRequest) {
  return Network.post<number, CoCoScheduleRequest>(
    `/rhn/manager/api/systems/${serverId}/details/coco/scheduleAction`,
    request
  );
}

export function scheduleSsmCoCoAttestation(serverIds: number[], request: CoCoScheduleRequest) {
  return Network.post<number, CoCoScheduleRequest & { serverIds: number[] }>(
    "/rhn/manager/api/systems/coco/scheduleAction",
    {
      ...request,
      serverIds,
    }
  );
}

export function getSystemCoCoAttestationsUrl(serverId: number): string {
  return `/rhn/manager/api/systems/${serverId}/details/coco/listAttestations`;
}

export function getGlobalCoCoAttestationsUrl(): string {
  return "/rhn/manager/api/audit/confidential-computing/listAttestations";
}
