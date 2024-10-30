import { useState } from "react";

import { ActionChain } from "components/action-schedule";
import { Messages, MessageType, Utils as MessageUtils } from "components/messages/messages";

import { ChannelAppStream } from "./appstreams.type";
import { AppStreamsChangesConfirm } from "./changes-confirm-appstreams";
import { AppStreamsList } from "./list-appstreams";
import { getStreamName, handleModuleEnableDisable } from "./utils";

type Props = {
  channelsAppStreams: Array<ChannelAppStream>;
};

const AppStreams = ({ channelsAppStreams }: Props) => {
  const [appStreams, setAppStreams] = useState<ChannelAppStream[]>(channelsAppStreams);
  const [toEnable, setToEnable] = useState<Map<number, Array<string>>>(new Map());
  const [toDisable, setToDisable] = useState<Map<number, Array<string>>>(new Map());
  const [showConfirm, setShowConfirm] = useState<boolean>(false);
  const [scheduledMsg, setScheduledMsg] = useState<MessageType[]>([]);

  const handleEnableDisable = (channel, appStream) =>
    handleModuleEnableDisable(channel, appStream, appStreams, toEnable, toDisable, setToEnable, setToDisable);

  /**
   * After scheduling the action, apply the changes optimistically to the currently displayed list
   */
  const applyChanges = () => {
    const updatedAppStreams = appStreams.map((ch) => ({
      ...ch,
      appStreams: {
        ...ch.appStreams,
        // Map over each stream in the channel and update its status if it's in toEnable/toDisable lists
        ...Object.keys(ch.appStreams).reduce((acc, key) => {
          acc[key] = ch.appStreams[key].map((as) => ({
            ...as,
            enabled:
              toEnable.get(ch.channel.id)?.includes(getStreamName(as)) ||
              (!toDisable.get(ch.channel.id)?.includes(getStreamName(as)) && as.enabled),
          }));
          return acc;
        }, {}),
      },
    }));

    handleReset();
    setAppStreams(updatedAppStreams);
  };

  const handleSubmitChanges = () => {
    setShowConfirm(true);
  };

  const handleConfirmChanges = (id: number, actionChain?: ActionChain) => {
    setShowConfirm(false);
    applyChanges();

    const msg = MessageUtils.info(
      actionChain ? (
        <span>
          {t('Action has been successfully added to the action chain <link>"{name}"</link>.', {
            name: actionChain.text,
            link: (str) => <a href={`/rhn/schedule/ActionChain.do?id=${id}`}>{str}</a>,
          })}
        </span>
      ) : (
        <span>
          {t("Updating the selection of AppStream modules has been <link>scheduled</link>.", {
            link: (str) => <a href={`/rhn/schedule/ActionDetails.do?aid=${id}`}>{str}</a>,
          })}
        </span>
      )
    );
    setScheduledMsg([...msg]);
  };

  const handleConfirmError = (msg: MessageType[]) => {
    setScheduledMsg([...msg]);
  };

  const handleReset = () => {
    setToEnable(new Map());
    setToDisable(new Map());
  };

  const showContent = () => {
    if (showConfirm) {
      return (
        <AppStreamsChangesConfirm
          toEnable={[...toEnable.values()].flat()}
          toDisable={[...toDisable.values()].flat()}
          onCancelClick={() => setShowConfirm(false)}
          onConfirm={handleConfirmChanges}
          onError={handleConfirmError}
        />
      );
    } else {
      return (
        <AppStreamsList
          toEnable={toEnable}
          toDisable={toDisable}
          onReset={handleReset}
          onModuleEnableDisable={handleEnableDisable}
          onSubmitChanges={handleSubmitChanges}
          channelsAppStreams={appStreams}
        />
      );
    }
  };

  return (
    <>
      {scheduledMsg.length > 0 && <Messages items={scheduledMsg} />}
      <h2>
        <i className={"fa spacewalk-icon-salt-add"} />
        {t("AppStreams")}
        &nbsp;
      </h2>

      {showContent()}
    </>
  );
};

export default AppStreams;
