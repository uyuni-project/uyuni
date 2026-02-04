import "./audit-common.css";

import SpaRenderer from "core/spa/spa-renderer";

import { ScheduleScapScanForm } from "components/audit/schedule-scap-scan-form";

import Network from "utils/network";

const ENDPOINTS = {
  SCHEDULE_CREATE: "/rhn/manager/api/audit/schedule/create",
} as const;

interface Minion {
  id: number;
}

interface ScheduleData {
  scapContentList?: any[];
  tailoringFiles?: any[];
  scapPolicies?: any[];
  profileId?: number;
  serverId?: number;
  entityType?: string;
}

declare global {
  interface Window {
    scheduleData: ScheduleData;
    profileId?: number;
    minions?: Minion[];
    entityType?: string;
  }
}

const ScheduleAuditScan = (): JSX.Element => {
  // Unpack scheduleData from window
  const scheduleData = window.scheduleData || {};

  // Set window properties for backward compatibility
  // Ideally these should be removed if no longer used by other legacy components,
  // but kept here to match original behavior for safety.
  window.profileId = scheduleData.profileId || 0;
  window.minions = scheduleData.serverId ? [{ id: scheduleData.serverId }] : [];
  window.entityType = scheduleData.entityType || "server";

  const tailoringFiles = scheduleData.tailoringFiles || [];
  const scapPolicies = scheduleData.scapPolicies || [];
  const scapContentList = scheduleData.scapContentList || [];

  // Get system ID from URL for recurring actions link
  const urlParams = new URLSearchParams(window.location.search);
  const sid = urlParams.get("sid");
  const createRecurringLink = `/rhn/manager/systems/details/recurring-actions?sid=${sid}#/create`;

  const onSubmit = async (model: any) => {
    return Network.post(ENDPOINTS.SCHEDULE_CREATE, {
      ids: window.minions?.map((m) => m.id),
      earliest: model.earliest,
      xccdfProfileId: model.xccdfProfileId,
      scapContentId: model.dataStreamName,
      tailoringFileId: model.tailoringFile,
      tailoringProfileID: model.tailoringProfileID,
      ovalFiles: model.ovalFiles,
      advancedArgs: model.advancedArgs,
      fetchRemoteResources: model.fetchRemoteResources,
      policyId: model.selectedScapPolicy,
    });
  };

  return (
    <ScheduleScapScanForm
      scapContentList={scapContentList}
      tailoringFiles={tailoringFiles}
      scapPolicies={scapPolicies}
      onSubmit={onSubmit}
      minions={window.minions}
      createRecurringLink={createRecurringLink}
    />
  );
};

export const renderer = () => {
  const container = document.getElementById("schedule-scap-scan");
  if (container) {
    SpaRenderer.renderNavigationReact(<ScheduleAuditScan />, container);
  }
};
