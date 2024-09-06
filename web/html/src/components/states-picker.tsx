import * as React from "react";

import _partition from "lodash/partition";
import _sortBy from "lodash/sortBy";
import _unionBy from "lodash/unionBy";

import { inferEntityParams } from "manager/recurring/recurring-actions-utils";

import { pageSize } from "core/user-preferences";

import { DangerDialog } from "components/dialog/DangerDialog";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { Column } from "components/table/Column";
import { TableFilter } from "components/table/TableFilter";

import { Utils } from "utils/functions";

import Network from "../utils/network";
import { AsyncButton } from "./buttons";
import { TextField } from "./fields";
import { Messages, MessageType } from "./messages/messages";
import { Utils as MessagesUtils } from "./messages/messages";
import { RankingTable } from "./ranking-table";
import { SaltStatePopup } from "./salt-state-popup";
import { Table } from "./table/Table";

function channelKey(channel) {
  return channel.label;
}

function channelIcon(channel) {
  let iconClass, iconTitle, iconStyle;
  if (channel.type === "state") {
    iconClass = "fa spacewalk-icon-salt-add";
    iconTitle = t("State Configuration Channel");
  } else if (channel.type === "internal_state") {
    iconClass = "fa spacewalk-icon-salt-add";
    iconTitle = t("Internal State");
    iconStyle = { border: "1px solid black" };
  } else {
    iconClass = "fa spacewalk-icon-software-channels";
    iconTitle = t("Normal Configuration Channel");
  }

  return <i className={iconClass} title={iconTitle} style={iconStyle} />;
}

type StatesPickerProps = {
  type?: string;
  matchUrl: (filter?: string) => any;
  applyRequest?: (systems: any[]) => any;
  saveRequest: (channels: any[]) => any;
  messages?: (messages: MessageType[] | any) => any;
};

class StatesPickerState {
  filter = "";
  channels: any[] = [];
  search = {
    filter: null as string | null,
    results: [] as any[],
  };
  assigned: any[] = [];
  changed = new Map();
  showSaltState?: any | null = undefined;
  rank?: boolean = undefined;
  messages: MessageType[] = [];
}

class StatesPicker extends React.Component<StatesPickerProps, StatesPickerState> {
  state = new StatesPickerState();

  constructor(props: StatesPickerProps) {
    super(props);
    this.init();
  }

  init = () => {
    Network.get(this.props.matchUrl()).then((data) => {
      data = this.getSortedList(data);
      this.setState({
        channels: data,
        search: {
          filter: this.state.filter,
          results: data,
        },
      });
    });
  };

  applySaltState = (items) => {
    if (this.state.changed.size > 0) {
      const response = window.confirm(t("There are unsaved changes. Do you want to proceed?"));
      if (response === false) {
        return null;
      }
    }
    return this.props.applyRequest?.(items);
  };

  onUpdateRanking = (channels) => {
    this.setState({ assigned: channels });
  };

  save = () => {
    let messages: MessageType[] = [];
    const channels = this.state.assigned;
    if (this.props.type === "state" && !channels.length) {
      this.setMessages(MessagesUtils.error(t("State configuration must not be empty")));
      this.setState({ changed: new Map() });
      this.hideRanking();
      return;
    } else if (
      channels.filter((channel) => channel.name.includes("reboot") && channel.type === "internal_state").length > 0
    ) {
      // Put reboot states last
      let position = 1;
      let counter = 0;
      _sortBy(channels, "position").forEach((channel) => {
        if (channel.name.includes("reboot") && channel.type === "internal_state") {
          if (channel.position !== channels.length) {
            channel.position = channels.length;
            messages = messages.concat(MessagesUtils.info(t("Reboot state will be put last.")));
          }
          counter++;
        } else {
          channel.position = position++;
        }
      });
      if (counter > 1) {
        this.setMessages(
          MessagesUtils.error(t("'Reboot system' and 'Reboot system if needed' states cannot be used together."))
        );
        this.setState({ changed: new Map() });
        this.hideRanking();
        return;
      }
    }
    const request = this.props.saveRequest(channels).then(
      (data, textStatus, jqXHR) => {
        const newSearchResults = this.state.search.results.map((channel) => {
          const changed = this.state.changed.get(channelKey(channel));
          // We want to make sure the search results are updated with the changes. If there was a change
          // pick the updated value from the response if not we keep the original.
          if (changed !== undefined) {
            return data.filter((c) => c.label === changed.value.label)[0] || changed.value;
          } else {
            return data.filter((c) => c.label === channel.label)[0] || channel;
          }
        });

        messages = messages.concat(MessagesUtils.info(t("State assignments have been saved.")));
        this.setState({
          changed: new Map(), // clear changed
          // Update the channels with the new data
          channels: _unionBy(
            data,
            this.state.channels.map((c) => Object.assign(c, { assigned: false, position: undefined })),
            "name"
          ),
          search: {
            filter: this.state.search.filter,
            results: this.getSortedList(newSearchResults),
          },
        });
        this.setMessages(messages);
        this.hideRanking();
      },
      (jqXHR, textStatus, errorThrown) => {
        this.setMessages(MessagesUtils.error(t("An error occurred on save.")));
      }
    );
    return request;
  };

  onSearchChange = (event) => {
    this.setState({
      filter: event.target.value,
    });
  };

  getSortedList = (data) => {
    const [assigned, unassigned] = _partition(data, (d) => d.assigned);
    return _sortBy(assigned, "position").concat(_sortBy(unassigned, (n) => n.name.toLowerCase()));
  };

  search = () => {
    return Promise.resolve().then(() => {
      if (this.state.filter !== this.state.search.filter) {
        // Since we don't commit our changes to the backend in case of state type we perform a local search
        this.props.type === "state"
          ? this.stateTypeSearch()
          : Network.get(this.props.matchUrl(this.state.filter)).then((data) => {
              this.setState({
                search: {
                  filter: this.state.filter,
                  results: this.getSortedList(data),
                },
              });
              this.clearMessages();
            });
      }
    });
  };

  stateTypeSearch = () => {
    this.setState({
      search: {
        filter: this.state.filter,
        results: this.state.channels.filter((c) => c.name.includes(this.state.filter)),
      },
    });
    this.clearMessages();
  };

  addChanged = (original, key, selected) => {
    const currentChannel = this.state.changed.get(key);
    if (selected === currentChannel?.original?.assigned) {
      this.state.changed.delete(key);
    } else {
      this.state.changed.set(key, {
        original: original,
        value: Object.assign({}, original, { assigned: selected }),
      });
    }
    this.setState({
      changed: this.state.changed,
    });
  };

  handleSelectionChange = (original) => {
    return (event) => {
      this.addChanged(original, event.target.value, event.target.checked);
    };
  };

  tableBody = () => {
    const elements: React.ReactNode[] = [];
    let rows: any[] = [];
    rows = this.state.search.results.map((channel) => {
      const changed = this.state.changed.get(channelKey(channel));
      if (changed !== undefined) {
        return changed;
      } else {
        return {
          original: channel,
        };
      }
    });

    for (var row of rows) {
      const changed = row.value;
      const currentChannel = changed === undefined ? row.original : changed;

      elements.push(
        <tr
          id={currentChannel.label + "-row"}
          key={currentChannel.label}
          className={changed !== undefined ? "changed" : ""}
        >
          <td>
            {channelIcon(currentChannel)}
            {currentChannel.type !== "internal_state" ? (
              /* eslint-disable-next-line jsx-a11y/anchor-is-valid */
              <a
                href="#"
                data-toggle="modal"
                data-target="#saltStatePopUp"
                onClick={() => {
                  this.showPopUp(currentChannel);
                }}
              >
                {currentChannel.name}
              </a>
            ) : (
              currentChannel.name
            )}
          </td>
          <td>{currentChannel.label}</td>
          <td>
            <i className="fa fa-info-circle fa-1-5x text-primary" title={currentChannel.description} />
          </td>
          <td>
            <div className="form-group">
              <input
                id={currentChannel.label + "-cbox"}
                type="checkbox"
                checked={currentChannel.assigned}
                value={currentChannel.label}
                onChange={this.handleSelectionChange(row.original)}
              />
            </div>
          </td>
        </tr>
      );
    }

    return (
      <tbody className="table-content">
        {elements.length > 0 ? (
          elements
        ) : (
          <tr>
            <td colSpan={3}>
              <div>{t("No states assigned. Use search to find and assign states.")}</div>
            </td>
          </tr>
        )}
      </tbody>
    );
  };

  showPopUp = (channel) => {
    Network.get("/rhn/manager/api/states/" + channel.id + "/content").then((data) => {
      this.setState({
        showSaltState: Object.assign({}, channel, { content: data }),
      });
    });
  };

  showRanking = () => {
    this.clearMessages();
    this.setState({ rank: true });
  };

  hideRanking = () => {
    this.setState({ rank: false });
  };

  onClosePopUp = () => {
    this.setState({
      showSaltState: null,
    });
  };

  setMessages = (message) => {
    this.setState({
      messages: message,
    });
    if (this.props.messages) {
      return this.props.messages(message);
    }
  };

  clearMessages() {
    this.setMessages([]);
  }

  getCurrentAssignment = () => {
    const unchanged = this.state.channels.filter((c) => !this.state.changed.has(channelKey(c)));
    const changed = Array.from(this.state.changed.values()).map((c) => c.value);

    return unchanged.concat(changed).filter((c) => c.assigned);
  };

  render() {
    const currentAssignment = this.getCurrentAssignment();

    let buttons;
    if (this.state.rank) {
      // Buttons for the ranking page
      buttons = [
        <button key="1" id="back-btn" className="btn btn-default" onClick={this.hideRanking}>
          {t("Back")}
        </button>,
        <AsyncButton key="2" id="confirm-btn" defaultType="btn-success" action={this.save} text={t("Confirm")} />,
      ];
    } else {
      // Buttons for the list/search page
      if (this.state.changed.size > 0) {
        // Save/Rank changes
        buttons = [
          <button key="1" id="save-btn" className="btn btn-success" onClick={this.showRanking}>
            {t("Save Changes")}
          </button>,
        ];
      } else {
        // No changes, reorder or apply existing assignments
        const assigned = currentAssignment.length > 0;
        buttons = [
          typeof this.props.applyRequest !== "undefined" && (
            <ExecuteStatesButton assigned={assigned} applySaltState={this.applySaltState} type={this.props.type} />
          ),
        ];

        if (assigned) {
          buttons.push(
            <button key="1" id="reorder-btn" className="btn btn-default" onClick={this.showRanking}>
              {t("Reorder")}
            </button>
          );
        }
      }
    }

    return (
      <span>
        {!this.props.messages && this.state.messages ? <Messages items={this.state.messages} /> : null}
        <SectionToolbar>
          <div className="action-button-wrapper">
            <div className="btn-group">{buttons}</div>
          </div>
        </SectionToolbar>
        <div className="panel panel-default">
          <div className="panel-body">
            {!this.state.rank ? (
              <div className={"row"} id={"search-row"}>
                <div className={"col-md-5"}>
                  <div style={{ paddingBottom: 0.7 + "em" }}>
                    <div className="input-group">
                      <TextField
                        id="search-field"
                        value={this.state.filter}
                        placeholder={
                          this.props.type === "state" ? t("Search in states") : t("Search in configuration channels")
                        }
                        onChange={this.onSearchChange}
                        onPressEnter={this.search}
                      />
                      <span className="input-group-btn">
                        <AsyncButton id="search-states" text={t("Search")} action={this.search} />
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            ) : null}

            {this.state.rank ? (
              <div className="col-md-offset-2 offset-md-2 col-md-8">
                <h2>{this.props.type === "state" ? t("Edit State Ranks") : t("Edit Channel Ranks")}</h2>
                <p>
                  {this.props.type === "state"
                    ? t("Edit the ranking of the states by dragging them.")
                    : t("Edit the ranking of the configuration channels by dragging them.")}
                </p>
                <RankingTable
                  items={currentAssignment}
                  onUpdate={this.onUpdateRanking}
                  emptyMsg={t("There are no states assigned.")}
                />
              </div>
            ) : (
              <span>
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>{this.props.type === "state" ? t("State Name") : t("Channel Name")}</th>
                      <th>{this.props.type === "state" ? t("State Label") : t("Channel Label")}</th>
                      <th>{t("Description")}</th>
                      <th>{t("Assign")}</th>
                    </tr>
                  </thead>
                  {this.tableBody()}
                </table>
              </span>
            )}
            <SaltStatePopup saltState={this.state.showSaltState} onClosePopUp={this.onClosePopUp} />
          </div>
        </div>
      </span>
    );
  }
}

type ExecuteStatesProps = {
  assigned: boolean;
  type: any;
  applySaltState: (memberIds: any[]) => any;
};

class ExecuteStatesButton extends React.Component<ExecuteStatesProps> {
  state = {
    selected: [],
    showPopup: false,
  };

  showPopup = () => {
    if (inferEntityParams().includes("MINION")) {
      window.minions && this.props.applySaltState(window.minions.map((m) => m.id));
    } else {
      this.setState({ showPopup: true });
    }
  };

  onClose = () => {
    this.setState({
      showPopup: false,
      selected: [],
    });
  };

  onSelect = (items) => {
    this.setState({ selected: items });
  };

  onConfirmExecute = () => {
    if (this.state.selected && this.state.selected.length !== 0) {
      this.setState({ selected: [] });
      return this.props.applySaltState(this.state.selected);
    }
  };

  render() {
    const entityParams = inferEntityParams();

    const contentPopup = [
      <Messages
        key="messages"
        items={MessagesUtils.info(t("Select the systems to schedule for immediate state execution and confirm."))}
      />,
      <Table
        selectable={(item) => item.hasOwnProperty("id")}
        onSelect={this.onSelect}
        selectedItems={this.state.selected}
        initialSortColumnKey={"name"}
        data={"/rhn/manager/api/recurringactions/targets/" + entityParams}
        identifier={(item) => item.id}
        initialItemsPerPage={pageSize}
        emptyText={t("This table is empty")}
        defaultSearchField={"name"}
        searchField={<ExecuteStatesFilter />}
      >
        <Column
          columnKey={"name"}
          comparator={Utils.sortByText}
          header={t("System Name")}
          cell={(row) => {
            return (
              <a href={`/rhn/systems/details/Overview.do?sid=${row.id}`} className="js-spa">
                {row.name}
              </a>
            );
          }}
        />
      </Table>,
    ];

    return (
      <>
        <AsyncButton
          key="2"
          id="apply-btn"
          defaultType="btn-success"
          disabled={!this.props.assigned}
          action={this.props.type === "state" ? this.showPopup : this.props.applySaltState}
          text={t("Execute States")}
        />
        {this.state.showPopup ? (
          <DangerDialog
            id={"show-execute-states-popup"}
            title={t("Select Sytems")}
            isOpen={true}
            onClose={this.onClose}
            content={contentPopup}
            submitText={t("Confirm")}
            submitIcon="fa-check"
            btnClass="btn-success"
            onConfirmAsync={this.onConfirmExecute}
          />
        ) : null}
      </>
    );
  }
}

class ExecuteStatesFilter extends React.Component {
  render() {
    const filterOptions = [{ value: "name", label: t("System Name") }];
    return <TableFilter filterOptions={filterOptions} {...this.props} />;
  }
}

export { StatesPicker };
