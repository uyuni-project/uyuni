import { useState } from "react";

import { ActionChain } from "components/action-schedule";
import { ActionChainLink, ActionLink } from "components/links";
import { Messages, MessageType, Utils as MessageUtils } from "components/messages";

import { AppStreamModule, ChannelAppStream } from "./appstreams.type";
import { AppStreamsChangesConfirm } from "./changes-confirm-appstreams";
import { AppStreamsList } from "./list-appstreams";

type Props = {
  channelsAppStreams: Array<ChannelAppStream>;
};

export const getStreamName = (module: AppStreamModule) => `${module.name}:${module.stream}`;

const AppStreams = ({ channelsAppStreams }: Props) => {
  const [appStreams, setAppStreams] = useState<ChannelAppStream[]>(channelsAppStreams);
  const [toEnable, setToEnable] = useState<Array<string>>([]);
  const [toDisable, setToDisable] = useState<Array<string>>([]);
  const [showConfirm, setShowConfirm] = useState<boolean>(false);
  const [scheduledMsg, setScheduledMsg] = useState<MessageType[]>([]);

  const isStreamEnabled = (stream: AppStreamModule) =>
    toEnable.includes(getStreamName(stream)) || (stream.enabled && !toDisable.includes(getStreamName(stream)));

  const handleEnableDisable = (appStream: AppStreamModule) => {
    const stream = `${appStream.name}:${appStream.stream}`;
    if (appStream.enabled) {
      setToDisable((prevState) =>
        prevState.includes(stream) ? prevState.filter((it) => it !== stream) : prevState.concat(stream)
      );
    } else {
      setToEnable((prevState) =>
        prevState.includes(stream) ? prevState.filter((it) => it !== stream) : prevState.concat(stream)
      );
    }

    // Disable every other stream of the module
    appStreams.forEach((ch) =>
      ch.appStreams[appStream.name]
        ?.filter((as) => getStreamName(as) !== stream && isStreamEnabled(as))
        .forEach((as) => handleEnableDisable(as))
    );
  };

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
            enabled: toEnable.includes(getStreamName(as)) || (!toDisable.includes(getStreamName(as)) && as.enabled),
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
    setToEnable([]);
    setToDisable([]);
  };

  const showContent = () => {
    if (showConfirm) {
      return (
        <AppStreamsChangesConfirm
          toEnable={toEnable}
          toDisable={toDisable}
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
