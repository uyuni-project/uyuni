import { useState } from "react";

import { AsyncButton, Button } from "../buttons";
import { BootstrapPanel } from "../panels/BootstrapPanel";
import { CronTimes, RecurringEventPicker, RecurringType } from "../picker/recurring-event-picker";
import { Toggler } from "../toggler";
import { Settings } from "./Utils";

interface Props {
  initialData: Settings;
  availableEnvironmentTypes: Record<string, string>;
  showOnScheduleOption?: boolean;
  saveHandler: (data: Settings) => void;
}

interface FormModel {
  enabled: boolean;
  environmentType: string;
  attestOnBoot: boolean;
  attestOnSchedule: boolean;
  // The following fields are set by the UI if showOnScheduleOption is true, but they are currently not
  // supported by the backend and not included in the Settings object.
  scheduleName?: string;
  scheduleType?: RecurringType;
  scheduleCron?: string;
  scheduleCronTimes?: CronTimes;
}

export const CoCoSettingsForm: React.FC<Props> = ({
  initialData,
  availableEnvironmentTypes,
  showOnScheduleOption = true,
  saveHandler,
}: Props): JSX.Element => {
  const [model, setModel] = useState<FormModel>(computeFormModel(initialData));

  function onResetChanges() {
    setModel(computeFormModel(initialData));
  }

  // Convert the settings object to the form model, which includes additional fields for handling UI-specific data.
  function computeFormModel(settings: Settings): FormModel {
    const { enabled, environmentType, attestOnBoot } = settings;
    const attestOnSchedule = settings.attestOnSchedule ?? false;

    return {
      enabled,
      environmentType,
      attestOnBoot,
      attestOnSchedule,
    };
  }

  // Convert from the form model to the settings object
  function computeSettings(model: FormModel): Settings {
    const { enabled, environmentType, attestOnBoot } = model;
    const attestOnSchedule = showOnScheduleOption ? model.attestOnSchedule : false;

    return {
      enabled,
      environmentType,
      attestOnBoot,
      attestOnSchedule,
    };
  }

  return (
    <>
      <BootstrapPanel>
        <div className="form-horizontal">
          <div className="form-group">
            <div className="col-md-offset-3 offset-md-3 col-md-6">
              <Toggler
                className="checkbox"
                text={t("Enable attestation")}
                value={model.enabled}
                handler={(enabled) => setModel((prev) => ({ ...prev, enabled }))}
              />
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label" htmlFor="environmentTypes">
              {t("Environment Type")}:
            </label>
            <div className="col-md-6">
              <select
                value={model.environmentType}
                onChange={(event) => {
                  const environmentType = event.target.value;
                  setModel((prev) => ({ ...prev, environmentType }));
                }}
                className="form-control"
                id="environmentTypes"
                name="environmentTypes"
                disabled={!model.enabled}
              >
                {Object.keys(availableEnvironmentTypes).map((k) => (
                  <option key={k} value={k}>
                    {availableEnvironmentTypes[k]}
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
                text={t("Perform attestation during the boot process")}
                value={model.attestOnBoot}
                handler={(attestOnBoot) => setModel((prev) => ({ ...prev, attestOnBoot }))}
                disabled={!model.enabled}
              />
            </div>
          </div>
          {showOnScheduleOption && (
            <div className="form-group">
              <div className="col-md-offset-3 offset-md-3 col-md-6">
                <Toggler
                  className="checkbox"
                  text={t("Perform attestation on a schedule")}
                  value={model.attestOnSchedule}
                  handler={(attestOnSchedule) => setModel((prev) => ({ ...prev, attestOnSchedule }))}
                  disabled={!model.enabled}
                />
              </div>
            </div>
          )}
        </div>
      </BootstrapPanel>
      {showOnScheduleOption && model.attestOnSchedule && (
        <BootstrapPanel title={t("Select a schedule")}>
          <RecurringEventPicker
            mode="Inline"
            hideScheduleName
            scheduleName={model.scheduleName}
            type={model.scheduleType}
            cron={model.scheduleCron}
            cronTimes={model.scheduleCronTimes}
            onScheduleNameChanged={(scheduleName) => setModel((prev) => ({ ...prev, scheduleName }))}
            onTypeChanged={(scheduleType) => setModel((prev) => ({ ...prev, scheduleType }))}
            onCronTimesChanged={(scheduleCronTimes) => setModel((prev) => ({ ...prev, scheduleCronTimes }))}
            onCronChanged={(scheduleCron) => setModel((prev) => ({ ...prev, scheduleCron }))}
          />
        </BootstrapPanel>
      )}
      <div className="row">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <AsyncButton
            id="save-btn"
            icon="fa-floppy-o"
            action={() => saveHandler(computeSettings(model))}
            text={t("Save")}
            className="btn-primary me-2"
          />
          <Button
            id="reset-btn"
            icon="fa-undo"
            text={t("Reset Changes")}
            className="btn-default"
            handler={onResetChanges}
          />
        </div>
      </div>
    </>
  );
};
