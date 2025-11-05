import { AppStreamActions } from "manager/appstreams/actions-appstreams";
import { AppStreamModule } from "manager/appstreams/appstreams.type";
import { getStreamName } from "manager/appstreams/utils";

import { LinkButton } from "components/buttons";
import { Panel } from "components/panels";

export const NO_CHANGE = "no-change";
export const DISABLE = "disable";

const changeChannelButton = (
  <LinkButton
    className="btn-link"
    key="change-channel-btn"
    title={t("Change Channel")}
    href="/rhn/manager/systems/ssm/appstreams"
  >
    {t("Change channel")}
  </LinkButton>
);

export const SSMAppStreamsList = ({
  channelAppStreams,
  onReset,
  numberOfChanges,
  onActionChange,
  getModuleAction,
  onSubmitChanges,
}) => {
  const { channel, appStreams } = channelAppStreams;
  const sortedModuleKeys = Object.keys(appStreams).sort();
  return (
    <>
      <p>{t("The following AppStream modules are available on the selected channel.")}</p>
      <AppStreamActions numberOfChanges={numberOfChanges} onReset={onReset} onSubmit={onSubmitChanges} />
      <Panel
        headingLevel="h4"
        icon="spacewalk-icon-software-channels"
        title={channel.name}
        buttons={[changeChannelButton]}
      >
        <table className="table table-striped">
          <thead>
            <tr>
              <th style={{ width: "25%" }}>{t("Module")}</th>
              <th style={{ width: "25%" }}>{t("AppStreams")}</th>
              <th style={{ width: "10%" }}>{t("Systems")}</th>
              <th style={{ width: "40%" }}>{t("Action")}</th>
            </tr>
          </thead>
          <tbody>
            {sortedModuleKeys.map((moduleKey) => {
              const modules: AppStreamModule[] = appStreams[moduleKey];
              const currentAction = getModuleAction(moduleKey);
              const sortedModules = [...modules].sort((a, b) => getStreamName(a).localeCompare(getStreamName(b)));
              const rowClassName = currentAction !== NO_CHANGE ? "changed" : "";

              const totalEnabledForModule = modules.reduce((sum, module) => sum + (module.systemCount || 0), 0);

              const actionOptions = [
                <option key={NO_CHANGE} value={NO_CHANGE}>
                  {t("Don't change")}
                </option>,
                <option key={DISABLE} value={DISABLE} disabled={totalEnabledForModule === 0}>
                  {t(`Disable module${totalEnabledForModule === 0 ? " (No system to disable)" : ""}`)}
                </option>,
              ];

              actionOptions.push(
                ...sortedModules.map((module) => (
                  <option
                    key={getStreamName(module)}
                    value={getStreamName(module)}
                  >{`${t("Enable ")} ${getStreamName(module)}`}</option>
                ))
              );

              const rowSpanCount = sortedModules.length;

              return sortedModules.map((module, index) => {
                const isFirstRow = index === 0;

                return (
                  <tr key={getStreamName(module)} className={rowClassName}>
                    {isFirstRow && (
                      <td rowSpan={rowSpanCount} style={{ verticalAlign: "top" }}>
                        <strong>{moduleKey}</strong>
                      </td>
                    )}

                    <td>{`${getStreamName(module)}`}</td>
                    <td>{module.systemCount}</td>

                    {isFirstRow && (
                      <td rowSpan={rowSpanCount} style={{ verticalAlign: "top" }}>
                        <select
                          className="form-control"
                          value={currentAction}
                          onChange={(e) => onActionChange(moduleKey, e.target.value)}
                        >
                          {actionOptions}
                        </select>
                      </td>
                    )}
                  </tr>
                );
              });
            })}
          </tbody>
        </table>
      </Panel>
    </>
  );
};
