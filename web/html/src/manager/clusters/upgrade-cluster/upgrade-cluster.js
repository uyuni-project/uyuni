// @flow
import { hot } from 'react-hot-loader';
import React, { useState } from 'react';
import { TopPanel } from "components/panels/TopPanel";
import { HashRouter, Route, Switch } from 'components/utils/HashRouter';
import withPageWrapper from 'components/general/with-page-wrapper';
import useClustersApi, { withErrorMessages } from '../shared/api/use-clusters-api';
import ScheduleClusterAction from '../shared/ui/schedule-cluster-action';
import FormulaConfig from '../shared/ui/formula-config';

import type { FormulaValuesType } from '../shared/api/use-clusters-api';
import type { ClusterType } from '../shared/api/use-clusters-api';

type Props = {
    cluster: ClusterType,
    flashMessage: string,
};

const UpgradeCluster = (props: Props) => {
    const [upgradeConfig, setUpgradeConfig] = useState<?FormulaValuesType>(null);

    const { scheduleUpgradeCluster } = useClustersApi();

    const scheduleUpgrade = (earliest: Date, actionChain: ?string): Promise<any> => {
        return scheduleUpgradeCluster(props.cluster.id, earliest, actionChain);
    }

    return (<TopPanel title={t('Upgrade ') + props.cluster.name}
        icon="spacewalk-icon-clusters"
        helpUrl="/docs/reference/clusters/clusters-menu.html">
        <HashRouter initialPath="upgrade-config">
            <Switch>
                <Route path="upgrade-config">
                    {({ goTo, back }) =>
                        <FormulaConfig title={t("Configuration override")}
                            values={upgradeConfig}
                            formula="upgrade_cluster"
                            context={{ "cluster": props.cluster.id }}
                            provider={props.cluster.provider.label}
                            onNext={(formulaValues) => { setUpgradeConfig(formulaValues); goTo("schedule"); }}
                        />}
                </Route>
                <Route path="schedule">
                    {({ goTo, back }) => {
                        return upgradeConfig ? <ScheduleClusterAction
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
                        /> : goTo()
                    }}
                </Route>
            </Switch>
        </HashRouter>
    </TopPanel>);
}

export default hot(module)(withPageWrapper<Props>(withErrorMessages(UpgradeCluster)));
