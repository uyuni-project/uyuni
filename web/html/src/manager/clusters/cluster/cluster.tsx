import { hot } from "react-hot-loader/root";
import withPageWrapper from "components/general/with-page-wrapper";
import * as React from "react";
import { useEffect, useState } from "react";
import { TopPanel } from "components/panels/TopPanel";
import { HashRouter, Route, Switch } from "components/utils/HashRouter";
import { TabLabel } from "components/tab-container";
import ClusterOverview from "./cluster-overview";
import ManagementSettings from "./cluster-config";
import useClustersApi, { withErrorMessages } from "../shared/api/use-clusters-api";
import useRoles from "core/auth/use-roles";
import { isClusterAdmin } from "core/auth/auth.utils";
import { LinkButton } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { showDialog } from "components/dialog/util";
import { fromServerMessage } from "components/messages";
import { ActionLink, ActionChainLink } from "components/links";

import { ClusterType, ErrorMessagesType } from "../shared/api/use-clusters-api";
import { ServerMessageType, MessageType } from "components/messages";

const msgMap = {
  action_scheduled: (actionId) => (
    <>
      {t("Action has been ")}
      <ActionLink id={actionId}>{t("scheduled")}</ActionLink>
      {t(" successfully.")}
    </>
  ),
  action_chain_scheduled: (actionChainId, actionChain) => (
    <>
      {t("Action has been successfully added to the Action Chain ")}
      <ActionChainLink id={actionChainId}>{actionChain}</ActionChainLink>.
    </>
  ),
  cluster_added: (name) => (
    <>
      {t("Cluster ")}
      <strong>{name}</strong>
      {t(" has been added successfully.")}
    </>
  ),
};

type Props = {
  cluster: ClusterType;
  flashMessage: ServerMessageType | null | undefined;
  setMessages: (arg0: Array<MessageType>) => void;
};

const Cluster = (props: Props) => {
  const roles = useRoles();
  const hasEditingPermissions = isClusterAdmin(roles);
  const { deleteCluster } = useClustersApi();
  const [name, setName] = useState<string>(props.cluster.name);

  const onShowDelete = () => {
    showDialog("delete-cluster");
  };

  const onDelete = () => {
    return deleteCluster(props.cluster.id)
      .then((_) => {
        window.location.href = "/rhn/manager/clusters";
      })
      .catch((error: ErrorMessagesType) => {
        props.setMessages(error.messages);
      });
  };

  const onUpdateName = (name: string) => {
    setName(name);
  };

  useEffect(() => {
    if (props.flashMessage) {
      const message = fromServerMessage(props.flashMessage, msgMap);
      if (message) {
        props.setMessages([message]);
      }
    }
  }, []);

  const panelButtons = (
    <div className="pull-right btn-group">
      {hasEditingPermissions && (
        <LinkButton
          id="deleteCluster"
          icon="fa-trash-o"
          title={t("Delete cluster")}
          text={t("Delete Cluster")}
          handler={onShowDelete}
        />
      )}
    </div>
  );

  return (
    <React.Fragment>
      <DeleteDialog
        id="delete-cluster"
        title={t("Delete cluster")}
        content={
          <span>
            <div>{t("Are you sure you want to delete cluster?")}</div>
            <div>{t("This will not destroy the cluster. It will only remove it from Uyuni")}</div>
          </span>
        }
        onConfirmAsync={onDelete}
      />
      <TopPanel
        title={name}
        button={panelButtons}
        icon="spacewalk-icon-clusters"
        helpUrl="reference/clusters/clusters-details.html"
      >
        <HashRouter initialPath="overview">
          <div className="spacewalk-content-nav">
            <ul className="nav nav-tabs">
              <Route path="overview">
                {({ match }) => <TabLabel active={match} text={t("Overview")} hash="#/overview" />}
              </Route>
              <Route path="settings">
                {({ match }) => <TabLabel active={match} text={t("Provider Settings")} hash="#/settings" />}
              </Route>
            </ul>
          </div>
          <Switch>
            <Route path="overview">
              <ClusterOverview
                cluster={props.cluster}
                setMessages={props.setMessages}
                onUpdateName={onUpdateName}
                hasEditingPermissions={hasEditingPermissions}
              />
            </Route>
            <Route path="settings">
              <ManagementSettings
                cluster={props.cluster}
                setMessages={props.setMessages}
                hasEditingPermissions={hasEditingPermissions}
              />
            </Route>
          </Switch>
        </HashRouter>
      </TopPanel>
    </React.Fragment>
  );
};

export default hot(withPageWrapper(withErrorMessages(Cluster)));
