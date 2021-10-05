import { hot } from "react-hot-loader/root";
import * as React from "react";
import { useState } from "react";
import withPageWrapper from "components/general/with-page-wrapper";
import { TopPanel } from "components/panels/TopPanel";
import SelectProvider from "./select-provider";
import FinishAddCluster from "./finish-add";
import FormulaConfig from "../shared/ui/formula-config";
import SelectServer from "../shared/ui/select-server";
import useClustersApi, { withErrorMessages } from "../shared/api/use-clusters-api";
import { HashRouter, Route, Switch } from "components/utils/HashRouter";
import { SystemLink } from "components/links";

import { ClusterProviderType, ServerType } from "../shared/api/use-clusters-api";
import { FormulaValuesType } from "../shared/api/use-clusters-api";
import { MessageType } from "components/messages";

type Props = {
  providers: Array<ClusterProviderType>;
  setMessages: (arg0: Array<MessageType>) => void;
};

const AddCluster = (props: Props) => {
  const [providerLabel, setProviderLabel] = useState<string | null | undefined>(null);
  const [managementNode, setManagementNode] = useState<ServerType | null | undefined>(null);
  const [providerConfig, setProviderConfig] = useState<FormulaValuesType | null | undefined>(null);
  const { fetchManagementNodes, addCluster } = useClustersApi();

  const onAdd = (name: string, label: string, description: string): Promise<any> => {
    if (providerLabel && managementNode && providerConfig) {
      return addCluster(name, label, description, providerLabel, managementNode.id, providerConfig);
    }
    return Promise.reject(new Error("invalid data"));
  };

  return (
    <TopPanel title={t("Add Cluster")} icon="spacewalk-icon-clusters" helpUrl="reference/clusters/clusters-add.html">
      <HashRouter initialPath="provider">
        <Switch>
          <Route path="provider">
            {({ goTo }) => (
              <SelectProvider
                selectedProvider={providerLabel}
                providers={props.providers}
                onNext={(providerLabel: string) => {
                  setProviderLabel(providerLabel);
                  goTo("management-node");
                }}
              />
            )}
          </Route>
          <Route path="management-node">
            {({ goTo, back }) =>
              providerLabel ? (
                <SelectServer
                  title={t("Select management node")}
                  selectedServers={managementNode ? [managementNode] : []}
                  fetchServers={() => fetchManagementNodes(providerLabel)}
                  onNext={(nodes) => {
                    setManagementNode(nodes[0]);
                    goTo("provider-settings");
                  }}
                  onPrev={back}
                />
              ) : (
                goTo()
              )
            }
          </Route>
          <Route path="provider-settings">
            {({ goTo, back }) =>
              providerLabel ? (
                <FormulaConfig
                  title={t("Provider Settings")}
                  values={providerConfig}
                  provider={providerLabel}
                  formula="settings"
                  onNext={(formulaValues) => {
                    setProviderConfig(formulaValues);
                    goTo("finish");
                  }}
                  onPrev={back}
                />
              ) : (
                goTo()
              )
            }
          </Route>
          <Route path="finish">
            {({ goTo, back }) => {
              const selectedProvider = props.providers.find((p) => p.label === providerLabel);
              return selectedProvider && managementNode ? (
                <FinishAddCluster
                  panel={
                    <div className="form-horizontal">
                      <div className="form-group">
                        <label className="col-md-3 control-label">{t("Provider:")}</label>
                        <div className="col-md-9">{selectedProvider.name}</div>
                      </div>
                      <div className="form-group">
                        <label className="col-md-3 control-label">{t("Management Node:")}</label>
                        <div className="col-md-9">
                          <SystemLink id={managementNode.id}>{managementNode.name}</SystemLink>
                        </div>
                      </div>
                    </div>
                  }
                  onAdd={onAdd}
                  onPrev={back}
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

export default hot(withPageWrapper(withErrorMessages(AddCluster)));
