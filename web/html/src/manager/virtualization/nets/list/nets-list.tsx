import * as React from "react";

import { Column } from "components/table/Column";
import { Utils } from "utils/functions";
import { LinkButton, AsyncButton } from "components/buttons";
import { Utils as MessagesUtils } from "components/messages";
import { Utils as ListUtils } from "../../list.utils";
import { ListTab } from "../../ListTab";
import { HypervisorCheck } from "../../HypervisorCheck";

type Props = {
  serverId: string;
  pageSize: number;
  hypervisor: string;
  allow_changing: boolean;
};

export function NetsList(props: Props) {
  const modalsData = [
    {
      type: "start",
      name: t("Start"),
      icon: "fa-play",
      bulk: true,
      row: false,
    },
    {
      type: "stop",
      name: t("Stop"),
      icon: "fa-stop",
      bulk: true,
      row: true,
    },
    {
      type: "delete",
      name: t("Delete"),
      icon: "fa-trash",
      bulk: true,
      row: true,
    },
  ];
  return (
    <>
      <HypervisorCheck hypervisor={props.hypervisor} />
      <ListTab
        serverId={props.serverId}
        pageSize={props.pageSize}
        type="network"
        urlType="nets"
        title={t("Virtual Networks")}
        description={t("This is a list of virtual networks which are configured to run on this host.")}
        modalsData={modalsData}
        idName="name"
        canCreate={props.allow_changing}
        messages={
          props.allow_changing
            ? []
            : MessagesUtils.warning(
                t("The Salt version on this system does not support virtual network creating and editing")
              )
        }
      >
        {(createModalButton, onAction) => {
          const columns = [
            <Column
              key="name"
              columnKey="name"
              comparator={Utils.sortByText}
              header={t("Name")}
              cell={(row) => row.name}
            />,
            <Column
              key="state"
              columnKey="state"
              header={t("State")}
              comparator={ListUtils.sortByState}
              cell={(row) => (row.active ? "running" : "stopped")}
            />,
            <Column
              key="autostart"
              columnKey="autostart"
              header={t("Autostart")}
              cell={(row) =>
                row.autostart && (
                  <i className="fa fa-check-square fa-1-5x" title={t(`${row.name} is started automatically`)} />
                )
              }
            />,
            <Column
              key="persistent"
              columnKey="persistent"
              header={t("Persistent")}
              cell={(row) =>
                row.persistent && <i className="fa fa-check-square fa-1-5x" title={t(`${row.name} is persistent`)} />
              }
            />,
            <Column
              key="bridge"
              columnKey="bridge"
              comparator={Utils.sortByText}
              header={t("Bridge")}
              cell={(row) => row.bridge}
            />,
          ];
          const actionsProvider = (row) => {
            return (
              <div className="btn-group">
                {!row.active && (
                  <AsyncButton
                    defaultType="btn-default btn-sm"
                    title={t("Start")}
                    icon="fa-play"
                    action={() => onAction("start", [row.name], {})}
                  />
                )}
                {row.active && createModalButton("stop", modalsData, row)}
                {props.allow_changing && (
                  <LinkButton
                    title={t("Edit")}
                    className="btn-default btn-sm"
                    icon="fa-edit"
                    href={`/rhn/manager/systems/details/virtualization/nets/${props.serverId}/edit/${row.name}`}
                  />
                )}
                {createModalButton("delete", modalsData, row)}
              </div>
            );
          };
          return { columns, actionsProvider };
        }}
      </ListTab>
    </>
  );
}
