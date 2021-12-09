import * as React from "react";
import { Tree } from "components/tree/tree";
import { TreeItem, TreeData } from "components/tree/tree";
import { CustomDiv } from "components/custom-objects";
import { ProgressBar } from "components/progressbar";
import { CustomDataHandler } from "components/table/CustomDataHandler";
import { SearchField } from "components/table/SearchField";
import { LinkButton } from "components/buttons";
import { Button } from "components/buttons";
import { AsyncButton } from "components/buttons";
import { Messages } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import { ActionStatus } from "components/action/ActionStatus";
import { ActionConfirm } from "components/dialog/ActionConfirm";
import { VirtualizationListRefreshApi } from "../../virtualization-list-refresh-api";
import { VirtualizationPoolsActionApi } from "../virtualization-pools-action-api";
import { useVirtNotification } from "../../useVirtNotification";
import { HypervisorCheck } from "../../HypervisorCheck";

import { MessageType } from "components/messages";
import { cloneReactElement } from "components/utils";

type Props = {
  serverId: string;
  pageSize: number;
  hypervisor: string;
};

function poolsInfoToTree(pools: any) {
  if (pools == null) {
    return null;
  }

  const children = pools.reduce((result, pool) => {
    const volumes = pool.volumes.map((volume, idx) => ({
      id: `${pool.uuid}/${idx}`,
      data: Object.assign({}, volume, { itemType: "volume", pool: pool.name }),
      children: [],
    }));

    const poolItem = {
      id: pool.uuid,
      data: Object.assign({}, pool, { itemType: "pool" }),
      children: volumes.map((volume) => volume.id),
    };
    return result.concat([poolItem], volumes);
  }, []);

  const rootNode = {
    id: "root",
    children: children.filter((child) => child.data.itemType === "pool").map((child) => child.id),
  };

  return {
    rootId: "root",
    items: [rootNode].concat(children),
  };
}

function getPoolsAndVolumes(tree: TreeData | null | undefined): Array<TreeItem> {
  const items = tree != null ? tree.items : [];
  return items.filter((item) => item.data && (item.data.itemType === "pool" || item.data.itemType === "volume"));
}

function searchData(datum, criteria): boolean {
  if (criteria && datum.data.name) {
    return datum.data.name.toLowerCase().includes(criteria.toLowerCase());
  }
  return true;
}

type FilteredTreeProps = {
  tree: TreeData;
  data?: Array<TreeItem>;
  children: React.ReactNode;
};

function FilteredTree(props: FilteredTreeProps) {
  const ids = (props.data || []).map((item) => item.id).concat(["root"]);

  const filteredItems = props.tree.items.filter((item) => ids.includes(item.id));

  // Extract the volumes of the filtered pools
  const filteredPools = filteredItems.filter((item) => item.data && item.data.itemType === "pool");
  const poolVolumesIds = filteredPools.reduce((acc, item) => acc.concat(item.children), [] as any[]);

  // Extract the pools containing the filtered volumes
  const filteredVolumes = filteredItems.filter((item) => item.data && item.data.itemType === "volume");
  const volumePoolIds = filteredVolumes
    .map((vol) => props.tree.items.filter((item) => (item.children || []).includes(vol.id)))
    .reduce((acc, item) => acc.concat(item), [])
    .map((item) => item.id);

  const allIds = ids.concat(poolVolumesIds, volumePoolIds);

  const filteredTreeData = {
    rootId: "root",
    items: props.tree.items.filter((item) => allIds.includes(item.id)),
  };

  return (
    <>{React.Children.toArray(props.children).map((child) => cloneReactElement(child, { data: filteredTreeData }))}</>
  );
}

const DeleteActionConfirm = (props) => {
  const [purge, setPurge] = React.useState(false);

  const isPool = props.selected.every((item) => item.itemType === "pool");
  return (
    <ActionConfirm
      id={props.id}
      key={props.id}
      type="delete"
      name={t("Delete")}
      itemName={isPool ? t("Virtual Storage Pool") : t("Virtual Storage Volume")}
      icon="fa-trash"
      selected={props.selected}
      onConfirm={(type, items, parameters) => {
        if (isPool) {
          props.onAction(
            type,
            items.map((pool) => pool.name),
            Object.assign({}, parameters, { purge })
          );
        } else {
          const volumes = items.reduce((res, volume) => {
            const pool = volume.pool;
            return Object.assign({}, res, { [pool]: (res[pool] || []).concat([volume.name]) });
          }, {});
          props.onAction(type, undefined, {}, volumes);
        }
      }}
      canForce={false}
      onClose={props.onClose}
      isOpen={props.isOpen}
    >
      {isPool && (
        <p>
          <input type="checkbox" id="purge" checked={purge} onChange={(event) => setPurge(event.target.checked)} />
          <label htmlFor="purge">
            {t("Delete the pool, including the contained volumes. ")}
            <strong>{t("Cannot be undone")}</strong>
          </label>
        </p>
      )}
    </ActionConfirm>
  );
};

export function PoolsList(props: Props) {
  const [selected, setSelected] = React.useState({});
  const [errors, setErrors] = React.useState([]);
  const [lastRefresh, setLastRefresh] = React.useState(Date.now());
  const [deleteModalOpen, setDeleteModalOpen] = React.useState(false);

  const refresh = (type: string) => {
    if (type === "pool") {
      setLastRefresh(Date.now());
    }
  };

  const [actionsResults, setActionsResults] = useVirtNotification(errors, setErrors, props.serverId, refresh);

  const actionCallback = (results: any) => {
    const newActions = Object.keys(results).reduce((actions, poolName) => {
      const newAction = { [`pool-${poolName}`]: { id: results[poolName], status: "Queued" } };
      return Object.assign(actions, newAction);
    }, {});
    setActionsResults(Object.assign({}, actionsResults, newActions));
  };

  function renderActionStatus(type: string, name: string) {
    const actionResult = actionsResults[`${type}-${name}`];
    if (actionResult !== undefined) {
      return <ActionStatus serverId={props.serverId} actionId={actionResult.id} status={actionResult.status} />;
    }
    return null;
  }

  function getCreationActionMessages(): Array<MessageType> {
    return Object.keys(actionsResults)
      .filter((key) => key.startsWith("new-") && actionsResults[key].type === "virt.pool_create")
      .flatMap((key) => {
        const action = actionsResults[key];
        return MessagesUtils.info(
          <p>
            {renderActionStatus("new", action.id)}
            {action.name}
          </p>
        );
      });
  }

  return (
    <VirtualizationPoolsActionApi hostid={props.serverId} callback={actionCallback}>
      {({ onAction, messages }) => {
        return (
          <VirtualizationListRefreshApi serverId={props.serverId} lastRefresh={lastRefresh} type="pools">
            {({ data: pools, error: refreshError }) => {
              function renderPool(pool: any, renderNameColumn: Function) {
                return [
                  <CustomDiv key="name" className="col" width="25" um="%">
                    {renderNameColumn(pool.name)}
                  </CustomDiv>,
                  <CustomDiv key="state" className="col text-center" width="5" um="%">
                    {pool.state}
                  </CustomDiv>,
                  <CustomDiv key="autostart" className="col text-center" width="5" um="%">
                    {pool.autostart && (
                      <i className="fa fa-check-square fa-1-5x" title={t(`${pool.name} is started automatically`)} />
                    )}
                  </CustomDiv>,
                  <CustomDiv key="persistent" className="col text-center" width="5" um="%">
                    {pool.persistent && (
                      <i className="fa fa-check-square fa-1-5x" title={t(`${pool.name} is persistent`)} />
                    )}
                  </CustomDiv>,
                  <CustomDiv key="target" className="col" width="30" um="%">
                    {pool.targetPath}
                  </CustomDiv>,
                  <CustomDiv key="usage" className="col" width="10" um="%">
                    {pool.capacity !== 0 && (
                      <ProgressBar
                        progress={Math.round((100 * pool.allocation) / pool.capacity)}
                        title={t(
                          `${pool.allocation / (1024 * 1024)} GiB / ${pool.capacity / (1024 * 1024)} GiB in use`
                        )}
                      />
                    )}
                    {pool.capacity === 0 && <span>{t("Unknown")}</span>}
                  </CustomDiv>,
                  <CustomDiv key="actionStatus" className="col" width="5" um="%">
                    {renderActionStatus("pool", pool.name)}
                  </CustomDiv>,
                  <CustomDiv key="actions" className="col text-right" width="10" um="%">
                    <div className="btn-group">
                      {pool.state === "running" && (
                        <AsyncButton
                          defaultType="btn-default btn-sm"
                          title={t("Refresh")}
                          icon="fa-refresh"
                          action={() => onAction("refresh", [pool.name], {})}
                        />
                      )}
                      {pool.state !== "running" && (
                        <AsyncButton
                          defaultType="btn-default btn-sm"
                          title={t("Start")}
                          icon="fa-play"
                          action={() => onAction("start", [pool.name], {})}
                        />
                      )}
                      {pool.state === "running" && (
                        <AsyncButton
                          defaultType="btn-default btn-sm"
                          title={t("Stop")}
                          icon="fa-stop"
                          action={() => onAction("stop", [pool.name], {})}
                        />
                      )}
                      <LinkButton
                        title={t("Edit Pool")}
                        className="btn-default btn-sm"
                        icon="fa-edit"
                        href={`/rhn/manager/systems/details/virtualization/storage/${props.serverId}/edit/${pool.name}`}
                      />
                      <Button
                        className="btn-default btn-sm"
                        title={t("Delete")}
                        icon="fa-trash"
                        handler={() => {
                          setSelected(pool);
                          setDeleteModalOpen(true);
                        }}
                      />
                    </div>
                  </CustomDiv>,
                ];
              }

              function renderVolume(volume: any, renderNameColumn: Function): React.ReactNode {
                return [
                  <CustomDiv key="name" className="col" width="calc((100% + 3em) * 0.25 - 3em)" um="">
                    {renderNameColumn(volume.name)}
                  </CustomDiv>,
                  <CustomDiv key="usedBy" className="col" width="calc((100% + 3em) * 0.45)" um="">
                    {volume.usedBy != null ? volume.usedBy.join(", ") : ""}
                  </CustomDiv>,
                  <CustomDiv key="usage" className="col" width="calc((100% + 3em) * 0.1)" um="">
                    {volume.capacity !== 0 && (
                      <ProgressBar
                        progress={Math.min(100, Math.round((100 * volume.allocation) / volume.capacity))}
                        title={t(
                          `${volume.allocation / (1024 * 1024)} GiB / ${volume.capacity / (1024 * 1024)} GiB in use`
                        )}
                      />
                    )}
                    {volume.capacity === 0 && <span>{t("Unknown")}</span>}
                  </CustomDiv>,
                  <CustomDiv key="actionStatus" className="col" width="calc(100% + 3em) * 0.05" um="">
                    {renderActionStatus("volume", `${volume.pool}/${volume.name}`)}
                  </CustomDiv>,
                  <CustomDiv key="actions" className="col text-right" width="calc((100% + 3em) * 0.1)" um="">
                    <div className="btn-group">
                      <Button
                        className="btn-default btn-sm"
                        title={t("Delete")}
                        icon="fa-trash"
                        handler={() => {
                          setSelected(volume);
                          setDeleteModalOpen(true);
                        }}
                      />
                    </div>
                  </CustomDiv>,
                ];
              }

              function renderItem(item: TreeItem, renderNameColumn: Function): React.ReactNode {
                const itemRenderers = {
                  pool: renderPool,
                  volume: renderVolume,
                };
                if (item.data != null) {
                  const itemData = item.data;
                  return itemRenderers[itemData.itemType](itemData, renderNameColumn);
                } else {
                  return null;
                }
              }

              const tree = poolsInfoToTree(pools);
              const header = [
                <CustomDiv key="name" className="col" width="25" um="%">
                  {t("Name")}
                </CustomDiv>,
                <CustomDiv key="state" className="col text-center" width="5" um="%">
                  {t("State")}
                </CustomDiv>,
                <CustomDiv key="autostart" className="col text-center" width="5" um="%">
                  {t("Autostart")}
                </CustomDiv>,
                <CustomDiv key="persistent" className="col text-center" width="5" um="%">
                  {t("Persistent")}
                </CustomDiv>,
                <CustomDiv key="target" className="col" width="30" um="%">
                  {t("Location")}
                </CustomDiv>,
                <CustomDiv key="usage" className="col" width="10" um="%">
                  {t("Usage")}
                </CustomDiv>,
                <CustomDiv key="actionStatus" className="col" width="5" um="%" />,
                <CustomDiv key="actions" className="col text-right" width="10" um="%">
                  {t("Actions")}
                </CustomDiv>,
              ];

              return (
                <>
                  <HypervisorCheck hypervisor={props.hypervisor} />
                  <div className="pull-right btn-group">
                    <LinkButton
                      text={t("Create Pool")}
                      title={t("Create Pool")}
                      className="btn-default"
                      icon="fa-plus"
                      href={`/rhn/manager/systems/details/virtualization/storage/${props.serverId}/new`}
                    />
                  </div>
                  <h2>{t("Virtual Storage Pools and Volumes")}</h2>
                  <p>{t("This is the list of storage pools defined on this host containing virtual guests disks.")}</p>
                  <Messages
                    items={([] as MessageType[]).concat(messages, refreshError || [], getCreationActionMessages())}
                  />
                  <CustomDataHandler
                    data={getPoolsAndVolumes(tree)}
                    identifier={(raw) => raw.id}
                    initialItemsPerPage={Number(props.pageSize)}
                    loading={tree == null}
                    additionalFilters={[]}
                    searchField={
                      <SearchField
                        filter={searchData}
                        placeholder={t("Filter by pool or volume name")}
                        name="pool-name-filter"
                      />
                    }
                  >
                    {tree != null && (
                      <FilteredTree tree={tree}>
                        <Tree header={header} renderItem={renderItem} />
                      </FilteredTree>
                    )}
                  </CustomDataHandler>
                  <DeleteActionConfirm
                    id="delete-modal"
                    selected={[selected].filter((item) => item)}
                    isOpen={deleteModalOpen}
                    onClose={() => {
                      setDeleteModalOpen(false);
                      setSelected({});
                    }}
                    onAction={onAction}
                  />
                </>
              );
            }}
          </VirtualizationListRefreshApi>
        );
      }}
    </VirtualizationPoolsActionApi>
  );
}
