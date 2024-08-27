import * as React from "react";

import { ActionChain } from "components/action-schedule";
import { TopPanel } from "components/panels/TopPanel";
import { Loading } from "components/utils/loading/Loading";

import { SimpleActionApi } from "../../SimpleActionApi";
import { NetworkProperties } from "../network-properties";
import { VirtualizationNetworkDefinitionApi } from "../virtualization-network-definition-api";

type Props = {
  serverId: string;
  netName: string;
  localTime: string;
  timezone: string;
  actionChains: Array<ActionChain>;
};

export function NetsEdit(props: Props) {
  return (
    <SimpleActionApi
      urlType="nets"
      idName="names"
      hostid={props.serverId}
      bounce={`/rhn/manager/systems/details/virtualization/nets/${props.serverId}`}
    >
      {({ onAction, messages: actionMessages }) => (
        <VirtualizationNetworkDefinitionApi hostid={props.serverId} networkName={props.netName}>
          {({ definition, messages: definitionMessages }) => {
            if (definition == null) {
              return <Loading />;
            }

            const onSubmit = (model: any) => onAction("edit", [model.definition.name], model);

            return (
              <TopPanel title={props.netName}>
                <NetworkProperties
                  serverId={props.serverId}
                  submitText={t("Update")}
                  submit={onSubmit}
                  initialModel={definition}
                  messages={actionMessages.concat(definitionMessages)}
                  timezone={props.timezone}
                  localTime={props.localTime}
                  actionChains={props.actionChains}
                />
              </TopPanel>
            );
          }}
        </VirtualizationNetworkDefinitionApi>
      )}
    </SimpleActionApi>
  );
}
