import { hot } from "react-hot-loader/root";
import * as React from "react";
import { useState } from "react";
import { TopPanel } from "components/panels/TopPanel";
import { HashRouter, Route, Switch } from "components/utils/HashRouter";
import withPageWrapper from "components/general/with-page-wrapper";
import useClustersApi, { withErrorMessages } from "../shared/api/use-clusters-api";
import ScheduleClusterAction from "../shared/ui/schedule-cluster-action";
import { SystemLink } from "components/links";
import FormulaConfig from "../shared/ui/formula-config";

import { ClusterType, FormulaValuesType, ClusterNodeType } from "../shared/api/use-clusters-api";
import { MessageType, ServerMessageType } from "components/messages";

type Props = {
  cluster: ClusterType;
  nodes: ClusterNodeType[];
  serverId?: number | null;
  flashMessage?: ServerMessageType;
  setMessages: (messages: MessageType[]) => void;
};

const RemoveNode = (props: Props) => {
  const [removeConfig, setRemoveConfig] = useState<FormulaValuesType | null | undefined>(null);
  const { scheduleRemoveNode } = useClustersApi();

  const scheduleRemove = (earliest: moment.Moment, actionChain: string | null | undefined): Promise<any> => {
    if (removeConfig) {
      return scheduleRemoveNode(
        props.cluster.id,
        props.nodes.filter((node) => (node.server ? true : false)).map((node) => (node.server ? node.server.id : 0)),
        removeConfig,
        earliest,
        actionChain
      ).then(() => {
        window.location.href = `/rhn/manager/cluster/${props.cluster.id}`;
      });
    }
    return Promise.reject(new Error("invalid data"));
  };

  return (
    <TopPanel title={t("Remove nodes")} icon="spacewalk-icon-clusters" helpUrl="reference/clusters/clusters-menu.html">
      <HashRouter initialPath="remove-config">
        <Switch>
          <Route path="remove-config">
            {({ goTo, back }) => (
              <FormulaConfig
                title={t("Remove node")}
                values={removeConfig}
                formula="remove_node"
                context={{
                  node_names: props.nodes.map((node) => node.hostname),
                  cluster: props.cluster.id,
                }}
                provider={props.cluster.provider.label}
                onNext={(formulaValues) => {
                  setRemoveConfig(formulaValues);
                  goTo("schedule");
                }}
              />
            )}
          </Route>
          <Route path="schedule">
            {({ goTo, back }) =>
              removeConfig ? (
                <ScheduleClusterAction
                  title={t("Schedule remove node")}
                  panel={
                    <div className="form-horizontal">
                      <div className="form-group">
                        <label className="col-md-3 control-label">{t("Cluster:")}</label>
                        <div className="col-md-9">{props.cluster.name}</div>
                      </div>
                      <div className="form-group">
                        <label className="col-md-3 control-label">{t("Nodes to remove:")}</label>
                        <div className="col-md-9">
                          {props.nodes.map((node) => (
                            <div>
                              {node.server ? (
                                <SystemLink id={node.server.id}>{node.hostname}</SystemLink>
                              ) : (
                                node.hostname
                              )}
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  }
                  schedule={scheduleRemove}
                  scheduleButtonLabel={t("Remove")}
                  actionType="cluster.remove_node"
                  onPrev={back}
                />
              ) : (
                goTo()
              )
            }
          </Route>
        </Switch>
      </HashRouter>
    </TopPanel>
  );
};

export default hot(withPageWrapper(withErrorMessages(RemoveNode)));
