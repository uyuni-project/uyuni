import * as React from "react";

import { Column } from "components/table/Column";
import { Utils } from "utils/functions";
import { LinkButton, AsyncButton } from "components/buttons";
import * as Systems from "components/systems";
import { Utils as ListUtils } from "../../list.utils";
import { ListTab } from "../../ListTab";
import { HypervisorCheck } from "../../HypervisorCheck";
import { ActionApi } from "../../ActionApi";
import { MigrateDialog } from "./MigrateDialog";

export type HostInfo = {
  hypervisor: string;
  cluster_other_nodes: string[];
};

type Props = {
  serverId: string;
  pageSize: number;
  saltEntitled: boolean;
  foreignEntitled: boolean;
  isAdmin: boolean;
  hostInfo?: HostInfo;
};

export function GuestsList(props: Props) {
  const [migrateVm, setMigrateVm] = React.useState(undefined);
  const modalsData = [
    {
      type: "start",
      name: t("Start / Resume"),
      icon: "fa-play",
      bulk: true,
      row: false,
    },
    {
      type: "shutdown",
      name: t("Stop"),
      icon: "fa-stop",
      bulk: true,
      row: true,
      canForce: true,
      forceName: t("Force off"),
    },
    {
      type: "restart",
      name: t("Restart"),
      icon: "fa-refresh",
      bulk: true,
      row: true,
      canForce: true,
      forceName: t("Reset"),
    },
    {
      type: "suspend",
      name: t("Suspend"),
      icon: "fa-pause",
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

  const panelButtons =
    !props.foreignEntitled && props.saltEntitled ? (
      <ActionApi urlTemplate={`/rhn/manager/api/systems/details/virtualization/guests/${props.serverId}/refresh`}>
        {({ onAction, messages }) => {
          return (
            <AsyncButton
              title={t("Synchronize Guests List")}
              text={t("Synchronize")}
              icon="fa-refresh"
              action={() => onAction((url) => url, "", {})}
              defaultType="btn-info"
            />
          );
        }}
      </ActionApi>
    ) : (
      []
    );

  return (
    <>
      <HypervisorCheck
        saltVirtHost={!props.foreignEntitled && props.saltEntitled}
        hypervisor={props.hostInfo?.hypervisor || ""}
      />

      <ListTab
        serverId={props.serverId}
        saltEntitled={props.saltEntitled}
        pageSize={props.pageSize}
        type="guest"
        title={t("Hosted Virtual Systems")}
        description={t("This is a list of virtual guests which are configured to run on this host.")}
        modalsData={modalsData}
        isActionVisible={(action) => !props.foreignEntitled && (action.type !== "delete" || props.saltEntitled)}
        getCreateActionsKeys={(actions) => {
          return Object.keys(actions).filter((key) => key.startsWith("new-") && actions[key].type === "virt.create");
        }}
        idName="uuid"
        panelButtons={panelButtons}
      >
        {(createModalButton, onAction) => {
          const columns = [
            <Column
              key="name"
              columnKey="name"
              comparator={Utils.sortByText}
              header={t("Guest")}
              cell={(row) => row.name}
            />,
            <Column
              key="server"
              columnKey="serverName"
              comparator={Utils.sortByText}
              header={t("System")}
              cell={(row) => {
                if (row.virtualSystemId == null) {
                  return t("Unregistered System");
                }

                if (row.accessible) {
                  return <a href={`/rhn/systems/details/Overview.do?sid=${row.virtualSystemId}`}>{row.serverName}</a>;
                }
                return row.serverName;
              }}
            />,
            <Column
              key="status"
              columnKey="statusType"
              comparator={ListUtils.sortByUpdate}
              header={t("Updates")}
              cell={(row) => {
                if (row.statusType == null) {
                  return "-";
                }
                return Systems.statusDisplay(row, props.isAdmin);
              }}
            />,
            <Column
              key="state"
              columnKey="stateLabel"
              header={t("State")}
              comparator={ListUtils.sortByState}
              cell={(row) => row.stateName}
            />,
            <Column
              key="memory"
              columnKey="memory"
              comparator={Utils.sortByNumber}
              header={t("Current Memory")}
              cell={(row) => `${row.memory} MiB`}
            />,
            <Column
              key="vcpus"
              columnKey="vcpus"
              comparator={Utils.sortByNumber}
              header={t("vCPUs")}
              cell={(row) => row.vcpus}
            />,
            <Column
              key="channel"
              columnKey="channelLabels"
              comparator={Utils.sortByText}
              header={t("Base Software Channel")}
              cell={(row) => {
                if (row.channelId == null) {
                  return t("(none)");
                }
                if (row.subscribable) {
                  return <a href={`/rhn/channels/ChannelDetail.do?cid=${row.channelId}`}>{row.channelLabels}</a>;
                }
                return row.channelLabels;
              }}
            />,
          ];

          const actionsProvider = (row) => {
            if (props.foreignEntitled) {
              return [];
            }
            const state = row.stateLabel;
            const clustered = props.saltEntitled && row.cluster_primitive;
            return (
              <div className="btn-group">
                {state !== "running" && row.name !== "Domain-0" && (
                  <AsyncButton
                    defaultType="btn-default btn-sm"
                    title={t(state === "paused" ? "Resume" : "Start")}
                    icon="fa-play"
                    action={() => onAction("start", [row.uuid], {})}
                  />
                )}
                {state === "running" &&
                  row.name !== "Domain-0" &&
                  !clustered &&
                  createModalButton("suspend", modalsData, row)}
                {state !== "stopped" && row.name !== "Domain-0" && createModalButton("shutdown", modalsData, row)}
                {(state === "paused" || state === "running") && createModalButton("restart", modalsData, row)}
                {props.saltEntitled && ["spice", "vnc"].includes(row.graphics_type) && (
                  <LinkButton
                    title={t("Graphical Console")}
                    className="btn-default btn-sm"
                    icon="fa-desktop"
                    href={`/rhn/manager/systems/details/virtualization/guests/console/${row.uuid}`}
                    target="_blank"
                  />
                )}
                {row.cluster_primitive && state === "running" && (
                  <AsyncButton
                    defaultType="btn-default btn-sm"
                    title={t("Migrate")}
                    icon="fa-share-square-o"
                    action={() => {
                      setMigrateVm(row);
                    }}
                  />
                )}
                <LinkButton
                  title={t("Edit")}
                  className="btn-default btn-sm"
                  icon="fa-edit"
                  href={`/rhn/manager/systems/details/virtualization/guests/${props.serverId}/edit/${row.uuid}`}
                />
                {props.saltEntitled && row.name !== "Domain-0" && createModalButton("delete", modalsData, row)}
              </div>
            );
          };

          return { columns, actionsProvider };
        }}
      </ListTab>
      <ActionApi urlTemplate={`/rhn/manager/api/systems/details/virtualization/guests/${props.serverId}/migrate`}>
        {({ onAction }) => (
          <MigrateDialog
            id="migrate-modal"
            key="migrate-modal"
            vm={migrateVm}
            onConfirm={(vm: any, target: string) => {
              onAction((url) => url, "", {
                uuids: [vm.uuid],
                primitive: vm.cluster_primitive,
                target: target,
              });
            }}
            onClose={() => setMigrateVm(undefined)}
            clusterNodes={props.hostInfo?.cluster_other_nodes}
          />
        )}
      </ActionApi>
    </>
  );
}
