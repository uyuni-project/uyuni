import * as React from "react";
import { AsyncButton } from "../components/buttons";
import { InnerPanel } from "components/panels/InnerPanel";
import { TextField } from "../components/fields";
import { Messages, MessageType } from "../components/messages";
import { Utils as MessagesUtils } from "../components/messages";
import Network from "../utils/network";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import { RankingTable } from "../components/ranking-table";
import { SaltStatePopup } from "../components/salt-state-popup";

function channelKey(channel) {
  return channel.label;
}

function channelIcon(channel) {
  let iconClass, iconTitle;
  if (channel.type === "state") {
    iconClass = "fa spacewalk-icon-salt-add";
    iconTitle = t("State Configuration Channel");
  } else {
    iconClass = "fa spacewalk-icon-software-channels";
    iconTitle = t("Normal Configuration Channel");
  }

  return <i className={iconClass} title={iconTitle} />;
}

type ConfigChannelsProps = {
  matchUrl: (filter?: string) => any;
  applyRequest: (component: ConfigChannels) => any;
  saveRequest: (channels: any[]) => any;
};

class ConfigChannelsState {
  filter = "";
  channels: any[] = [];
  search = {
    filter: null as string | null,
    results: [] as any[],
  };
  assigned: any[] = [];
  changed = new Map();
  messages: MessageType[] | null = null;
  showSaltState?: any | null = undefined;
  rank?: boolean = undefined;
}

class ConfigChannels extends React.Component<ConfigChannelsProps, ConfigChannelsState> {
  state = new ConfigChannelsState();

  constructor(props: ConfigChannelsProps) {
    super(props);

    [
      "init",
      "tableBody",
      "handleSelectionChange",
      "onSearchChange",
      "search",
      "save",
      "applySaltState",
      "addChanged",
      "showPopUp",
      "onClosePopUp",
      "showRanking",
      "hideRanking",
      "onUpdateRanking",
      "getCurrentAssignment",
    ].forEach((method) => (this[method] = this[method].bind(this)));
    this.init();
  }

  init() {
    Network.get(this.props.matchUrl()).then((data) => {
      this.setState({
        channels: data,
        search: {
          filter: this.state.filter,
          results: data,
        },
      });
    });
  }

  applySaltState() {
    if (this.state.changed.size > 0) {
      const response = window.confirm(t("There are unsaved changes. Do you want to proceed?"));
      if (DEPRECATED_unsafeEquals(response, false)) {
        return null;
      }
    }

    const request = this.props.applyRequest(this);

    return request;
  }

  onUpdateRanking(channels) {
    this.setState({ assigned: channels });
  }

  save() {
    const channels = this.state.assigned;
    const request = this.props.saveRequest(channels).then(
      (data, textStatus, jqXHR) => {
        const newSearchResults = this.state.search.results.map((channel) => {
          const changed = this.state.changed.get(channelKey(channel));
          if (changed !== undefined) {
            return changed.value;
          } else {
            return channel;
          }
        });

        this.setState({
          changed: new Map(), // clear changed
          channels: data, // set data for system tab
          search: {
            filter: this.state.search.filter,
            results: newSearchResults,
          },
          messages: MessagesUtils.info(t("State assignments have been saved.")),
        });

        this.hideRanking();
      },
      (jqXHR, textStatus, errorThrown) => {
        this.setState({
          messages: MessagesUtils.error(t("An error occurred on save.")),
        });
      }
    );
    return request;
  }

  onSearchChange(event) {
    this.setState({
      filter: event.target.value,
    });
  }

  search() {
    return Promise.resolve().then(() => {
      if (this.state.filter !== this.state.search.filter) {
        Network.get(this.props.matchUrl(this.state.filter)).then((data) => {
          this.setState({
            search: {
              filter: this.state.filter,
              results: data,
            },
            messages: null,
          });
        });
      }
    });
  }

  addChanged(original, key, selected) {
    const currentChannel = this.state.changed.get(key);
    if (
      !DEPRECATED_unsafeEquals(currentChannel, undefined) &&
      DEPRECATED_unsafeEquals(selected, currentChannel.original.assigned)
    ) {
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
  }

  handleSelectionChange(original) {
    return (event) => {
      this.addChanged(original, event.target.value, event.target.checked);
    };
  }

  tableBody() {
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
            {/* eslint-disable-next-line jsx-a11y/anchor-is-valid */}
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
          </td>
          <td>{currentChannel.label}</td>
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
  }

  showPopUp(channel) {
    Network.get("/rhn/manager/api/states/" + channel.id + "/content").then((data) => {
      this.setState({
        showSaltState: Object.assign({}, channel, { content: data }),
      });
    });
  }

  showRanking() {
    this.clearMessages();
    this.setState({ rank: true });
  }

  hideRanking() {
    this.setState({ rank: false });
  }

  onClosePopUp() {
    this.setState({
      showSaltState: null,
    });
  }

  clearMessages() {
    this.setState({
      messages: null,
    });
  }

  getCurrentAssignment() {
    const unchanged = this.state.channels.filter((c) => !this.state.changed.has(channelKey(c)));
    const changed = Array.from(this.state.changed.values()).map((c) => c.value);

    return unchanged.concat(changed).filter((c) => c.assigned);
  }

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
          <AsyncButton
            key="2"
            id="apply-btn"
            defaultType="btn-success"
            disabled={!assigned}
            action={this.applySaltState}
            text={t("Execute States")}
          />,
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

    const messages = this.state.messages ? <Messages items={this.state.messages} /> : null;

    return (
      <span>
        {messages}
        <InnerPanel title={t("Configuration Channels")} icon="spacewalk-icon-salt-add" buttons={buttons}>
          {!this.state.rank ? (
            <div className={"row"} id={"search-row"}>
              <div className={"col-md-5"}>
                <div style={{ paddingBottom: 0.7 + "em" }}>
                  <div className="input-group">
                    <TextField
                      id="search-field"
                      value={this.state.filter}
                      placeholder={t("Search in configuration channels")}
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
              <h2>Edit Channel Ranks</h2>
              <p>{t("Edit the ranking of the configuration channels by dragging them.")}</p>
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
                    <th>{t("Channel Name")}</th>
                    <th>{t("Channel Label")}</th>
                    <th>{t("Assign")}</th>
                  </tr>
                </thead>
                {this.tableBody()}
              </table>
            </span>
          )}

          <SaltStatePopup saltState={this.state.showSaltState} onClosePopUp={this.onClosePopUp} />
        </InnerPanel>
      </span>
    );
  }
}

export { ConfigChannels };
