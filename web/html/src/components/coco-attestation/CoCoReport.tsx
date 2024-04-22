import * as React from "react";

import { ActionLink, SystemLink } from "../links";
import { BootstrapPanel } from "../panels/BootstrapPanel";
import { TabContainer } from "../tab-container";
import CoCoResult from "./CoCoResult";
import { AttestationReport, renderStatus, renderTime } from "./Utils";

type Props = {
  report: AttestationReport;
  activeTab?: string;
};

type State = {
  activeTabHash: string;
};

class CoCoReport extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      activeTabHash: this.buildHash(this.props.activeTab ?? "overview"),
    };
  }

  onTabHashChange = (hash) => {
    this.setState({ activeTabHash: hash });
  };

  buildHash = (label) => `/details/${this.props.report.id}/${label}`;

  render = () => {
    const report: AttestationReport = this.props.report;

    const overview = (
      <BootstrapPanel key="overview">
        <div className="table-responsive">
          <table className="table">
            <tbody>
              <tr>
                <td>{t("Status:")}</td>
                <td>{report.statusDescription}</td>
              </tr>
              <tr>
                <td>{t("System")}:</td>
                <td>
                  <SystemLink id={report.systemId}>{report.systemName}</SystemLink> ({report.systemId})
                </td>
              </tr>
              <tr>
                <td>{t("Environment Type")}:</td>
                <td>{report.environmentTypeDescription}</td>
              </tr>
              <tr>
                <td>{t("Created on")}:</td>
                <td>{renderTime(report.creationTime)}</td>
              </tr>
              <tr>
                <td>{t("Last modified on")}:</td>
                <td>{renderTime(report.modificationTime)}</td>
              </tr>
              {report.actionId && (
                <tr>
                  <td>{t("Action")}:</td>
                  <td>
                    <ActionLink id={report.actionId}>
                      {report.actionScheduledBy
                        ? t("{actionName} scheduled by {actionScheduledBy}", report)
                        : report.actionName}
                    </ActionLink>
                  </td>
                </tr>
              )}
              {report.status === "SUCCEEDED" && (
                <tr>
                  <td>{t("Attested on")}:</td>
                  <td>{renderTime(report.attestationTime)}</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </BootstrapPanel>
    );

    const labels = report.results.map((result) => (
      <span key={result.id}>{renderStatus(result.status, result.resultTypeLabel)}</span>
    ));
    const hashes = report.results.map((result) => this.buildHash(result.resultType.toLowerCase().replace("_", "-")));
    const contents = report.results.map((result) => <CoCoResult key={result.id} result={result} />);

    return (
      <TabContainer
        labels={[t("Overview"), ...labels]}
        hashes={[this.buildHash("overview"), ...hashes]}
        tabs={[overview, ...contents]}
        initialActiveTabHash={this.state.activeTabHash}
        onTabHashChange={this.onTabHashChange}
      />
    );
  };
}

export default CoCoReport;
