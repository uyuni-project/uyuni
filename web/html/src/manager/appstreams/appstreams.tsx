import { useState } from "react";

import { Button } from "components/buttons";

import { Dialog } from "../../components/dialog/Dialog";
import { AppStreamModule, ChannelModule } from "./appstreams.type";
import { getNSVCA } from "./appstreams-utils";
import { ChannelModules } from "./channel-modules";
import { ConfirmChanges } from "./confirm-changes";
import { ModulePackages } from "./module-packages";

type Props = {
  channelsModules: Array<ChannelModule>;
};

const AppStreams = (props: Props) => {
  const [moduleToShowPackages, setModuleToShowPackages] = useState<{ nsvca: string; channelId: number } | null>(null);
  const [toEnable, setToEnable] = useState<Array<string>>([]);
  const [toDisable, setToDisable] = useState<Array<string>>([]);
  const [showConfirmModal, setShowConfirmModal] = useState<boolean>(false);
  const numberOfChanges = toEnable.length + toDisable.length;

  const handleToggle = (appStream: AppStreamModule) => {
    const nsvca = getNSVCA(appStream);
    if (appStream.enabled) {
      setToDisable((prevState) =>
        prevState.includes(nsvca) ? prevState.filter((it) => it !== nsvca) : prevState.concat(nsvca)
      );
    } else {
      setToEnable((prevState) =>
        prevState.includes(nsvca) ? prevState.filter((it) => it !== nsvca) : prevState.concat(nsvca)
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
      <div className="btn-group">
        <Button
          id="applyModuleChanges"
          icon="fa-plus"
          className={"btn-success"}
          text={t("Enable/Disable AppStreams") + (numberOfChanges > 0 ? " (" + numberOfChanges + ")" : "")}
          handler={() => setShowConfirmModal(true)}
        />
      </div>
      <div className="panel panel-default">
        <table className="table table-striped">
          <thead>
            <tr>
              <th>Channel</th>
              <th>Modules</th>
              <th>Streams</th>
              <th>NSVCA</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {props.channelsModules.map((channelModule) => {
              const { numberOfAppStreams, modules, modulesNames, channelId, channelLabel } = channelModule;
              if (modulesNames.length === 0) {
                return (
                  <tr key={channelId}>
                    <td>{channelLabel}</td>
                    <td colSpan={4}>The channel has no modules</td>
                  </tr>
                );
              }
              return modulesNames.map((moduleName, channelIdx) => (
                <ChannelModules
                  showPackages={(nsvca) => setModuleToShowPackages({ nsvca, channelId })}
                  key={moduleName}
                  modules={modules}
                  moduleName={moduleName}
                  channelIdx={channelIdx}
                  channelLabel={channelLabel}
                  numberOfAppStreams={numberOfAppStreams}
                  toEnable={toEnable}
                  toDisable={toDisable}
                  onToggle={handleToggle}
                />
              ));
            })}
          </tbody>
        </table>
      </div>

      <Dialog
        isOpen={moduleToShowPackages !== null}
        onClose={() => setModuleToShowPackages(null)}
        title="Packages"
        className="modal-lg"
        id="modulePackagesPopUp"
        content={<ModulePackages nsvca={moduleToShowPackages?.nsvca} channelId={moduleToShowPackages?.channelId} />}
      />

      <Dialog
        isOpen={showConfirmModal}
        onClose={() => setShowConfirmModal(false)}
        title="Confirm AppStreams Changes"
        className="modal-lg"
        id="appStreamChangesModal"
        content={<ConfirmChanges toEnable={toEnable} toDisable={toDisable} />}
      />
    </>
  );
};

export default AppStreams;
