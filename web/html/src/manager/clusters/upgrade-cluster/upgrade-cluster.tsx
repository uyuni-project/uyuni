import { hot } from "react-hot-loader/root";
import * as React from "react";
import { useState } from "react";
import { TopPanel } from "components/panels/TopPanel";
import { HashRouter, Route, Switch } from "components/utils/HashRouter";
import withPageWrapper from "components/general/with-page-wrapper";
import useClustersApi, { withErrorMessages } from "../shared/api/use-clusters-api";
import ScheduleClusterAction from "../shared/ui/schedule-cluster-action";
import FormulaConfig from "../shared/ui/formula-config";
import UpgradeClusterPlan from "./upgrade-cluster-plan";

import { FormulaValuesType } from "../shared/api/use-clusters-api";
import { ClusterType } from "../shared/api/use-clusters-api";
import { MessageType, ServerMessageType } from "components/messages";

type Props = {
  cluster: ClusterType;
  showUpgradePlan?: boolean;
  flashMessage?: ServerMessageType;
  setMessages: (messages: MessageType[]) => void;
};

const UpgradeCluster = (props: Props) => {
  const [upgradeConfig, setUpgradeConfig] = useState<FormulaValuesType | null | undefined>(null);

  const { scheduleUpgradeCluster } = useClustersApi();

  const scheduleUpgrade = (earliest: moment.Moment, actionChain: string | null | undefined): Promise<any> => {
    return scheduleUpgradeCluster(props.cluster.id, earliest, actionChain).then(() => {
      window.location.href = `/rhn/manager/cluster/${props.cluster.id}`;
    });
  };

  return (
    <TopPanel
      title={t("Upgrade ") + props.cluster.name}
      icon="spacewalk-icon-clusters"
      helpUrl="reference/clusters/clusters-menu.html"
    >
      <HashRouter initialPath={props.showUpgradePlan ? "upgrade-plan" : "upgrade-config"}>
        <Switch>
          <Route path="upgrade-plan">
            {({ goTo }) => (
              <UpgradeClusterPlan
                cluster={props.cluster}
                onNext={() => {
                  goTo("upgrade-config");
                }}
              />
            )}
          </Route>
          <Route path="upgrade-config">
            {({ goTo, back }) => (
              <FormulaConfig
                title={t("Configuration override")}
                values={upgradeConfig}
                formula="upgrade_cluster"
                context={{ cluster: props.cluster.id }}
                provider={props.cluster.provider.label}
                onPrev={props.showUpgradePlan ? back : undefined}
                onNext={(formulaValues) => {
                  setUpgradeConfig(formulaValues);
                  goTo("schedule");
                }}
              />
            )}
          </Route>
          <Route path="schedule">
            {({ goTo, back }) => {
              return upgradeConfig ? (
                <ScheduleClusterAction
                  title={t("Schedule upgrade cluster")}
                  panel={
                    <div className="form-horizontal">
                      <div className="form-group">
                        <label className="col-md-3 control-label">{t("Cluster:")}</label>
                        <div className="col-md-9">{props.cluster.name}</div>
                      </div>
                    </div>
                  }
                  schedule={scheduleUpgrade}
                  scheduleButtonLabel={t("Upgrade")}
                  onPrev={back}
                  actionType="cluster.upgrade_cluster"
                />
              ) : (
                goTo()
              );
            }}
          </Route>
        </Switch>
      </HashRouter>
    </TopPanel>
  );
};

export default hot(withPageWrapper(withErrorMessages(UpgradeCluster)));
