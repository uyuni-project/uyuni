// @flow
import * as React from 'react';

import { Messages } from 'components/messages';
import { Utils as MessagesUtils } from 'components/messages';
import { Table } from 'components/table/Table';
import { Column } from 'components/table/Column';
import { SearchField } from 'components/table/SearchField';
import { Utils } from 'utils/functions';
import { LinkButton, AsyncButton } from 'components/buttons';
import { ModalButton } from 'components/dialog/ModalButton';
import * as Systems from 'components/systems';
import { VirtualizationGuestActionApi } from '../virtualization-guest-action-api';
import { VirtualizationGuestsListRefreshApi } from '../virtualization-guests-list-refresh-api';
import { Utils as GuestsListUtils } from './guests-list.utils';
import { ActionConfirm } from 'components/dialog/ActionConfirm';
import { ActionStatus } from 'components/action/ActionStatus';

declare var userPrefPageSize: number;

type Props = {
  serverId: string,
  refreshInterval: number,
  saltEntitled: boolean,
  foreignEntitled: boolean,
  isAdmin: boolean,
};

export function GuestsList(props: Props) {
  const [selectedItems, setSelectedItems] = React.useState([]);
  const [selected, setSelected] = React.useState(undefined);
  const [actionsResults, setActionsResults] = React.useState({});
  const [errors, setErrors] = React.useState([]);
  const [webSocketErr, setWebSocketErr] = React.useState(false);
  const [pageUnloading, setPageUnloading] = React.useState(false);

  let websocket = { };

  const onBeforeUnload = () => {
    setPageUnloading(true);
  }

  React.useEffect(() => {
    if (props.saltEntitled) {
      const { port } = window.location;
      const url = `wss://${window.location.hostname}${port ? `:${port}` : ''}/rhn/websocket/minion/virt-notifications`;
      const ws = new WebSocket(url);

      ws.onopen = () => {
        // Tell the websocket that we want to hear from all action results on this virtual host.
        ws.send(props.serverId);
      };

      ws.onclose = () => {
        setErrors((errors || []).concat(
            !pageUnloading && !webSocketErr
              ? t('Websocket connection closed. Refresh the page to try again.')
              : []
        ));
      };

      ws.onerror = () => {
        setErrors([t('Error connecting to server. Refresh the page to try again.')]);
        setWebSocketErr(true);
      };

      ws.onmessage = (e) => {
        if (typeof e.data === 'string') {
          const newActions = JSON.parse(e.data);
          /*
           * The received items are split in two maps:
           *   - one with the actions that we don't already know about
           *   - one with existing actions.
           * Since for creation the key in the newActions map will not fit
           * the one we already got before, update the existing actions by matching
           * their IDs.
           */
          const aidMap = Object.keys(actionsResults)
            .reduce((res, key) => Object.assign({}, res, {[actionsResults[key].id]: key}), {});
          const updatedActions = Object.keys(newActions)
            .filter(key => Object.keys(aidMap).includes(String(newActions[key].id)))
            .reduce((res, key) => {
              const newAction = newActions[key];
              const action = actionsResults[newAction.id];
              return Object.assign({},
                res,
                {[aidMap[newAction.id]]: Object.assign({}, action, {'status': newAction.status})});
            }, {});
          const addedActions = Object.keys(newActions)
            .filter(key => !Object.keys(aidMap).includes(String(newActions[key].id)))
            .reduce((res, key) => Object.assign({}, res, {[key]: newActions[key]}), {});
          setActionsResults(Object.assign({}, actionsResults, updatedActions, addedActions));
        }
      };

      window.addEventListener('beforeunload', onBeforeUnload);

      websocket = ws;
    }

    return () => {
      window.removeEventListener('beforeunload', onBeforeUnload);
      if (websocket !== null) {
        websocket.close();
      }
    }
  }, [props.serverId]);

  const searchData = (datum: Object, criteria?: string): boolean => {
    if (criteria) {
      return datum.name.toLowerCase().includes(criteria.toLowerCase());
    }
    return true;
  }

  const actionCallback = (results: Object): void => {
    const newActions = Object.keys(results).reduce((actions, uuid) => {
      const newAction = { [uuid]: { id: results[uuid], status: 'Queued' } };
      return Object.assign(actions, newAction);
    }, {});
    setActionsResults(Object.assign({}, actionsResults, newActions));
  }

  const createModalButton = (actionType: string, actionData: Array<Object>, row: Object): React.Node => {
    const action = actionData.find(item => item.type === actionType);
    if (action) {
      return (
        <ModalButton
          className="btn-default btn-sm"
          title={action.name}
          icon={action.icon}
          target={`${actionType}-modal`}
          item={row}
          onClick={setSelected}
        />
      );
    }
    return <div />;
  }

  const createConfirmModal = (action: Object, fn: Function): Array<React.Node> => {
    return ([
      !action.bulkonly
      && (
        <ActionConfirm
          id={`${action.type}-modal`}
          key={`${action.type}-modal`}
          type={action.type}
          name={action.name}
          itemName={t('Guest')}
          icon={action.icon}
          selected={[selected].filter(item => item)}
          fn={(type, guests, params) => fn(type, guests.map(guest => guest.uuid), params)}
          canForce={action.canForce}
          forceName={action.forceName}
          onClose={() => setSelected({})}
        />
      ), (
        <ActionConfirm
          id={`${action.type}-selected-modal`}
          key={`${action.type}-selected-modal`}
          type={action.type}
          name={action.name}
          itemName={t('Guest')}
          icon={action.icon}
          selected={selectedItems}
          fn={(type, guests, params) => fn(type, guests.map(guest => guest.uuid), params)}
          canForce={action.canForce}
          forceName={action.forceName}
        />
      ),
    ]);
  }

  const createSelectedModalButton = (action: Object): React.Node => {
    return (
      <ModalButton
        key={`${action.type}-selected-button`}
        id={`${action.type}-selected`}
        icon={action.icon}
        className="btn-default"
        text={action.name}
        title={t('{0} selected', action.name)}
        target={`${action.type}-selected-modal`}
        disabled={selectedItems.length === 0}
      />
    );
  }

  const getCreationActionMessages = (): React.Node => {
    return Object.keys(actionsResults)
      .filter(key => key.startsWith("new-") && actionsResults[key].type === "virt.create")
      .flatMap(key => {
        const action = actionsResults[key];
        return MessagesUtils.info(
          <p><ActionStatus serverId={props.serverId} actionId={action.id} status={action.status}/>{action.name}</p>
        );
      });
  }


  return (
    <VirtualizationGuestActionApi
      hostid={props.serverId}
      callback={actionCallback}
    >
      {
        ({
          onAction,
          messages,
        }) => {
          const modalsData = [
            {
              type: 'start', name: t('Start / Resume'), icon: 'fa-play', bulkonly: true,
            },
            {
              type: 'shutdown', name: t('Stop'), icon: 'fa-stop', bulkonly: false, canForce: true, forceName: t('Force off'),
            },
            {
              type: 'restart', name: t('Restart'), icon: 'fa-refresh', bulkonly: false, canForce: true, forceName: t('Reset'),
            },
            {
              type: 'suspend', name: t('Suspend'), icon: 'fa-pause', bulkonly: false,
            },
            {
              type: 'delete', name: t('Delete'), icon: 'fa-trash', bulkonly: false,
            },
          ];
          const isActionVisible = (action) => !props.foreignEntitled && (action.type !== 'delete' || props.saltEntitled);
          const panelButtons = (
            <div className="pull-right btn-group">
              {props.saltEntitled
                && (
                <LinkButton
                  text={t('Create Guest')}
                  title={t('Create Guest')}
                  className="btn-default"
                  icon="fa-plus"
                  href={`/rhn/manager/systems/details/virtualization/guests/${props.serverId}/new`}
                />)
              }
              {modalsData
                .filter(action => isActionVisible(action))
                .map(action => createSelectedModalButton(action))}
            </div>);

          return (
            <VirtualizationGuestsListRefreshApi
              serverId={props.serverId}
              refreshInterval={props.refreshInterval}
            >
              {
                ({
                  guests,
                  refreshError,
                }) => (
                  <div>
                    {panelButtons}
                    <h2>{t('Hosted Virtual Systems')}</h2>
                    <p>{t('This is a list of virtual guests which are configured to run on this host.')}</p>
                    <Messages
                      items={
                        [].concat(
                          errors.map(msg => MessagesUtils.error(msg)),
                          getCreationActionMessages()
                        )
                      }
                    />
                    { refreshError && <Messages items={refreshError} /> }
                    { messages && <Messages items={messages} /> }
                    <Table
                      data={Object.keys(guests).map(id => guests[id])}
                      emptyText={t('No virtual guest to show.')}
                      identifier={row => row.uuid}
                      initialSortColumnKey="name"
                      initialItemsPerPage={userPrefPageSize}
                      selectable
                      selectedItems={selectedItems.map(guest => guest.uuid)}
                      onSelect={items => setSelectedItems(guests.filter(guest => items.includes(guest.uuid)))}
                      searchField={(
                        <SearchField
                          filter={searchData}
                          criteria=""
                          placeholder={t('Filter by name')}
                        />
                      )}
                    >
                      <Column
                        columnKey="name"
                        comparator={Utils.sortByText}
                        header={t('Guest')}
                        cell={row => row.name}
                      />
                      <Column
                        columnKey="serverName"
                        comparator={Utils.sortByText}
                        header={t('System')}
                        cell={(row) => {
                          if (row.virtualSystemId == null) {
                            return t('Unregistered System');
                          }

                          if (row.accessible) {
                            return <a href={`/rhn/systems/details/Overview.do?sid=${row.virtualSystemId}`}>{row.serverName}</a>;
                          }
                          return row.serverName;
                        }}
                      />
                      <Column
                        columnKey="statusType"
                        comparator={GuestsListUtils.sortByUpdate}
                        header={t('Updates')}
                        cell={(row) => {
                          if (row.statusType == null) {
                            return '-';
                          }
                          return Systems.statusDisplay(row, props.isAdmin);
                        }}
                      />
                      <Column
                        columnKey="stateLabel"
                        header={t('State')}
                        comparator={GuestsListUtils.sortByState}
                        cell={row => row.stateName}
                      />
                      <Column
                        columnKey="memory"
                        comparator={Utils.sortByNumber}
                        header={t('Current Memory')}
                        cell={row => `${row.memory / 1024} MiB`}
                      />
                      <Column
                        columnKey="vcpus"
                        comparator={Utils.sortByNumber}
                        header={t('vCPUs')}
                        cell={row => row.vcpus}
                      />
                      <Column
                        columnKey="channelLabels"
                        comparator={Utils.sortByText}
                        header={t('Base Software Channel')}
                        cell={(row) => {
                          if (row.channelId == null) {
                            return t('(none)');
                          }
                          if (row.subscribable) {
                            return <a href={`/rhn/channels/ChannelDetail.do?cid=${row.channelId}`}>{row.channelLabels}</a>;
                          }
                          return row.channelLabels;
                        }}
                      />
                      {props.saltEntitled && (
                      <Column
                        header={t('Action Status')}
                        cell={(row) => {
                          const actionResult = actionsResults[row.uuid];
                          if (actionResult !== undefined) {
                            return (
                              <ActionStatus
                                serverId={props.serverId}
                                actionId={actionResult.id}
                                status={actionResult.status}
                              />
                            );
                          }
                          return '-';
                        }}
                          />)}
                      {!props.foreignEntitled &&
                       (<Column
                        header={t('Actions')}
                        columnClass="text-right"
                        headerClass="text-right"
                        cell={(row) => {
                          const state = row.stateLabel;
                          return (
                            <div className="btn-group">
                              {state !== 'running' && row.name !== 'Domain-0'
                               && (
                                 <AsyncButton
                                   defaultType="btn-default btn-sm"
                                   title={t(state === 'paused' ? 'Resume' : 'Start')}
                                   icon="fa-play"
                                   action={() => onAction('start', [row.uuid], {})}
                                 />) }
                              {state === 'running' && row.name !== 'Domain-0'
                               && createModalButton('suspend', modalsData, row) }
                              {state !== 'stopped' && row.name !== 'Domain-0'
                               && createModalButton('shutdown', modalsData, row) }
                              {(state === 'paused' || state === 'running') && createModalButton('restart', modalsData, row) }
                              {props.saltEntitled && state === 'running' && (
                                <LinkButton
                                  title={t('Graphical Console')}
                                  className="btn-default btn-sm"
                                  icon="fa-desktop"
                                  href={`/rhn/manager/systems/details/virtualization/guests/${props.serverId}/console/${row.uuid}`}
                                  target="_blank"
                                />
                              )}
                              <LinkButton
                                title={t('Edit')}
                                className="btn-default btn-sm"
                                icon="fa-edit"
                                href={`/rhn/manager/systems/details/virtualization/guests/${props.serverId}/edit/${row.uuid}`}
                              />
                              { props.saltEntitled && row.name !== 'Domain-0'
                                && createModalButton('delete', modalsData, row) }
                            </div>
                          );
                        }}
                        />)
                      }
                    </Table>

                    {modalsData
                      .filter(action => isActionVisible(action))
                      .map(action => createConfirmModal(action, onAction).map(modal => modal))}
                  </div>
                )
              }
            </VirtualizationGuestsListRefreshApi>
          );
        }
      }
    </VirtualizationGuestActionApi>
  );
}
