import { ActionChain } from "components/action-schedule";

import * as React from "react";
import { Loading } from "components/utils/Loading";
import { Form } from "components/input/Form";
import { FormMultiInput } from "components/input/FormMultiInput";
import { Panel } from "components/panels/Panel";
import { PanelRow } from "components/panels/PanelRow";
import { Text } from "components/input/Text";
import { Password } from "components/input/Password";
import { Select } from "components/input/Select";
import { Check } from "components/input/Check";
import { unflattenModel, flattenModel } from "components/input/form-utils";
import Validation from "components/validation";
import { SubmitButton, Button } from "components/buttons";
import { Messages } from "components/messages";
import { ActionSchedule } from "components/action-schedule";
import { VirtualizationPoolCapsApi } from "./virtualization-pools-capabilities-api";
import * as FieldsData from "./properties/fields-data";

import { MessageType } from "components/messages";
import { localizedMoment } from "utils";

type Props = {
  serverId: string;
  submitText: string;
  submit: (arg0: any) => void;
  initialModel: any | null | undefined;
  messages: Array<MessageType>;
  localTime: string;
  timezone: string;
  actionChains: Array<ActionChain>;
};

function clearFields(initialModel, setModel) {
  if (initialModel != null) {
    // Split the hosts name and port
    let model = initialModel;
    if ((model.source || {}).hosts) {
      const splitHosts = model.source.hosts.map((host) => {
        const [hostname, port] = host.split(":");
        return { name: hostname, port };
      });
      model.source.hosts = splitHosts;
    }
    // Flatten the model for the form
    let flattened = flattenModel(model);
    // Set source_adapter_selection
    if (flattened.type === "scsi") {
      flattened[`source_adapter_selection`] = FieldsData.computeSourceAdapterSelection(flattened);
    }
    setModel(flattened);
  } else {
    setModel({});
  }
}

export function PoolProperties(props: Props) {
  const [model, setModel] = React.useState(props.initialModel ? flattenModel(props.initialModel) : {});
  const [invalid, setInvalid] = React.useState(false);
  const [actionChain, setActionChain] = React.useState<ActionChain | null | undefined>(null);
  const [earliest, setEarliest] = React.useState(localizedMoment());

  React.useEffect(() => {
    clearFields(props.initialModel, setModel);
  }, [props.initialModel]);

  const onValidate = (isValid: boolean) => {
    setInvalid(!isValid);
  };

  const onChange = (newModel: any) => {
    setModel(Object.assign({}, newModel));
  };

  const onSubmit = () => {
    // Prepare the model for the submit action
    let data = unflattenModel(model);

    // Merge hosts name and port
    if (data.source != null && data.source.hosts != null) {
      const hosts = data.source.hosts.map((host) => {
        const port = host.port ? `:${host.port}` : "";
        return `${host.name}${port}`;
      });
      data.source.hosts = hosts;
    }

    data["actionChain"] = actionChain;
    data["earliest"] = earliest;
    if (data.source_adapter_selection != null) {
      delete data["source_adapter_selection"];
    }

    props.submit(data);
  };

  const onDateTimeChanged = (value: moment.Moment) => {
    setEarliest(value);
  };

  const onActionChainChanged = (newActionChain: ActionChain | null | undefined) => {
    setActionChain(newActionChain);
  };

  const onPoolTypeChanged = () => {
    const hostsProps = FieldsData.getValue(model.type, "source_hosts.show", false)
      ? { source_hosts0_name: "", source_host0_port: "" }
      : {};
    const devicesProps =
      FieldsData.getValue(model.type, "source_devices.show", false) &&
      FieldsData.getValue(model.type, "source_devices.min", 1)
        ? { source_devices0_path: "", source_devices0_separator: "" }
        : {};
    setModel(
      Object.assign(
        {},
        {
          name: model.name,
          type: model.type,
        },
        hostsProps,
        devicesProps
      )
    );
  };

  return (
    <VirtualizationPoolCapsApi hostId={props.serverId}>
      {({ capabilities, messages: capsError }) => {
        if (capabilities != null) {
          const pool_types = capabilities["pool_types"]
            .filter((pool_type) => pool_type.supported)
            .map((pool_type) => pool_type["name"]);

          const pool_type = capabilities["pool_types"].find((type) => type.name === model.type);
          const source_format_types = (((pool_type || {})["options"] || {})["pool"] || {})["sourceFormatType"];
          const source_format_default = (((pool_type || {})["options"] || {})["pool"] || {})["default_format"];

          const default_adapter_type = "scsi_host";
          const selected_adapter_type = model.source_adapter_type || default_adapter_type;

          const default_adapter_selection = FieldsData.getValue(
            model.type,
            `source_adapter.${selected_adapter_type}.default_selection`,
            ""
          );

          const selected_adapter_selection = model.source_adapter_selection || default_adapter_selection;

          const adapter_selections = FieldsData.getValue(
            model.type,
            `source_adapter.${selected_adapter_type}.selection`,
            {}
          );
          const adapter_fields = FieldsData.getValue(
            model.type,
            `source_adapter.${selected_adapter_type}.fields`,
            []
          ).concat(
            FieldsData.getValue(model.type, `source_adapter.${selected_adapter_type}.${selected_adapter_selection}`, [])
          );

          const renderDeviceFields = (index: number, in_list: boolean) => {
            const has_separator = FieldsData.getValue(model.type, "source_devices.allow_part", false);
            const path_field = (
              <Text
                name={`source_devices${index}_path`}
                label={t(FieldsData.getValue(model.type, "source_devices.label", "Path"))}
                required
                labelClass={has_separator ? "col-md-6" : "col-md-3"}
                divClass={has_separator ? "col-md-6" : "col-md-6"}
              />
            );

            // TODO Add link to doc for the part separator
            const separator_field = has_separator && (
              <div className={"col-md-4"}>
                <Select
                  name={`source_devices${index}_separator`}
                  label={t("Partition separator")}
                  labelClass={"col-md-4"}
                  divClass={"col-md-5"}
                  defaultValue=""
                  options={[
                    { value: "yes", label: t("Yes") },
                    { value: "no", label: t("No") },
                  ]}
                  isClearable
                />
              </div>
            );

            if (has_separator) {
              return (
                <>
                  <div className={"col-md-6"}>{path_field}</div>
                  {separator_field}
                </>
              );
            }

            return path_field;
          };

          const renderHostFields = (index: number, in_list: boolean) => {
            return (
              <>
                <div className="col-md-7">
                  <Text
                    name={`source_hosts${index}_name`}
                    label={t("Host name")}
                    labelClass="col-md-5"
                    divClass="col-md-7"
                    required={FieldsData.getValue(model.type, "source_hosts.required", false)}
                  />
                </div>
                <div className={in_list ? "col-md-2" : "col-md-3"}>
                  <Text
                    name={`source_hosts${index}_port`}
                    label={t("Port")}
                    labelClass="col-md-3"
                    divClass={in_list ? "col-md-9" : "col-md-5"}
                  />
                </div>
              </>
            );
          };

          return (
            <Form
              className="form-horizontal"
              model={model}
              onValidate={onValidate}
              onChange={onChange}
              onSubmit={onSubmit}
            >
              <Messages items={props.messages} />
              <Panel key="general" title={t("General")} headingLevel="h2">
                {(props.initialModel || {}).name === undefined && (
                  <Text
                    name="name"
                    label={t("Name")}
                    required
                    invalidHint={t("Can not contain the following characters: /\\")}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                    validators={[Validation.matches(/^[^/\\]+$/)]}
                  />
                )}
                <Select
                  labelClass="col-md-3"
                  divClass="col-md-6"
                  label={t("Pool Type")}
                  name="type"
                  required
                  disabled={(props.initialModel || {}).type !== undefined}
                  hint={t(FieldsData.getValue(model.type, "description", ""))}
                  defaultValue={pool_types[0]}
                  onChange={onPoolTypeChanged}
                  options={pool_types}
                />
                <Check
                  name="autostart"
                  label={t("Start during virtual host boot")}
                  divClass="col-md-6 col-md-offset-3"
                />
              </Panel>

              {FieldsData.getValue(model.type, "panels", []).includes("source") && (
                <Panel key="source" title={t("Source")} headingLevel="h3">
                  {FieldsData.getValue(model.type, "source_hosts.show", false) &&
                    !FieldsData.getValue(model.type, "source_hosts.list", false) && (
                      <PanelRow>{renderHostFields(0, false)}</PanelRow>
                    )}
                  {FieldsData.getValue(model.type, "source_hosts.show", false) &&
                    FieldsData.getValue(model.type, "source_hosts.list", false) && (
                      <FormMultiInput
                        id="source_hosts"
                        prefix="source_hosts"
                        title={t("Hosts")}
                        onAdd={(index: number) => {
                          const newProperties = {
                            [`source_hosts${index}_name`]: "",
                            [`source_hosts${index}_port`]: "",
                          };
                          setModel(Object.assign({}, model, newProperties));
                        }}
                        onRemove={(index: number) => {
                          setModel(
                            Object.entries(model).reduce((res, entry) => {
                              const property = !entry[0].startsWith(`source_hosts${index}_`)
                                ? { [entry[0]]: entry[1] }
                                : undefined;
                              return Object.assign(res, property);
                            }, {})
                          );
                        }}
                        disabled={false}
                      >
                        {(index: number) => renderHostFields(index, true)}
                      </FormMultiInput>
                    )}
                  {FieldsData.getValue(model.type, "source_devices.show", false) &&
                    !FieldsData.getValue(model.type, "source_devices.list", false) && (
                      <PanelRow>{renderDeviceFields(0, false)}</PanelRow>
                    )}
                  {FieldsData.getValue(model.type, "source_devices.show", false) &&
                    FieldsData.getValue(model.type, "source_devices.list", true) && (
                      <FormMultiInput
                        id="source_devices"
                        prefix="source_devices"
                        title={t("Devices")}
                        onAdd={(index: number) => {
                          const newProperties = {
                            [`source_devices${index}_path`]: "",
                            [`source_devices${index}_separator`]: "",
                          };
                          setModel(Object.assign({}, model, newProperties));
                        }}
                        onRemove={(index: number) => {
                          setModel(
                            Object.entries(model).reduce((res, entry) => {
                              const property = !entry[0].startsWith(`source_devices${index}_`)
                                ? { [entry[0]]: entry[1] }
                                : undefined;
                              return Object.assign(res, property);
                            }, {})
                          );
                        }}
                        disabled={false}
                      >
                        {(index: number) => renderDeviceFields(index, true)}
                      </FormMultiInput>
                    )}
                  {FieldsData.getValue(model.type, "source_dir.show", false) && (
                    <Text
                      name="source_dir"
                      label={t(
                        FieldsData.getValue(
                          model.type,
                          `source_dir.label.${model.source_format || "auto"}`,
                          "Directory"
                        )
                      )}
                      required={FieldsData.getValue(model.type, "source_dir.required", false)}
                      labelClass="col-md-3"
                      divClass="col-md-6"
                    />
                  )}
                  {FieldsData.getValue(model.type, "source_initiator.show", false) && (
                    <Text
                      name="source_initiator"
                      label={t("Initiator IQN")}
                      required={FieldsData.getValue(model.type, "source_initiator.required", false)}
                      labelClass="col-md-3"
                      divClass="col-md-6"
                    />
                  )}
                  {FieldsData.getValue(model.type, "source_adapter", null) != null && (
                    <>
                      <Select
                        name="source_adapter_type"
                        label={t("Adapter type")}
                        labelClass="col-md-3"
                        divClass="col-md-6"
                        defaultValue={default_adapter_type}
                        onChange={() => (model.source_adapter_selection = undefined)}
                        options={Object.keys(FieldsData.getValue(model.type, "source_adapter", {}))}
                      />
                      <Select
                        name="source_adapter_selection"
                        label={t(
                          FieldsData.getValue(model.type, `source_adapter.${selected_adapter_type}.selection_title`, "")
                        )}
                        labelClass="col-md-3"
                        divClass="col-md-6"
                        defaultValue={default_adapter_selection}
                        options={Object.keys(adapter_selections).map((k) => ({
                          value: k,
                          label: adapter_selections[k],
                        }))}
                      />
                      {/* required one of name || parent_address && parent_address_uid */}
                      {adapter_fields.includes("name") && (
                        <Text
                          name="source_adapter_name"
                          label={t("Adapter name")}
                          required
                          labelClass="col-md-3"
                          divClass="col-md-6"
                        />
                      )}
                      {adapter_fields.includes("parent_address") && (
                        <Text
                          name="source_adapter_parentAddress"
                          label={t("Adapter parent PCI address")}
                          required
                          labelClass="col-md-3"
                          divClass="col-md-6"
                          invalidHint={t("PCI address formatted like 0000:00:00.0")}
                          validators={[
                            Validation.matches(/^[0-9a-fA-F]{4}:[0-9a-fA-F]{2}:[0-9a-fA-F]{1}.[0-9a-fA-F]$/),
                          ]}
                        />
                      )}
                      {adapter_fields.includes("parent_address_uid") && (
                        <Text
                          name="source_adapter_parentAddressUid"
                          label={t("Adapter parent address unique ID")}
                          required
                          labelClass="col-md-3"
                          divClass="col-md-6"
                          invalidHint={t("PCI address formatted like 0000:00:00.0")}
                          validators={[Validation.isInt]}
                        />
                      )}
                      {adapter_fields.includes("parent") && (
                        <Text
                          name="source_adapter_parent"
                          label={t("Adapter parent name")}
                          labelClass="col-md-3"
                          divClass="col-md-6"
                        />
                      )}
                      {adapter_fields.includes("parent_wwnn") && (
                        <Text
                          name="source_adapter_parentWwnn"
                          label={t("Adapter parent wwnn")}
                          labelClass="col-md-3"
                          divClass="col-md-6"
                          invalidHint={t("16 characters long hexadecimal value. Example: 500277a4100c4e21")}
                          validators={[Validation.matches(/^(0x)?[0-9a-fA-F]{16}$/)]}
                        />
                      )}
                      {adapter_fields.includes("parent_wwpn") && (
                        <Text
                          name="source_adapter_parentWwpn"
                          label={t("Adapter parent wwpn")}
                          labelClass="col-md-3"
                          divClass="col-md-6"
                          invalidHint={t("16 characters long hexadecimal value. Example: 500277a4100c4e21")}
                          validators={[Validation.matches(/^(0x)?[0-9a-fA-F]{16}$/)]}
                        />
                      )}
                      {adapter_fields.includes("parent_fabric_wwn") && (
                        <Text
                          name="source_adapter_parentFabricWwn"
                          label={t("Adapter parent fabric wwn")}
                          labelClass="col-md-3"
                          divClass="col-md-6"
                          invalidHint={t("16 characters long hexadecimal value. Example: 500277a4100c4e21")}
                          validators={[Validation.matches(/^(0x)?[0-9a-fA-F]{16}$/)]}
                        />
                      )}
                      {adapter_fields.includes("wwnn") && (
                        <Text
                          name="source_adapter_wwnn"
                          label={t("Adapter wwnn")}
                          required
                          labelClass="col-md-3"
                          divClass="col-md-6"
                          invalidHint={t("16 characters long hexadecimal value. Example: 500277a4100c4e21")}
                          validators={[Validation.matches(/^(0x)?[0-9a-fA-F]{16}$/)]}
                        />
                      )}
                      {adapter_fields.includes("wwpn") && (
                        <Text
                          name="source_adapter_wwpn"
                          label={t("Adapter wwpn")}
                          required
                          labelClass="col-md-3"
                          divClass="col-md-6"
                          invalidHint={t("16 characters long hexadecimal value. Example: 500277a4100c4e21")}
                          validators={[Validation.matches(/^(0x)?[0-9a-fA-F]{16}$/)]}
                        />
                      )}
                      {adapter_fields.includes("managed") && (
                        <Check
                          name="source_adapter_managed"
                          label={t("Manage vHBA deletion")}
                          divClass="col-md-6 col-md-offset-3"
                        />
                      )}
                    </>
                  )}
                  {FieldsData.getValue(model.type, "source_name.show", false) && (
                    <Text
                      name="source_name"
                      label={t("Source name")}
                      required={FieldsData.getValue(model.type, "source_name.required", false)}
                      labelClass="col-md-3"
                      divClass="col-md-6"
                    />
                  )}
                  {source_format_types != null && (
                    <Select
                      name="source_format"
                      label={t("Format")}
                      labelClass="col-md-3"
                      divClass="col-md-6"
                      defaultValue={source_format_default}
                      options={source_format_types}
                    />
                  )}
                  {FieldsData.getValue(model.type, "has_auth", false) && (
                    <>
                      <Text
                        name="source_auth_username"
                        label={t("Username")}
                        labelClass="col-md-3"
                        divClass="col-md-6"
                      />
                      <Password
                        name="source_auth_password"
                        label={t("Passphrase")}
                        labelClass="col-md-3"
                        divClass="col-md-6"
                      />
                    </>
                  )}
                </Panel>
              )}

              {FieldsData.getValue(model.type, "panels", []).includes("target") && (
                <Panel key="target" title={t("Target")} headingLevel="h3">
                  <Text
                    name="target_path"
                    label={t("Path")}
                    hint={t(FieldsData.getValue(model.type, "target_path.hint", undefined))}
                    required={FieldsData.getValue(model.type, "target_path.required", false)}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Text
                    name="target_owner"
                    label={t("Owner UID")}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                    validators={[Validation.isInt({ min: 0 })]}
                    invalidHint={t("UID is a numeric value")}
                  />
                  <Text
                    name="target_group"
                    label={t("Group ID")}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                    validators={[Validation.isInt({ min: 0 })]}
                    invalidHint={t("GID is a numeric value")}
                  />
                  <Text
                    name="target_mode"
                    label={t("Permission mode")}
                    hint={t("target directory permissions in octal form, like 0775")}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                    validators={[Validation.isInt({ gt: 0 })]}
                    invalidHint={t("GID is a numeric value")}
                  />
                  {/* TODO Add Link to the UI Reference */}
                  <Text name="target_seclabel" label={t("SELinux label")} labelClass="col-md-3" divClass="col-md-6" />
                </Panel>
              )}

              <Panel key="schedule" title={t("Schedule")} headingLevel="h3">
                <ActionSchedule
                  earliest={earliest}
                  actionChains={props.actionChains}
                  onActionChainChanged={onActionChainChanged}
                  onDateTimeChanged={onDateTimeChanged}
                />
              </Panel>
              <div className="col-md-offset-3 col-md-6">
                <SubmitButton id="submit-btn" className="btn-success" text={props.submitText} disabled={invalid} />
                <Button
                  id="clear-btn"
                  className="btn-default pull-right"
                  icon="fa-eraser"
                  text={t("Clear Fields")}
                  handler={clearFields}
                />
              </div>
            </Form>
          );
        }
        return <Loading text={t("Loading...")} withBorders={false} />;
      }}
    </VirtualizationPoolCapsApi>
  );
}
