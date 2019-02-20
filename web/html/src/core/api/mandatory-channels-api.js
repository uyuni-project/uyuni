/* eslint-disable */
// @flow
'use strict';

import React from 'react';
import Network from '../../utils/network';
import ChannelUtils from '../../utils/channels';

import type JsonResult from '../../utils/network';
import type {ChannelsDependencies} from '../../utils/channels';

const msgMap = {
    "base_not_found_or_not_authorized": t("Base channel not found or not authorized."),
    "child_not_found_or_not_authorized": t("Child channel not found or not authorized."),
    "invalid_channel_id": t("Invalid channel id")
};

type ChannelDto = {
  id: number,
  name: string,
  custom: boolean,
  subscribable: boolean,
  recommended: boolean
}

type ChildChannelsProps = {
  base: Object,
  channels: Array<ChannelDto>,
  children: Function,
}

type ChildChannelsState = {
    messages: Array<Object>,
    requiredChannels: Map<number, Set<number>>,
    requiredByChannels: Map<number, Set<number>>,
    mandatoryChannelsRaw: Object,
    dependencyDataAvailable: boolean,
}

class MandatoryChannelsApi extends React.Component<ChildChannelsProps, ChildChannelsState> {
    constructor(props: ChildChannelsProps) {
        super(props);

        this.state = {
            messages: [],
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
            let dependencies : ChannelsDependencies = ChannelUtils.processChannelDependencies(allTheNewMandatoryChannelsData);

            this.setState({
              mandatoryChannelsRaw: allTheNewMandatoryChannelsData,
              requiredChannels: dependencies.requiredChannels,
              requiredByChannels: dependencies.requiredByChannels,
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

    handleResponseError(jqXHR: Object, arg: string = '') {
        const msg = Network.responseErrorMessage(jqXHR,
            (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
        this.setState((prevState) => ({
                messages: prevState.messages.concat(msg)
            })
        );
    }

    dependenciesTooltip = (channelId: number) => {
        const resolveChannelNames : Function = (channelIds: Array<number>): Array<?string> => {
            return Array.from(channelIds || new Set())
                .map((channelId: number): ?ChannelDto  => this.props.channels.find(c => c.id == channelId))
                .filter((channel: ?ChannelDto): boolean => channel != null)
                .map((channel: ?ChannelDto): ?string => channel && channel.name);
        }
        return ChannelUtils.dependenciesTooltip(
            resolveChannelNames(this.state.requiredChannels.get(channelId)),
            resolveChannelNames(this.state.requiredByChannels.get(channelId)));
    }

    render() {
        return this.props.children({
            requiredChannels: this.state.requiredChannels,
            requiredByChannels: this.state.requiredByChannels,
            dependencyDataAvailable: this.state.dependencyDataAvailable,
            dependenciesTooltip: this.dependenciesTooltip,
            fetchMandatoryChannelsByChannelIds: this.fetchMandatoryChannelsByChannelIds,
        })
    }
}

export default MandatoryChannelsApi;
