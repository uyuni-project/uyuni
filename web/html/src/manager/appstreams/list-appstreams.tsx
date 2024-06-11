import { useState } from "react";

import { Dialog } from "../../components/dialog/Dialog";
import { AppStreamActions } from "./actions-appstreams";
import { AppStreamPackages } from "./appstream-packages";
import { AppStreamModule, Channel, ChannelAppStream } from "./appstreams.type";
import { AppStreamPanel } from "./panel-appstream";
import { numberOfChanges } from "./utils";

interface Props {
  channelsAppStreams: ChannelAppStream[];
  toEnable: Map<number, string[]>;
  toDisable: Map<number, string[]>;
  onReset: () => void;
  onSubmitChanges: () => void;
  onModuleEnableDisable: (channel: Channel, module: AppStreamModule) => void;
}

export const AppStreamsList = ({
  channelsAppStreams,
  toEnable,
  toDisable,
  onReset,
  onSubmitChanges,
  onModuleEnableDisable,
}: Props) => {
  const [moduleToShowPackages, setModuleToShowPackages] = useState<{ stream: string; channelId: number } | null>(null);
  const changes = numberOfChanges(toEnable, toDisable);

  // Sort channels by label
  channelsAppStreams.sort((a, b) => a.channel.name.localeCompare(b.channel.name));

  return (
    <>
      <p>{t("The following AppStream modules are currently available to the system.")}</p>
      <AppStreamActions numberOfChanges={changes} onReset={onReset} onSubmit={onSubmitChanges} />
      {channelsAppStreams.map((channelAppStream) => {
        const { channel, appStreams } = channelAppStream;
        const showPackages = (stream) => setModuleToShowPackages({ stream: stream, channelId: channel.id });
        return (
          <AppStreamPanel
            key={channel.id}
            channel={channel}
            appStreams={appStreams}
            toEnable={toEnable}
            toDisable={toDisable}
            onToggle={(appStream) => onModuleEnableDisable(channel, appStream)}
            showPackages={showPackages}
          />
        );
      })}
      <Dialog
        isOpen={moduleToShowPackages !== null}
        onClose={() => setModuleToShowPackages(null)}
        title={t("Packages in {stream}", { stream: moduleToShowPackages?.stream })}
        className="modal-lg"
        id="modulePackagesPopUp"
        content={
          <AppStreamPackages stream={moduleToShowPackages?.stream} channelId={moduleToShowPackages?.channelId} />
        }
      />
    </>
  );
};
