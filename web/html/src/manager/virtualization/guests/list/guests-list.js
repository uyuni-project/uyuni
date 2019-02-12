// @flow
/* global module */

const { hot } = require('react-hot-loader');
const React = require('react');
const MessageContainer = require('components/messages').Messages;
const MessagesUtils = require('components/messages').Utils;
const { Table } = require('components/table');
const { Column } = require('components/table');
const { SearchField } = require('components/table');
const Functions = require('utils/functions');
const { LinkButton, AsyncButton } = require('components/buttons');
const { DangerDialog } = require('components/dialog/DangerDialog');
const { ModalButton } = require('components/dialog/ModalButton');
const Systems = require('components/systems');
const { VirtualizationGuestActionApi } = require('../virtualization-guest-action-api');
const { VirtualizationGuestsListRefreshApi } = require('../virtualization-guests-list-refresh-api');
const { Utils: GuestsListUtils } = require('./guests-list.utils');

const { Utils } = Functions;

declare function t(msg: string, ...args: Array<any>): string;
declare var userPrefPageSize: number;

type Props = {
  serverId: string,
  refreshInterval: number,
  saltEntitled: boolean,
  isAdmin: boolean,
};

type State = {
  actionsResults: Object,
  errors: Array<string>,
  selectedItems: Array<Object>,
  selected?: Object,
  pageUnloading: boolean,
  websocketErr: boolean,
};

class GuestsList extends React.Component<Props, State> {
  static searchData(datum: Object, criteria: string) {
    if (criteria) {
      return datum.name.toLowerCase().includes(criteria.toLowerCase());
    }
    return true;
  }

  websocket = { };

  constructor(props: Props) {
    super(props);

    this.state = {
      actionsResults: {},
      errors: [],
      selectedItems: [],
      selected: undefined,
      pageUnloading: false,
      websocketErr: false,
    };
  }

  componentDidMount() {
    if (this.props.saltEntitled) {
      const { port } = window.location;
      const url = `wss://${window.location.hostname}${port ? `:${port}` : ''}/rhn/websocket/minion/virt-notifications`;
      const ws = new WebSocket(url);

      ws.onopen = () => {
        // Tell the websocket that we want to hear from all action results on this virtual host.
        ws.send(this.props.serverId);
      };

      ws.onclose = () => {
        this.setState(prevState => ({
          errors: (prevState.errors || []).concat(
            !prevState.pageUnloading && !prevState.websocketErr
              ? t('Websocket connection closed. Refresh the page to try again.')
              : [],
          ),
        }));
      };

      ws.onerror = () => {
        this.setState({
          errors: [t('Error connecting to server. Refresh the page to try again.')],
          websocketErr: true,
        });
      };

      ws.onmessage = (e) => {
        if (typeof e.data === 'string') {
          const newActions = JSON.parse(e.data);
          this.setState(prevState => ({ actionsResults: Object.assign({}, prevState.actionsResults, newActions) }));
        }
      };

      window.addEventListener('beforeunload', this.onBeforeUnload);

      this.websocket = ws;
    }
  }

  componentWillUnmount() {
    window.removeEventListener('beforeunload', this.onBeforeUnload);
    if (this.websocket !== undefined) {
      this.websocket.close();
    }
  }

  onBeforeUnload = () => {
    this.setState({
      pageUnloading: true,
    });
  }

  handleSelectItems = (items: Array<Object>) => {
    this.setState({
      selectedItems: items,
    });
  }

  selectGuest = (row: Object) => {
    this.setState({
      selected: row,
    });
  }

  actionCallback = (results: Object) => {
    const newActions = Object.keys(results).reduce((actions, uuid) => {
      const newAction = { [uuid]: { id: results[uuid], status: 'Queued' } };
      return Object.assign(actions, newAction);
    }, {});
    this.setState(prevState => ({ actionsResults: Object.assign({}, prevState.actionsResults, newActions) }));
  }

  createModalButton = (actionType: string, actionData: Array<Object>, row: Object) => {
    const action = actionData.find(item => item.type === actionType);
    if (action) {
      return (
        <ModalButton
          className="btn-default btn-sm"
          title={action.name}
          icon={action.icon}
          target={`${actionType}-modal`}
          item={row}
          onClick={this.selectGuest}
        />
      );
    }
    return <div />;
  }

  createConfirmModal(action: Object, fn: Function) {
    return ([
      !action.bulkonly
      && (
        <DangerDialog
          key={`${action.type}-modal`}
          id={`${action.type}-modal`}
          title={t(`${action.name} Guest`)}
          content={(
            <span>
              {t(`Are you sure you want to ${action.name.toLowerCase()} guest `)}
              <strong>{this.state.selected ? this.state.selected.name : ''}</strong>
              ?
            </span>
          )}
          item={this.state.selected}
          onConfirm={item => fn(action.type, [item.uuid], {})}
          onClosePopUp={() => this.selectGuest({})}
          submitText={action.name}
          submitIcon={action.icon}
        />
      ), (
        <DangerDialog
          key={`${action.type}-selected-modal`}
          id={`${action.type}-selected-modal`}
          title={t(`${action.name} Selected Guest(s)`)}
          content={(
            <span>
              {this.state.selectedItems.length === 1
                ? t('Are you sure you want to {0} the selected guest?', action.name.toLowerCase())
                : t('Are you sure you want to {0} the selected guests? ({1} guests selected)',
                  action.name.toLowerCase(), this.state.selectedItems.length)}
            </span>
          )}
          onConfirm={() => fn(action.type, this.state.selectedItems, {})}
          submitText={action.name}
          submitIcon={action.icon}
        />
      ),
    ]);
  }

  createSelectedModalButton(action: Object) {
    return (
      <ModalButton
        key={`${action.type}-selected-button`}
        id={`${action.type}-selected`}
        icon={action.icon}
        className="btn-default"
        text={action.name}
        title={t('{0} selected', action.name)}
        target={`${action.type}-selected-modal`}
        disabled={this.state.selectedItems.length === 0}
      />
    );
  }

  render() {
    return (
      <VirtualizationGuestActionApi
        hostid={this.props.serverId}
        callback={this.actionCallback}
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
                type: 'shutdown', name: t('Stop'), icon: 'fa-stop', bulkonly: false,
              },
              {
                type: 'restart', name: t('Restart'), icon: 'fa-refresh', bulkonly: false,
              },
              {
                type: 'suspend', name: t('Suspend'), icon: 'fa-pause', bulkonly: false,
              },
              {
                type: 'delete', name: t('Delete'), icon: 'fa-trash', bulkonly: false,
              },
            ];
            const panelButtons = (
              <div className="pull-right btn-group">
                {this.props.saltEntitled
                  && (
                  <LinkButton
                    text={t('Create Guest')}
                    title={t('Create Guest')}
                    className="btn-default"
                    icon="fa-plus"
                    href={`/rhn/manager/systems/details/virtualization/guests/${this.props.serverId}/new`}
                  />)
                }
                {modalsData
                  .filter(action => action.type !== 'delete' || this.props.saltEntitled)
                  .map(action => this.createSelectedModalButton(action))}
              </div>);

            return (
              <VirtualizationGuestsListRefreshApi
                serverId={this.props.serverId}
                refreshInterval={this.props.refreshInterval}
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
                      <MessageContainer items={this.state.errors.map(msg => MessagesUtils.error(msg))} />
                      { refreshError && <MessageContainer items={refreshError} /> }
                      { messages && <MessageContainer items={messages} /> }
                      <Table
                        data={Object.keys(guests).map(id => guests[id])}
                        emptyText={t('No virtual guest to show.')}
                        identifier={row => row.uuid}
                        initialSortColumnKey="name"
                        initialItemsPerPage={userPrefPageSize}
                        selectable
                        selectedItems={this.state.selectedItems}
                        onSelect={this.handleSelectItems}
                        searchField={(
                          <SearchField
                            filter={GuestsList.searchData}
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
                            return Systems.statusDisplay(row, this.props.isAdmin);
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
                        {this.props.saltEntitled && (
                        <Column
                          header={t('Action Status')}
                          cell={(row) => {
                            const actionResult = this.state.actionsResults[row.uuid];
                            if (actionResult !== undefined) {
                              const icons = {
                                Queued: 'fa-clock-o text-info',
                                Failed: 'fa-times-circle-o text-danger',
                                Completed: 'fa-check-circle text-success',
                              };
                              return (
                                <a href={`/rhn/systems/details/history/Event.do?sid=${this.props.serverId}&aid=${actionResult.id}`}>
                                  <i
                                    className={`fa ${icons[actionResult.status]} fa-1-5x`}
                                    title={actionResult.status}
                                  />
                                </a>);
                            }
                            return '-';
                          }}
                        />)}
                        <Column
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
                                 && this.createModalButton('suspend', modalsData, row) }
                                {state !== 'stopped' && row.name !== 'Domain-0'
                                 && this.createModalButton('shutdown', modalsData, row) }
                                {(state === 'paused' || state === 'running') && this.createModalButton('restart', modalsData, row) }
                                <LinkButton
                                  title={t('Edit')}
                                  className="btn-default btn-sm"
                                  icon="fa-edit"
                                  href={`/rhn/manager/systems/details/virtualization/guests/${this.props.serverId}/edit/${row.uuid}`}
                                />
                                { this.props.saltEntitled && row.name !== 'Domain-0'
                                  && this.createModalButton('delete', modalsData, row) }
                              </div>
                            );
                          }}
                        />
                      </Table>

                      {modalsData
                        .filter(action => action.type !== 'delete' || this.props.saltEntitled)
                        .map(action => this.createConfirmModal(action, onAction).map(modal => modal))}
                    </div>
                  )
                }
              </VirtualizationGuestsListRefreshApi>
            );
          }
        }
      </VirtualizationGuestActionApi>);
  }
}

module.exports = {
  GuestsList: hot(module)(GuestsList),
};
