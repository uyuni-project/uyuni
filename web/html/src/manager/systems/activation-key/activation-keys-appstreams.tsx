import { useState } from "react";

import { AppStreamActions } from "manager/appstreams/actions-appstreams";
import { AppStreamPanel } from "manager/appstreams/panel-appstream";
import { handleModuleEnableDisable, numberOfChanges } from "manager/appstreams/utils";

import { Messages, MessageType, Utils as MessageUtils } from "components/messages/messages";

import Network from "utils/network";

import { ChannelAppStream } from "../../appstreams/appstreams.type";

declare global {
  interface Window {
    activationKeyId: number;
  }
}

type Props = {
  channelsAppStreams: Array<ChannelAppStream>;
};

const AppStreams = (props: Props) => {
  const [channelsAppStreams, setChannelAppStreams] = useState(props.channelsAppStreams);
  const [toEnable, setToEnable] = useState<Map<number, Array<string>>>(new Map());
  const [toDisable, setToDisable] = useState<Map<number, Array<string>>>(new Map());
  const [statusMessage, setStatusMessage] = useState<MessageType[]>([]);
  const handleEnableDisable = (channel, appStream) =>
    handleModuleEnableDisable(channel, appStream, channelsAppStreams, toEnable, toDisable, setToEnable, setToDisable);

  const handleReset = () => {
    setToEnable(new Map());
    setToDisable(new Map());
  };

  const handleError = (error) => {
    setStatusMessage(MessageUtils.error(t("Error updating activation key.")));
    Loggerhead.error(error);
  };

  const handleConfirm = (data) => {
    setStatusMessage(MessageUtils.info(t("Activation key has been modified.")));
    handleReset();
    setChannelAppStreams(data);
  };

  const handleSubmit = () => {
    const channelsKeys = new Set([...toEnable.keys(), ...toDisable.keys()]);
    const changes = new Map();
    channelsKeys.forEach((channelKey) => {
      changes.set(channelKey, {
        toInclude: toEnable.get(channelKey) ?? [],
        toRemove: toDisable.get(channelKey) ?? [],
      });
    });
    const request = Network.post(`/rhn/manager/api/activationkeys/appstreams/save?tid=${window.activationKeyId}`, {
      changes,
    })
      .then((data) => {
        handleConfirm(data.data);
      })
      .catch((jqXHR) => handleError(Network.responseErrorMessage(jqXHR)));

    return request;
  };

  const changes = numberOfChanges(toEnable, toDisable);

  return (
    <>
      {statusMessage.length > 0 && <Messages items={statusMessage} />}
      <h2>
        <i className={"fa spacewalk-icon-salt-add"} />
        {t("AppStreams")}
      </h2>
      <AppStreamActions numberOfChanges={changes} onReset={handleReset} onSubmit={handleSubmit} />
      {channelsAppStreams.map((channelAppStreams) => {
        const { channel, appStreams } = channelAppStreams;
        return (
          <AppStreamPanel
            key={channel.id}
            channel={channel}
            appStreams={appStreams}
            toEnable={toEnable}
            toDisable={toDisable}
            onToggle={(appStream) => handleEnableDisable(channel, appStream)}
          />
        );
      })}
    </>
  );
};

export default AppStreams;
