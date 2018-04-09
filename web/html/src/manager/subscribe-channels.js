// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {AsyncButton, Button} = require("../components/buttons");
const {DateTimePicker} = require("../components/datetimepicker");
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Messages = require("../components/messages").Messages;
const MessagesUtils = require("../components/messages").Utils;
const {BootstrapPanel} = require("../components/panel");
const {ChannelAnchorLink, ActionLink} = require("../components/links");

import type JsonResult from "../utils/network";

declare function t(msg: string): string;
declare function t(msg: string, arg: string): string;
declare function getServerId(): number;
declare var localTime: string;
declare var timezone: string;

const msgMap = {
  "taskomatic_error": t("Error scheduling job in Taskomatic. Please check the logs."),
  "base_not_found_or_not_authorized": t("Base channel not found or not authorized."),
  "child_not_found_or_not_authorized": t("Child channel not found or not authorized."),
  "invalid_channel_id": t("Invalid channel id")
};

type SystemChannelsProps = {
  serverId: number
}

type ChannelDto = {
  id: number,
  name: string,
  custom: boolean,
  subscribable: boolean
}

type SystemChannelsState = {
  messages: Array<Object>,
  earliest: Date,
  selectedBase: ?ChannelDto,
  selectedChildren: Map<number, Array<ChannelDto>>,
  availableBase: Array<ChannelDto>,
  availableChildren: Array<ChannelDto>,
  page: number,
  scheduled: boolean
}

class SystemChannels extends React.Component<SystemChannelsProps, SystemChannelsState> {

  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      earliest: Functions.Utils.dateWithTimezone(localTime),
      selectedBase: null,
      selectedChildren: new Map(),
      availableBase: [],
      availableChildren: [],
      page: 1,
      scheduled: false
    };
  }

  componentDidMount() {
    this.updateView()
  }

  updateView = () => {
    Network.get(`/rhn/manager/api/systems/${this.props.serverId}/channels`)
      .promise.then(data => {
        const base : ChannelDto = data.data && data.data.base ? data.data.base : this.getNoBase();
        this.setState({
          selectedBase: base,
          selectedChildren: new Map([[base.id, data.data.children]])
        });
        if (data.data && data.data.base) {
          this.getAccessibleChildren(data.data.base.id);
        }
      })
      .catch(this.handleResponseError);

    Network.get(`/rhn/manager/api/systems/${this.props.serverId}/channels-available-base`)
      .promise.then(data => {
        this.setState({
          availableBase: data.data
        });
      })
      .catch(this.handleResponseError);
  }

  getAccessibleChildren = (baseId) => {
    // TODO cache children to avoid repeated calls
    Network.get(`/rhn/manager/api/systems/${this.props.serverId}/channels/${baseId}/accessible-children`)
      .promise.then((data : JsonResult<Array<ChannelDto>>) => {
        this.setState({
          // sort child channels by name to have a consistent order in the UI
          availableChildren: data.data.sort((a, b) => a.name.localeCompare(b.name))
        });
      })
      .catch(this.handleResponseError);
  }

  handleResponseError = (jqXHR, arg = "") => {
    const msg = Network.responseErrorMessage(jqXHR,
      (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
    this.setState({ messages: this.state.messages.concat(msg) });
  }

  handleBaseChange = (event: SyntheticInputEvent<*>) => {
    const baseId : number = parseInt(event.target.value);
    if (!this.state.selectedChildren.has(baseId)) {
      this.state.selectedChildren.set(baseId, []);
    }
    this.setState({
      selectedBase: baseId > -1 ?
          this.state.availableBase.find(c => c.id == baseId) :
          this.getNoBase(),
      availableChildren: [],
      selectedChildren: this.state.selectedChildren
    });

    this.getAccessibleChildren(event.target.value);
  }

  getNoBase =() => {
    return { id: -1, name: t("(none, disable service)"), custom: false, subscribable: true};
  }

  handleChildChange = (event: SyntheticInputEvent<*>) => {
    let child : ?ChannelDto = this.state.availableChildren.find(c => c.id.toString() == event.target.value);
    let selectedChildrenList : ?Array<ChannelDto>;
    if (child && this.state.selectedBase) {
      selectedChildrenList =  this.state.selectedChildren.get(this.state.selectedBase.id);
    }
    if (selectedChildrenList && child) {
      if (event.target.checked) {
        selectedChildrenList.push(child);
      } else {
        selectedChildrenList.splice(selectedChildrenList.findIndex(e => e.id.toString() == event.target.value), 1);
      }

    }
    this.setState({
      selectedChildren: this.state.selectedChildren
    });
  }

  handleNext = () => {
    this.setState({
      page: 2
    });
  }

  handlePrevious = () => {
    this.setState({
      page: 1
    });
  }

  getSelectedChildren = () : ?Array<ChannelDto> => {
    if (this.state.selectedBase && this.state.selectedBase.id) {
      return this.state.selectedChildren.get(this.state.selectedBase.id)
    }
    return null;
  }

  handleConfirm = () => {
    let selectedChildrenList = this.getSelectedChildren();
    return Network.post(`/rhn/manager/api/systems/${this.props.serverId}/channels`,
      JSON.stringify({
          base: this.state.selectedBase,
          children: selectedChildrenList,
          earliest: Functions.Formats.LocalDateTime(this.state.earliest)
      }), "application/json")
        .promise.then(data => {
            if (data.success) {
              this.setState({
                messages: MessagesUtils.info(<span>{t("Changing the channels has been ")}
                        <ActionLink id={data.data}>{t("scheduled")}.</ActionLink></span>),
                scheduled: true
              });
            } else {
              this.setState({
                messages: MessagesUtils.error(data.messages)
              });
            }
        })
        .catch(this.handleResponseError);
  }

  onDateTimeChanged = (date) => {
      this.setState({earliest: date});
  }

  render() {
    return (<span>
      <Messages items={this.state.messages}/>
      {
        this.state.page == 1 ?
          this.renderSelectionPage():
          this.renderConfirmPage()
      }
    </span>)
  }

  renderSelectionPage = () => {
    var baseChannels = [], childChannels;
    baseChannels.push(<div className="radio">
        <input type="radio" value="-1" id="base_none"
          checked={-1 === (this.state.selectedBase && this.state.selectedBase.id)}
          onChange={this.handleBaseChange}/>
        <label htmlFor="base_none">{t("(none, disable service)")}</label>
        <hr/>
      </div>);
    if (this.state.availableBase) {
      const baseOptions = this.state.availableBase
        .filter(c => !c.custom);
      const customOptions = this.state.availableBase
        .filter(c => c.custom);

      if (baseOptions.length > 0) {
        baseChannels.push(
          <div>
            <h4>{t("SUSE Channels")}</h4>
            { baseOptions.map(c => <div className="radio">
                <input type="radio" value={c.id} id={"base_" + c.id}
                  checked={c.id === (this.state.selectedBase && this.state.selectedBase.id)}
                  onChange={this.handleBaseChange}/>
                <label htmlFor={"base_" + c.id}>{c.name}</label>
                <ChannelAnchorLink id={c.id} newWindow={true}/>
              </div>)
            }
            <hr/>
        </div>);
      }
      if (customOptions.length > 0) {
        baseChannels.push(
          <div>
            <h4>{t("Custom Channels")}</h4>
            { customOptions.map(c => <div className="radio">
                <input type="radio" value={c.id} id={"base_" + c.id}
                  checked={c.id === (this.state.selectedBase && this.state.selectedBase.id)}
                  onChange={this.handleBaseChange}/>
                <label htmlFor={"base_" + c.id}>{c.name}</label>
                <ChannelAnchorLink id={c.id} newWindow={true}/>
              </div>)
            }
            <hr/>
        </div>);
      }
    }

    if (this.state.availableChildren) {
      let selectedChildrenList = this.getSelectedChildren();

      childChannels = this.state.availableChildren.map(c => <div className="checkbox">
        <input type="checkbox" value={c.id} id={"child_" + c.id}
          checked={selectedChildrenList && selectedChildrenList.some(child => child.id === c.id)}
          disabled={!c.subscribable}
          onChange={this.handleChildChange}/>
        <label htmlFor={"child_" + c.id}>{c.name}</label>
        <ChannelAnchorLink id={c.id} newWindow={true}/>
      </div>)
    }

    return (
      <BootstrapPanel
        footer={
          <div className="btn-group">
            <Button
              id="btn-next"
              text={t("Next")}
              className="btn-default"
              icon="fa-arrow-right"
              handler={this.handleNext}
            />
          </div>
        }>
          <span>
            <div className="row channel-for-system">
                  <div className="col-md-6">
                    <BootstrapPanel
                      title={t("Base Software Alteration")}
                      icon="spacewalk-icon-software-channels"
                      header={
                        <div className="page-summary">
                          {t("You can change the base software channel your system is subscribed to. The system will be unsubscribed from all software channels, and subscribed to the new base software channel.")}
                        </div>
                      }>
                        <div>{ baseChannels } </div>
                    </BootstrapPanel>
                  </div>
                  <div className="col-md-6">
                    <BootstrapPanel
                      title={t("Child Channels Subscriptions")}
                      icon="spacewalk-icon-software-channels"
                      header={
                        <div className="page-summary">
                          {t("This system is subscribed to the checked channels beneath, if any. Disabled checkboxes indicate channels that can't be manually subscribed or unsubscribed from.")}
                        </div>
                      }>
                        <div>{ this.state.selectedBase && this.state.selectedBase.name}
                            { this.state.selectedBase && this.state.selectedBase.id > -1 &&
                               <ChannelAnchorLink id={this.state.selectedBase.id} newWindow={true}/>}
                        </div>
                        <hr/>
                        { childChannels && childChannels.length > 0 ?
                            <div>{ childChannels } </div> :
                            <div>
                              { this.state.selectedBase && this.state.selectedBase.id > -1 ?
                                <span><i className="fa fa-exclamation-triangle fa-1-5x" title={t("No child channels available.")}/>
                                  {t("No child channels available.")}</span>
                                : undefined }
                            </div>
                        }

                    </BootstrapPanel>
                  </div>
            </div>
            <div className="row">
              <span className="help-block">
                <strong>Warning:</strong> 'FastTrack' and Beta child software channels are not available with Extended Update Support.
              </span>
            </div>
          </span>
      </BootstrapPanel>
    );
  }

  renderConfirmPage = () => {
    const selectedChildrenList = this.getSelectedChildren();
    return (
      <BootstrapPanel
        title={t("Confirm Software Channel Change")}
        icon="spacewalk-icon-software-channels"
        footer={
          <div className="btn-group">
            <Button
              id="btn-prev"
              className="btn-default"
              icon="fa-arrow-left"
              text={t("Prev")}
              handler={this.handlePrevious}
              disabled={this.state.scheduled}
            />
            <AsyncButton
              id="btn-confirm"
              defaultType="btn-success"
              name={t("Confirm")}
              action={this.handleConfirm}
              disabled={this.state.scheduled}
            />
          </div>
        }>
        <div>
            <div>{ this.state.selectedBase && this.state.selectedBase.name}
                 { this.state.selectedBase && this.state.selectedBase.id > -1 &&
                    <ChannelAnchorLink id={this.state.selectedBase.id} newWindow={true}/> }
                 <hr/>
            </div>
            <div>{
              this.state.availableChildren && this.state.availableChildren.map(c => <div className="checkbox">
                  <input type="checkbox" value={c.id}
                    checked={selectedChildrenList && selectedChildrenList.some(child => child.id === c.id)}
                    disabled={true}/>
                  <label>{c.name}</label>
                  <ChannelAnchorLink id={c.id}/>
              </div>)
            }
            {  this.state.availableChildren.length == 0 && this.state.selectedBase && this.state.selectedBase.id > -1 ?
                <div><i className="fa fa-exclamation-triangle fa-1-5x" title={t("No child channels available.")}/>
                    {t("No child channels available.")}</div> :
                undefined }

          </div>
            <div className="spacewalk-scheduler">
                <div className="form-horizontal">
                    <div className="form-group">
                        <label className="col-md-3 control-label">
                            {t("Earliest:")}
                        </label>
                        <div className="col-md-6">
                            <DateTimePicker onChange={this.onDateTimeChanged} value={this.state.earliest} timezone={timezone} />
                        </div>
                    </div>
                </div>
            </div>
        </div>
      </BootstrapPanel>
    );
  }

}

ReactDOM.render(
  <SystemChannels serverId={getServerId()} />,
    document.getElementById("subscribe-channels-div")
);
