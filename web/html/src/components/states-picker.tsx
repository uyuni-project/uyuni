import * as React from "react";

import _partition from "lodash/partition";
import _sortBy from "lodash/sortBy";
import _unionBy from "lodash/unionBy";

import { SectionToolbar } from "components/section-toolbar/section-toolbar";

import Network from "../utils/network";
import { AsyncButton } from "./buttons";
import { TextField } from "./fields";
import { Messages, MessageType } from "./messages";
import { Utils as MessagesUtils } from "./messages";
import { RankingTable } from "./ranking-table";
import { SaltStatePopup } from "./salt-state-popup";

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
  applyRequest?: () => any;
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
  messages: MessageType[] | any;
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

  applySaltState = () => {
    if (this.state.changed.size > 0) {
      const response = window.confirm(t("There are unsaved changes. Do you want to proceed?"));
      if (response === false) {
        return null;
      }
    }
    return this.props.applyRequest?.();
  };

  onUpdateRanking = (channels) => {
    this.setState({ assigned: channels });
  };

  save = () => {
    const channels = this.state.assigned;
    if (this.props.type === "state" && !channels.length) {
      this.setMessages(MessagesUtils.error(t("State configuration must not be empty")));
      this.setState({ changed: new Map() });
      this.hideRanking();
      return;
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
        this.setMessages(MessagesUtils.info(t("State assignments have been saved.")));
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
    this.setMessages(null);
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
            <AsyncButton
              key="2"
              id="apply-btn"
              defaultType="btn-success"
              disabled={!assigned}
              action={this.applySaltState}
              text={t("Execute States")}
            />
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
                        placeholder={t(
                          "Search in {0}",
                          this.props.type === "state" ? t("states") : t("configuration channels")
                        )}
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
              <div className="col-md-offset-2 col-md-8">
                <h2>{t("Edit {0} Ranks", this.props.type === "state" ? t("State") : t("Channel"))}</h2>
                <p>
                  {t(
                    "Edit the ranking of the {0} by dragging them.",
                    this.props.type === "state" ? t("states") : t("configuration channels")
                  )}
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

export { StatesPicker };
