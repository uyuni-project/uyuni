import "manager/minion/audit/audit-common.css";

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
  scapContentList: any[];
  tailoringFiles: any[];
  scapPolicies: any[];
}

declare global {
  interface Window {
    minions?: Minion[];
  }
}

const ScheduleAuditScanSsm = (): JSX.Element => {
  const scheduleData = ((window as any).scheduleData as ScheduleData) || {};

  const tailoringFiles = scheduleData.tailoringFiles || [];
  const scapPolicies = scheduleData.scapPolicies || [];
  const scapContentList = scheduleData.scapContentList || [];

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
    />
  );
};

export const renderer = () => {
  const container = document.getElementById("schedule-scap-scan");
  if (container) {
    SpaRenderer.renderNavigationReact(<ScheduleAuditScanSsm />, container);
  }
};
