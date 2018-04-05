// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {AsyncButton, Button} = require("../components/buttons");
const {ActionSchedule} = require("../components/action-schedule");
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Messages = require("../components/messages").Messages;
const MessagesUtils = require("../components/messages").Utils;
const Toggler = require("../components/toggler");
const {BootstrapPanel} = require("../components/panel");
const {ChannelAnchorLink, ActionLink, ActionChainLink} = require("../components/links");
const ChannelUtils = require("../utils/channels");

import type JsonResult from "../utils/network";
import type {ActionChain} from "../components/action-schedule";

declare function t(msg: string): string;
declare function t(msg: string, arg: string): string;
declare function getServerId(): number;
declare var localTime: string;
declare var timezone: string;
declare var actionChains: Array<ActionChain>;

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
  subscribable: boolean,
  recommended: boolean
}

type SystemChannelsState = {
  messages: Array<Object>,
  earliest: Date,
  selectedBase: ?ChannelDto,
  selectedChildrenIds: Map<number, Set<number>>, // base channel id -> set<child channel id>
  availableBase: Array<ChannelDto>,
  // mapping channel ids to their DTOs
  availableChildren: Map<number, ChannelDto>,
  // channel dependencies: which child channels are required by a child channel?
  requiredChannels: Map<number, Set<number>>,
  // channel dependencies: by which child channels is a child channel required?
  requiredByChannels: Map<number, Set<number>>,
  page: number,
  scheduled: boolean,
  actionChain: ?ActionChain
}

class SystemChannels extends React.Component<SystemChannelsProps, SystemChannelsState> {

  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      earliest: Functions.Utils.dateWithTimezone(localTime),
      selectedBase: null,
      selectedChildrenIds: new Map(),
      availableBase: [],
      availableChildren: new Map(),
      requiredChannels: new Map(),
      requiredByChannels: new Map(),
      page: 1,
      scheduled: false,
      actionChain: null
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
          selectedChildrenIds: new Map([[base.id, new Set(data.data.children.map(c => c.id))]])
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
    Network.get(`/rhn/manager/api/systems/${this.props.serverId}/channels/${baseId}/accessible-children`).promise
      .then((data : JsonResult<Array<ChannelDto>>) => {
        const availableChildren = new Map(data.data
          .sort((a, b) => a.name.localeCompare(b.name))
          .map(channel => [channel.id, channel]));
        this.setState({
          // sort child channels by name to have a consistent order in the UI
          availableChildren: availableChildren
        });
        const childrenIds : Array<number> = Array.from(availableChildren.keys());
        return Network.post('/rhn/manager/api/admin/mandatoryChannels', JSON.stringify(childrenIds), "application/json").promise;
      })
      .then((data : JsonResult<Map<number, Array<number>>>) => {
        const requiredChannels = new Map(Object.entries(data.data)
          .map(entry => {
            const channelId = parseInt(entry[0]);
            const requiredChannelList = entry[1];
            return [
              channelId,
              new Set(requiredChannelList.filter(requiredId => requiredId !== channelId && !this.state.availableBase.map(channel => channel.id).includes(requiredId)))
            ];
          }));

        const requiredByChannels = ChannelUtils.computeReverseDependencies(requiredChannels);

        this.setState({requiredChannels: requiredChannels,
                       requiredByChannels: requiredByChannels})

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
    if (!this.state.selectedChildrenIds.has(baseId)) {
      this.state.selectedChildrenIds.set(baseId, new Set());
    }
    this.setState({
      selectedBase: baseId > -1 ?
          this.state.availableBase.find(c => c.id == baseId) :
          this.getNoBase(),
      availableChildren: new Map(),
      selectedChildrenIds: this.state.selectedChildrenIds
    });

    this.getAccessibleChildren(event.target.value);
  }

  getNoBase =() => {
    return { id: -1, name: t("(none, disable service)"), custom: false, subscribable: true};
  }

  handleChildChange = (event: SyntheticInputEvent<*>) => {
    this.selectChildChannel(parseInt(event.target.value), event.target.checked);
  }

  selectChildChannel = (childChannelId, select) => {
    const child : ?ChannelDto = this.state.availableChildren.get(childChannelId);
    if (child == null || this.state.selectedBase == null) {
      return;
    }
    const selectedChildrenIds = this.state.selectedChildrenIds.get(this.state.selectedBase.id);
    if (selectedChildrenIds) {
      if (select) {
        const dependingChannelIds = this.state.requiredChannels.get(child.id) || [];
        Array.from(dependingChannelIds)
          .filter(channel => channel.id !== child.id)
          .forEach(channelId => selectedChildrenIds.add(channelId));
        selectedChildrenIds.add(child.id);
      } else { // unselect
        const dependingChannelIds = this.state.requiredByChannels.get(child.id) || [];
        Array.from(dependingChannelIds)
          .filter(channel => channel.id !== child.id)
          .forEach(channelId => selectedChildrenIds.delete(channelId));
        selectedChildrenIds.delete(child.id);
      }
    }
    this.setState({
      selectedChildrenIds: this.state.selectedChildrenIds
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
      const selectedChildrenIds = this.state.selectedChildrenIds.get(this.state.selectedBase.id);
      return Array.from(selectedChildrenIds)
        .map(channelId => this.state.availableChildren.get(channelId))
        .filter(channel => channel !== undefined);
    }
    return null;
  }

  toggleRecommended = () => {
    if (this.areRecommendedChildrenSelected()) {
      const selectedRecommendedChildren = this.getSelectedChildren().filter(channel => channel.recommended);
      selectedRecommendedChildren.forEach(channel => this.selectChildChannel(channel.id, false));
    } else {
      const selectedChildrenIds = (this.getSelectedChildren() || []).map(channel => channel.id);
      const unselectedRecommendedChildren = Array.from(this.state.availableChildren.values())
          .filter(channel => channel.recommended && !selectedChildrenIds.includes(channel.id))
      unselectedRecommendedChildren.forEach(channel => this.selectChildChannel(channel.id, true));
    }
  }

  areRecommendedChildrenSelected = () : Boolean => {
    const selectedChildrenIds = (this.getSelectedChildren() || []).map(channel => channel.id);
    const recommendedChildren = Array.from(this.state.availableChildren.values()).filter(channel => channel.recommended);
    const selectedRecommendedChildren = recommendedChildren.filter(channel => selectedChildrenIds.includes(channel.id));
    const unselectedRecommendedChildren = recommendedChildren.filter(channel => !selectedChildrenIds.includes(channel.id));

    return selectedRecommendedChildren.length > 0 && unselectedRecommendedChildren.length == 0;
  }

  handleConfirm = () => {
    let selectedChildrenList = this.getSelectedChildren();
    return Network.post(`/rhn/manager/api/systems/${this.props.serverId}/channels`,
      JSON.stringify({
          base: this.state.selectedBase,
          children: selectedChildrenList,
          earliest: Functions.Formats.LocalDateTime(this.state.earliest),
          actionChain: this.state.actionChain ? this.state.actionChain.text : null
      }), "application/json")
        .promise.then(data => {
            if (data.success) {
              const msg = MessagesUtils.info(this.state.actionChain ?
                     <span>{t("Action has been successfully added to the Action Chain ")}
                          <ActionChainLink id={data.data}>{this.state.actionChain ? this.state.actionChain.text : ""}</ActionChainLink>.</span> :
                       <span>{t("Changing the channels has been ")}
                          <ActionLink id={data.data}>{t("scheduled")}.</ActionLink></span>);

              this.setState({
                messages: msg,
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
      this.setState({
        earliest: date,
        actionChain: null
      });
  }

  onActionChainChanged = (actionChain: ?ActionChain) => {
    this.setState({actionChain: actionChain})
  }

  dependenciesTooltip = (channelId) => {
    const resolveChannelNames = (channelIds) => {
      return Array.from(channelIds || new Set())
        .map(channelId => this.state.availableChildren.get(channelId))
        .filter(channel => channel != null)
        .map(channel => channel.name);
    }
    return ChannelUtils.dependenciesTooltip(
      resolveChannelNames(this.state.requiredChannels.get(channelId)),
      resolveChannelNames(this.state.requiredByChannels.get(channelId)));
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

      childChannels = Array.from(this.state.availableChildren.values()).map(c => <div className="checkbox">
        <input type="checkbox" value={c.id} id={"child_" + c.id}
          checked={selectedChildrenList && selectedChildrenList.some(child => child.id === c.id)}
          disabled={!c.subscribable}
          onChange={this.handleChildChange}/>
        <label title={this.dependenciesTooltip(c.id)} htmlFor={"child_" + c.id}>{c.name}</label>
        {
          c.recommended
            ? <span className='recommended-tag-base' title={'This extension is recommended'}>{t('recommended')}</span>
            : null
        }
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
                      <div style={{"overflow": "auto"}} >
                        <div style={{"float": "right"}}>
                          <Toggler.WithRecommended
                             enabled={this.areRecommendedChildrenSelected()}
                             handler={() => this.toggleRecommended()} />
                        </div>
                      </div>
                      <hr />
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
              this.state.availableChildren && Array.from(this.state.availableChildren.values()).map(c => <div className="checkbox">
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

            <ActionSchedule actionChains={actionChains}
               earliest={this.state.earliest}
               timezone={timezone} localTime={localTime}
               onActionChainChanged={this.onActionChainChanged}
               onDateTimeChanged={this.onDateTimeChanged}/>
        </div>
      </BootstrapPanel>
    );
  }

}

ReactDOM.render(
  <SystemChannels serverId={getServerId()} />,
    document.getElementById("subscribe-channels-div")
);
