import { ActionChain } from "components/action-schedule";
import { MessageType } from "components/messages";

import * as React from "react";
import { Panel } from "components/panels/Panel";
import { Text } from "components/input/Text";
import { Select } from "components/input/Select";
import { Check } from "components/input/Check";
import Validation from "components/validation";
import { Messages } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import { Loading } from "components/utils/Loading";
import { guestNicsPanel } from "./properties/guest-nics-panel";
import { GuestDisksPanel } from "./properties/GuestDisksPanel";
import { GuestPropertiesForm } from "./properties/guest-properties-form";
import { GuestPropertiesTraditional } from "./properties/guest-properties-traditional";
import { VirtualizationDomainsCapsApi } from "./virtualization-domains-caps-api";
import { VirtualizationListRefreshApi } from "../virtualization-list-refresh-api";
import { VirtualizationPoolCapsApi } from "../pools/virtualization-pools-capabilities-api";
import { TemplatesMessages } from "./properties/templates";

type Props = {
  host: any;
  submitText: string;
  submit: Function;
  initialModel: any | null | undefined;
  messages: Array<MessageType>;
  localTime: string;
  timezone: string;
  actionChains: Array<ActionChain>;
  cobblerProfiles: { string?: string };
};

/**
 * Component editing a virtual machine properties
 */
export function GuestProperties(props: Props) {
  if (!props.host.saltEntitled) {
    return (
      <GuestPropertiesTraditional
        host={props.host}
        submitText={props.submitText}
        submit={props.submit}
        initialModel={props.initialModel}
        messages={props.messages}
        localTime={props.localTime}
        timezone={props.timezone}
        actionChains={props.actionChains}
      />
    );
  }

  const validationChecks = [
    {
      check: (model: any) => !Number.isNaN(Number.parseInt(model.vcpu, 10)) && model.vcpu > props.host.cpu.count,
      message: Messages.warning("Overcommitting CPU can harm performances."),
    },
  ];

  const osTypesLabels = {
    hvm: "Fully Virtualized",
    xen: "Para Virtualized",
    xenpvh: "PVH",
  };

  return (
    <VirtualizationListRefreshApi serverId={props.host.id} lastRefresh={Date.now()} type="nets">
      {({ data: networks, error: netListError }) => (
        <VirtualizationListRefreshApi serverId={props.host.id} lastRefresh={Date.now()} type="pools">
          {({ data: pools, error: poolListError }) => (
            <VirtualizationPoolCapsApi hostId={props.host.id}>
              {({ capabilities: poolCaps, messages: poolsCapsErrors }) => (
                <VirtualizationDomainsCapsApi hostId={props.host.id}>
                  {({ osTypes, domainsCaps, messages }) => {
                    const allMessages = ([] as any[])
                      .concat(props.messages, messages, netListError, poolListError, poolsCapsErrors)
                      .filter((item) => item);
                    const { initialModel } = props;

                    if (
                      initialModel != null &&
                      networks != null &&
                      pools != null &&
                      osTypes.length > 0 &&
                      domainsCaps.length > 0 &&
                      poolCaps != null
                    ) {
                      return (
                        <GuestPropertiesForm
                          submitText={props.submitText}
                          submit={props.submit}
                          initialModel={initialModel}
                          validationChecks={validationChecks}
                          messages={allMessages}
                          localTime={props.localTime}
                          timezone={props.timezone}
                          actionChains={props.actionChains}
                        >
                          {({ model, changeModel }) => {
                            const vmTypes = domainsCaps
                              .map((cap) => cap.domain)
                              .filter((vmType, idx, array) => array.indexOf(vmType) === idx);
                            const vmType =
                              model.vmType || initialModel.vmType || (vmTypes.includes("kvm") ? "kvm" : vmTypes[0]);
                            const arch = initialModel.arch || props.host.cpu.arch.toLowerCase();
                            const caps = domainsCaps.find((cap) => cap.arch === arch && cap.domain === vmType);

                            const onChangeProfile = (name, value) => {
                              if (value) {
                                // remove the image template of the first disk
                                // set default disk image size to 20G if no image is set
                                const size = model["disk0_source_size"];
                                changeModel(
                                  Object.assign(
                                    model,
                                    { disk0_source_template: undefined },
                                    !size ? { disk0_source_size: 20 } : {}
                                  )
                                );
                              }
                            };

                            return [
                              <Panel key="general" title={t("General")} headingLevel="h2">
                                {initialModel.name === undefined && (
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
                                {initialModel.vmType === undefined && (
                                  <Select
                                    labelClass="col-md-3"
                                    divClass="col-md-6"
                                    label={t("Hypervisor")}
                                    name="vmType"
                                    required
                                    defaultValue={vmTypes.includes("kvm") ? "kvm" : vmTypes[0]}
                                    options={vmTypes}
                                  />
                                )}
                                {initialModel.osType === undefined && (
                                  <Select
                                    labelClass="col-md-3"
                                    divClass="col-md-6"
                                    label={t("Virtual Machine Type")}
                                    name="osType"
                                    required
                                    defaultValue={osTypes[0]}
                                    options={osTypes.map((item) => ({
                                      value: item,
                                      label: osTypesLabels[item],
                                    }))}
                                  />
                                )}
                                <Text
                                  name="memory"
                                  label={t("Maximum Memory (MiB)")}
                                  required
                                  invalidHint={t("A positive integer is required")}
                                  labelClass="col-md-3"
                                  divClass="col-md-6"
                                  validators={[Validation.isInt({ gt: 0 })]}
                                />
                                <Text
                                  name="vcpu"
                                  label={t("Virtual CPU Count")}
                                  required
                                  invalidHint={t("A positive integer is required")}
                                  labelClass="col-md-3"
                                  divClass="col-md-6"
                                  validators={[Validation.isInt({ gt: 0 })]}
                                />
                                {initialModel.arch === undefined && (
                                  <Select
                                    labelClass="col-md-3"
                                    divClass="col-md-6"
                                    label={t("Architecture")}
                                    name="arch"
                                    required
                                    defaultValue={arch}
                                    options={domainsCaps
                                      .map((cap) => cap.arch)
                                      .filter((item, index, array) => array.indexOf(item) === index)}
                                  />
                                )}
                                <Check name="uefi" label={t("Enable UEFI")} divClass="col-md-6 col-md-offset-3" />
                                {model["uefi"] && (
                                  <>
                                    <Text
                                      name="uefiLoader"
                                      label={t("UEFI firmware path")}
                                      required={model["uefi"] && !props.host.uefiAutoLoader}
                                      labelClass="col-md-3"
                                      divClass="col-md-6"
                                    />
                                    <Text
                                      name="nvramTemplate"
                                      label={t("NVRAM template path")}
                                      required={model["uefi"] && !props.host.uefiAutoLoader}
                                      labelClass="col-md-3"
                                      divClass="col-md-6"
                                    />
                                  </>
                                )}
                                {initialModel.vmType === undefined && props.cobblerProfiles !== {} && (
                                  <>
                                    <Select
                                      labelClass="col-md-3"
                                      divClass="col-md-6"
                                      label={t("Auto-installation Profile")}
                                      name="cobbler_profile"
                                      defaultValue=""
                                      onChange={onChangeProfile}
                                      isClearable
                                      options={Object.keys(props.cobblerProfiles)
                                        .sort((k1, k2) =>
                                          props.cobblerProfiles[k1].localeCompare(props.cobblerProfiles[k2])
                                        )
                                        .map((k) => ({
                                          value: k,
                                          label: props.cobblerProfiles[k],
                                        }))}
                                    />
                                    <Text
                                      name="kernel_options"
                                      label={t("Kernel options")}
                                      labelClass="col-md-3"
                                      divClass="col-md-6"
                                      disabled={!model["cobbler_profile"]}
                                    />
                                  </>
                                )}
                                {initialModel.name === undefined && props.host.inCluster && (
                                  <>
                                    {!props.host.raCanStartResources && (
                                      <Messages
                                        items={MessagesUtils.warning(
                                          t(
                                            "Cluster support is disabled since VirtualDomain resource agent " +
                                              "does not support the start_resources parameter."
                                          )
                                        )}
                                      />
                                    )}
                                    <Check
                                      name="in_cluster"
                                      label={t("Define as a cluster resource")}
                                      divClass="col-md-6 col-md-offset-3"
                                      disabled={!props.host.raCanStartResources}
                                    />
                                    <Text
                                      name="cluster_definitions"
                                      label={t("Path to the cluster shared folder for VM definitions")}
                                      labelClass="col-md-3"
                                      divClass="col-md-6"
                                      disabled={!model["in_cluster"] || !props.host.raCanStartResources}
                                      required={model["in_cluster"]}
                                    />
                                  </>
                                )}
                                {initialModel.name === undefined && props.host.templates && (
                                  <Select
                                    labelClass="col-md-3"
                                    divClass="col-md-6"
                                    name="template"
                                    label={t("Template")}
                                    options={props.host.templates}
                                    formatOptionLabel={({ value }) => {
                                      const description = TemplatesMessages[value];
                                      if (description != null) {
                                        return `${value} - ${description}`;
                                      }
                                      return value;
                                    }}
                                  />
                                )}
                              </Panel>,
                              <GuestDisksPanel
                                changeModel={changeModel}
                                pools={pools}
                                caps={caps}
                                poolCaps={poolCaps}
                              />,
                              guestNicsPanel(model, changeModel, networks),
                              <Panel key="graphics" title={t("Graphics")} headingLevel="h2">
                                <Select
                                  labelClass="col-md-3"
                                  divClass="col-md-6"
                                  label={t("Type")}
                                  name="graphicsType"
                                  isClearable
                                  options={[
                                    {
                                      value: "vnc",
                                      label: "VNC",
                                      osTypes: ["hvm", "xen", "xenpvh"],
                                    },
                                    {
                                      value: "spice",
                                      label: "Spice",
                                      osTypes: ["hvm"],
                                    },
                                  ].filter(
                                    (entry) =>
                                      caps !== undefined &&
                                      caps.devices.graphics.type.includes(entry.value) &&
                                      entry.osTypes.includes(model.osType)
                                  )}
                                />
                              </Panel>,
                            ];
                          }}
                        </GuestPropertiesForm>
                      );
                    }
                    return <Loading text={t("Loading...")} withBorders={false} />;
                  }}
                </VirtualizationDomainsCapsApi>
              )}
            </VirtualizationPoolCapsApi>
          )}
        </VirtualizationListRefreshApi>
      )}
    </VirtualizationListRefreshApi>
  );
}
GuestProperties.defaultProps = {
  cobblerProfiles: {},
};
