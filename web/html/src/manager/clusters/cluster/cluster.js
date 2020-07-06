// @flow
import {hot} from 'react-hot-loader';
import withPageWrapper from 'components/general/with-page-wrapper';
import React from 'react';
import {useEffect, useState} from 'react';
import {TopPanel} from 'components/panels/TopPanel';
import {HashRouter, Route, Switch} from 'components/utils/HashRouter';
import {TabLabel} from 'components/tab-container'
import ClusterOverview from './cluster-overview';
import ManagementSettings from './cluster-config';
import ClusterEvents from './cluster-events';
import useClustersApi, {withErrorMessages} from '../shared/api/use-clusters-api';
import useRoles from "core/auth/use-roles";
import {isClusterAdmin} from "core/auth/auth.utils";
import {LinkButton} from 'components/buttons';
import {DeleteDialog} from 'components/dialog/DeleteDialog';
import {showDialog} from 'components/dialog/util';
import {Messages} from 'components/messages';

import type {ClusterType, ErrorMessagesType} from '../shared/api/use-clusters-api'
import type {MessageType} from 'components/messages';

type Props = {
  cluster: ClusterType,
  flashMessage: String,
  setMessages: (Array<MessageType>) => void
};

const Cluster = (props: Props) => {
    const roles = useRoles();
    const hasEditingPermissions = isClusterAdmin(roles);
    const {deleteCluster} = useClustersApi();
    const [name, setName] = useState<string>(props.cluster.name);

    const onShowDelete = () => {
        showDialog("delete-cluster");
    }

    const onDelete = () => {
      return deleteCluster(props.cluster.id).then((_) => {
          window.location = "/rhn/manager/clusters";
      })
      .catch((error : ErrorMessagesType) => {
          props.setMessages(error.messages);
      });
    }

    const onUpdateName = (name: string) => {
      setName(name);
    }

    useEffect(() => {
      if(props.flashMessage) {
        props.setMessages([Messages.info(props.flashMessage)])
      }
    }, []);

    const panelButtons = (
        <div className="pull-right btn-group">
        {
            hasEditingPermissions &&
            <LinkButton
                id="deleteCluster"
                icon="fa-trash-o"
                title={t('Delete cluster')}
                text={t('Delete Cluster')}
                handler={onShowDelete}
            />
        }
        </div>
    );

    return (
      <React.Fragment>
        <DeleteDialog id="delete-cluster"
          title={t("Delete cluster")}
          content={<span><div>{t("Are you sure you want to delete cluster?")}</div><div>{t("This will not destroy the cluster. It will only remove it from Uyuni")}</div></span>}
          onConfirmAsync={onDelete}
        />
        <TopPanel title={name}
            button={panelButtons}
            icon="spacewalk-icon-clusters"
            helpUrl="/docs/reference/clusters/clusters-menu.html">
            <HashRouter initialPath="overview">
              <div className="spacewalk-content-nav">
                <ul className="nav nav-tabs">
                    <Route path="overview">
                        {({match}) =>
                            <TabLabel active={match} text={t("Overview")} hash="#/overview" />
                        }
                    </Route>
                    <Route path="settings">
                        {({match}) =>
                            <TabLabel active={match} text={t("Provider Settings")} hash="#/settings" />
                        }
                    </Route>
                    <Route path="events/pending">
                        {({match, hash}) => match || (!match && hash !== "events/history") ?
                            <TabLabel active={match} text={t("Events")} hash="#/events/pending" /> : null
                        }
                    </Route>
                    <Route path="events/history">
                        {({match}) => match ?
                            <TabLabel active={match} text={t("Events")} hash="#/events/history" /> : null
                        }
                    </Route>                    
                </ul>

                  <Route path="events/pending">
                    {({match}) => match ? 
                      <ul className="nav nav-tabs nav-tabs-pf">
                            <TabLabel active={true} text={t("Pending")} hash="#/events/pending" />
                            <TabLabel active={false} text={t("History")} hash="#/events/history" />
                      </ul>  : null }
                  </Route>
                  <Route path="events/history">
                    {({match}) => match ?
                      <ul className="nav nav-tabs nav-tabs-pf">
                        <TabLabel active={false} text={t("Pending")} hash="#/events/pending" />
                        <TabLabel active={true} text={t("History")} hash="#/events/history" />
                      </ul> : null }
                  </Route>
              </div>
              <Switch>
                <Route path="overview">
                  <ClusterOverview cluster={props.cluster} setMessages={props.setMessages} onUpdateName={onUpdateName}
                    hasEditingPermissions={hasEditingPermissions} />
                </Route>
                <Route path="settings">
                  <ManagementSettings cluster={props.cluster} setMessages={props.setMessages}
                    hasEditingPermissions={hasEditingPermissions} />
                </Route>
                <Route path="events/pending">
                  <ClusterEvents cluster={props.cluster} setMessages={props.setMessages}
                    eventsType="pending" />
                </Route>
                <Route path="events/history">
                  <ClusterEvents cluster={props.cluster} setMessages={props.setMessages}
                    eventsType="history" />
                </Route>                                
              </Switch>

            </HashRouter>
        </TopPanel>
      </React.Fragment>);
}

export default hot(module)(withPageWrapper<Props>(withErrorMessages(Cluster)));
