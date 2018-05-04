'use strict';

const React = require("react");
const AceEditor = require("../components/ace-editor").AceEditor;
const Buttons = require("../components/buttons");
const Panels = require("../components/panel");
const Fields = require("../components/fields");
const PopUp = require("../components/popup").PopUp;
const Messages = require("../components/messages").Messages;
const MessagesUtils = require("../components/messages").Utils;
const Network = require("../utils/network");

const AsyncButton = Buttons.AsyncButton;
const LinkButton = Buttons.LinkButton;
const InnerPanel = Panels.InnerPanel;
const PanelRow = Panels.PanelRow;
const TextField = Fields.TextField;

function channelKey(channel) {
    return channel.label;
}

function channelIcon(channel) {
  let iconClass, iconTitle;
  if(channel.type === "state") {
    iconClass = "fa spacewalk-icon-salt-add";
    iconTitle = t("State Configuration Channel");
  } else {
    iconClass = "fa spacewalk-icon-software-channels";
    iconTitle = t("Normal Configuration Channel");
  }

  return (
    <i className={iconClass} title={iconTitle}/>
  );
}

class SaltStatePopup extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
      let popUpContent, icon, title, footer;

      if(this.props.saltState) {
          popUpContent = <AceEditor className="form-control" id="content-state"
                  minLines="20" maxLines="40" name="content" readOnly="true"
                  mode="yaml" content={this.props.saltState.content} >
              </AceEditor>;

          icon = this.props.saltState && channelIcon(this.props.saltState);
          title = this.props.saltState &&
              <span>{icon}{t("Configuration Channel: {0}", this.props.saltState.name)}</span>;

          footer = <div className="btn-group">
            <LinkButton href={"/rhn/configuration/ChannelOverview.do?ccid=" + this.props.saltState.id}
              className="btn-default" icon="fa-edit" text={t("Edit")}
              title={t("Edit Configuration Channel")}
            />
          </div>;
      }

      return (<PopUp
                title={title}
                className="modal-lg"
                id="saltStatePopUp"
                content={popUpContent}
                onClosePopUp={this.props.onClosePopUp}
                footer={footer}
              />)
  }

}

class ConfigChannels extends React.Component {

  constructor(props) {
    super(props);

    ["init", "tableBody", "handleSelectionChange", "onSearchChange", "search", "save",
      "applySaltState", "setView", "addChanged", "showPopUp", "onClosePopUp",
      "showRanking", "hideRanking", "onUpdateRanking", "getCurrentAssignment"]
    .forEach(method => this[method] = this[method].bind(this));

    this.state = {
        filter: "",
        view: "system",
        channels: [],
        search: {
            filter: null,
            results: []
        },
        assigned: [],
        changed: new Map(),
        messages: null
    };
    this.init();
  }

  init() {
    Network.get(this.props.matchUrl()).promise.then(data => {
      this.setState({
        channels: data
      });
    });
  }

  applySaltState() {
    if (this.state.changed.size > 0) {
        const response = confirm(t("There are unsaved changes. Do you want to proceed?"))
        if (response == false) {
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
    const request = this.props.saveRequest(channels).promise.then(
    (data, textStatus, jqXHR) => {
      console.log("success: " + data);

      const newSearchResults = this.state.search.results.map(channel => {
          const changed = this.state.changed.get(channelKey(channel))
          if(changed !== undefined) {
              return changed.value;
          } else {
              return channel;
          }
      });

      this.setState({
        changed: new Map(), // clear changed
        view: "system", // switch view to system
        channels: data, // set data for system tab
        search: {
            filter: this.state.search.filter,
            results: newSearchResults
        },
        messages: MessagesUtils.info(t('State assignments have been saved.'))
      });

      this.hideRanking();
     },
     (jqXHR, textStatus, errorThrown) => {
      console.log("fail: " + textStatus);

      this.setState({
        messages: MessagesUtils.error(t('An error occurred on save.'))
      });
     });
    return request;
  }

  onSearchChange(event) {
    this.setState({
        filter: event.target.value
    });
  }

  search() {
    if (this.state.filter === this.state.search.filter) {
        this.setState({
            view: "search"
        });
        return Promise.resolve();
    } else {
       return Network.get(this.props.matchUrl(this.state.filter)).promise.then(data => {
          console.log(data);
          this.setState({
            view: "search",
            search:  {
                filter: this.state.filter,
                results: data
            },
            messages: null
          });
        })
    }
  }

  setView(view) {
    return () => {
      this.setState({
          view: view
      });
    }
  }

  addChanged(original, key, selected) {
      const currentChannel = this.state.changed.get(key);
      if (currentChannel != undefined &&
          selected == currentChannel.original.assigned) {
            this.state.changed.delete(key);
      } else {
            this.state.changed.set(key, {
                original: original,
                value: Object.assign({}, original, {assigned: selected})
            });
      }
      this.setState({
         changed: this.state.changed
      });
  }

  handleSelectionChange(original) {
      return event => {
         this.addChanged(
            original,
            event.target.value,
            event.target.checked
         );
      }
  }

  tableBody() {
    const elements = [];
    var rows = [];
    if(this.state.view === "system") {
        rows = this.state.channels.filter(channel => channel.assigned).map(channel => {
            const changed = this.state.changed.get(channelKey(channel))
            if(changed !== undefined) {
                return changed;
            } else {
                return {
                    original: channel,
                };
            }
        });
    } else if(this.state.view === "search") {
        rows = this.state.search.results.map(channel => {
            const changed = this.state.changed.get(channelKey(channel))
            if(changed !== undefined) {
                return changed;
            } else {
                return {
                    original: channel,
                };
            }
        });
    } else if(this.state.view === "changes") {
        for(var channel of this.state.changed.values()) {
            rows.push(channel)
        }
    }

    for(var row of rows) {
      const changed = row.value;
      const currentChannel = changed === undefined? row.original : changed;

      elements.push(
        <tr id={currentChannel.label + '-row'} key={currentChannel.label} className={changed !== undefined ? "warning" : ""}>
          <td>{channelIcon(currentChannel)}<a href="#" data-toggle="modal" data-target="#saltStatePopUp" onClick={() => {this.showPopUp(currentChannel);}}>{currentChannel.name}</a></td>
          <td>{currentChannel.label}</td>
          <td>
            <div className="form-group">
                <input id={currentChannel.label + "-cbox"} type="checkbox" checked={currentChannel.assigned} value={currentChannel.label} onChange={this.handleSelectionChange(row.original)}/>
            </div>
          </td>
        </tr>
      );
     }

    return (
      <tbody className="table-content">
        {elements.length > 0 ? elements :
            <tr>
                <td colSpan="2">
                    <div>{t("No states assigned. Use search to find and assign states.")}</div>
                </td>
            </tr>
        }
      </tbody>
    );
  }

  setView(view) {
    return event => {
      this.setState({
          view: view
      });
    }
  }

  showPopUp(channel) {
    Network.get("/rhn/manager/api/states/" + channel.id + "/content").promise.then(data => {
        this.setState({
            showSaltState: Object.assign({}, channel, {content: data})
        })
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
        showSaltState: null
    })
  }

  clearMessages() {
      this.setState({
          messages: null
      })
  }

  getCurrentAssignment() {
    const unchanged = this.state.channels.filter(c => !this.state.changed.has(channelKey(c)));
    const changed = Array.from(this.state.changed.values()).map(c => c.value);

    return unchanged.concat(changed).filter(c => c.assigned);
  }

  render() {
    const currentAssignment = this.getCurrentAssignment();

    let buttons;
    if(this.state.rank) {
      // Buttons for the ranking page
      buttons = [
        <button key="1" id="back-btn" className="btn btn-default" onClick={this.hideRanking}>{t("Back")}</button>,
        <AsyncButton key="2" id="confirm-btn" defaultType="btn-success" action={this.save} name={t("Confirm")} />
      ];
    } else {
      // Buttons for the list/search page
      if(this.state.changed.size > 0) {
        // Save/Rank changes
        buttons = [
          <button key="1" id="save-btn" className="btn btn-success" onClick={this.showRanking}>{t("Save Changes")}</button>
        ];
      } else {
        // No changes, reorder or apply existing assignments
        const assigned = currentAssignment.length > 0;
        buttons = [
          <AsyncButton key="2" id="apply-btn" defaultType="btn-success" disabled={!assigned} action={this.applySaltState} name={t("Apply")} />
        ];

        if(assigned) {
          buttons.push(
            <button key="1" id="reorder-btn" className="btn btn-default" onClick={this.showRanking}>{t("Reorder")}</button>
          );
        }
      }
    }

    const messages = this.state.messages ?
      <Messages items={this.state.messages}/>
      : null;

    return (
      <span>
        {messages}
        <InnerPanel title={t("Configuration Channels")} icon="spacewalk-icon-salt-add" buttons={buttons}>
          { !this.state.rank &&
            <PanelRow className="input-group">
              <TextField id="search-field" value={this.state.filter} placeholder={t("Search in configuration channels")} onChange={this.onSearchChange} onPressEnter={this.search}/>
              <span className="input-group-btn">
                <AsyncButton id="search-states" name={t("Search")} action={this.search} />
                <button id="system-btn" className={this.state.view == "system" ? "btn btn-info" : "btn btn-default"} onClick={this.setView("system")}>{t("System")}</button>
                <button id="changes-btn" className={this.state.view == "changes" ? "btn btn-info" : "btn btn-default"} disabled={this.state.changed.size == 0} onClick={this.setView("changes")}>
                  {this.state.changed.size > 0 ? this.state.changed.size : t("No")} {t("Changes")}
                </button>
              </span>
            </PanelRow>
          }

          { this.state.rank ?
            <div className="col-md-offset-2 col-md-8">
              <h2>Edit Channel Ranks</h2>
              <p>{t("Edit the ranking of the configuration channels by dragging them.")}</p>
              <RankingTable items={currentAssignment} onUpdate={this.onUpdateRanking} emptyMsg={t("There are no states assigned.")}/>
            </div>
            :
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
          }

          <SaltStatePopup saltState={this.state.showSaltState} onClosePopUp={this.onClosePopUp}/>

        </InnerPanel>
      </span>
    );
  }

}

class RankingTable extends React.Component {

  constructor(props) {
    super(props);

    this.defaultEmptyMsg = t("There are no entries to show.");

    this.state = {
      items: this.props.items
    };
  }

  cloneItems() {
    return this.state.items.map(i => Object.assign({}, i));
  }

  handleUpdate() {
    const newItems = this.cloneItems();
    const ids = $(this.node).sortable('toArray', { attribute: "data-id" });
    if(ids.length > 0) {
      ids.forEach((id, ix) => {
        const item = newItems.find((elm) => elm.label === id);
        item.position = ix + 1;
      });
    }

    $(this.node).sortable('cancel');
    this.setState({ items: newItems });

    if(this.props.onUpdate) {
      this.props.onUpdate(newItems);
    }
  }

  componentDidMount() {
    $(this.node).sortable({
      update: this.handleUpdate.bind(this),
      items: "a"
    });

    this.handleUpdate();
  }

  getElements() {
    const sortedItems = this.state.items
      .sort((a, b) =>
        a.position == undefined ? 1
          : b.position == undefined ? -1
            : (a.position - b.position));

    return sortedItems.map(i => {
      // TODO: Provide a callback as prop for optional mapping and generify this default implementation
      const icon = channelIcon(i);
      return (
        <a href="#" className="list-group-item" key={i.label} data-id={i.label}>
          <i className="fa fa-sort"/>{icon}{i.name} ({i.label})
        </a>
      );
    });
  }

  render() {
    return (
      <div>
        { this.state.items.length > 0 ?
          <div ref={(node) => this.node = node} className="list-group">
            { this.getElements() }
          </div>
          :
          <div className="alert alert-info">{this.props.emptyMsg || this.defaultEmptyMsg}</div>
        }
      </div>
    );
  }
}

module.exports = {
    ConfigChannels : ConfigChannels
}
