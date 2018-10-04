// @flow
'use strict';

import MandatoryChannelsApi from "../../core/api/mandatory-channels-api";

const React = require('react');
const Network = require('../../utils/network');
const {Loading} = require('../../components/loading/loading');
const ChannelUtils = require('../../utils/channels');
const {Toggler} = require("../../components/toggler");
const {ChannelAnchorLink} = require("../../components/links");

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
  collapsed: Boolean
}

class ChildChannels extends React.Component<ChildChannelsProps, ChildChannelsState> {
  constructor(props) {
    super(props);

    this.state = {
      collapsed: this.props.collapsed
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
  }

  handleChannelChange = (event: SyntheticInputEvent<*>, requiredChannels, requiredByChannels) => {
    const channelId = parseInt(event.target.value);
    const selectedFlag = event.target.checked;
    const channelIds: Array<number> = this.selectChannelWithDependencies(channelId, selectedFlag, requiredChannels, requiredByChannels);

    this.props.selectChannels(channelIds, selectedFlag);
  }

  selectChannelWithDependencies = (channelId: number, select: Boolean, requiredChannels, requiredByChannels) => {
    let dependingChannelIds;
    if (select) {
      dependingChannelIds = requiredChannels.get(channelId) || [];
    }
    else { // unselect
      dependingChannelIds = requiredByChannels.get(channelId) || [];
    }
    return dependingChannelIds ? [channelId, ...Array.from(dependingChannelIds).filter(c => c !== channelId)] : [channelId];
  }

  toggleRecommended = (areRecommendedChildrenSelected) => {
    if (areRecommendedChildrenSelected()) {
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
  };

  toggleChannelVisibility = ({onShow})  => {
    const prevState = this.state; //TODO: [LN] review this. Which parameters are passed on the second callback
    this.setState(
        {collapsed: !this.state.collapsed},
        () => {
            if (prevState.collapsed != this.state.collapsed && !this.state.collapsed) {
                onShow();
            }
        }
    );
  };

  renderChannels = ({
                      requiredChannels,
                      requiredByChannels,
                      mandatoryChannelsRaw,
                      dependencyDataAvailable,
                      dependenciesTooltip
                    }) => {
    if(!dependencyDataAvailable) {
      return <Loading text='Loading dependencies..' />;
    }
    else {
      if (this.props.channels.length == 0) {
        return <span>&nbsp;{t('no child channels')}</span>;
      }
      else {
        return this.props.channels.map(c => {
          const toolTip = dependenciesTooltip(c.id);
          const isMandatory =
            this.props.base &&
            requiredChannels.has(this.props.base.id) &&
            requiredChannels.get(this.props.base.id).has(c.id);
          const isDisabled = isMandatory && this.props.selectedChannelsIds.includes(c.id);
          return (
            <div key={c.id} className='checkbox'>
              <input type='checkbox'
                     value={c.id}
                     id={'child_' + c.id}
                     name='childChannels'
                     checked={this.props.selectedChannelsIds.includes(c.id)}
                     disabled={isDisabled}
                     onChange={(event) => this.handleChannelChange(event, requiredChannels, requiredByChannels)}
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
  };

  render() {

    return (
      <MandatoryChannelsApi base={this.props.base} channels={this.props.channels} selectedChannelsIds={this.props.selectedChannelsIds}>
        {({
            requiredChannels,
            requiredByChannels,
            mandatoryChannelsRaw,
            dependencyDataAvailable,
            areRecommendedChildrenSelected,
            dependenciesTooltip,
            fetchMandatoryChannelsByChannelIds
          }) => (
          <div className='child-channels-block'>
            <h4 className='pointer' onClick={() => this.toggleChannelVisibility({onShow: fetchMandatoryChannelsByChannelIds})}>
              <i className={'fa ' + (this.state.collapsed ? 'fa-angle-right' : 'fa-angle-down')} />
              {this.props.base.name}
            </h4>
            {/* keep the block hidden but in the DOM to let the form submit collects checkboxes */}
            <div className={this.state.collapsed ? 'hide' : 'col-lg-12'}>
              {
                this.props.channels.some(channel => channel.recommended) ?
                  <Toggler
                    handler={() => this.toggleRecommended(areRecommendedChildrenSelected)}
                    value={areRecommendedChildrenSelected()}
                    text={t("include recommended")}
                  />
                  : null
              }
              {this.renderChannels({
                requiredChannels,
                requiredByChannels,
                mandatoryChannelsRaw,
                dependencyDataAvailable,
                dependenciesTooltip
              })}
              <hr/>
            </div>
          </div>
        )}
      </MandatoryChannelsApi>
    );
  }
}

module.exports = {
  ChildChannels: ChildChannels
}
