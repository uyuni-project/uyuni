import { useState } from "react";

import { AppStreamModule, ChannelAppStream } from "./appstreams.type";
import { AppStreamsChangesConfirm } from "./changes-confirm-appstreams";
import { AppStreamsList } from "./list-appstreams";

type Props = {
  channelsAppStreams: Array<ChannelAppStream>;
};

export const getStreamName = (module: AppStreamModule) => `${module.name}:${module.stream}`;

const AppStreams = (props: Props) => {
  const [toEnable, setToEnable] = useState<Array<string>>([]);
  const [toDisable, setToDisable] = useState<Array<string>>([]);
  const [showConfirm, setShowConfirm] = useState<boolean>(false);
  const [confirmed, setConfirmed] = useState<boolean>(false);
  const [scheduledMsg, setScheduledMsg] = useState<any>(null);

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
    props.channelsAppStreams.forEach((ch) =>
      ch.appStreams[appStream.name]
        .filter((as) => getStreamName(as) !== stream && isStreamEnabled(as))
        .forEach((as) => handleEnableDisable(as))
    );
  };

  const handleSubmitChanges = () => {
    setShowConfirm(true);
  };

  const handleConfirmChanges = (msg) => {
    setShowConfirm(false);
    setConfirmed(true);
    setScheduledMsg(msg);
  };

  const handleReset = () => {
    setToEnable([]);
    setToDisable([]);
  };

  const showContent = () => {
    if (confirmed) {
      return scheduledMsg;
    } else if (showConfirm) {
      return (
        <AppStreamsChangesConfirm
          toEnable={toEnable}
          toDisable={toDisable}
          onCancelClick={() => setShowConfirm(false)}
          onConfirm={handleConfirmChanges}
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
          channelsAppStreams={props.channelsAppStreams}
        />
      );
    }
  };

  return (
    <>
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
