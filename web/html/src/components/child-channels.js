// @flow
'use strict';

const React = require('react');
const Network = require('../utils/network');
const {Loading} = require('./loading');
const ChannelUtils = require('../utils/channels');
const {Toggler} = require("./toggler");
const {ChannelAnchorLink} = require("./links");

type ChildChannelsProps = {
  key: number,
  channels: Array,
  base: Object,
  showBase: Boolean,
  selectedChannelsIds: Array<number>,
  selectChannels: Function,
  saveState: Function,
  loadState: Function,
  collapsed: Boolean
}

type ChildChannelsState = {
  requiredChannels: Map<number, Array<number>>,
  requiredByChannels: Map<number, Array<number>>,
  mandatoryChannelsRaw: Map,
  dependencyDataAvailable: Boolean,
  collapsed: Boolean
}

class ChildChannels extends React.Component<ChildChannelsState, ChildChannelsProps> {
  constructor(props) {
    super(props);

    ['fetchMandatoryChannelsByChannelIds', 'handleChannelChange', 'dependenciesTooltip',
    'toggleRecommended', 'areRecommendedChildrenSelected', 'selectChannelWithDependencies', 'toggleChannelVisibility']
    .forEach(method => this[method] = this[method].bind(this));

    this.state = {
      requiredChannels: new Map(),
      requiredByChannels: new Map(),
      mandatoryChannelsRaw: new Map(),
      dependencyDataAvailable: false,
      collapsed: this.props.collapsed || true
    }
  }

  componentWillUnmount() {
    if (this.props.saveState) {
      this.props.saveState(this.state);
    }
  }

  componentWillMount() {
    if (this.props.loadState) {
      if (this.props.loadState()) {
        this.state = this.props.loadState();
      }
    }
    this.fetchMandatoryChannelsByChannelIds();
  }

  componentDidUpdate(prevProps, prevState) {
    if (prevState.collapsed != this.state.collapsed && !this.state.collapsed) {
      this.fetchMandatoryChannelsByChannelIds();
    }
  }

  fetchMandatoryChannelsByChannelIds() {
    if(!this.state.collapsed) {
      // fetch dependencies data for all child channels and base channel as well
      const needDepsInfoChannels = this.props.base && this.props.base.id != -1 ?
        [this.props.base.id, ...this.props.channels.map(c => c.id)]
        : this.props.channels.map(c => c.id);

      const mandatoryChannelsNotCached = needDepsInfoChannels.filter((channelId) => !this.state.mandatoryChannelsRaw[channelId]);
      if(mandatoryChannelsNotCached.length > 0) {
        Network.post('/rhn/manager/api/admin/mandatoryChannels', JSON.stringify(mandatoryChannelsNotCached), "application/json").promise
          .then((data : JsonResult<Map<number, Array<number>>>) => {
            const allTheNewMandatoryChannelsData = Object.assign({}, this.state.mandatoryChannelsRaw, data.data);
            let {requiredChannels, requiredByChannels} = ChannelUtils.processChannelDependencies(allTheNewMandatoryChannelsData);

            this.setState({
              mandatoryChannelsRaw: allTheNewMandatoryChannelsData,
              requiredChannels,
              requiredByChannels,
              dependencyDataAvailable: true,
            });
          })
          .catch(this.handleResponseError);
      } else {
        this.setState({
          dependencyDataAvailable: true,
        })
      }
    }
  }

  handleResponseError(jqXHR, arg = '') {
    const msg = Network.responseErrorMessage(jqXHR,
      (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
    this.setState((prevState) => ({
        messages: prevState.messages.concat(msg)
      })
    );
  }

  handleChannelChange(event: SyntheticInputEvent<*>) {
    const channelId = parseInt(event.target.value);
    const selectedFlag = event.target.checked;
    const channelIds: Array<number> = this.selectChannelWithDependencies(channelId, selectedFlag);

    this.props.selectChannels(channelIds, selectedFlag);
  }

  selectChannelWithDependencies(channelId: number, select: Boolean) {
    let dependingChannelIds;
    if (select) {
      dependingChannelIds = this.state.requiredChannels.get(channelId) || [];
    }
    else { // unselect
      dependingChannelIds = this.state.requiredByChannels.get(channelId) || [];
    }
    return dependingChannelIds ? [channelId, ...Array.from(dependingChannelIds).filter(c => c !== channelId)] : [channelId];
  }

  dependenciesTooltip = (channelId: number) => {
    const resolveChannelNames = (channelIds: Array<number>) => {
      return Array.from(channelIds || new Set())
        .map(channelId => this.props.channels.find(c => c.id == channelId))
        .filter(channel => channel != null)
        .map(channel => channel.name);
    }
    return ChannelUtils.dependenciesTooltip(
      resolveChannelNames(this.state.requiredChannels.get(channelId)),
      resolveChannelNames(this.state.requiredByChannels.get(channelId)));
  }

  toggleRecommended = () => {
    if (this.areRecommendedChildrenSelected()) {
      this.props.selectChannels(
        this.props.channels
          .filter(channel => channel.recommended)
          .map(channel => channel.id),
        false);
    }
    else {
      this.props.selectChannels(
        this.props.channels
          .filter(channel => channel.recommended && !this.props.selectedChannelsIds.includes(channel.id))
          .map(channel => channel.id),
        true);
    }
  }

  areRecommendedChildrenSelected = () : Boolean => {
    const recommendedChildren = this.props.channels.filter(channel => channel.recommended);
    const selectedRecommendedChildren = recommendedChildren.filter(channel => this.props.selectedChannelsIds.includes(channel.id));
    const unselectedRecommendedChildren = recommendedChildren.filter(channel => !this.props.selectedChannelsIds.includes(channel.id));

    return selectedRecommendedChildren.length > 0 && unselectedRecommendedChildren.length == 0;
  }

  toggleChannelVisibility() {
    this.setState({collapsed: !this.state.collapsed});
  }

  render() {
    let channels;
    if(!this.state.dependencyDataAvailable) {
      channels = <Loading text='Loading dependencies..' />;
    }
    else {
      if (this.props.channels.length == 0) {
        channels = <span>&nbsp;{t('no child channels')}</span>;
      }
      else {
        channels =
          this.props.channels.map(c => {
              const toolTip = this.dependenciesTooltip(c.id);
              const isMandatory =
                  this.props.base &&
                  this.state.requiredChannels.has(this.props.base.id) &&
                  this.state.requiredChannels.get(this.props.base.id).has(c.id);
              const isDisabled = isMandatory && this.props.selectedChannelsIds.includes(c.id);
              return (
                <div key={c.id} className='checkbox'>
                  <input type='checkbox'
                      value={c.id}
                      id={'child_' + c.id}
                      name='childChannels'
                      checked={this.props.selectedChannelsIds.includes(c.id)}
                      disabled={isDisabled}
                      onChange={this.handleChannelChange}
                  />
                  {
                    // add an hidden carbon-copy of the disabled input since the disabled one will not be included in the form submit
                    isDisabled ?
                      <input type='checkbox' value={c.id} name='childChannels'
                          hidden='hidden' checked={this.props.selectedChannelsIds.includes(c.id)} readOnly={true}/>
                      : null
                  }
                  <label title={toolTip} htmlFor={"child_" + c.id}>{c.name}</label>
                  &nbsp;
                  {
                    toolTip ?
                      <a href="#"><i className="fa fa-info-circle spacewalk-help-link" title={toolTip}></i></a>
                      : null
                  }
                  &nbsp;
                  {
                    c.recommended ?
                      <span className='recommended-tag-base' title={'This channel is recommended'}>{t('recommended')}</span>
                      : null
                  }
                  {
                    isMandatory ?
                      <span className='mandatory-tag-base' title={'This channel is mandatory'}>{t('mandatory')}</span>
                      : null
                  }
                  <ChannelAnchorLink id={c.id} newWindow={true}/>
                </div>
              )
          })
      }
    }

    return (
      <div className='child-channels-block'>
        <h4 className='pointer' onClick={this.toggleChannelVisibility}>
          <i className={'fa ' + (this.state.collapsed ? 'fa-angle-right' : 'fa-angle-down')} />
          {this.props.base.name}
        </h4>
        {/* keep the block hidden but in the DOM to let the form submit collects checkboxes */}
        <div className={this.state.collapsed ? 'hide' : 'col-lg-12'}>
          {
            this.props.channels.some(channel => channel.recommended) ?
              <Toggler
                  handler={this.toggleRecommended}
                  value={this.areRecommendedChildrenSelected()}
                  text={t("include recommended")}
              />
            : null
          }
          {channels}
          <hr/>
        </div>
      </div>
    );
  }
}

module.exports = {
  ChildChannels: ChildChannels
}