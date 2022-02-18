import * as React from "react";

import { ActionChain } from "components/action-schedule";
import { TopPanel } from "components/panels/TopPanel";

import { SimpleActionApi } from "../../SimpleActionApi";
import { NetworkProperties } from "../network-properties";

type Props = {
  serverId: any;
  localTime: string;
  timezone: string;
  actionChains: Array<ActionChain>;
};

export function NetsCreate(props: Props) {
  return (
    <SimpleActionApi
      urlType="nets"
      idName="names"
      hostid={props.serverId}
      bounce={`/rhn/manager/systems/details/virtualization/nets/${props.serverId}`}
    >
      {({ onAction, messages }) => {
        const onSubmit = (model: any) => onAction("create", [model.definition.name], model);

        return (
          <TopPanel title={t("Create Virtual Network")}>
            <NetworkProperties
              serverId={props.serverId}
              submitText={t("Create")}
              submit={onSubmit}
              initialModel={{}}
              messages={messages}
              timezone={props.timezone}
              localTime={props.localTime}
              actionChains={props.actionChains}
            />
          </TopPanel>
        );
      }}
    </SimpleActionApi>
  );
}
