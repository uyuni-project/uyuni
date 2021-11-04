import { hot } from "react-hot-loader/root";
import * as React from "react";
import _isEqual from "lodash/isEqual";
import { TopPanel } from "components/panels/TopPanel";
import { Loading } from "components/utils/Loading";
import { getOrderedItemsFromModel } from "components/input/FormMultiInput";
import { ActionChain } from "components/action-schedule";
import { GuestProperties } from "../GuestProperties";
import * as GuestNicsPanel from "../properties/guest-nics-panel";
import * as DiskUtils from "../properties/disk-utils";
import { SimpleActionApi } from "../../SimpleActionApi";
import { VirtualizationGuestDefinitionApi } from "../virtualization-guest-definition-api";

type Props = {
  host: any;
  guestUuid: string;
  localTime: string;
  timezone: string;
  actionChains: Array<ActionChain>;
};

class GuestsEdit extends React.Component<Props> {
  static getModelFromDefinition(definition: any) {
    return Object.assign(
      {
        name: definition.name,
        memory: definition.maxMemory / 1024,
        vcpu: definition.vcpu.max,
        graphicsType: definition.graphics ? definition.graphics.type : "",
        osType: definition.os.type,
        arch: definition.os.arch,
        vmType: definition.type,
        uefi: definition.os.uefiLoader != null,
        uefiLoader: definition.os.uefiLoader,
        nvramTemplate: definition.os.nvramTemplate,
      },
      GuestNicsPanel.getModelFromDefinition(definition),
      DiskUtils.getModelFromDefinition(definition)
    );
  }

  static getRequestParameterFromModel(model: any, initialModel: any) {
    // Diff the model with the initial one to avoid changing nics if user hasn't touched them.
    const initialNicProps = Object.entries(initialModel).filter((entry) => entry[0].startsWith("network"));
    const newNicProps = Object.entries(model).filter((entry) => entry[0].startsWith("network"));
    const nics = !_isEqual(initialNicProps, newNicProps)
      ? getOrderedItemsFromModel(model, "network").map((index) => GuestNicsPanel.getRequestParams(model, index))
      : [];

    const nicsParams = nics.length !== 0 ? { interfaces: nics } : undefined;

    // Diff the model with the initial one to avoid changing disks if user hasn't touched them.
    const initialDiskProps = Object.entries(initialModel).filter((entry) => entry[0].startsWith("disk"));
    const newDiskProps = Object.entries(model).filter((entry) => entry[0].startsWith("disk"));
    const disks = !_isEqual(initialDiskProps, newDiskProps)
      ? getOrderedItemsFromModel(model, "disk").map((index) => DiskUtils.getRequestParams(model, index))
      : [];

    const disksParams = disks.length !== 0 ? { disks } : undefined;

    return Object.assign(
      Object.entries(model).reduce(
        (res, entry) =>
          Object.assign(
            res,
            !entry[0].startsWith("disk") && !entry[0].startsWith("network") ? { [entry[0]]: entry[1] } : undefined
          ),
        {}
      ),
      {
        memory: model.memory,
        earliest: model.earliest,
      },
      nicsParams,
      disksParams
    );
  }

  render() {
    return (
      <VirtualizationGuestDefinitionApi hostid={this.props.host.id} guestUuid={this.props.guestUuid}>
        {({ definition, messages: definitionMessages }) => (
          <SimpleActionApi
            urlType="guests"
            idName="uuids"
            hostid={this.props.host.id}
            bounce={`/rhn/manager/systems/details/virtualization/guests/${this.props.host.id}`}
          >
            {({ onAction, messages: actionMessages }) => {
              if (definition == null) {
                return <Loading text={t("Loading...")} withBorders={false} />;
              }

              const initialModel = GuestsEdit.getModelFromDefinition(definition);
              const onSubmit = (properties) =>
                onAction(
                  "update",
                  [this.props.guestUuid],
                  GuestsEdit.getRequestParameterFromModel(properties, initialModel)
                );
              const messages = [].concat(definitionMessages, actionMessages).filter((item) => item);
              const guestName = definition !== null ? definition.name : "";
              return (
                <TopPanel title={guestName} icon="fa spacewalk-icon-virtual-guest">
                  <GuestProperties
                    host={this.props.host}
                    submitText={t("Update")}
                    submit={onSubmit}
                    messages={messages}
                    initialModel={initialModel}
                    localTime={this.props.localTime}
                    timezone={this.props.timezone}
                    actionChains={this.props.actionChains}
                  />
                </TopPanel>
              );
            }}
          </SimpleActionApi>
        )}
      </VirtualizationGuestDefinitionApi>
    );
  }
}

const HotGuestsEdit = hot(GuestsEdit);

export { HotGuestsEdit as GuestsEdit };
