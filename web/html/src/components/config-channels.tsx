import * as React from "react";

import { StatesPicker } from "components/states-picker";

import { Messages, MessageType } from "./messages/messages";

type ConfigChannelsProps = {
  matchUrl: (filter?: string) => any;
  applyRequest: (component: ConfigChannels) => any;
  saveRequest: (channels: any[]) => any;
};

class ConfigChannelsState {
  messages: MessageType[] | null = null;
}

class ConfigChannels extends React.Component<ConfigChannelsProps, ConfigChannelsState> {
  state = new ConfigChannelsState();

  setMessages = (messages) => {
    this.setState({
      messages: messages,
    });
  };

  applyRequest = () => {
    this.props.applyRequest(this);
  };

  render() {
    const messages = this.state.messages ? <Messages items={this.state.messages} /> : null;

    return (
      <span>
        {messages}
        <h2>
          <i className={"fa spacewalk-icon-salt-add"} />
          {t("Configuration Channels")}
          &nbsp;
        </h2>
        <StatesPicker
          matchUrl={this.props.matchUrl}
          saveRequest={this.props.saveRequest}
          applyRequest={this.applyRequest}
          messages={this.setMessages}
        />
      </span>
    );
  }
}

export { ConfigChannels };
