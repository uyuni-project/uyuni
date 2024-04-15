import * as React from "react";

import { Messages, MessageType, Utils as MessagesUtils } from "components/messages";
import { TopPanel } from "components/panels/TopPanel";

import { LocalizedMoment, localizedMoment } from "utils/datetime";
import Network from "utils/network";

import { ActionChain, ActionSchedule } from "./action-schedule";
import { AsyncButton, Button } from "./buttons";
import CoCoAttestationTable from "./coco-attestation/CoCoAttestationTable";
import CoCoReport from "./coco-attestation/CoCoReport";
import { AttestationReport } from "./coco-attestation/Utils";
import { ActionChainLink, ActionLink } from "./links";
import { SectionToolbar } from "./section-toolbar/section-toolbar";

type Props = {
  /** The id of the system to be shown */
  serverId?: number;

  /** The action chains */
  actionChains?: Array<ActionChain>;
};

type State = {
  messages: MessageType[];
  confirmSchedule: boolean;
  showReportDetails: boolean;
  earliest: LocalizedMoment;
  actionChain?: ActionChain;
  reportSelected?: AttestationReport;
  activeTab?: string;
};

class CoCoScansList extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      messages: [],
      confirmSchedule: false,
      showReportDetails: false,
      earliest: localizedMoment(),
    };
  }

  onDateTimeChanged = (date) => {
    this.setState({ earliest: date });
  };

  onActionChainChanged = (actionChain) => {
    this.setState({ actionChain: actionChain });
  };

  onSchedule = () => {
    const request = {
      actionType: "coco.attestation",
      earliest: this.state.earliest,
      actionChain: this.state.actionChain ? this.state.actionChain.text : null,
    };

    Network.post(`/rhn/manager/api/systems/${this.props.serverId}/details/coco/scheduleAction`, request)
      .then((data) => {
        // Notify the successful outcome
        const msg = MessagesUtils.info(
          request.actionChain ? (
            <span>
              {t('Action has been successfully added to the action chain <link>"{name}"</link>.', {
                name: request.actionChain,
                link: (str) => <ActionChainLink id={data}>{str}</ActionChainLink>,
              })}
            </span>
          ) : (
            <span>
              {t("The action has been <link>scheduled</link>.", {
                link: (str) => <ActionLink id={data}>{str}</ActionLink>,
              })}
            </span>
          )
        );

        this.setState({
          confirmSchedule: false,
          messages: msg,
        });
      })
      .catch((err) => {
        this.setState({ messages: MessagesUtils.error(t("Unable to perform action.")) });
      });
  };

  onBack = () => {
    this.setState({
      messages: [],
      confirmSchedule: false,
      showReportDetails: false,
    });
  };

  onRunAttestation = () => {
    this.setState({
      messages: [],
      confirmSchedule: true,
    });
  };

  onDetails = (report: AttestationReport) => {
    this.setState({
      reportSelected: report,
      showReportDetails: true,
    });
  };

  render = () => {
    const messages = <Messages items={this.state.messages} />;

    const confirmSchedulePanel = this.props.serverId !== undefined && (
      <TopPanel title={t("Confirm Attestation Execution")} icon="fa-check-square-o">
        {messages}
        <SectionToolbar>
          <div className="action-button-wrapper">
            <div className="btn-group pull-right">
              <AsyncButton
                className="btn-default"
                defaultType="btn-danger"
                text={t("Schedule")}
                action={this.onSchedule}
              />
            </div>
          </div>
          <div className="selector-button-wrapper">
            <div className="btn-group pull-left">
              <Button className="btn-default" icon="fa-chevron-left" text={t("Back to list")} handler={this.onBack} />
            </div>
          </div>
        </SectionToolbar>
        <ActionSchedule
          earliest={this.state.earliest}
          actionChains={this.props.actionChains}
          onActionChainChanged={this.onActionChainChanged}
          onDateTimeChanged={this.onDateTimeChanged}
          systemIds={[this.props.serverId]}
          actionType="coco.attestation"
        />
      </TopPanel>
    );

    const attestationList = (
      <TopPanel title={t("Confidential Computing Attestations")} icon="fa fa-list">
        {messages}
        <SectionToolbar>
          <div className="action-button-wrapper">
            <span className="btn-group pull-right">
              <AsyncButton
                id="run-btn"
                icon="fa-refresh"
                action={this.onRunAttestation}
                text={t("Schedule Attestation")}
              />
            </span>
          </div>
        </SectionToolbar>
        <CoCoAttestationTable
          dataUrl={`/rhn/manager/api/systems/${this.props.serverId}/details/coco/listAttestations`}
          showSystem={this.props.serverId === undefined}
          onReportDetails={this.onDetails}
        />
      </TopPanel>
    );

    if (this.state.confirmSchedule) {
      return confirmSchedulePanel;
    }

    if (this.state.showReportDetails && this.state.reportSelected !== undefined) {
      return (
        <TopPanel title={t("Attestation Report details")} icon="fa fa-file-text-o">
          {messages}
          <SectionToolbar>
            <div className="selector-button-wrapper">
              <div className="btn-group pull-left">
                <Button className="btn-default" icon="fa-chevron-left" text={t("Back to list")} handler={this.onBack} />
              </div>
            </div>
          </SectionToolbar>
          <CoCoReport report={this.state.reportSelected} />
        </TopPanel>
      );
    }

    return attestationList;
  };
}

export default CoCoScansList;
