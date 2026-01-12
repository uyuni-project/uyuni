import "./scap-policy-details.css";

import * as React from "react";
import { useState, useEffect } from "react";
import SpaRenderer from "core/spa/spa-renderer";
import Network from "utils/network";
import { TopPanel } from "components/panels/TopPanel";
import { Panel } from "components/panels/Panel";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { LinkButton } from "components/buttons";
import moment from "moment";

type ScanHistoryEntry = {
  xid: number;
  sid: number;
  serverName: string;
  completed: string;
  startTime: string;
  endTime: string;
  profile: string;
  scapActionId: number;
  pass: number;
  fail: number;
  other: number;
};

type PolicyData = {
  id: number;
  policyName: string;
  description?: string;
  dataStreamName: string;
  xccdfProfileId: string;
  tailoringFile?: number;
  tailoringFileName?: string;
  tailoringProfileId?: string;
  ovalFiles?: string;
  advancedArgs?: string;
  fetchRemoteResources?: boolean;
};


const SummaryCard = ({ value, label }: { value: string | number; label: string }) => (
  <div className="col-md-3">
    <div className="panel panel-default text-center">
      <div className="panel-body">
        <h3>{value}</h3>
        <p>{t(label)}</p>
      </div>
    </div>
  </div>
);

const ConfigRow = ({ label, value, code = false }: { label: string; value: string; code?: boolean }) => (
  <>
    <dt className="col-sm-3">{t(label)}</dt>
    <dd className="col-sm-9">{code ? <code>{value}</code> : value}</dd>
  </>
);

// Helper Functions
const getComplianceStatus = (failCount: number) => ({
  className: failCount === 0 ? 'label-success' : 'label-danger',
  text: failCount === 0 ? t("Compliant") : t("Non-Compliant")
});

const ScapPolicyDetails = ({ policyId, policyData }: { policyId: number; policyData: PolicyData }) => {
  const [scanHistory, setScanHistory] = useState<ScanHistoryEntry[]>([]);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    Network.get(`/rhn/manager/api/audit/scap/policy/${policyId}/scan-history`)
      .then(data => {
        setScanHistory(data);
        setLoading(false);
      })
      .catch(error => {
        console.error("Error loading scan history:", error);
        setLoading(false);
      });
  }, [policyId]);
  
  const policy: PolicyData = policyData;
  
  // Calculate summary stats
  const totalScans = scanHistory.length;
  const uniqueSystems = new Set(scanHistory.map(s => s.sid)).size;
  const compliantScans = scanHistory.filter(s => 
    (s.fail || 0) === 0
  ).length;
  const complianceRate = totalScans > 0 
    ? ((compliantScans / totalScans) * 100).toFixed(1) 
    : "0";
  
  return (
    <div>
      <TopPanel
        title={policy.policyName}
        icon="spacewalk-icon-manage-configuration-files"
        button={
          <LinkButton
            href="/rhn/manager/audit/scap/policies"
            text={t("Back to List")}
            icon="fa-chevron-left"
            className="btn-default"
          />
        }
      >
        {/* Summary Cards */}
        <div className="row mb-20">
          <SummaryCard value={totalScans} label="Total Scans" />
          <SummaryCard value={uniqueSystems} label="Systems Scanned" />
          <SummaryCard value={compliantScans} label="Compliant Scans" />
          <SummaryCard value={`${complianceRate}%`} label="Compliance Rate" />
        </div>
        
        {/* Policy Configuration Section */}
        <div className="panel panel-default mb-20">
          <div className="panel-heading">
            <h4>{t("Policy Configuration")}</h4>
          </div>
          <div className="panel-body">
            <dl className="row">
              <ConfigRow label="SCAP Content" value={policy.dataStreamName || t("N/A")} />
              <ConfigRow label="XCCDF Profile" value={policy.xccdfProfileId || t("N/A")} code />
              
              {policy.tailoringFileName && <ConfigRow label="Tailoring File" value={policy.tailoringFileName} />}
              {policy.tailoringProfileId && <ConfigRow label="Tailoring Profile" value={policy.tailoringProfileId} code />}
              {policy.ovalFiles && <ConfigRow label="OVAL Files" value={policy.ovalFiles} />}
              {policy.advancedArgs && <ConfigRow label="Advanced Arguments" value={policy.advancedArgs} code />}
              
              <ConfigRow label="Fetch Remote Resources" value={policy.fetchRemoteResources ? t("Yes") : t("No")} />
              
              {policy.description && <ConfigRow label="Description" value={policy.description} />}
            </dl>
          </div>
        </div>
      </TopPanel>

      {/* Scan History Table - Separate Panel */}
      <Panel headingLevel="h4" title={t("Scan History")} className="scap-scan-history-panel panel-default">
        {loading ? (
          <div className="text-center">
            <i className="fa fa-spinner fa-spin fa-2x" />
          </div>
        ) : scanHistory.length === 0 ? (
          <div className="alert alert-info">
            {t("No scans have been performed with this policy yet.")}
          </div>
        ) : (
          <Table
            data={scanHistory}
            identifier={(scan) => scan.xid}
            initialSortColumnKey="completed"
            initialSortDirection={-1}
            searchField={<SearchField filter={(datum, criteria) => 
              criteria ? datum.serverName.toLowerCase().includes(criteria.toLowerCase()) : true
            } />}
          >
              <Column
                columnKey="serverName"
                header={t("System")}
                cell={(row) => (
                  <a href={`/rhn/systems/details/Overview.do?sid=${row.sid}`}>
                    {row.serverName}
                  </a>
                )}
              />
              <Column
                columnKey="completed"
                header={t("Scan Date")}
                cell={(row) => moment(row.completed).format("YYYY-MM-DD HH:mm")}
              />

              <Column
                columnKey="passed"
                header={t("Passed")}
                columnClass="text-center"
                headerClass="text-center"
                cell={(row) => row.pass || 0}
              />
              <Column
                columnKey="failed"
                header={t("Failed")}
                columnClass="text-center"
                headerClass="text-center"
                cell={(row) => row.fail || 0}
              />
              <Column
                columnKey="other"
                header={t("Other")}
                columnClass="text-center"
                headerClass="text-center"
                cell={(row) => row.other || 0}
              />
              <Column
                columnKey="status"
                header={t("Status")}
                columnClass="text-center"
                headerClass="text-center"
                cell={(row) => {
                  const status = getComplianceStatus(row.fail || 0);
                  return <span className={`label ${status.className}`}>{status.text}</span>;
                }}
              />
              <Column
                columnKey="actions"
                header={t("Actions")}
                columnClass="text-center"
                headerClass="text-center"
                cell={(row) => (
                  <LinkButton
                    href={`/rhn/systems/details/audit/XccdfDetails.do?sid=${row.sid}&xid=${row.xid}`}
                    text={t("View Details")}
                    className="btn-default btn-sm"
                  />
                )}
              />
            </Table>
        )}
      </Panel>
    </div>
  );
};

export const renderer = () => {
  const policyId = (window as any).policyId;
  const policyData = (window as any).policyData;
  
  return SpaRenderer.renderNavigationReact(
    <ScapPolicyDetails policyId={policyId} policyData={policyData} />,
    document.getElementById("scap-policy-details")
  );
};
