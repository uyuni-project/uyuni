// @flow
const React = require("react");
const PropTypes = React.PropTypes;
const MessageContainer = require("components/messages").Messages;
const MessagesUtils = require("components/messages").Utils;
const {Table, Column, SearchField, Highlight} = require("components/table");
const Functions = require("utils/functions");
const Utils = Functions.Utils;
const {LinkButton, AsyncButton} = require("components/buttons");
const {DeleteDialog} = require("components/dialog/DeleteDialog");
const {DangerDialog} = require("components/dialog/DangerDialog");
const {ModalButton} = require("components/dialog/ModalButton");
const Systems = require("components/systems");
import VirtualizationGuestActionApi from '../virtualization-guest-action-api';
import VirtualizationGuestsListRefreshApi from '../virtualization-guests-list-refresh-api';

class GuestsList extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      actionsResults: {},
      errors: [],
      selectedItems: []
    }

    this.onBeforeUnload = (event) => {
        this.setState({
            pageUnloading: true
        });
    };

    this.handleSelectItems = (items) => {
      this.setState({
        selectedItems: items
      });
    };

    this.selectGuest = (row) => {
      this.setState({
        selected: row
      });
    };

    this.actionCallback = (results) => {
      var newActions = {};
      Object.entries(results).forEach(([uuid, actionid]) => {
        newActions[uuid] = {'id': actionid, 'result': 'Queued'};
      });
      this.setState({actionsResults: Object.assign({}, this.state.actionsResults, newActions)});
    };
  }

  componentDidMount() {
    if (this.props.salt_entitled) {
      const port = window.location.port;
      const url = "wss://" +
        window.location.hostname +
        (port ? ":" + port : "") +
         "/rhn/websocket/minion/virt-notifications";
      const ws = new WebSocket(url);

      ws.onopen = () => {
        // Tell the websocket that we want to hear from all action results on this virtual host.
        ws.send(this.props.server_id);
      }

      ws.onclose = (e) => {
        const errs = this.state.errors ? this.state.errors : [];
        if (!this.state.pageUnloading && !this.state.websocketErr) {
            errs.push(t("Websocket connection closed. Refresh the page to try again."));
        }
        this.setState({
            errors: errs,
        });
      };

      ws.onerror = (e) => {
        console.error("Websocket error: " + e);
        this.setState({
           errors: [t("Error connecting to server. Refresh the page to try again.")],
           websocketErr: true
        });
      };

      ws.onmessage = (e) => {
        const event = JSON.parse(e.data);
        var newActions = {};

        Object.entries(event).forEach(([uuid, action]) => {
          newActions[uuid] = {
            id: action.id,
            result: action.status,
          };
        });

        this.setState({actionsResults: Object.assign({}, this.state.actionsResults, newActions)});
      }

      window.addEventListener("beforeunload", this.onBeforeUnload)

      this.setState({
        websocket: ws
      });
    }
  }

  componentWillUnmount() {
    window.removeEventListener("beforeunload", this.onBeforeUnload)
  }

  createSelectedModalButton(action) {
    return (
      <ModalButton key={action.type + "-selected-button"}
                   id={action.type + "-selected"}
                   icon={action.icon}
                   className="btn-default"
                   text={action.name}
                   title={t("{0} selected", action.name)}
                   target={action.type + "-selected-modal"}
                   disabled={this.state.selectedItems.length === 0}/>
    );
  }

  createConfirmModal(action, fn) {
    return ([
        !action.bulkonly &&
          <DangerDialog
            key={action.type + '-modal'}
            id={action.type + '-modal'}
            title={t(`${action.name} Guest`)}
            content={
              <span>{t(`Are you sure you want to ${action.name.toLowerCase()} guest `)}
                <strong>{this.state.selected ? this.state.selected.name : ''}</strong>?
              </span>
            }
            item={this.state.selected}
            onConfirm={(item) => fn(action.type, [item.uuid], {})}
            onClosePopUp={() => this.selectGuest(undefined)}
            submitText={action.name}
            submitIcon={action.icon}
          />,
        <DangerDialog
          key={action.type + '-selected-modal'}
          id={action.type + "-selected-modal"}
          title={t(`${action.name} Selected Guest(s)`)}
          content={
            <span>
              {this.state.selectedItems.length == 1 ?
                t("Are you sure you want to {0} the selected guest?",
                  action.name.toLowerCase()) :
                t("Are you sure you want to {0} the selected guests? ({1} guests selected)",
                  action.name.toLowerCase(), this.state.selectedItems.length)}
            </span>
          }
          onConfirm={() => fn(action.type, this.state.selectedItems, {})}
          submitText={action.name}
          submitIcon={action.icon}
        />
    ]);
  }

  createModalButton(action_type, actions_data, row) {
    const action = actions_data.find(action => action.type == action_type);
    return (
      <ModalButton
        className="btn-default btn-sm"
        title={action.name}
        icon={action.icon}
        target={action_type + "-modal"}
        item={row}
        onClick={this.selectGuest}
      />
    );
  }

  sortByUpdate(aRaw, bRaw, columnKey, sortDirection) {
    const statusValues = {'critical': 0, 'updates': 1, 'actions scheduled': 2,
                        'updates scheduled': 3, 'up2date': 4, 'kickstarting': 5,
                        'awol': 6, 'unentitled': 7};
    const a = statusValues[aRaw[columnKey]];
    const b = statusValues[bRaw[columnKey]];
    const result = (a > b ? 1 : (a < b ? -1 : 0));
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  }

  sortByState(aRaw, bRaw, columnKey, sortDirection) {
    const stateValues = {'running': 0, 'stopped': 1, 'crashed': 2, 'paused': 3, 'unknown': 4};
    const a = stateValues[aRaw[columnKey]];
    const b = stateValues[bRaw[columnKey]];
    const result = (a > b ? 1 : (a < b ? -1 : 0));
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  }

  searchData(datum, criteria) {
      if (criteria) {
        return datum.name.toLowerCase().includes(criteria.toLowerCase());
      }
      return true;
  }

  buildRows(guests) {
    return Object.keys(guests).map((id) => guests[id]);
  }

  render() {
    return (<VirtualizationGuestActionApi hostid={this.props.server_id}
                                          callback={this.actionCallback}>
      {
        ({
          onAction,
          messages,
        }) => {
          const data = this.state.serverData ? this.state.serverData : [];
          const modals_data = [
            {'type': 'start', 'name': t('Start / Resume'), 'icon': 'fa-play', 'bulkonly': true},
            {'type': 'shutdown', 'name': t('Stop'), 'icon': 'fa-stop', 'bulkonly': false},
            {'type': 'restart', 'name': t('Restart'), 'icon': 'fa-refresh', 'bulkonly': false},
            {'type': 'suspend', 'name': t('Suspend'), 'icon': 'fa-pause', 'bulkonly': false},
            {'type': 'delete', 'name': t('Delete'), 'icon': 'fa-trash', 'bulkonly': false}
          ];
          const panelButtons =
            <div className="pull-right btn-group">
              {modals_data
                .filter(action => action.type != 'delete' || this.props.salt_entitled)
                .map(action => this.createSelectedModalButton(action))}
            </div>;

          return (
            <VirtualizationGuestsListRefreshApi server_id={this.props.server_id} refreshInterval={this.props.refreshInterval}>
            {
              ({
                guests,
                refreshError,
              }) => {
                return (
                  <div>
                    {panelButtons}
                    <h2>{t("Hosted Virtual Systems")}</h2>
                    <p>{t("This is a list of virtual guests which are configured to run on this host.")}</p>
                    <MessageContainer items={this.state.errors.map(msg => MessagesUtils.error(msg))}/>
                    { refreshError && <MessageContainer items={refreshError}/> }
                    <Table
                      data={this.buildRows(guests)}
                      emptyText={t('No virtual guest to show.')}
                      identifier={(row) => row.uuid}
                      initialSortColumnKey="name"
                      initialItemsPerPage={userPrefPageSize}
                      selectable
                      selectedItems={this.state.selectedItems}
                      onSelect={this.handleSelectItems}>
                      searchField={
                          <SearchField filter={this.searchData}
                              criteria={""}
                              placeholder={t("Filter by name")} />
                      }>
                      <Column
                        columnKey="name"
                        comparator={Utils.sortByText}
                        header={t("Guest")}
                        cell={ (row) => row.name }
                      />
                      <Column
                        columnKey="serverName"
                        comparator={Utils.sortByText}
                        header={t("System")}
                        cell={ (row) => {
                            if (row.virtualSystemId == null) {
                                return t("Unregistered System");
                            } else {
                              if (row['accessible']) {
                                return <a href={'/rhn/systems/details/Overview.do?sid=' + row.virtualSystemId}>{row.serverName}</a>;
                              } else {
                                return row.serverName;
                              }
                            }
                          }
                        }
                      />
                      <Column
                        columnKey="statusType"
                        comparator={this.sortByUpdate}
                        header={t("Updates")}
                        cell={ (row) => {
                            if (row.statusType == null) {
                              return "-";
                            } else {
                              return Systems.statusDisplay(row, this.props.is_admin);
                            }
                          }
                        }
                      />
                      <Column
                        columnKey="stateLabel"
                        header={t("State")}
                        comparator={this.sortByState}
                        cell={ (row) => row.stateName }
                      />
                      <Column
                        columnKey="memory"
                        comparator={Utils.sortByNumber}
                        header={t("Current Memory")}
                        cell={ (row) => row.memory / 1024 + ' MiB' }
                      />
                      <Column
                        columnKey="vcpus"
                        comparator={Utils.sortByNumber}
                        header={t("vCPUs")}
                        cell={ (row) => row.vcpus }
                      />
                      <Column
                        columnKey="channelLabels"
                        comparator={Utils.sortByText}
                        header={t("Base Software Channel")}
                        cell={ (row) => {
                            if (row.channelId == null) {
                              return t("(none)");
                            } else {
                              if (row.subscribable) {
                                return <a href={'/rhn/channels/ChannelDetail.do?cid=' + row.channelId}>{row.channelLabels}</a>;
                              } else {
                                return row.channelLabels;
                              }
                            }
                          }
                        }
                      />
                      { this.props.salt_entitled && <Column
                        header={t("Action Status")}
                        cell={ (row) => {
                            const actionResult = this.state.actionsResults[row.uuid];
                            if (actionResult == "Failed")
                              return <i className="fa fa-times-circle-o fa-1-5x text-danger" title={t("Failed to schedule")}/>;
                            else if (actionResult != undefined) {
                              const icons = {
                                "Queued": "fa-clock-o text-info",
                                "Failed": "fa-times-circle-o text-danger",
                                "Completed": "fa-check-circle text-success"
                              };
                              return (<a href={`/rhn/systems/details/history/Event.do?sid=${this.props.server_id}&aid=${actionResult.id}`}>
                                        <i className={`fa ${icons[actionResult.result]} fa-1-5x`}
                                           title={actionResult.result}/>
                                      </a>);
                            } else {
                              return "-";
                            }
                          }
                        }
                        /> }
                      <Column
                        header={t("Actions")}
                        columnClass="text-right"
                        headerClass="text-right"
                        cell={ (row) => {
                          const state = row.stateLabel;
                          return <div className="btn-group">
                              { state != 'running' &&
                                <AsyncButton
                                  defaultType="btn-default btn-sm"
                                  title={t(state == "paused" ? "Resume" : "Start")}
                                  icon="fa-play"
                                  action={() => onAction("start", [row.uuid], {})}
                                /> }
                              { state == 'running' && this.createModalButton('suspend', modals_data, row) }
                              { state != 'stopped' && this.createModalButton('shutdown', modals_data, row) }
                              { (state == 'paused' || state == 'running') && this.createModalButton('restart', modals_data, row) }
                              <LinkButton
                                title={t("Edit")}
                                className="btn-default btn-sm"
                                icon="fa-edit"
                                href={`/rhn/manager/systems/details/virtualization/guests/${this.props.server_id}/edit/${row.uuid}`}
                              />
                              { this.props.salt_entitled && this.createModalButton('delete', modals_data, row) }
                            </div>;
                        }}
                      />
                    </Table>

                    {modals_data
                      .filter(action => action.type != 'delete' || this.props.salt_entitled)
                      .map(action => this.createConfirmModal(action, onAction).map(modal => modal))}
                  </div>
                );
              }
            }
            </VirtualizationGuestsListRefreshApi>
          );
        }
      }
    </VirtualizationGuestActionApi>);
  }
}

GuestsList.propTypes = {
    server_id: PropTypes.string.isRequired,
    refreshInterval: PropTypes.number.isRequired,
    salt_entitled: PropTypes.bool.isRequired,
    is_admin: PropTypes.bool.isRequired,
};

export default GuestsList;
