import * as React from "react";

import { Messages, MessageType, Utils as MessagesUtils } from "components/messages";
import { TopPanel } from "components/panels/TopPanel";

import Network from "utils/network";

import { AsyncButton, Button } from "./buttons";
import { BootstrapPanel } from "./panels/BootstrapPanel";
import { CronTimes, RecurringEventPicker, RecurringType } from "./picker/recurring-event-picker";
import { SectionToolbar } from "./section-toolbar/section-toolbar";
import { Toggler } from "./toggler";
import { Loading } from "./utils";

type Props = {
  /** The id of the system to be shown */
  serverId: number;

  /** Environment types */
  availableEnvironmentTypes: object;

  showOnScheduleOption?: boolean;
};

type State = {
  checksForEnvironmentTooltip: string;
  messages: MessageType[];
  supported: boolean;
  enabled: boolean;
  environmentType: string;
  attestOnBoot: boolean;
  attestOnSchedule: boolean;
  scheduleName?: string;
  scheduleType?: RecurringType;
  scheduleCron?: string;
  scheduleCronTimes?: CronTimes;
  originalData?: any;
  loading: boolean;
};

class CoCoSettingsForm extends React.Component<Props, State> {
  public static readonly defaultProps: Partial<Props> = {
    showOnScheduleOption: true,
  };

  constructor(props: Props) {
    super(props);

    this.state = {
      checksForEnvironmentTooltip: "",
      messages: [],
      supported: false,
      enabled: false,
      environmentType: Object.values(props.availableEnvironmentTypes)[0],
      attestOnBoot: false,
      attestOnSchedule: false,
      loading: true,
    };

    this.init();
  }

  init = () => {
    this.setState({ loading: true });

    Network.get(`/rhn/manager/api/systems/${this.props.serverId}/details/coco/settings`).then(
      this.handleResult,
      this.handleRequestError
    );
  };

  onSave = () => {
    const data = {
      supported: this.state.supported,
      enabled: this.state.enabled,
      environmentType: this.state.environmentType,
      attestOnBoot: this.state.attestOnBoot,
    };

    return Network.post(`/rhn/manager/api/systems/${this.props.serverId}/details/coco/settings`, data).then(
      this.handleResult,
      this.handleRequestError
    );
  };

  toggleCocoAttestation = (value) => {
    this.setState({
      enabled: value,
    });
  };

  environmentTypeChanged = (event) => {
    this.setState({
      environmentType: event.target.value,
      checksForEnvironmentTooltip: t(`The enviroment {environment} supports the following checks: <br/>`, {
        environment: event.target.value,
      }),
    });
  };

  toggleAttestOnBoot = (value) => {
    this.setState({
      attestOnBoot: value,
    });
  };

  toggleAttestOnSchedule = (value) => {
    this.setState({
      attestOnSchedule: value,
    });
  };

  onScheduleNameChanged = (scheduleName) => {
    this.setState({ scheduleName: scheduleName });
  };

  onTypeChanged = (type) => {
    this.setState({ scheduleType: type });
  };

  onCronTimesChanged = (cronTimes) => {
    this.setState({ scheduleCronTimes: cronTimes });
  };

  onCustomCronChanged = (cron) => {
    this.setState({ scheduleCron: cron });
  };

  handleResult = (result) => {
    if (!result.success) {
      this.setState({
        messages: MessagesUtils.error(result.messages),
        loading: false,
      });
    } else if (!result.data.supported) {
      this.setState({
        supported: false,
        messages: MessagesUtils.warning(result.messages),
        loading: false,
      });
    } else {
      this.setState({
        messages: MessagesUtils.success(result.messages),
        supported: true,
        enabled: result.data.enabled,
        environmentType: result.data.environmentType,
        attestOnBoot: result.data.attestOnBoot,
        loading: false,
        originalData: result.data,
      });
    }
  };

  handleRequestError = (err) => {
    this.setState({
      messages: Network.responseErrorMessage(err),
      supported: false,
      enabled: false,
      environmentType: Object.values(this.props.availableEnvironmentTypes)[0],
      attestOnBoot: false,
      loading: false,
    });
  };

  onResetChanges = () => {
    this.setState((prevState) => ({
      messages: prevState.originalData === undefined ? MessagesUtils.error(t("Unable to revert changes")) : [],
      enabled: prevState.originalData?.enabled ?? prevState.enabled,
      environmentType: prevState.originalData?.environmentType ?? prevState.environmentType,
      attestOnBoot: prevState.originalData?.attestOnBoot ?? prevState.attestOnBoot,
    }));
  };

  render = () => {
    if (this.state.loading) {
      return (
        <div className="panel panel-default">
          <Loading />
        </div>
      );
    } else {
      const messages = <Messages items={this.state.messages} />;

      const form = (
        <>
          <p>{t("On this page you can configure Confidential Computing settings for this server.")}</p>
          <SectionToolbar>
            <div className="action-button-wrapper">
              <span className="btn-group pull-right">
                <AsyncButton id="save-btn" icon="fa-floppy-o" action={this.onSave} text={t("Save")} />
                <Button
                  id="reset-btn"
                  icon="fa-undo"
                  text={t("Reset Changes")}
                  className="btn btn-default"
                  handler={this.onResetChanges}
                />
              </span>
            </div>
          </SectionToolbar>
          <BootstrapPanel>
            <div className="form-horizontal">
              <div className="form-group">
                <div className="col-md-offset-3 col-md-6">
                  <Toggler
                    className="checkbox"
                    text={t("Enable attestation")}
                    value={this.state.enabled}
                    handler={this.toggleCocoAttestation}
                  />
                </div>
              </div>
              <div className="form-group">
                <label className="col-md-3 control-label">{t("Environment Type")}:</label>
                <div className="col-md-6">
                  <select
                    value={this.state.environmentType ?? undefined}
                    onChange={this.environmentTypeChanged}
                    className="form-control"
                    name="activationKeys"
                    disabled={!this.state.enabled}
                  >
                    {Object.keys(this.props.availableEnvironmentTypes).map((k) => (
                      <option key={k} value={k}>
                        {this.props.availableEnvironmentTypes[k]}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label className="col-md-3 control-label">{t("Executions")}:</label>
                <div className="col-md-6">
                  <Toggler
                    className="checkbox"
                    text={t("Peform attestation during the boot process")}
                    value={this.state.attestOnBoot}
                    handler={this.toggleAttestOnBoot}
                    disabled={!this.state.enabled}
                  />
                </div>
              </div>
              {this.props.showOnScheduleOption && (
                <div className="form-group">
                  <div className="col-md-offset-3 col-md-6">
                    <Toggler
                      className="checkbox"
                      text={t("Peform attestation on a schedule")}
                      value={this.state.attestOnSchedule}
                      handler={this.toggleAttestOnSchedule}
                      disabled={!this.state.enabled}
                    />
                  </div>
                </div>
              )}
            </div>
          </BootstrapPanel>
          {this.state.attestOnSchedule && (
            <BootstrapPanel title={t("Select a schedule")}>
              <RecurringEventPicker
                mode="Inline"
                hideScheduleName
                timezone={window.timezone}
                scheduleName={this.state.scheduleName}
                type={this.state.scheduleType}
                cron={this.state.scheduleCron}
                cronTimes={this.state.scheduleCronTimes}
                onScheduleNameChanged={this.onScheduleNameChanged}
                onTypeChanged={this.onTypeChanged}
                onCronTimesChanged={this.onCronTimesChanged}
                onCronChanged={this.onCustomCronChanged}
              />
            </BootstrapPanel>
          )}
        </>
      );

      return (
        <TopPanel title={t("Settings")} icon="fa fa-pencil-square-o">
          {messages}
          {this.state.supported ? form : null}
        </TopPanel>
      );
    }
  };
}

export default CoCoSettingsForm;
