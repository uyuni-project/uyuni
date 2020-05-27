/* eslint-disable */
// @flow
'use strict';

import React from 'react';
import {Loading} from 'components/utils/Loading';
import {Toggler} from "components/toggler";
import {ChannelAnchorLink} from "components/links";

import type {Node} from "react";
import type {ChannelDto} from "./activation-key-channels-api";
import type {RequiredChannelsResultType} from "core/channels/api/use-mandatory-channels-api";


type ChildChannelsProps = {
  channels: Array<ChannelDto>,
  base: Object,
  showBase: boolean,
  selectedChannelsIds: Array<number>,
  selectChannels: Function,
  isDependencyDataLoaded: boolean,
  requiredChannelsResult: RequiredChannelsResultType,
  fetchMandatoryChannelsByChannelIds: Function,
  collapsed: boolean
}

type ChildChannelsState = {
  collapsed: boolean
}


class ChildChannels extends React.Component<ChildChannelsProps, ChildChannelsState> {
  constructor(props: ChildChannelsProps) {
    super(props);

    this.state = {
      collapsed: this.props.collapsed
    }
  }

  componentDidMount = () => {
    this.props.fetchMandatoryChannelsByChannelIds({base: this.props.base, channels: this.props.channels});
  };

  handleChannelChange = (event: SyntheticInputEvent<*>) => {
    const channelId = parseInt(event.target.value);
    const selectedFlag = event.target.checked;
    const channelIds: Array<number> = this.selectChannelWithDependencies(channelId, selectedFlag);

    this.props.selectChannels(channelIds, selectedFlag);
  };

  selectChannelWithDependencies = (channelId: number, select: boolean) => {
    let dependingChannelIds;
    if (select) {
      dependingChannelIds = this.props.requiredChannelsResult.requiredChannels.get(channelId) || [];
    }
    else { // unselect
      dependingChannelIds = this.props.requiredChannelsResult.requiredByChannels.get(channelId) || [];
    }
    return dependingChannelIds ? [channelId, ...Array.from(dependingChannelIds).filter(c => c !== channelId)] : [channelId];
  };

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
  };

  toggleChannelVisibility = () => {
    this.setState({collapsed: !this.state.collapsed});
  };

  areRecommendedChildrenSelected = () : boolean => {
    const recommendedChildren = this.props.channels.filter(channel => channel.recommended);
    const selectedRecommendedChildren = recommendedChildren.filter(channel => this.props.selectedChannelsIds.includes(channel.id));
    const unselectedRecommendedChildren = recommendedChildren.filter(channel => !this.props.selectedChannelsIds.includes(channel.id));

    return selectedRecommendedChildren.length > 0 && unselectedRecommendedChildren.length == 0;
  };

  renderChannels = (): Node => {
    if(!this.props.isDependencyDataLoaded) {
      return <Loading text='Loading dependencies..' />;
    }
    else {
      if (this.props.channels.length == 0) {
        return <span>&nbsp;{t('no child channels')}</span>;
      }
      else {
        return this.props.channels.map(c => {
          const toolTip = this.props.requiredChannelsResult.dependenciesTooltip(c.id, this.props.channels);
          const mandatoryChannelsForBaseId: ?Set<number> = this.props.base && this.props.requiredChannelsResult.requiredChannels.get(this.props.base.id);

          const isMandatory = mandatoryChannelsForBaseId && mandatoryChannelsForBaseId.has(c.id);
          const isDisabled = isMandatory;
          return (
            <div key={c.id} className='checkbox'>
              <input type='checkbox'
                     value={c.id}
                     id={'child_' + c.id}
                     name='childChannels'
                     checked={isMandatory || this.props.selectedChannelsIds.includes(c.id)}
                     disabled={isDisabled}
                     onChange={(event) => this.handleChannelChange(event)}
              />
              {
                // add an hidden carbon-copy of the disabled input since the disabled one will not be included in the form submit
                isDisabled ?
                  <input type='checkbox' value={c.id} name='childChannels'
                         hidden='hidden' checked={isMandatory || this.props.selectedChannelsIds.includes(c.id)} readOnly={true}/>
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
        <h4 className='pointer' onClick={() => this.toggleChannelVisibility()}>
          <i className={'fa ' + (this.state.collapsed ? 'fa-angle-right' : 'fa-angle-down')} />
          {this.props.base.name}
        </h4>
        {/* keep the block hidden but in the DOM to let the form submit collects checkboxes */}
        <div className={this.state.collapsed ? 'hide' : 'col-lg-12'}>
          {
            this.props.channels.some(channel => channel.recommended) ?
              <Toggler
                handler={() => this.toggleRecommended()}
                value={this.areRecommendedChildrenSelected()}
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
