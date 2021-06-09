import { hot } from "react-hot-loader/root";
import * as React from "react";
import { useState } from "react";
import { TopPanel } from "components/panels/TopPanel";
import withPageWrapper from "components/general/with-page-wrapper";
import useClustersApi, { withErrorMessages } from "../shared/api/use-clusters-api";
import FormulaConfig from "../shared/ui/formula-config";
import SelectServer from "../shared/ui/select-server";
import { HashRouter, Route, Switch } from "components/utils/HashRouter";
import ScheduleClusterAction from "../shared/ui/schedule-cluster-action";
import { SystemLink } from "components/links";

import { FormulaValuesType, ServerType } from "../shared/api/use-clusters-api";
import { ClusterType } from "../shared/api/use-clusters-api";
import { MessageType, ServerMessageType } from "components/messages";

type Props = {
  cluster: ClusterType;
  flashMessage?: ServerMessageType;
  setMessages: (messages: MessageType[]) => void;
};

const JoinCluster = (props: Props) => {
  const [joinConfig, setJoinConfig] = useState<FormulaValuesType | null>(null);
  const [nodesToJoin, setNodesToJoin] = useState<Array<ServerType>>([]);

  const { fetchNodesToJoin, scheduleJoinNode } = useClustersApi();

  const scheduleJoin = (earliest: moment.Moment, actionChain: string | null): Promise<any> => {
    if (nodesToJoin && joinConfig) {
      return scheduleJoinNode(
        props.cluster.id,
        nodesToJoin.map(node => node.id),
        joinConfig,
        earliest,
        actionChain
      ).then(() => {
        window.location.href = `/rhn/manager/cluster/${props.cluster.id}`;
      });
    }
    return Promise.reject(new Error("invalid data"));
  };

  return (
    <TopPanel
      title={t("Join ") + props.cluster.name}
      icon="spacewalk-icon-clusters"
      helpUrl="reference/clusters/clusters-menu.html"
    >
      <div className="alert alert-info">
        {t("NOTE: before joining a new node, make sure that the node has:")}
        <ul>
          <li>
            {t(
              "Container as a Service Platform (CaaSP) channels assigned - or any children channels of CaaSP channels"
            )}
          </li>
          <li>{t("The management node can access the node as root via ssh without password")}</li>
        </ul>
      </div>
      <HashRouter initialPath="select-node">
        <Switch>
          <Route path="select-node">
            {({ goTo, back }) => (
              <SelectServer
                title={t("Select system to join")}
                multiple={true}
                selectedServers={nodesToJoin}
                fetchServers={() => fetchNodesToJoin(props.cluster.id)}
                onNext={nodes => {
                  setNodesToJoin(nodes);
                  goTo("join-config");
                }}
              />
            )}
          </Route>
          <Route path="join-config">
            {({ goTo, back }) =>
              nodesToJoin ? (
                <FormulaConfig
                  title={t("New node configuration")}
                  values={joinConfig}
                  formula="join_node"
                  context={{ nodes: nodesToJoin.map(node => node.id), cluster: props.cluster.id }}
                  provider={props.cluster.provider.label}
                  onNext={formulaValues => {
                    setJoinConfig(formulaValues);
                    goTo("schedule");
                  }}
                  onPrev={back}
                />
              ) : (
                goTo()
              )
            }
          </Route>
          <Route path="schedule">
            {({ goTo, back }) => {
              return nodesToJoin ? (
                <ScheduleClusterAction
                  title={t("Schedule join node")}
                  panel={
                    <div className="form-horizontal">
                      <div className="form-group">
                        <label className="col-md-3 control-label">{t("Cluster:")}</label>
                        <div className="col-md-9">{props.cluster.name}</div>
                      </div>
                      <div className="form-group">
                        <label className="col-md-3 control-label">{t("Node to join:")}</label>
                        {nodesToJoin.map(nodeToJoin => (
                          <div className="col-md-9">
                            <SystemLink id={nodeToJoin.id}>{nodeToJoin.name}</SystemLink>
                          </div>
                        ))}
                      </div>
                    </div>
                  }
                  schedule={scheduleJoin}
                  scheduleButtonLabel={t("Join")}
                  onPrev={back}
                  actionType="cluster.join_node"
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

export default hot(withPageWrapper(withErrorMessages(JoinCluster)));
