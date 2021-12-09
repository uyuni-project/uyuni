import { ActionChain } from "components/action-schedule";

import * as React from "react";
import _isNil from "lodash/isNil";
import Validation from "components/validation";
import { Loading } from "components/utils/Loading";
import { Form } from "components/input/Form";
import { Radio } from "components/input/Radio";
import { Panel } from "components/panels/Panel";
import { Text } from "components/input/Text";
import { Range } from "components/input/Range";
import { Select } from "components/input/Select";
import { Check } from "components/input/Check";
import { unflattenModel, flattenModel, stripBlankValues, convertNumbers } from "components/input/form-utils";
import { SubmitButton, Button } from "components/buttons";
import { Messages } from "components/messages";
import { ActionSchedule } from "components/action-schedule";
import { VirtualizationNetworkDevsApi } from "./virtualization-network-devs-api";
import { VirtualPortFields } from "./properties/VirtualPortFields";
import { Interface } from "./properties/Interface";
import { Vlans } from "./properties/Vlans";
import { IpConfig } from "./properties/IpConfig";
import { DnsConfig } from "./properties/DnsConfig";
import * as FieldsData from "./properties/fields-data";
import * as utils from "./properties/utils";

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
  if (initialModel) {
    let definition = initialModel;

    // Convert some int properties to string since validators only work with strings
    // TODO Remove once https://github.com/uyuni-project/uyuni/issues/3391 is completed
    const intToString = (value: any, key: string) => {
      if (!_isNil((value || {})[key]) && typeof value[key] === "number") {
        value[key] = value[key].toString();
      }
    };

    (definition.srv || []).forEach((srv) => {
      intToString(srv, "port");
      intToString(srv, "priority");
      intToString(srv, "weight");
    });

    (definition.vlans || []).forEach((vlan) => intToString(vlan, "tag"));

    // rename the ipv[46] to ipv[46]def
    // TODO Remove once https://github.com/uyuni-project/uyuni/issues/3392 is completed
    ["ipv4", "ipv6"].forEach((prop) => {
      if (definition[prop] != null) {
        intToString(definition[prop], "prefix");
        definition[`${prop}def`] = definition[prop];
        delete definition[prop];
      }
    });

    // Compute the selection fields from the model
    // TODO Remove once https://github.com/uyuni-project/uyuni/issues/3392 is completed
    definition["ipv6-enabled"] = !_isNil(definition.ipv6def);
    if (!_isNil(definition.pf)) {
      definition["interface-selection-type"] = "pf";
    } else if ((Array.isArray(definition.interfaces) && definition.interfaces.length > 0) || !_isNil(definition.vf)) {
      definition["interface-selection-type"] = "vf";
    }
    if ((definition.virtualport || {}).type === "802.1qbh") {
      definition["virtualport-params-type"] = _isNil(definition.virtualport.profileid) ? "vsi" : "profileid";
    }

    // Merge the DNS host names into coma-separated string
    if (!_isNil((definition.dns || {}).hosts)) {
      definition.dns.hosts = definition.dns.hosts.map((host) => ({
        address: host.address,
        names: Array.isArray(host.names) ? host.names.join() : host.names,
      }));
    }

    // Since the flattenModel would flatten these arrays while they should stay as arrays for the Select component
    // merge the vf and interfaces into coma-separated string and split them after the flattening
    ["vf", "interfaces"].forEach((key) => {
      if (Array.isArray(definition[key])) {
        definition[key] = definition[key].join(",");
      }
    });

    let flattened = flattenModel(definition);

    // Now getting vf and interfaces back to array for multi-valued Select component
    ["vf", "interfaces"].forEach((key) => {
      if (!_isNil(flattened[key]) && typeof flattened[key] === "string" && flattened[key] !== "") {
        flattened[key] = flattened[key].split(",");
      }
    });

    setModel(flattened);
  } else {
    setModel({});
  }
}

export function NetworkProperties(props: Props) {
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
    let definition = unflattenModel(convertNumbers(stripBlankValues(model)));

    const toClean = ["interface-selection-type", "ipv6-enabled", "virtualport-params-type"];
    toClean.forEach((field) => {
      if (definition[field] != null) {
        delete definition[field];
      }
    });

    // Rename the ipv4def and ipv6def properties
    ["ipv4", "ipv6"].forEach((prop) => {
      if (definition[`${prop}def`] != null) {
        definition[prop] = definition[`${prop}def`];
        delete definition[`${prop}def`];
      }
    });

    // Split the DNS hosts names to array
    if (definition.dns != null && definition.dns.hosts != null) {
      definition.dns.hosts = definition.dns.hosts.map((host) => ({
        address: host.address,
        names: host.names.split(","),
      }));
    }

    props.submit({
      actionChain,
      earliest,
      definition,
    });
  };

  const onDateTimeChanged = (value: moment.Moment) => {
    setEarliest(value);
  };

  const onActionChainChanged = (newActionChain: ActionChain | null | undefined) => {
    setActionChain(newActionChain);
  };

  const onModeChanged = () => {
    const defaultIfaceSelection = FieldsData.getValue(model.type, "pf", false)
      ? { "interface-selection-type": "vf" }
      : {};
    setModel(
      Object.assign(
        {},
        {
          name: model.name,
          type: model.type,
          autostart: model.autostart,
        },
        defaultIfaceSelection
      )
    );
  };

  const showInterfaces =
    model["interface-selection-type"] !== "pf" && FieldsData.getValue(model.type, "interfaces", false);
  const showPhysicalFunction =
    model["interface-selection-type"] === "pf" && FieldsData.getValue(model.type, "pf", false);
  const showVirtualFunctions =
    model["interface-selection-type"] === "vf" && FieldsData.getValue(model.type, "vf", false);

  const modes = Object.keys(FieldsData.mapping).map((value) => ({
    value,
    label: FieldsData.getValue(value, "label", value),
  }));

  return (
    <VirtualizationNetworkDevsApi hostId={props.serverId}>
      {({ netDevices, messages: netDevicesError }) => {
        if (netDevices != null) {
          return (
            <Form
              className="form-horizontal"
              model={model}
              onValidate={onValidate}
              onChange={onChange}
              onSubmit={onSubmit}
              title={t("network properties")}
            >
              <Messages items={props.messages} />
              <Panel key="general" title={t("General")} headingLevel="h2">
                {props.initialModel?.name === undefined && (
                  <Text
                    name="name"
                    label={t("Name")}
                    required
                    invalidHint={t("Can not contain the following characters: /\\")}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                )}
                <Select
                  labelClass="col-md-3"
                  divClass="col-md-6"
                  label={t("Network type")}
                  name="type"
                  required
                  hint={FieldsData.getValue(model.type, "description", "")}
                  defaultValue={modes[0].value}
                  onChange={onModeChanged}
                  options={modes}
                />
                {model.type === "macvtap" && (
                  <Select
                    labelClass="col-md-3"
                    divClass="col-md-6"
                    label={t("Macvtap mode")}
                    name="macvtapmode"
                    defaultValue="vepa"
                    required
                    options={FieldsData.getValue(model.type, "type", [])}
                  />
                )}
                <Check
                  name="autostart"
                  label={t("Start during virtual host boot")}
                  divClass="col-md-6 col-md-offset-3"
                />
              </Panel>

              {model.type !== "isolated" && (
                <Panel key="connectivity" title={t("Connectivity")} headingLevel="h2">
                  {FieldsData.getValue(model.type, "bridge_name", true) && (
                    <Text
                      name="bridge"
                      label={t("Bridge")}
                      required={FieldsData.getValue(model.type, "bridge_name", false)}
                      labelClass="col-md-3"
                      divClass="col-md-6"
                    />
                  )}
                  {FieldsData.getValue(model.type, "mtu", false) && (
                    <Text
                      name="mtu"
                      label={t("Maximum Transmission Unit (MTU)")}
                      labelClass="col-md-3"
                      divClass="col-md-6"
                      validators={[Validation.isInt({ min: 0 })]}
                      invalidHint={t("The value has to be a positive integer")}
                    />
                  )}
                  {FieldsData.getValue(model.type, "virtualport_types", undefined) && (
                    <>
                      <Select
                        labelClass="col-md-3"
                        divClass="col-md-6"
                        label={t("Virtual Port Type")}
                        name="virtualport_type"
                        key={`virtualport_type_${model.type}`}
                        options={FieldsData.getValue(model.type, "virtualport_types", []).map((label) => ({
                          value: label.toLowerCase().replaceAll(" ", ""),
                          label: label,
                        }))}
                        isClearable
                      />
                      <VirtualPortFields type={model.virtualport_type} />
                    </>
                  )}
                  {FieldsData.getValue(model.type, "pf", false) && (
                    <Radio
                      name="interface-selection-type"
                      inline={true}
                      label={t("Select interfaces")}
                      required
                      labelClass="col-md-3"
                      divClass="col-md-6"
                      defaultValue="vf"
                      items={[
                        { label: t("By physical function"), value: "pf" },
                        {
                          label: FieldsData.getValue(model.type, "vf", false)
                            ? t("By virtual functions")
                            : t("By interfaces"),
                          value: "vf",
                        },
                      ]}
                    />
                  )}
                  {showInterfaces && (
                    <>
                      <Select
                        labelClass="col-md-3"
                        divClass="col-md-6"
                        label={t("Interfaces")}
                        name="interfaces"
                        options={netDevices}
                        getOptionValue={(option) => (option != null ? option.name : "")}
                        formatOptionLabel={(option, { context }) => {
                          return <Interface device={option} short={context === "value"} />;
                        }}
                        isMulti
                      />
                      {(model.interfaces || []).map((nic) => (
                        <div className="col-md-6 col-md-offset-3" key={nic}>
                          <Interface device={netDevices.find((dev) => dev.name === nic)} />
                        </div>
                      ))}
                    </>
                  )}
                  {showPhysicalFunction && (
                    <Select
                      labelClass="col-md-3"
                      divClass="col-md-6"
                      label={t("Physical Function")}
                      name="pf"
                      required
                      options={netDevices.filter((dev) => dev.PF).sort((a, b) => a.name.localeCompare(b.name))}
                      getOptionValue={(option) => option.name}
                      formatOptionLabel={(option, { context }) => {
                        return <Interface device={option} short={context === "value"} />;
                      }}
                    />
                  )}
                  {showVirtualFunctions && (
                    <>
                      <Select
                        labelClass="col-md-3"
                        divClass="col-md-6"
                        label={t("Virtual Functions")}
                        name="vf"
                        required
                        isMulti
                        options={netDevices.filter((dev) => dev.VF).sort((a, b) => a.name.localeCompare(b.name))}
                        getOptionValue={(option) => option["PCI address"]}
                        formatOptionLabel={(option, { context }) => {
                          return <Interface device={option} short={context === "value"} showPciAddress />;
                        }}
                      />
                      <div className="col-md-12">
                        {(model.vf || []).map((nic) => (
                          <div className="col-md-6 col-md-offset-3" key={nic}>
                            <Interface device={netDevices.find((dev) => dev["PCI address"] === nic)} showPciAddress />
                          </div>
                        ))}
                      </div>
                    </>
                  )}
                  {FieldsData.getValue(model.type, "vlan", () => false)(model) && (
                    <Text
                      name="vlans0_tag"
                      label={t("VLAN tag")}
                      labelClass="col-md-3"
                      divClass="col-md-6"
                      validators={[Validation.isInt({ min: 0, max: 4095 })]}
                      invalidHint={t("Integer between 0 and 4095")}
                    />
                  )}
                  {FieldsData.getValue(model.type, "vlan_trunk", () => false)(model) && (
                    <>
                      <Check name="vlantrunk" label={t("VLAN tags trunking")} divClass="col-md-6 col-md-offset-3" />
                      <Vlans />
                    </>
                  )}
                  {model.type === "nat" && (
                    <>
                      <Range
                        prefix="nat_address"
                        label={t("NAT IPv4 range")}
                        title={t("NAT IPv4 range")}
                        labelClass="col-md-3"
                        divClass="col-md-6"
                        validators={[
                          utils.allOrNone,
                          (value) =>
                            Object.values(value).every(
                              (item) =>
                                typeof item === "string" && (item === "" || item.match(utils.ipv4Pattern) != null)
                            ),
                        ]}
                        invalidHint={t("Both values has to be IPv4 addresses")}
                      />
                      <Range
                        prefix="nat_port"
                        label={t("NAT port range")}
                        title={t("NAT port range")}
                        labelClass="col-md-3"
                        divClass="col-md-6"
                        validators={[
                          utils.allOrNone,
                          (value) =>
                            Object.values(value).every(
                              (item) => typeof item === "string" && (item === "" || item.match(/^[0-9]+$/))
                            ),
                          ({ nat_port_start, nat_port_end }) =>
                            (nat_port_start === "" && nat_port_end === "") ||
                            parseInt(nat_port_start, 10) <= parseInt(nat_port_end, 10),
                        ]}
                        invalidHint={t("Both values has to be positive integers")}
                      />
                    </>
                  )}
                </Panel>
              )}

              {FieldsData.getValue(model.type, "addressing", false) && (
                <Panel key="addressing" title={t("Addressing")} headingLevel="h2">
                  <IpConfig />
                  <Check name="ipv6-enabled" label={t("Enable IPv6")} divClass="col-md-6 col-md-offset-3" />
                  {model["ipv6-enabled"] && <IpConfig ipv6 />}
                  <Text name="domain" label={t("Domain name")} labelClass="col-md-3" divClass="col-md-6" />
                  <DnsConfig />
                </Panel>
              )}

              <Panel key="schedule" title={t("Schedule")} headingLevel="h2">
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
    </VirtualizationNetworkDevsApi>
  );
}
