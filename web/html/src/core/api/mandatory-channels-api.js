// @flow
'use strict';

import React from 'react';
import Network from '../../utils/network';
import ChannelUtils from '../../utils/channels';

type ChildChannelsProps = {
  base: Object,
  channels: Array,
  selectedChannelsIds: Array<number>,
  children: Function,
}

type ChildChannelsState = {
    requiredChannels: Map<number, Array<number>>,
    requiredByChannels: Map<number, Array<number>>,
    mandatoryChannelsRaw: Map,
    dependencyDataAvailable: boolean,
}

class MandatoryChannelsApi extends React.Component<ChildChannelsProps, ChildChannelsState> {
    constructor(props) {
        super(props);

        this.state = {
            requiredChannels: new Map(),
            requiredByChannels: new Map(),
            mandatoryChannelsRaw: new Map(),
            dependencyDataAvailable: false,
        }
    }

    fetchMandatoryChannelsByChannelIds = () => {
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

    handleResponseError(jqXHR, arg = '') {
        const msg = Network.responseErrorMessage(jqXHR,
            (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
        this.setState((prevState) => ({
                messages: prevState.messages.concat(msg)
            })
        );
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

    areRecommendedChildrenSelected = () : Boolean => {
        const recommendedChildren = this.props.channels.filter(channel => channel.recommended);
        const selectedRecommendedChildren = recommendedChildren.filter(channel => this.props.selectedChannelsIds.includes(channel.id));
        const unselectedRecommendedChildren = recommendedChildren.filter(channel => !this.props.selectedChannelsIds.includes(channel.id));

        return selectedRecommendedChildren.length > 0 && unselectedRecommendedChildren.length == 0;
    }

    render() {
        return this.props.children({
            requiredChannels: this.state.requiredChannels,
            requiredByChannels: this.state.requiredByChannels,
            dependencyDataAvailable: this.state.dependencyDataAvailable,
            areRecommendedChildrenSelected: this.areRecommendedChildrenSelected,
            dependenciesTooltip: this.dependenciesTooltip,
            fetchMandatoryChannelsByChannelIds: this.fetchMandatoryChannelsByChannelIds,
        })
    }
}

export default MandatoryChannelsApi;
