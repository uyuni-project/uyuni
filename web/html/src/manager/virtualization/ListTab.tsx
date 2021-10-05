import * as React from "react";

import { Messages } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Button } from "components/buttons";
import { LinkButton } from "components/buttons";
import { VirtualizationListRefreshApi } from "./virtualization-list-refresh-api";
import { useVirtNotification } from "./useVirtNotification";
import { ActionConfirm } from "components/dialog/ActionConfirm";
import { ActionStatus } from "components/action/ActionStatus";
import { SimpleActionApi } from "./SimpleActionApi";

import { MessageType } from "components/messages";

type ModalDataType = {
  type: string;
  name: string;
  icon: string;
  bulk: boolean;
  row: boolean;
  canForce?: boolean;
  forceName?: string;
  modalCreator?: (id: string, selection: any[], onClose: () => void) => React.ReactNode;
};

type CreateModalButtonType = (actionType: string, actionData: Array<ModalDataType>, row: any) => React.ReactNode;

type Props = {
  serverId: string;
  saltEntitled: boolean;
  pageSize: number;
  type: string;
  urlType?: string;
  title: string;
  description: string;
  modalsData: Array<ModalDataType>;
  children: (
    createModalButton: CreateModalButtonType,
    onAction: Function
  ) => {
    columns: React.ReactNode;
    actionsProvider: (row: any) => React.ReactNode;
  };
  isActionVisible: (action: ModalDataType) => boolean;
  canCreate: boolean;
  getCreateActionsKeys: (actionResults: any) => Array<string>;
  idName: string;
  panelButtons: React.ReactNode;
  messages?: Array<MessageType>;
};

export function ListTab(props: Props) {
  const [selectedItems, setSelectedItems] = React.useState([]);
  const [selected, setSelected] = React.useState<any>(undefined);
  const [errors, setErrors] = React.useState<Array<string>>([]);
  const [lastRefresh, setLastRefresh] = React.useState(Date.now());
  const [openedModals, setOpenedModals] = React.useState({});

  const refresh = (type: string) => {
    if (type === props.type) {
      setLastRefresh(Date.now());
    }
  };

  const urlType = props.urlType || `${props.type}s`;

  const [actionsResults, setActionsResults] = useVirtNotification(
    errors,
    setErrors,
    props.serverId,
    refresh,
    props.saltEntitled
  );

  const searchData = (datum: any, criteria?: string): boolean => {
    if (criteria) {
      return datum.name.toLowerCase().includes(criteria.toLowerCase());
    }
    return true;
  };

  const actionCallback = (results: any): void => {
    const newActions = Object.keys(results).reduce((actions, id) => {
      const newAction = { [`${props.type}-${id}`]: { id: results[id], status: "Queued" } };
      return Object.assign(actions, newAction);
    }, {});
    setActionsResults(Object.assign({}, actionsResults, newActions));
    setLastRefresh(Date.now());
  };

  const createModalButton = (actionType: string, actionData: Array<ModalDataType>, row: any) => {
    const action = actionData.find((item) => item.type === actionType);
    if (action) {
      return (
        <Button
          className="btn-default btn-sm"
          title={action.name}
          icon={action.icon}
          handler={() => {
            setSelected(row);
            // Mark the corresponding row modal as shown
            setOpenedModals(Object.assign({}, openedModals, { [`${actionType}-modal`]: true }));
          }}
        />
      );
    }
    return <div />;
  };

  const capsType = props.type.replace(/(?:^[a-z])/, (word) => word.toUpperCase());
  const createConfirmModal = (action: ModalDataType, onConfirm: Function) => {
    const defaultModalCreator = (id: string, selection: any[], onClose: () => void) => (
      <ActionConfirm
        id={id}
        key={id}
        type={action.type}
        name={action.name}
        itemName={t(capsType)}
        icon={action.icon}
        selected={selection}
        onConfirm={(type, items, params) =>
          onConfirm(
            type,
            items.map((item) => item[props.idName]),
            params
          )
        }
        canForce={action.canForce || false}
        forceName={action.forceName}
        onClose={() => {
          // Mark the corresponding modal hidden
          setOpenedModals(Object.assign({}, openedModals, { [id]: false }));
          onClose();
        }}
        isOpen={openedModals[id] || false}
      />
    );
    const modalCreator = action.modalCreator || defaultModalCreator;
    return [
      action.row &&
        modalCreator(
          `${action.type}-modal`,
          [selected].filter((item) => item),
          () => setSelected({})
        ),
      action.bulk && modalCreator(`${action.type}-selected-modal`, selectedItems, () => {}),
    ];
  };

  const createSelectedModalButton = (action: any) => {
    return (
      action.bulk && (
        <Button
          key={`${action.type}-selected-button`}
          id={`${action.type}-selected`}
          icon={action.icon}
          className="btn-default"
          text={action.name}
          title={t("{0} selected", action.name)}
          disabled={selectedItems.length === 0}
          handler={() => {
            // Mark the corresponding bulk modal as shown
            setOpenedModals(Object.assign({}, openedModals, { [`${action.type}-selected-modal`]: true }));
          }}
        />
      )
    );
  };

  const getCreationActionMessages = (): Array<MessageType> => {
    return props.getCreateActionsKeys(actionsResults).flatMap((key) => {
      const action = actionsResults[key];
      const messagesMapper = {
        Failed: MessagesUtils.error,
      };
      return (messagesMapper[action.status] || MessagesUtils.info)(
        <p>
          <ActionStatus serverId={props.serverId} actionId={action.id} status={action.status} />
          {action.name}
        </p>
      );
    });
  };

  return (
    <SimpleActionApi urlType={urlType} idName={`${props.idName}s`} hostid={props.serverId} callback={actionCallback}>
      {({ onAction, messages }) => {
        const panelButtons = (
          <>
            <div className="pull-right btn-group">{props.panelButtons}</div>
            <span className="pull-right">&nbsp;&nbsp;</span>
            <div className="pull-right btn-group">
              {props.saltEntitled && props.canCreate && (
                <LinkButton
                  text={t(`Create ${capsType}`)}
                  title={t(`Create ${capsType}`)}
                  className="btn-default"
                  icon="fa-plus"
                  href={`/rhn/manager/systems/details/virtualization/${urlType}/${props.serverId}/new`}
                />
              )}
              {props.modalsData
                .filter((action) => props.isActionVisible(action))
                .map((action) => createSelectedModalButton(action))}
            </div>
          </>
        );

        return (
          <VirtualizationListRefreshApi serverId={props.serverId} lastRefresh={lastRefresh} type={urlType}>
            {({ data, refreshError }) => {
              const { columns, actionsProvider } = props.children(createModalButton, onAction);
              const allMessages = ([] as any[]).concat(
                errors.map((msg) => MessagesUtils.error(msg)[0]),
                getCreationActionMessages(),
                props.messages,
                messages
              );
              return (
                <div>
                  {panelButtons}
                  <h2>{props.title}</h2>
                  <p>{props.description}</p>
                  <Messages items={allMessages} />
                  <Table
                    data={data != null ? Object.keys(data).map((id) => data[id]) : []}
                    emptyText={t(`No virtual ${props.type} to show.`)}
                    identifier={(row) => row[props.idName]}
                    initialSortColumnKey="name"
                    initialItemsPerPage={props.pageSize}
                    selectable
                    selectedItems={selectedItems.map((item) => item[props.idName])}
                    onSelect={(items) => setSelectedItems(data.filter((d) => items.includes(d[props.idName])))}
                    searchField={<SearchField filter={searchData} placeholder={t("Filter by name")} />}
                  >
                    {columns}
                    {props.saltEntitled && (
                      <Column
                        header={t("Action Status")}
                        cell={(row) => {
                          const actionResult = actionsResults[`${props.type}-${row[props.idName]}`];
                          if (actionResult !== undefined) {
                            return (
                              <ActionStatus
                                serverId={props.serverId}
                                actionId={actionResult.id}
                                status={actionResult.status}
                              />
                            );
                          }
                          return "-";
                        }}
                      />
                    )}
                    {actionsProvider != null && (
                      <Column
                        header={t("Actions")}
                        columnClass="text-right"
                        headerClass="text-right"
                        cell={actionsProvider}
                      />
                    )}
                  </Table>

                  {props.modalsData
                    .filter((action) => props.isActionVisible(action))
                    .map((action) => createConfirmModal(action, onAction).map((modal) => modal))}
                </div>
              );
            }}
          </VirtualizationListRefreshApi>
        );
      }}
    </SimpleActionApi>
  );
}

ListTab.defaultProps = {
  isActionVisible: () => true,
  canCreate: true,
  saltEntitled: true,
  getCreateActionsKeys: (actions) => Object.keys(actions).filter((key) => key.startsWith("new-")),
  modalsData: [],
  panelButtons: [],
  messages: [],
};
