import ChildChannels from "./child-channels";
import ActivationKeyChannelsApi from "./activation-key-channels-api";
import * as React from "react";
import { Loading } from "components/utils/Loading";
import { Messages } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import MandatoryChannelsApi from "core/channels/api/mandatory-channels-api";
import { availableChannelsType, ChannelDto } from "./activation-key-channels-api";

type ActivationKeyChannelsProps = {
  activationKeyId: number;
};

type ActivationKeyChannelsState = {
  currentSelectedBaseId: number;
  currentChildSelectedIds: Array<number>;
};

class ActivationKeyChannels extends React.Component<ActivationKeyChannelsProps, ActivationKeyChannelsState> {
  constructor(props: ActivationKeyChannelsProps) {
    super(props);
    this.state = {
      currentSelectedBaseId: -1,
      currentChildSelectedIds: [],
    };
  }

  getDefaultBase(): ChannelDto {
    return { id: -1, name: t("SUSE Manager Default"), custom: false, subscribable: true, recommended: false };
  }

  handleBaseChange = (event: React.ChangeEvent<HTMLSelectElement>): Promise<number> => {
    const newBaseId: number = parseInt(event.target.value, 10);
    return new Promise((resolve: Function) =>
      this.setState({ currentSelectedBaseId: newBaseId }, () => resolve(newBaseId))
    );
  };

  selectChildChannels = (channelIds: Array<number>, selectedFlag: boolean) => {
    var selectedIds = [...this.state.currentChildSelectedIds];
    if (selectedFlag) {
      selectedIds = [...channelIds.filter((c) => !selectedIds.includes(c)), ...selectedIds];
    } else {
      selectedIds = [...selectedIds.filter((c) => !channelIds.includes(c))];
    }
    this.setState({ currentChildSelectedIds: selectedIds });
  };

  onNewBaseChannel = ({ currentSelectedBaseId, currentChildSelectedIds }: ActivationKeyChannelsState) => {
    this.setState({ currentSelectedBaseId, currentChildSelectedIds });
  };

  renderChildChannels = (loadingChildren: boolean, availableChannels: availableChannelsType) => {
    return loadingChildren ? (
      <Loading text={t("Loading child channels..")} />
    ) : (
      availableChannels.map((g) => {
        const base = g.base;
        const channels = g.children.sort((c1, c2) => c1.name.localeCompare(c2.name));

        return (
          <MandatoryChannelsApi>
            {({ requiredChannelsResult, isDependencyDataLoaded, fetchMandatoryChannelsByChannelIds }) => (
              <ChildChannels
                key={base ? base.id : "no-base"}
                channels={channels}
                base={base ? base : {}}
                showBase={availableChannels.length > 1}
                selectedChannelsIds={this.state.currentChildSelectedIds}
                selectChannels={this.selectChildChannels}
                isDependencyDataLoaded={isDependencyDataLoaded}
                requiredChannelsResult={requiredChannelsResult}
                fetchMandatoryChannelsByChannelIds={fetchMandatoryChannelsByChannelIds}
                collapsed={Array.from(availableChannels.keys()).length > 1}
              />
            )}
          </MandatoryChannelsApi>
        );
      })
    );
  };

  render() {
    const defaultChannelName = this.getDefaultBase().name;
    return (
      <ActivationKeyChannelsApi
        onNewBaseChannel={this.onNewBaseChannel}
        defaultBaseId={this.getDefaultBase().id}
        activationKeyId={this.props.activationKeyId}
        currentSelectedBaseId={this.state.currentSelectedBaseId}
      >
        {({ messages, loading, loadingChildren, availableBaseChannels, availableChannels, fetchChildChannels }) => {
          if (loading) {
            return (
              <div className="form-group">
                <Loading text={t("Loading..")} />
              </div>
            );
          }

          return (
            <div>
              <div className="form-group">
                <label className="col-lg-3 control-label">{t("Base Channel:")}</label>
                <div className="col-lg-6">
                  <select
                    name="selectedBaseChannel"
                    className="form-control"
                    value={this.state.currentSelectedBaseId}
                    onChange={(event) =>
                      this.handleBaseChange(event).then((newBaseId) => fetchChildChannels(newBaseId))
                    }
                  >
                    <option value={this.getDefaultBase().id}>{this.getDefaultBase().name}</option>
                    {availableBaseChannels
                      .sort((b1, b2) => (b1.name || "").localeCompare(b2.name || ""))
                      .map((b) => (
                        <option key={b.id} value={b.id}>
                          {b.name}
                        </option>
                      ))}
                  </select>
                  <span className="help-block">
                    {t(
                      `Selecting the "${defaultChannelName}" base channel enables a system to register to the ` +
                        "correct channel that corresponds to the installed operating system. You can also select " +
                        "SUSE provided channels, or use custom base channels but if a system using such a channel " +
                        `is not compatible then the fall back will be the "${defaultChannelName}" channel.`
                    )}
                  </span>
                  <Messages
                    items={MessagesUtils.warning(
                      t(
                        `When "${this.getDefaultBase().name}" is selected and the installed ` +
                          "product is not detected, no channel will be added even if children " +
                          "channels are selected."
                      )
                    )}
                  />
                </div>
              </div>
              <div className="form-group">
                <label className="col-lg-3 control-label">{t("Child Channels:")}</label>
                <div className="col-lg-6">
                  {this.renderChildChannels(loadingChildren, availableChannels)}
                  <span className="help-block">
                    {t(
                      "Any system registered using this activation key will be subscribed to the selected child channels."
                    )}
                  </span>
                </div>
              </div>
            </div>
          );
        }}
      </ActivationKeyChannelsApi>
    );
  }
}

export default ActivationKeyChannels;
