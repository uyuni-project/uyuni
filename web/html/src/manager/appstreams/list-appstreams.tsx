import { useState } from "react";

import { Button } from "components/buttons";

import { Dialog } from "../../components/dialog/Dialog";
import { AppStreamPackages } from "./appstream-packages";
import { ChannelAppStreams } from "./channel-appstreams";

export const AppStreamsList = ({ channelsAppStreams, toEnable, toDisable, onSubmitChanges, onModuleEnableDisable }) => {
  const [moduleToShowPackages, setModuleToShowPackages] = useState<{ stream: string; channelId: number } | null>(null);
  const numberOfChanges = toEnable.length + toDisable.length;

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
      <div className="panel panel-default">
        <table className="table table-striped">
          <thead>
            <tr>
              <th>Channel</th>
              <th>Modules</th>
              <th>Streams</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {channelsAppStreams.map((channelAppStream) => {
              const { numberOfAppStreams, appStreams, modulesNames, channelId, channelLabel } = channelAppStream;
              return modulesNames.map((moduleName, channelIdx) => (
                <ChannelAppStreams
                  showPackages={(stream) => setModuleToShowPackages({ stream, channelId })}
                  key={moduleName}
                  appStreams={appStreams}
                  moduleName={moduleName}
                  channelIdx={channelIdx}
                  channelId={channelId}
                  channelLabel={channelLabel}
                  numberOfAppStreams={numberOfAppStreams}
                  toEnable={toEnable}
                  toDisable={toDisable}
                  onToggle={onModuleEnableDisable}
                />
              ));
            })}
          </tbody>
        </table>
      </div>
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
