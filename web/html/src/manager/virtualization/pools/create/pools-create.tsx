import * as React from "react";

import { ActionChain } from "components/action-schedule";
import { TopPanel } from "components/panels/TopPanel";

import { PoolProperties } from "../pool-properties";
import { VirtualizationPoolsActionApi } from "../virtualization-pools-action-api";

type Props = {
  serverId: string;
  localTime: string;
  timezone: string;
  actionChains: Array<ActionChain>;
};

export function PoolsCreate(props: Props) {
  return (
    <VirtualizationPoolsActionApi
      hostid={props.serverId}
      bounce={`/rhn/manager/systems/details/virtualization/storage/${props.serverId}`}
    >
      {({ onAction, messages }) => {
        const onSubmit = (model: any) => onAction("create", [model.name], model);

        return (
          <TopPanel title={t("Create Virtual Storage Pool")}>
            <PoolProperties
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
    </VirtualizationPoolsActionApi>
  );
}
