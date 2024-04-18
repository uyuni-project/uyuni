import { useState } from "react";

import { Button } from "components/buttons";
import { Panel } from "components/panels/Panel";

import { Dialog } from "../../components/dialog/Dialog";
import { AppStreamPackages } from "./appstream-packages";
import { AppStreamModule, ChannelAppStream } from "./appstreams.type";
import { ChannelAppStreams } from "./channel-appstreams";

interface Props {
  channelsAppStreams: ChannelAppStream[];
  toEnable: string[];
  toDisable: string[];
  onSubmitChanges: () => void;
  onModuleEnableDisable: (module: AppStreamModule) => void;
}

export const AppStreamsList = ({
  channelsAppStreams,
  toEnable,
  toDisable,
  onSubmitChanges,
  onModuleEnableDisable,
}: Props) => {
  const [moduleToShowPackages, setModuleToShowPackages] = useState<{ stream: string; channelId: number } | null>(null);
  const numberOfChanges = toEnable.length + toDisable.length;

  // Sort channels by label
  channelsAppStreams.sort((a, b) => a.channel.name.localeCompare(b.channel.name));

  return (
    <>
      <p>{t("Use the status button for changes and then confirm using the Apply Changes button.")}</p>
      <div className="text-right margin-bottom-sm">
        <Button
          id="applyModuleChanges"
          className="btn-success"
          disabled={numberOfChanges === 0}
          text={t("Apply Changes") + (numberOfChanges > 0 ? " (" + numberOfChanges + ")" : "")}
          handler={onSubmitChanges}
        />
      </div>
      {channelsAppStreams.map((channelAppStream) => {
        const { channel, appStreams } = channelAppStream;
        return (
          <Panel headingLevel="h4" icon="spacewalk-icon-software-channels" title={channel.name}>
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>{t("Modules")}</th>
                  <th>{t("Streams")}</th>
                  <th>{t("Enabled")}</th>
                </tr>
              </thead>
              <tbody>
                {Object.keys(appStreams).map((moduleName, channelIdx) => (
                  <ChannelAppStreams
                    showPackages={(stream) => setModuleToShowPackages({ stream: stream, channelId: channel.id })}
                    key={moduleName}
                    streams={appStreams[moduleName]}
                    moduleName={moduleName}
                    toEnable={toEnable}
                    toDisable={toDisable}
                    onToggle={onModuleEnableDisable}
                  />
                ))}
              </tbody>
            </table>
          </Panel>
        );
      })}
      <Dialog
        isOpen={moduleToShowPackages !== null}
        onClose={() => setModuleToShowPackages(null)}
        title={t("Packages")}
        className="modal-lg"
        id="modulePackagesPopUp"
        content={
          <AppStreamPackages stream={moduleToShowPackages?.stream} channelId={moduleToShowPackages?.channelId} />
        }
      />
    </>
  );
};
