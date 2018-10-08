// @flow
'use strict';

import MandatoryChannelsApi from "../../core/api/mandatory-channels-api";

import React from 'react';
import {Loading} from '../../components/loading/loading';
import {Toggler} from "../../components/toggler";
import {ChannelAnchorLink} from "../../components/links";

type ChildChannelsProps = {
  key: number,
  channels: Array,
  base: Object,
  showBase: Boolean,
  selectedChannelsIds: Array<number>,
  selectChannels: Function,
  requiredChannels: Map<number, Array<number>>,
  requiredByChannels: Map<number, Array<number>>,
  dependencyDataAvailable: boolean,
  areRecommendedChildrenSelected: Function,
  dependenciesTooltip: Function,
  fetchMandatoryChannelsByChannelIds: Function,
  collapsed: boolean
}

type ChildChannelsState = {
  collapsed: boolean
}


class ChildChannels extends React.Component<ChildChannelsProps, ChildChannelsState> {
  constructor(props) {
    super(props);

    this.state = {
      collapsed: this.props.collapsed
    }
  }

  componentDidMount = () => {
    !this.state.collapsed && this.props.fetchMandatoryChannelsByChannelIds()
  }

  handleChannelChange = (event: SyntheticInputEvent<*>) => {
    const channelId = parseInt(event.target.value);
    const selectedFlag = event.target.checked;
    const channelIds: Array<number> = this.selectChannelWithDependencies(channelId, selectedFlag);

    this.props.selectChannels(channelIds, selectedFlag);
  }

  selectChannelWithDependencies = (channelId: number, select: boolean) => {
    let dependingChannelIds;
    if (select) {
      dependingChannelIds = this.props.requiredChannels.get(channelId) || [];
    }
    else { // unselect
      dependingChannelIds = this.props.requiredByChannels.get(channelId) || [];
    }
    return dependingChannelIds ? [channelId, ...Array.from(dependingChannelIds).filter(c => c !== channelId)] : [channelId];
  }

  toggleRecommended = (areRecommendedChildrenSelected: Function) => {
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

  toggleChannelVisibility = ({onShow}: {onShow: Function})  => {
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

  renderChannels = () => {
    if(!this.props.dependencyDataAvailable) {
      return <Loading text='Loading dependencies..' />;
    }
    else {
      if (this.props.channels.length == 0) {
        return <span>&nbsp;{t('no child channels')}</span>;
      }
      else {
        return this.props.channels.map(c => {
          const toolTip = this.props.dependenciesTooltip(c.id);
          const isMandatory =
            this.props.base &&
            this.props.requiredChannels.has(this.props.base.id) &&
            this.props.requiredChannels.get(this.props.base.id).has(c.id);
          const isDisabled = isMandatory && this.props.selectedChannelsIds.includes(c.id);
          return (
            <div key={c.id} className='checkbox'>
              <input type='checkbox'
                     value={c.id}
                     id={'child_' + c.id}
                     name='childChannels'
                     checked={this.props.selectedChannelsIds.includes(c.id)}
                     disabled={isDisabled}
                     onChange={(event) => this.handleChannelChange(event)}
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
          <div className='child-channels-block'>
            <h4 className='pointer' onClick={() => this.toggleChannelVisibility({onShow: this.props.fetchMandatoryChannelsByChannelIds})}>
              <i className={'fa ' + (this.state.collapsed ? 'fa-angle-right' : 'fa-angle-down')} />
              {this.props.base.name}
            </h4>
            {/* keep the block hidden but in the DOM to let the form submit collects checkboxes */}
            <div className={this.state.collapsed ? 'hide' : 'col-lg-12'}>
              {
                this.props.channels.some(channel => channel.recommended) ?
                  <Toggler
                    handler={() => this.toggleRecommended(this.props.areRecommendedChildrenSelected)}
                    value={this.props.areRecommendedChildrenSelected()}
                    text={t("include recommended")}
                  />
                  : null
              }
              {this.renderChannels()}
              <hr/>
            </div>
          </div>
    );
  }
}

export default ChildChannels;
