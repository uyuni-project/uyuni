import { hot } from "react-hot-loader/root";
import * as React from "react";
import { useEffect } from "react";
import withPageWrapper from "components/general/with-page-wrapper";
import { TopPanel } from "components/panels/TopPanel";
import { LinkButton } from "components/buttons";
import useRoles from "core/auth/use-roles";
import { isClusterAdmin } from "core/auth/auth.utils";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Utils } from "utils/functions";
import { SystemLink } from "components/links";
import { fromServerMessage, ServerMessageType } from "components/messages";
import { withErrorMessages } from "../shared/api/use-clusters-api";

import { ClusterType } from "../shared/api/use-clusters-api";
import { MessageType } from "components/messages";

const msgMap = {
  cluster_deleted: (name) => (
    <>
      {t("Cluster ")}
      <strong>{name}</strong>
      {t(" has been deleted successfully.")}
    </>
  ),
};

type Props = {
  clusters: Array<ClusterType>;
  flashMessage?: ServerMessageType;
  setMessages: (arg0: Array<MessageType>) => void;
};

const ListClusters = (props: Props) => {
  const roles = useRoles();
  const hasEditingPermissions = isClusterAdmin(roles);
  const panelButtons = (
    <div className="pull-right btn-group">
      {hasEditingPermissions && (
        <LinkButton
          id="addCluster"
          icon="fa-plus"
          className="btn-link js-spa"
          title={t("Add an existing cluster")}
          text={t("Add Cluster")}
          href="/rhn/manager/clusters/add"
        />
      )}
    </div>
  );

  const filterFunc = (row, criteria) => {
    const keysToSearch = ["name", "type"];
    if (criteria) {
      return keysToSearch
        .map((key) => row[key])
        .join()
        .toLowerCase()
        .includes(criteria.toLowerCase());
    }
    return true;
  };

  useEffect(() => {
    if (props.flashMessage) {
      const message = fromServerMessage(props.flashMessage, msgMap);
      if (message) {
        props.setMessages([message]);
      }
    }
  }, []);

  return (
    <React.Fragment>
      <TopPanel
        title={t("Clusters")}
        icon="spacewalk-icon-clusters"
        button={panelButtons}
        helpUrl="reference/clusters/clusters-overview.html"
      >
        <Table
          data={props.clusters}
          identifier={(row) => row.id}
          initialSortColumnKey="name"
          searchField={<SearchField filter={filterFunc} placeholder={t("Filter by any value")} />}
        >
          <Column
            columnKey="name"
            comparator={Utils.sortByText}
            header={t("Name")}
            cell={(row: ClusterType) => (
              <a className="js-spa" href={`/rhn/manager/cluster/${row.id}`}>
                {row.name}
              </a>
            )}
          />
          <Column
            columnKey="type"
            comparator={Utils.sortByText}
            header={t("Type")}
            cell={(row: ClusterType) => row.provider.name}
          />
          <Column
            columnKey="managementNode"
            comparator={(a, b, ck, sd) =>
              Utils.sortByText(
                { managementNode: a.managementNode.name },
                { managementNode: b.managementNode.name },
                ck,
                sd
              )
            }
            header={t("Management node")}
            cell={(row: ClusterType) => <SystemLink id={row.managementNode.id}>{row.managementNode.name}</SystemLink>}
          />
        </Table>
      </TopPanel>
    </React.Fragment>
  );
};

export default hot(withPageWrapper(withErrorMessages(ListClusters)));
