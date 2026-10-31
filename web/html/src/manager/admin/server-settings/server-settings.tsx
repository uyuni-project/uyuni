/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { useState } from "react";

import { AsyncButton, Button } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { Check, Form, Text } from "components/input";
import { InputBase } from "components/input/InputBase";
import { Panel } from "components/panels/Panel";
import { TopPanel } from "components/panels/TopPanel";
import { MessagesContainer, showErrorToastr, showSuccessToastr } from "components/toastr/toastr";

import Network, { JsonResult } from "utils/network";

import { ServerSetting, ServerSettingsModel } from "./server-settings.types";

const ENDPOINT = "/rhn/manager/api/admin/config/server-settings";

const toModel = (settings: ServerSetting[]): ServerSettingsModel =>
  Object.fromEntries(
    settings.map((setting) => [
      setting.key,
      setting.type === "BOOLEAN" ? setting.value === "true" : (setting.value ?? ""),
    ])
  );

/** The values which differ from the stored values, booleans as "true" or "false" */
export const changedValues = (settings: ServerSetting[], model: ServerSettingsModel): Record<string, string> => {
  const stored = toModel(settings);
  return Object.fromEntries(
    Object.entries(model)
      .filter(([key, value]) => value !== stored[key])
      .map(([key, value]) => [key, String(value)])
  );
};

const defaultHint = (setting: ServerSetting) => {
  if (setting.type === "BOOLEAN") {
    return setting.defaultValue === "true" ? t("Default: enabled") : t("Default: disabled");
  }
  return setting.defaultValue ? t("Default: {value}", { value: setting.defaultValue }) : t("Default: not set");
};

type Props = {
  settings: ServerSetting[];
};

const ServerSettings = (props: Props) => {
  const [settings, setSettings] = useState(props.settings);
  const [model, setModel] = useState(toModel(props.settings));
  const [errors, setErrors] = useState({});

  const onResponse = (response: JsonResult<ServerSetting[]>) => {
    setSettings(response.data);
    setModel(toModel(response.data));
    setErrors({});
  };

  const onError = (error: JQueryXHR) => {
    const fieldErrors = error.responseJSON?.errors;
    if (fieldErrors) {
      setErrors(fieldErrors);
    }
    if (error.responseJSON?.messages?.length || !fieldErrors) {
      Network.showResponseErrorToastr(error);
    } else {
      showErrorToastr(t("Some values are invalid."));
    }
  };

  const save = () =>
    Network.post(ENDPOINT, changedValues(settings, model))
      .then((response) => {
        onResponse(response);
        showSuccessToastr(t("Server settings saved"));
      })
      .catch(onError);

  const reset = (key: string) =>
    Network.post(`${ENDPOINT}/reset`, { key })
      .then((response) => {
        onResponse(response);
        showSuccessToastr(t("{key} reset to the default value", { key }));
      })
      .catch(onError);

  // InputBase renders the hint in a list, so it needs a key
  const hint = (setting: ServerSetting) => (
    <span key="hint">
      {setting.description}
      <br />
      {defaultHint(setting)}
      {setting.modified && (
        <Button
          id={`reset-${setting.key}`}
          className="btn-link btn-sm"
          text={t("Reset to default")}
          handler={() => reset(setting.key)}
        />
      )}
    </span>
  );

  return (
    <TopPanel title={t("Server Configuration - Server Settings")} icon="fa-info-circle">
      <div className="page-summary">
        <MessagesContainer />
        <p>{t("Change the server settings which are stored in the database.")}</p>
      </div>
      <Form model={model} errors={errors} onChange={(newModel) => setModel({ ...newModel })}>
        <Panel headingLevel="h2" title={t("Server Settings")}>
          {settings.map((setting) =>
            setting.type === "BOOLEAN" ? (
              <InputBase<boolean>
                key={setting.key}
                name={setting.key}
                label={setting.key}
                hint={hint(setting)}
                labelClass="col-md-3"
                divClass="col-md-6"
              >
                {({ setValue, onBlur }) => (
                  <Check
                    id={setting.key}
                    name={setting.key}
                    checked={model[setting.key] === true}
                    onChange={(checked) => setValue(setting.key, checked)}
                    onBlur={onBlur}
                  />
                )}
              </InputBase>
            ) : (
              <Text
                key={setting.key}
                name={setting.key}
                label={setting.key}
                hint={hint(setting)}
                placeholder={setting.defaultValue ?? undefined}
                type={setting.type === "INTEGER" ? "number" : "text"}
                labelClass="col-md-3"
                divClass="col-md-6"
              />
            )
          )}
          <div className="row">
            <div className="col-md-6 col-md-offset-3 offset-md-3">
              <AsyncButton id="saveButton" className="btn-primary" text={t("Save")} action={save} />
            </div>
          </div>
        </Panel>
      </Form>
    </TopPanel>
  );
};

export default withPageWrapper(ServerSettings);
