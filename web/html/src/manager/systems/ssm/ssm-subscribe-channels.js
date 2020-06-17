/* eslint-disable */
// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {AsyncButton, Button} = require("components/buttons");
const {ActionSchedule} = require("components/action-schedule");
const Network = require("utils/network");
const Functions = require("utils/functions");
const {Messages} = require("components/messages");
const {Table} = require("components/table/Table");
const {Column} = require("components/table/Column");
const { BootstrapPanel } = require('components/panels/BootstrapPanel');
const MessagesUtils = require("components/messages").Utils;
const {ChannelLink, ActionLink, ActionChainLink, SystemLink} = require("components/links");
const {PopUp} = require("components/popup");
const {Toggler} = require("components/toggler");
const ChannelUtils = require("core/channels/utils/channels-dependencies.utils");
const SpaRenderer  = require("core/spa/spa-renderer").default;

import type JsonResult from "utils/network";
import type {ActionChain} from "components/action-schedule";

declare var localTime: string;
declare var timezone: string;
declare var userPrefPageSize: string;
declare var actionChains: Array<ActionChain>;

const msgMap = {
  "taskomatic_error": t("Error scheduling job in Taskomatic. Please check the logs."),
  "no_base_channel_guess": t("Could not determine system default channel."),
  "invalid_change": t("Channel change is invalid."),
  "incompatible_base": t("Selected base is not compatible with system."),
  "no_base_change_found": t("No base channel change found.")
};

const actionLabelMap = {
  "NO_CHANGE": t("No Change"),
  "SUBSCRIBE": t("Subscribe"),
  "UNSUBSCRIBE": t("Unsubsribe")
};

function getAllowedChangeId(allowed: SsmAllowedChildChannelsDto, childId: string) {
  return (allowed.oldBaseChannel ? allowed.oldBaseChannel.id : "none") +
   "_" +
   (allowed.newBaseDefault ? "default_" : "expl_") +
   (allowed.newBaseChannel ? allowed.newBaseChannel.id : "none") +
   "_" + childId;
}

type ServersListPopupProps = {
  servers: Array<SsmServerDto>,
  channelName: string,
  title: string,
  onClosePopUp: () => void
}

class ServersListPopup extends React.Component<ServersListPopupProps> {

  constructor(props) {
    super(props);
  }

  render() {
      return (<PopUp
                title={this.props.title + " " + this.props.channelName}
                className="modal-lg"
                id="channelServersPopup"
                onClosePopUp={this.props.onClosePopUp}
                content={
                  <Table
                    data={this.props.servers}
                    identifier={srv => srv.id}
                    initialSortColumnKey="modified"
                    initialSortDirection={-1}
                    initialItemsPerPage={userPrefPageSize}
                    >
                    <Column
                      columnKey="name"
                      comparator={Functions.Utils.sortByText}
                      header={t("System")}
                      cell={ (srv: SsmServerDto) =>
                        <SystemLink id={srv.id} newWindow={true}>{srv.name}</SystemLink> }
                    />
                  </Table>
                }/>)
  }

}

type BaseChannelProps = {
  baseChannels: Array<SsmAllowedBaseChannelsJson>,
  baseChanges: SsmBaseChannelChangesJson,
  footer: Footer,
  onSelectBase: (string, string) => void
}

type BaseChannelState = {
  baseChanges: Map<string, string>,
  popupServersList: Array<SsmServerDto>,
  popupServersChannelName: string
}

class BaseChannelPage extends React.Component<BaseChannelProps, BaseChannelState> {

  constructor(props: BaseChannelProps) {
    super(props);
    this.state = {
      baseChanges: props.baseChanges.changes.reduce((acc, cur) => acc.set(cur.oldBaseId, cur.newBaseId), new Map()),
      popupServersList: [],
      popupServersChannelName: ""
    }
  }

  onChangeBase = (oldBaseId: string, newBaseId: string) => {
    const changes = this.state.baseChanges;
    changes.set(oldBaseId, newBaseId);
    this.setState({
      baseChanges: changes
    });
    this.props.onSelectBase(oldBaseId, newBaseId);
  }

  showServersListPopUp = (channel: SsmAllowedBaseChannelsJson) => {
    this.setState({
      popupServersList: channel.servers,
      popupServersChannelName: channel.base.name
    });
  }

  onCloseServersListPopup = () => {
    this.setState({
      popupServersList: [],
      popupServersChannelName: ""
    });
  }

  render() {
    const defaultOption = <option value="-1">{t("System Default Base Channel")}</option>;

    return (
      <BootstrapPanel
        title={t("Base Channel")}
        icon="spacewalk-icon-software-channels"
        header={
          <div className="page-summary">
            {t("As a Channel Administrator, you may change the base channels your systems are subscribed to. Valid channels are either channels created by your organization, or the default SUSE base channel for your operating system version and processor type. Systems will be unsubscribed from all channels, and subscribed to their new base channels. ")}
            <p>
              <strong>{t("This operation can have a dramatic effect on the packages and patches available to the systems, and should be used with caution.")}</strong>
            </p>
          </div>
        }
        footer={this.props.footer}>
        <Table
          data={this.props.baseChannels}
          identifier={channel => channel.base.id}
          initialSortColumnKey="modified"
          initialSortDirection={-1}
          initialItemsPerPage={userPrefPageSize}
          >
          <Column
            columnKey="name"
            comparator={Functions.Utils.sortByText}
            header={t("Current base Channel")}
            cell={ (channel: SsmAllowedBaseChannelsJson) =>
              <ChannelLink id={channel.base.id} newWindow={true}>{channel.base.name}</ChannelLink> }
          />
          <Column
            columnKey="systems"
            comparator={Functions.Utils.sortByText}
            header={t("Systems")}
            cell={ (channel: SsmAllowedBaseChannelsJson) =>
              <a href="#" data-toggle="modal" data-target="#channelServersPopup"
                onClick={() => this.showServersListPopUp(channel)}>{channel.servers.length}</a>
             }
           />
          <Column
            columnKey="desired"
            header={t("Desired base Channel")}
            cell={ (channel: SsmAllowedBaseChannelsJson) => {
              const newBaseId = this.state.baseChanges.get(channel.base.id);
              const baseOptions = channel.allowedBaseChannels
                .filter(c => !c.custom)
                .map(c => <option value={c.id}>{c.name}</option> )
                .concat(defaultOption);
              const customOptions = channel.allowedBaseChannels
                .filter(c => c.custom)
                .map(c => <option value={c.id}>{c.name}</option> );

              return (
              <select id={"desired_base_" + channel.base.id} size="5" defaultValue="0" value={ newBaseId } onChange={(ev) => this.onChangeBase(channel.base.id, ev.target.value)}>
                <option value="0">{t("No Change")}</option>
                {
                   customOptions && customOptions.length > 0 ?
                    <optgroup label={t("SUSE Channels")}>
                      {baseOptions}
                    </optgroup> :
                    baseOptions
                }
                { customOptions && customOptions.length > 0 ?
                  <optgroup label={t("Custom Channels")}>
                    { customOptions }
                  </optgroup> :
                  null
                }
              </select>
            );
            } }
          />
        </Table>

        <ServersListPopup servers={this.state.popupServersList} channelName={this.state.popupServersChannelName}
           title={t("Systems subscribed to")}
           onClosePopUp={this.onCloseServersListPopup}/>

      </BootstrapPanel>);
  }
}

type SsmChannelDto = {
  id: string,
  name: string,
  custom: boolean
}

type SsmServerDto = {
  id: string,
  name: string
}

type SsmAllowedChildChannelsDto = {
  oldBaseChannel: SsmChannelDto,
  newBaseChannel?: SsmChannelDto,
  newBaseDefault: boolean,
  servers: Array<SsmServerDto>,
  childChannels: Array<SsmChannelDto>,
  incompatibleServers: Array<SsmServerDto>
}

type ChildChannelProps = {
  childChannels: Array<SsmAllowedChildChannelsDto>,
  childChanges: Array<ChannelChangeDto>,
  footer: Footer,
  onChangeChild: (allowedChannels: SsmAllowedChildChannelsDto, childId: string, action: string) => void
}

type ChildChannelState = {
  selections: Map<string, string>,
  popupServersList: Array<SsmServerDto>,
  popupServersChannelName: string,
  // channel dependencies: which child channels are required by a child channel?
  requiredChannels: Map<number, Set<number>>,
  // channel dependencies: by which child channels is a child channel required?
  requiredByChannels: Map<number, Set<number>>
}

class ChildChannelPage extends React.Component<ChildChannelProps, ChildChannelState> {

  constructor(props: ChildChannelProps) {
    super(props);

    const selections : Map<string, string> = new Map();
    props.childChanges.forEach(change => {
      change.childChannelActions.forEach((childAction, childId) =>
        selections.set(this.getChangeId(change, childId), childAction))
    })

    this.state = {
      selections : selections,
      popupServersList: [],
      popupServersChannelName: "",
      requiredChannels: new Map(),
      requiredByChannels: new Map()
    }
  }

  componentDidMount() {
    // get channel dependencies
    // TODO cache stuff to avoid repeated calls
    const childrenIds : Array<number> = Array.from(this.props.childChannels
      .flatMap(dto => dto.childChannels.map(channel => channel.id)));
    Network.post('/rhn/manager/api/admin/mandatoryChannels', JSON.stringify(childrenIds), "application/json").promise
      .then((response : JsonResult<Map<number, Array<number>>>) => {
        const channelDeps = ChannelUtils.processChannelDependencies(response.data);
        this.setState({
          requiredChannels: channelDeps.requiredChannels,
          requiredByChannels: channelDeps.requiredByChannels
        });
      })
      .catch(err => console.log(err.statusText));
  }

  getChangeId = (change: ChannelChangeDto, childId: string) => {
    return (change.oldBaseId ? change.oldBaseId : "none") +
    "_" +
    (change.newBaseDefault ? "default_" : "expl_") +
    (change.newBaseId ? change.newBaseId : "none") +
    "_" +
    (childId ? childId : "");
  }

  onChangeChild = (allowedChannels: SsmAllowedChildChannelsDto, childId: number, action: string) => {
    let dependencies = [];
    const childReqChannels = this.state.requiredChannels.get(childId);
    const childReqByChannels = this.state.requiredByChannels.get(childId);
    if (action === "SUBSCRIBE" && childReqChannels) {
      dependencies = Array.from(childReqChannels);
    } else if (action === "UNSUBSCRIBE" && childReqByChannels) {
      dependencies = Array.from(childReqByChannels);
    } else if (action === "NO_CHANGE" && childReqChannels && childReqByChannels) {
      // in this case we can't make any assumptions about the actual assignment of the channel,
      // let's reset both the forward and backward deps
      dependencies = Array.from(childReqChannels).concat(childReqByChannels);
    }

    // change the channel AND its dependencies
    [childId].concat(dependencies).forEach(channelId => {
      const allowedId = getAllowedChangeId(allowedChannels, channelId);
      this.state.selections.set(allowedId, action);
      this.setState({selections: this.state.selections});
      this.props.onChangeChild(allowedChannels, channelId, action);
    });
  }

  dependenciesTooltip = (channelId) => {
    const resolveChannelNames = (channelIds) => {
      return this.props.childChannels
        .flatMap(dto => dto.childChannels)
        .filter(channel => (channelIds || new Set()).has(channel.id))
        .map(channel => channel.name);
    }
    return ChannelUtils.dependenciesTooltip(
      resolveChannelNames(this.state.requiredChannels.get(channelId)),
      resolveChannelNames(this.state.requiredByChannels.get(channelId)));
  }

  toggleRecommended = (change: SsmAllowedChildChannelsDto) => {
    const recommendedChildChannelIds = change.childChannels
      .filter(channel => channel.recommended)
      .map(channel => channel.id)

    if (this.areRecommendedChildrenSelected(change)) {
      recommendedChildChannelIds
        .filter(channelId => this.state.selections.get(getAllowedChangeId(change, channelId)) === "SUBSCRIBE")
        .forEach(channelId => this.onChangeChild(change, channelId, "NO_CHANGE"));
    } else {
      recommendedChildChannelIds
        .filter(channelId => this.state.selections.get(getAllowedChangeId(change, channelId)) !== "SUBSCRIBE")
        .forEach(channelId => this.onChangeChild(change, channelId, "SUBSCRIBE"));
    }
  }

  areRecommendedChildrenSelected = (change: SsmAllowedChildChannelsDto) => {
    const recommendedChannels = change.childChannels
      .filter(channel => channel.recommended);
    const recommendedNonSubscribeActions = recommendedChannels
      .map(channel => getAllowedChangeId(change, channel.id))
      .map(changeId => this.state.selections.get(changeId))
      .filter(action => action !== "SUBSCRIBE");

    return recommendedChannels.length > 0 && recommendedNonSubscribeActions.length == 0;
  }

  showServersListPopUp = (channelName: string, servers: Array<SsmServerDto>) => {
    this.setState({
        popupServersList: servers,
        popupServersChannelName: channelName
    });
  }

  onCloseServersListPopup = () => {
    this.setState({
      popupServersList: [],
      popupServersChannelName: ""
    });
  }

  render() {
    const rows = this.props.childChannels.map(allowed => {
      return (
          <div key={getAllowedChangeId(allowed, "")}>
            <div className="row">
              <div className="col-md-8">
                <h4 style={{"float": "left", "paddingRight": "10px"}}>
                  { allowed.newBaseChannel ?
                    <ChannelLink id={allowed.newBaseChannel.id} newWindow={true}>{allowed.newBaseChannel.name}</ChannelLink> :
                     t("(Couldn't determine new base channel)")
                  }
                </h4>
                <Toggler
                   handler={() => this.toggleRecommended(allowed)}
                   value={this.areRecommendedChildrenSelected(allowed)}
                   text={t('include recommended')}
                   disabled={!allowed.childChannels.some(channel => channel.recommended)}
                />
              </div>
              <div className="col-md-4 text-right">
                <strong>
                  { allowed.servers && allowed.servers.length > 0 ?
                    <a href="#" data-toggle="modal" data-target="#channelServersPopup"
                      onClick={() => this.showServersListPopUp(allowed.newBaseChannel ? allowed.newBaseChannel.name : "",
                       allowed.servers)}>
                        {allowed.servers.length} {t("system(s) to subscribe")}
                    </a> : null
                  }
                  { allowed.incompatibleServers && allowed.incompatibleServers.length > 0 ?
                    <a href="#" data-toggle="modal" data-target="#channelServersPopup"
                      onClick={() => this.showServersListPopUp(allowed.newBaseChannel ? allowed.newBaseChannel.name : t("(none)"),
                       allowed.incompatibleServers)}>
                        <i className="fa fa-exclamation-triangle fa-1-5x" aria-hidden="true"></i>
                        {allowed.incompatibleServers.length} {t("system(s) incompatible")}
                    </a> : null
                  }
                </strong>
              </div>
            </div>
            <hr/>
            <dl className="col-lg-12">
              { allowed.childChannels.map(child =>
                <dt className="row">
                  <div className="col-md-6">
                    <ChannelLink id={child.id} newWindow={true}>{ child.name }</ChannelLink> &nbsp;
                    {
                      this.dependenciesTooltip(child.id)
                        ? <a href="#">
                            <i className="fa fa-info-circle spacewalk-help-link" title={this.dependenciesTooltip(child.id)}></i>
                          </a>
                        : null
                    }
                    &nbsp;
                    {
                      child.recommended
                        ? <span className='recommended-tag-base' title={'This extension is recommended'}>{t('recommended')}</span>
                        : null
                    }
                  </div>
                  <div className="col-md-4">
                    <div className="row radio">
                      <div className="col-md-4">
                        <input type="radio" name={"ch_action_" + child.id} id={"ch_action_no_change_" + child.id} value="NO_CHANGE"
                          checked={this.state.selections.get(getAllowedChangeId(allowed, child.id)) === 'NO_CHANGE'}
                          onChange={(ev) => this.onChangeChild(allowed, child.id, ev.target.value)}
                        />
                        <label htmlFor={"ch_action_no_change_" + child.id}>{t("No change")}</label>
                      </div>
                      <div className="col-md-4">
                        <input type="radio" name={"ch_action_" + child.id} id={"ch_action_subscr_" + child.id} value="SUBSCRIBE"
                          checked={this.state.selections.get(getAllowedChangeId(allowed, child.id)) === 'SUBSCRIBE'}
                          onChange={(ev) => this.onChangeChild(allowed, child.id, ev.target.value)}
                        />
                        <label htmlFor={"ch_action_subscr_" + child.id}>{t("Subscribe")}</label>
                      </div>
                      <div className="col-md-4">
                        <input type="radio" name={"ch_action_" + child.id} id={"ch_action_unscr_" + child.id} value="UNSUBSCRIBE"
                          checked={this.state.selections.get(getAllowedChangeId(allowed, child.id)) === 'UNSUBSCRIBE'}
                          onChange={(ev) => this.onChangeChild(allowed, child.id, ev.target.value)}
                        />
                        <label htmlFor={"ch_action_unscr_" + child.id}>{t("Unsubscribe")}</label>
                      </div>
                    </div>
                  </div>
                </dt>)
              }
            </dl>
          </div>
      );
    });
    return (
        <BootstrapPanel
          title={t("Child Channels")}
          icon="spacewalk-icon-software-channels"
          header={
                <div className="page-summary">
                  {t("Below is a list of channels in your organization. ")}
                  <ul>
                    <li>{t("To make no changes for a channel, check Do Nothing for that channel.")}</li>
                    <li>{t("To subscribe selected systems to a channel, check Subscribed for that channel.")}</li>
                    <li>{t("To unsubscribe selected systems from a channel, check Unsubscribed for that channel.")}</li>
                  </ul>
                </div>
          }
          footer={this.props.footer}>
          { rows }
          <ServersListPopup servers={this.state.popupServersList} channelName={this.state.popupServersChannelName}
             title={t("Systems to subscribe to")}
             onClosePopUp={this.onCloseServersListPopup}/>
        </BootstrapPanel>
    );
  }

}


type SummaryPageProps = {
  allowedChanges: Array<SsmAllowedChildChannelsDto>,
  finalChanges: Array<ChannelChangeDto>,
  footer: Footer,
  onChangeEarliest: (earliest: Date) => void,
  onChangeActionChain: (actionChain: ?ActionChain) => void
}

type SummaryPageState = {
  popupServersList: Array<SsmServerDto>,
  popupServersChannelName: string,
  earliest: Date,
  actionChain: ?ActionChain
}

class SummaryPage extends React.Component<SummaryPageProps, SummaryPageState> {

  constructor(props) {
    super(props);
    this.state = {
      popupServersList: [],
      popupServersChannelName: "",
      earliest: Functions.Utils.dateWithTimezone(localTime),
      actionChain: null
    }
  }

  onDateTimeChanged = (date: Date) => {
      this.setState({"earliest": date});
      this.props.onChangeEarliest(date);
  }

  showServersListPopUp = (channelName: string, servers: Array<SsmServerDto>) => {
    this.setState({
        popupServersList: servers,
        popupServersChannelName: channelName
    });
  }

  onCloseServersListPopup = () => {
    this.setState({
      popupServersList: [],
      popupServersChannelName: ""
    });
  }


  setScheduleTime = (newtime) => {
    const time = new Date(newtime);
    this.setState({
      earliest: time
    });
    this.props.onChangeEarliest(time);
  }

  onActionChainChanged = (actionChain: ?ActionChain) => {
    this.setState({
      actionChain: actionChain
    });
    this.props.onChangeActionChain(actionChain);
  }

  render() {
    const rows = this.props.allowedChanges.map(allowed => {
      const newBaseName = allowed.newBaseChannel ? allowed.newBaseChannel.name : t("(Couldn't determine new base channel)");
      return (
          <div key={getAllowedChangeId(allowed, "")}>
            <div className="row">
              <div className="col-md-8">
                <h4>
                  { allowed.oldBaseChannel ?
                    <ChannelLink id={allowed.oldBaseChannel.id} newWindow={true}> { allowed.oldBaseChannel.name } </ChannelLink> :
                    t("(None)")
                  }
                  <i className="fa fa-arrow-right" style={{margin: "0px 10px 0px 10px"}} aria-hidden="true"></i>
                  { allowed.newBaseChannel ?
                    <ChannelLink id={allowed.newBaseChannel.id} newWindow={true}>{ newBaseName }</ChannelLink> :
                    t("(Couldn't determine new base channel)")
                  }
                  { allowed.newBaseDefault ? t(" (system default)") : ""}
                </h4>
              </div>
              <div className="col-md-4 text-right">
                <strong>
                  { allowed.servers && allowed.servers.length > 0 ?
                    <a href="#" data-toggle="modal" data-target="#channelServersPopup"
                      onClick={() => this.showServersListPopUp(newBaseName, allowed.servers)}>
                        {allowed.servers.length} {t("system(s) to subscribe")}
                    </a> : null
                  }
                  { allowed.incompatibleServers && allowed.incompatibleServers.length > 0 ?
                    <a href="#" data-toggle="modal" data-target="#channelServersPopup"
                      onClick={() => this.showServersListPopUp(newBaseName, allowed.incompatibleServers)}>
                        <i className="fa fa-exclamation-triangle fa-1-5x" aria-hidden="true"></i>
                        {allowed.incompatibleServers.length} {t("system(s) incompatible")}
                    </a> : null
                  }
                </strong>
              </div>
            </div>
            <hr/>
            <dl className="col-lg-12">
              { allowed.childChannels.map(child =>
                <dt className="row">
                  <div className="col-md-6">
                    <ChannelLink id={child.id} newWindow={true}>{ child.name + (child.recommended ? " (R)" : "") }</ChannelLink>
                  </div>
                  <div className="col-md-4">
                    { actionLabelMap[this.getChildAction(allowed, child.id)] }
                  </div>
                </dt>)
              }
            </dl>
          </div>);
      });

    return <BootstrapPanel
      title={t("Channel Changes Overview")}
      icon="spacewalk-icon-software-channels"
      footer={this.props.footer}>
      { rows }

      <ActionSchedule timezone={timezone} localTime={localTime}
         earliest={this.state.earliest}
         actionChains={actionChains}
         actionChain={this.state.actionChain}
         onActionChainChanged={this.onActionChainChanged}
         onDateTimeChanged={this.onDateTimeChanged}
         systemIds={this.state.popupServersList.map(s => Number(s.id))}
         actionType="channels.subscribe" />

      <ServersListPopup servers={this.state.popupServersList} channelName={this.state.popupServersChannelName}
         title={t("Systems to subscribe to")}
         onClosePopUp={this.onCloseServersListPopup}/>

    </BootstrapPanel>;
  }

  getChildAction = (allowed: SsmAllowedChildChannelsDto, childId: string) => {
    const ch = this.props.finalChanges.find(fc =>
       fc.newBaseId && allowed.newBaseChannel && fc.newBaseId == allowed.newBaseChannel.id &&
         ((allowed.oldBaseChannel && fc.oldBaseId == allowed.oldBaseChannel.id) || (!allowed.oldBaseChannel && !fc.oldBaseId)));
    return ch ? ch.childChannelActions.get(childId) : "";
  }
}

type ResultPageProps = {
  results: Array<ScheduleChannelChangesResultDto>,
  footer: Footer
}

class ResultPage extends React.Component<ResultPageProps> {

  constructor(props) {
    super(props);
  }

  render() {
    const content = null;
    return <BootstrapPanel
      title={t("Channel Changes Actions")}
      icon="spacewalk-icon-software-channels"
      footer={this.props.footer}
      >
      <Table
        data={this.props.results}
        identifier={dto => dto.server.id}
        initialSortColumnKey="modified"
        initialSortDirection={-1}
        initialItemsPerPage={userPrefPageSize}
        >
        <Column
          columnKey="server"
          comparator={Functions.Utils.sortByText}
          header={t("System")}
          cell={ (dto: ScheduleChannelChangesResultDto) =>
            <SystemLink id={dto.server.id} newWindow={true}>{ dto.server.name }</SystemLink> }
        />
        <Column
          columnKey="status"
          comparator={Functions.Utils.sortByText}
          header={t("Status")}
          cell={ (dto: ScheduleChannelChangesResultDto) => {
              const actionId = dto.actionId;
              return actionId ?
                <span className="text-info"><ActionLink id={actionId} newWindow={true}>{t("Scheduled")}</ActionLink></span> :
                <span className="text-danger"><i className="fa fa-exclamation-triangle fa-1-5x" aria-hidden="true"></i>
                  {dto.errorMessage ? msgMap[dto.errorMessage] : t("Unknown error. Could not schedule channel change")}
                </span>
            }
          }
        />
      </Table>

    </BootstrapPanel>;
  }

}


type SsmAllowedBaseChannelsJson = {
  base: SsmChannelDto,
  allowedBaseChannels: Array<SsmChannelDto>,
  servers: Array<SsmServerDto>
}

type SsmBaseChannelChangesJson_Change = {
  oldBaseId: string,
  newBaseId: string
}

type SsmBaseChannelChangesJson = {
  changes: Array<SsmBaseChannelChangesJson_Change>
}


type SsmChannelProps = {

}

type SsmChannelState = {
  groupedChildChannels: Array<SsmAllowedChildChannelsDto>,
  allowedChanges: Array<SsmAllowedChildChannelsDto>,
  allowedBaseChannels: Array<SsmAllowedBaseChannelsJson>,
  messages: Array<Object>,
  baseChanges: SsmBaseChannelChangesJson,
  finalChanges: Array<ChannelChangeDto>,
  earliest: Date,
  actionChain: ?ActionChain,
  page: number,
  scheduleResults: Array<ScheduleChannelChangesResultDto>
}

type ChannelChangeDto = {
  oldBaseId: ?string,
  newBaseId: ?string,
  newBaseDefault: boolean,
  childChannelActions: Map<string, string>
}

type SsmScheduleChannelChangesJson = {
  earliest: string,
  changes: Array<ChannelChangeDto>
}

type ScheduleChannelChangesResultDto = {
  server: SsmServerDto,
  actionId: ?string,
  errorMessage: ?string
}

type SsmScheduleChannelChangesResultJson = {
  actionChainId: number,
  result: Array<ScheduleChannelChangesResultDto>
}

type FooterProps = {
  page: number,
  children?: React.Node
}

const Footer = (props: FooterProps) => <span>
    <div className="btn-group">
      { props.children }
    </div>
    <span style={ {marginLeft: "10px", verticalAlign: "middle"} }>{ props.page + 1 } {t("of")} 4</span>
  </span>;


class SsmChannelPage extends React.Component<SsmChannelProps, SsmChannelState> {

  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      allowedBaseChannels: [],
      groupedChildChannels: [],
      allowedChanges: [],
      baseChanges: {changes: []},
      finalChanges: [],
      page: 0,
      earliest: new Date(),
      actionChain: null,
      scheduleResults: []
    }
  }

  componentDidMount() {
    Network.get(`/rhn/manager/systems/ssm/channels/bases`)
      .promise.then((data : JsonResult<Array<SsmAllowedBaseChannelsJson>>) => {
        const baseChanges = this.setState({
            allowedBaseChannels: data.data,
            baseChanges: {
              changes: data.data.map(bc => {
                return {
                  oldBaseId: bc.base ? bc.base.id : "0",
                  newBaseId: "0"
                };
              })
            }
          });
      })
      .catch(this.handleResponseError);
  }

  handleResponseError = (jqXHR, arg = "") => {
    const msg = Network.responseErrorMessage(jqXHR,
      (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);

    // check if partially successful
    if (jqXHR.responseJSON.data) {
      const errsMsgs = jqXHR.responseJSON.data
        .filter(dto => dto.errorMessage)
        .map(dto => dto.errorMessage);
      const anySuccess = jqXHR.responseJSON.data
        .some(dto => dto.actionId && !dto.errorMessage);
      const msgs = anySuccess ?
        msg.concat(MessagesUtils.warning(t("Some changes scheduled successfully."))) :
        msg;
    }

    this.setState({ messages: this.state.messages.concat(msg) });
  }

  onChangeBase = (oldBaseId: string, newBaseId: string) => {
    let change = this.state.baseChanges.changes.find(e => e.oldBaseId == oldBaseId);
    if (!change) {
      change = {
        oldBaseId: oldBaseId,
        newBaseId: newBaseId
      }
      this.state.baseChanges.changes.push(change);
    } else {
      change.newBaseId = newBaseId;
    }

    this.setState({
      baseChanges: this.state.baseChanges
    });
  }

  onChangeChild = (allowedChange: SsmAllowedChildChannelsDto, childId: string, action: string) => {
    this.state.finalChanges
      // find allowed changes by new base channel
      .filter(ch => ch.newBaseId && allowedChange.newBaseChannel && ch.newBaseId == allowedChange.newBaseChannel.id)
      // set child action for each final change that matches the new base channel
      .forEach(ch => {
        ch.childChannelActions.set(childId, action);
      });
  }

  onGotoChildChannels = () => {
    return Network.post("/rhn/manager/systems/ssm/channels/allowed-changes",
        JSON.stringify(this.state.baseChanges), "application/json")
        .promise
        .then((data : JsonResult<Array<SsmAllowedChildChannelsDto>>) => {
          // group the allowed changes by the new base in order to show child channels only once
          const groupByNewBase : Map<string, SsmAllowedChildChannelsDto> = new Map();
          const finalChanges : Array<ChannelChangeDto> = [];
          data.data.forEach((e : SsmAllowedChildChannelsDto)=> {
            // sort child channels by name to have a consisten order in the UI
            e.childChannels.sort((a, b) => a.name.localeCompare(b.name));

            let newBaseId = !e.newBaseChannel ? "nonewbase" : e.newBaseChannel.id;

            let allowedChildren : ?SsmAllowedChildChannelsDto = groupByNewBase.get(newBaseId);
            if (!allowedChildren) {
              allowedChildren = {
                oldBaseChannel: e.oldBaseChannel,
                newBaseChannel: e.newBaseChannel,
                newBaseDefault: e.newBaseDefault,
                servers: e.servers,
                childChannels: e.childChannels,
                incompatibleServers: e.incompatibleServers
              }
            } else {
              allowedChildren.servers = allowedChildren.servers.concat(e.servers);
              allowedChildren.incompatibleServers = allowedChildren.incompatibleServers.concat(e.incompatibleServers);
            }

            groupByNewBase.set(newBaseId, allowedChildren);

            // create final changes to be scheduled
            finalChanges.push({
              oldBaseId: e.oldBaseChannel ? e.oldBaseChannel.id : null,
              newBaseId: e.newBaseChannel ? e.newBaseChannel.id : null,
              newBaseDefault: e.newBaseDefault,
              childChannelActions: e.childChannels.reduce((acc, cur) => acc.set(cur.id, "NO_CHANGE"), new Map())
            })
          });

          this.setState({
            allowedChanges: data.data,
            groupedChildChannels: Array.from(groupByNewBase.values()),
            finalChanges: finalChanges,
            page: 1
          });
      })
      .catch(this.handleResponseError);
  }

  backToBaseChannels = () => {
    this.setState({
      page: 0
    });
  }

  onGotoConfirm = () => {
    this.setState({
      page: 2
    });
  }

  backToChildChannels = () => {
    this.setState({
      page: 1
    });
  }

  onChangeEarliest = (earliest: Date) => {
    this.setState({
      earliest: earliest
    })
  }

  onChangeActionChain = (actionChain: ?ActionChain) => {
    this.setState({
      actionChain: actionChain
    })
  }

  onConfirm = () => {
    const req : SsmScheduleChannelChangesJson = {
      earliest: Functions.Formats.LocalDateTime(this.state.earliest),
      actionChain: this.state.actionChain ? this.state.actionChain.text : null,
      changes: this.state.finalChanges
    };
    return Network.post("/rhn/manager/systems/ssm/channels",
        JSON.stringify(req), "application/json")
        .promise
        .then((data : JsonResult<SsmScheduleChannelChangesResultJson>) => {
          const msg = MessagesUtils.info(this.state.actionChain ?
                  <span>{t("Action has been successfully added to the Action Chain ")}
                    <ActionChainLink id={data.data.actionChainId }>{this.state.actionChain ? this.state.actionChain.text : ""}</ActionChainLink>.</span> :
                  <span>{t("Channel changes scheduled.")}</span>);
          this.setState({
            messages: msg,
            scheduleResults: data.data.result,
            page: 3
          });
        })
        .catch(this.handleResponseError);
  }

  render() {
    let content;
    if (this.state.page == 0) {
      content = (
          <BaseChannelPage
            baseChannels={this.state.allowedBaseChannels}
            baseChanges={this.state.baseChanges}
            onSelectBase={this.onChangeBase}
            footer={
              <Footer page={this.state.page}>
                <AsyncButton id="next-btn"
                  defaultType="btn-success"
                  icon="fa-arrow-right"
                  disabled={this.state.allowedBaseChannels.length == 0}
                  text={t("Next")}
                  action={this.onGotoChildChannels}/>
              </Footer>
            }/>);
    } else if (this.state.page == 1) {
      content = (
        <ChildChannelPage
            childChannels={this.state.groupedChildChannels}
            childChanges={this.state.finalChanges}
            onChangeChild={this.onChangeChild}
            footer={
              <Footer page={this.state.page}>
                <Button id="btn-prev"
                  icon="fa-arrow-left"
                  className="btn-default"
                  text={t("Prev")}
                  handler={this.backToBaseChannels}/>
                <AsyncButton id="next-btn"
                  defaultType="btn-success"
                  icon="fa-arrow-right"
                  text={t("Next")}
                  action={this.onGotoConfirm}/>
              </Footer>
            }/>);
    } else if (this.state.page == 2) {
      content = (
        <SummaryPage
          allowedChanges={this.state.allowedChanges}
          finalChanges={this.state.finalChanges}
          onChangeEarliest={this.onChangeEarliest}
          onChangeActionChain={this.onChangeActionChain}
          ref={(component) => window.schedulePage = component}
          footer={
            <Footer page={this.state.page}>
              <Button id="btn-prev"
                icon="fa-arrow-left"
                className="btn-default"
                text={t("Prev")}
                handler={this.backToChildChannels}/>
              <AsyncButton id="confirm-btn"
                defaultType="btn-success"
                text={t("Confirm")}
                action={this.onConfirm}/>
            </Footer>
          }/>);
    } else if (this.state.page == 3) {
      content = (<ResultPage
        results={this.state.scheduleResults}
        footer={
          <Footer page={this.state.page}>
          </Footer>
        }
      />);
    }
    return (<div>
      <Messages items={this.state.messages}/>
      { content }
    </div>);
  }
}


export const renderer = (id) => SpaRenderer.renderNavigationReact(
  <SsmChannelPage ref={(component) => window.ssmChannelPage = component}/>,
    document.getElementById(id)
);
