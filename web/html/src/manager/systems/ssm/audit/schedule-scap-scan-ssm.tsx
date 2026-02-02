import "manager/minion/audit/audit-common.css";

import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import Network from "utils/network";
import { ScheduleScapScanForm } from "components/audit/schedule-scap-scan-form";

declare global {
  interface Window {
    scheduleData: {
      scapContentList: any[];
      tailoringFiles: any[];
      scapPolicies: any[];
    };
    minions?: any[];
  }
}

const ScheduleAuditScanSsm = () => {
    // Unpack scheduleData from window
    const scheduleData = window.scheduleData || {} as any;
    
    const tailoringFiles = scheduleData.tailoringFiles || [];
    const scapPolicies = scheduleData.scapPolicies || [];
    const scapContentList = scheduleData.scapContentList || [];

    const onSubmit = (model) => {
        return Network.post("/rhn/manager/api/audit/schedule/create", {
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
  return SpaRenderer.renderNavigationReact(<ScheduleAuditScanSsm />, document.getElementById("schedule-scap-scan"));
};
