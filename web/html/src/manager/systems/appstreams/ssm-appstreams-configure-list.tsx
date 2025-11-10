import { useMemo } from "react";

import { AppStreamActions } from "manager/appstreams/actions-appstreams";
import { AppStreamModule } from "manager/appstreams/appstreams.type";
import { getStreamName } from "manager/appstreams/utils";

import { LinkButton } from "components/buttons";
import { Panel } from "components/panels";
import { Column, Table } from "components/table";

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

  const tableData = useMemo(() => {
    const sortedModuleKeys = Object.keys(appStreams).sort();

    return sortedModuleKeys.map((moduleKey) => {
      const modules: AppStreamModule[] = appStreams[moduleKey];
      const sortedModules = [...modules].sort((a, b) => getStreamName(a).localeCompare(getStreamName(b)));
      const totalEnabledForModule = modules.reduce((sum, module) => sum + (module.systemCount || 0), 0);
      const currentAction = getModuleAction(moduleKey);

      return {
        moduleKey: moduleKey,
        currentAction: currentAction,
        totalEnabledForModule: totalEnabledForModule,
        allModulesInGroup: sortedModules,

        children: sortedModules.map((module) => ({
          ...module,
          streamName: getStreamName(module),
          isChild: true,
          parentAction: currentAction,
        })),
      };
    });
  }, [appStreams, getModuleAction]);

  const identifier = (row) => {
    if (row.isChild) {
      return row.streamName;
    }
    return row.moduleKey;
  };

  const getRowClass = (row) => {
    const action = row.isChild ? row.parentAction : row.currentAction;
    return action !== NO_CHANGE ? "changed" : "";
  };

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
        <Table data={tableData} identifier={identifier} cssClassFunction={getRowClass} expandable>
          <Column
            header={t("Module")}
            columnKey="module"
            cell={(row, criteria, nestingLevel) => {
              return nestingLevel === 0 ? <strong>{row.moduleKey}</strong> : null;
            }}
            width="25%"
          />
          <Column
            header={t("AppStreams")}
            columnKey="appstream"
            cell={(row, criteria, nestingLevel = 0) => {
              if (nestingLevel > 0) {
                return row.streamName;
              }
              return <span>{t("({count} AppStreams)", { count: row.children.length })}</span>;
            }}
            width="25%"
          />
          <Column
            header={t("Systems")}
            columnKey="systems"
            cell={(row, criteria, nestingLevel = 0) => {
              if (nestingLevel > 0) {
                return row.systemCount;
              }
              return <strong>{row.totalEnabledForModule}</strong>;
            }}
            width="10%"
          />
          <Column
            header={t("Action")}
            columnKey="action"
            cell={(row, criteria, nestingLevel = 0) => {
              if (nestingLevel > 0) return null;

              const { moduleKey, currentAction, totalEnabledForModule, allModulesInGroup } = row;

              const actionOptions = [
                <option key={NO_CHANGE} value={NO_CHANGE}>
                  {t("Don't change")}
                </option>,
                <option key={DISABLE} value={DISABLE} disabled={totalEnabledForModule === 0}>
                  {t(`Disable module${totalEnabledForModule === 0 ? " (No system to disable)" : ""}`)}
                </option>,
              ];

              actionOptions.push(
                ...allModulesInGroup.map((module) => (
                  <option
                    key={getStreamName(module)}
                    value={getStreamName(module)}
                  >{`${t("Enable ")} ${getStreamName(module)}`}</option>
                ))
              );

              return (
                <select
                  className="form-control"
                  value={currentAction}
                  onChange={(e) => onActionChange(moduleKey, e.target.value)}
                  onClick={(e) => e.stopPropagation()}
                >
                  {actionOptions}
                </select>
              );
            }}
            width="40%"
          />
        </Table>
      </Panel>
    </>
  );
};
