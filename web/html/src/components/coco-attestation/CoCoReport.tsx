import * as React from "react";

import { ActionLink, SystemLink } from "../links";
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
      <div className="panel panel-default" key="overview">
        <ul className="list-group">
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Status:")}</strong>
              </div>
              <div className="col-md-10">{report.statusDescription}</div>
            </div>
          </li>
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("System")}:</strong>
              </div>
              <div className="col-md-10">
                <SystemLink id={report.systemId}>{report.systemName}</SystemLink> ({report.systemId})
              </div>
            </div>
          </li>
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Environment Type")}:</strong>
              </div>
              <div className="col-md-10">{report.environmentTypeDescription}</div>
            </div>
          </li>
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Created on")}:</strong>
              </div>
              <div className="col-md-10">{renderTime(report.creationTime)}</div>
            </div>
          </li>
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Last modified on")}:</strong>
              </div>
              <div className="col-md-10">{renderTime(report.modificationTime)}</div>
            </div>
          </li>
          {report.actionId && (
            <li className="list-group-item">
              <div className="row">
                <div className="col-md-2">
                  <strong>{t("Action")}:</strong>
                </div>
                <div className="col-md-10">
                  <ActionLink id={report.actionId}>
                    {report.actionScheduledBy
                      ? t("{actionName} scheduled by {actionScheduledBy}", report)
                      : report.actionName}
                  </ActionLink>
                </div>
              </div>
            </li>
          )}
          {report.status === "SUCCEEDED" && (
            <li className="list-group-item">
              <div className="row">
                <div className="col-md-2">
                  <strong>{t("Attested on")}:</strong>
                </div>
                <div className="col-md-10">{renderTime(report.attestationTime)}</div>
              </div>
            </li>
          )}
        </ul>
      </div>
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
