import * as React from "react";

import { AsyncButton, Button } from "../buttons";
import { BootstrapPanel } from "../panels/BootstrapPanel";
import { RecurringEventPicker } from "../picker/recurring-event-picker";
import { SectionToolbar } from "../section-toolbar/section-toolbar";
import { Toggler } from "../toggler";
import { Settings } from "./Utils";

type Props = {
  initialData: Settings;
  availableEnvironmentTypes: object;
  showOnScheduleOption?: boolean;
  saveHandler: (data: Settings) => void;
};

type State = Settings;

class CoCoSettingsForm extends React.Component<Props, State> {
  public static readonly defaultProps: Partial<Props> = {
    showOnScheduleOption: true,
  };

  constructor(props: Props) {
    super(props);

    this.state = {
      ...this.props.initialData,
    };
  }

  toggleCocoAttestation = (value) => {
    this.setState({
      enabled: value,
    });
  };

  environmentTypeChanged = (event) => {
    this.setState({ environmentType: event.target.value });
  };

  toggleAttestOnBoot = (value) => {
    this.setState({ attestOnBoot: value });
  };

  toggleAttestOnSchedule = (value) => {
    this.setState({ attestOnSchedule: value });
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

  onResetChanges = () => {
    this.setState({
      ...this.props.initialData,
    });
  };

  render(): React.ReactNode {
    return (
      <>
        <SectionToolbar>
          <div className="action-button-wrapper">
            <span className="btn-group pull-right">
              <AsyncButton
                id="save-btn"
                icon="fa-floppy-o"
                action={() => this.props.saveHandler(this.state as Settings)}
                text={t("Save")}
              />
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
              <div className="col-md-offset-3 offset-md-3 col-md-6">
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
                <div className="col-md-offset-3 offset-md-3 col-md-6">
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
  }
}

export default CoCoSettingsForm;
