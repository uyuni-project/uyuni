import { useEffect, useRef, useState } from "react";

import { AsyncButton, Button, LinkButton } from "components/buttons";
import { Form, FormGroup, Label, Text } from "components/input";
import { LargeTextInput, LargeTextInputRef } from "components/large-text-input";
import { BootstrapPanel, Panel } from "components/panels";
import { CronTimes, RecurringEventPicker, RecurringType } from "components/picker/recurring-event-picker";
import { Toggler } from "components/toggler";

import { HOST_KEY_DOCUMENT_FIELD, SECURE_EXECUTION_HEADER_FIELD, Settings } from "./Utils";

const PEM_CERTIFICATE_PLACEHOLDER = ["-----BEGIN CERTIFICATE-----", "...", "-----END CERTIFICATE-----"].join("\n");

interface Props {
  initialData: Settings;
  availableEnvironmentTypes: Record<string, string>;
  showOnScheduleOption?: boolean;
  saveHandler: (data: Promise<Settings>) => Promise<unknown>;
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
  currentSecureExecutionHeader?: string;
  currentHostKeyDocument?: string;
}

export const CoCoSettingsForm: React.FC<Props> = ({
  initialData,
  availableEnvironmentTypes,
  showOnScheduleOption = true,
  saveHandler,
}: Props): JSX.Element => {
  const [model, setModel] = useState<FormModel>(() => computeFormModel(initialData));
  const [validated, setValidated] = useState(true);
  const [headerDownloadUrl, setHeaderDownloadUrl] = useState<string | undefined>(undefined);
  const [hostKeyDownloadUrl, setHostKeyDownloadUrl] = useState<string | undefined>(undefined);

  const hostKeyRef = useRef<LargeTextInputRef>(null);

  // Ensure the model is recomputed if initialData changes
  useEffect(() => setModel(computeFormModel(initialData)), [initialData]);

  // Compute the download url and ensure they are recomputed and revoked correctly
  useEffect(() => {
    let headerUrl: string | undefined;
    let hostKeyUrl: string | undefined;

    if (model.currentSecureExecutionHeader) {
      headerUrl = createDownloadUrl(model.currentSecureExecutionHeader, "application/octet-stream");
      setHeaderDownloadUrl(headerUrl);
    } else {
      setHeaderDownloadUrl(undefined);
    }

    if (model.currentHostKeyDocument) {
      hostKeyUrl = createDownloadUrl(model.currentHostKeyDocument, "text/plain");
      setHostKeyDownloadUrl(hostKeyUrl);
    } else {
      setHostKeyDownloadUrl(undefined);
    }

    // Free the old URLs when data changes or component unmounts
    return () => {
      if (headerUrl) URL.revokeObjectURL(headerUrl);
      if (hostKeyUrl) URL.revokeObjectURL(hostKeyUrl);
    };
  }, [model.currentSecureExecutionHeader, model.currentHostKeyDocument]);

  function onResetChanges() {
    setModel(computeFormModel(initialData));
  }

  function isIbmZEnvironmentType(environmentType: string): boolean {
    return environmentType.startsWith("KVM_IBM_Z");
  }

  // Convert the settings object to the form model, which includes additional fields for handling UI-specific data.
  function computeFormModel(settings: Settings): FormModel {
    const { enabled, environmentType, attestOnBoot } = settings;
    const attestOnSchedule = settings.attestOnSchedule ?? false;

    let [currentSecureExecutionHeader, currentHostKeyDocument]: (string | undefined)[] = [undefined, undefined];

    if (isIbmZEnvironmentType(settings.environmentType)) {
      currentSecureExecutionHeader = settings.inputData?.[SECURE_EXECUTION_HEADER_FIELD];
      currentHostKeyDocument = settings.inputData?.[HOST_KEY_DOCUMENT_FIELD];
    }

    return {
      enabled,
      environmentType,
      attestOnBoot,
      attestOnSchedule,
      currentSecureExecutionHeader,
      currentHostKeyDocument,
    };
  }

  // Convert from the form model to the settings object
  async function computeSettings(model: FormModel): Promise<Settings> {
    const { enabled, environmentType, attestOnBoot } = model;
    const attestOnSchedule = showOnScheduleOption ? model.attestOnSchedule : false;
    const inputData: Record<string, any> = {};

    if (isIbmZEnvironmentType(model.environmentType)) {
      const headerPromise = model.currentSecureExecutionHeader
        ? Promise.resolve(model.currentSecureExecutionHeader)
        : getExtensionHeaderFileContent();

      const hostKeyPromise = model.currentHostKeyDocument
        ? Promise.resolve(model.currentHostKeyDocument)
        : hostKeyRef.current?.getContent();

      const [secureExecutionHeader, hostKeyDocument] = await Promise.all([headerPromise, hostKeyPromise]);

      inputData[SECURE_EXECUTION_HEADER_FIELD] = secureExecutionHeader;
      inputData[HOST_KEY_DOCUMENT_FIELD] = hostKeyDocument;
    }

    return {
      enabled,
      environmentType,
      attestOnBoot,
      attestOnSchedule,
      inputData,
    };
  }

  function getExtensionHeaderFileContent(): Promise<string | undefined> {
    return new Promise((resolve, reject) => {
      const uploadField = document.getElementById("secureExecutionHeaderFile") as HTMLInputElement | null;
      const uploadedFile = uploadField?.files?.[0];
      if (uploadedFile) {
        const reader = new FileReader();
        reader.onload = () => {
          const dataUrl = reader.result as string;
          // Take only the Base64 content from the Data URL
          resolve(dataUrl.split(",")[1]);
        };
        reader.onerror = () => reject(reader.error);
        reader.readAsDataURL(uploadedFile);
      } else {
        reject(t("Unable to retrieve the uploaded file"));
      }
    });
  }

  function createDownloadUrl(content: string, type: string): string {
    let data: BlobPart;
    if (type === "application/octet-stream") {
      // Binary data is Base64 encoded
      const binaryData = atob(content);
      // charCodeAt() is preferred over codePointAt() because atob() outputs characters strictly between 0 and 255
      data = Uint8Array.from(binaryData, (c) => c.charCodeAt(0));
    } else if (type === "text/plain") {
      // Take the content as is for plain text
      data = content;
    } else {
      throw new TypeError("Invalid data type");
    }

    return URL.createObjectURL(new Blob([data], { type }));
  }

  return (
    <Panel headingLevel="h3" header={t("Settings")}>
      <Form
        className="form-horizontal"
        model={model}
        onChange={(updatedValues) => setModel((prev) => ({ ...prev, ...updatedValues }))}
        onValidate={setValidated}
        onSubmit={() => saveHandler(computeSettings(model))}
      >
        <FormGroup>
          <div className="col-md-offset-3 offset-md-3 col-md-6">
            <Toggler
              className="checkbox"
              text={t("Enable attestation")}
              value={model.enabled}
              handler={(enabled) => setModel((prev) => ({ ...prev, enabled }))}
            />
          </div>
        </FormGroup>

        <FormGroup>
          <Label className="col-md-3 control-label" name={t("Environment Type")} required htmlFor="environmentTypes" />
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
        </FormGroup>

        {isIbmZEnvironmentType(model.environmentType) && (
          <>
            {model.currentSecureExecutionHeader !== undefined ? (
              <FormGroup>
                <Label className="col-md-3 control-label" name={t("Secure execution header")} required />
                <div className="col-md-6">
                  <LinkButton
                    text="secure-extension-header.bin"
                    href={headerDownloadUrl}
                    className="btn-link pl-0"
                    title={t("Download the current Secure execution header")}
                    download="secure-extension-header.bin"
                  />
                  <Button
                    icon={"fa-edit"}
                    title={t("Change")}
                    className="btn-tertiary"
                    handler={() => setModel((prev) => ({ ...prev, currentSecureExecutionHeader: undefined }))}
                  />
                </div>
              </FormGroup>
            ) : (
              <Text
                name="secureExecutionHeaderFile"
                label={t("Secure execution header")}
                required
                type="file"
                labelClass="col-md-3"
                divClass="col-md-6"
              />
            )}
            {model.currentHostKeyDocument !== undefined ? (
              <FormGroup>
                <Label className="col-md-3 control-label" name={t("Host key document")} required />
                <div className="col-md-6">
                  <LinkButton
                    text="host-key-document.pem"
                    href={hostKeyDownloadUrl}
                    className="btn-link pl-0"
                    title={t("Download the current Host Key Document certificate")}
                    download="host-key-document.pem"
                  />
                  <Button
                    icon={"fa-edit"}
                    title={t("Change")}
                    className="btn-tertiary"
                    handler={() => setModel((prev) => ({ ...prev, currentHostKeyDocument: undefined }))}
                  />
                </div>
              </FormGroup>
            ) : (
              <LargeTextInput
                ref={hostKeyRef}
                name="hostKeyDocument"
                required
                label={t("Host key document")}
                uploadLabel={t("Certificate File")}
                uploadHint={t("Certificate file, in PEM format")}
                pasteLabel={t("PEM certificate")}
                pasteHint={t("The text representing the certificate, in PEM format")}
                pastePlaceholder={PEM_CERTIFICATE_PLACEHOLDER}
              />
            )}
          </>
        )}

        <FormGroup>
          <Label className="col-md-3 control-label" name={t("Executions")} />
          <div className="col-md-6">
            <Toggler
              className="checkbox"
              text={t("Perform attestation during the boot process")}
              value={model.attestOnBoot}
              handler={(attestOnBoot) => setModel((prev) => ({ ...prev, attestOnBoot }))}
              disabled={!model.enabled}
            />
          </div>
        </FormGroup>

        {showOnScheduleOption && (
          <FormGroup>
            <div className="col-md-offset-3 offset-md-3 col-md-6">
              <Toggler
                className="checkbox"
                text={t("Perform attestation on a schedule")}
                value={model.attestOnSchedule}
                handler={(attestOnSchedule) => setModel((prev) => ({ ...prev, attestOnSchedule }))}
                disabled={!model.enabled}
              />
            </div>
          </FormGroup>
        )}

        {showOnScheduleOption && model.attestOnSchedule && (
          <FormGroup>
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
          </FormGroup>
        )}

        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <AsyncButton
            id="save-btn"
            icon="fa-floppy-o"
            action={() => saveHandler(computeSettings(model))}
            text={t("Save")}
            disabled={!validated}
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
      </Form>
    </Panel>
  );
};
