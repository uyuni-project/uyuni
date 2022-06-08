import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { AsyncButton } from "components/buttons";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { Highlight } from "components/table/Highlight";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import Network from "utils/network";

function listKeys() {
  return Network.get("/rhn/manager/api/systems/keys");
}

function acceptKey(key: string) {
  return Network.post("/rhn/manager/api/systems/keys/" + key + "/accept");
}

function deleteKey(key: string) {
  return Network.post("/rhn/manager/api/systems/keys/" + key + "/delete");
}

function rejectKey(key: string) {
  return Network.post("/rhn/manager/api/systems/keys/" + key + "/reject");
}

function actionsFor(id, state, update, enabled) {
  const acc = () => (
    <AsyncButton
      disabled={!enabled}
      key="accept"
      title={t("Accept")}
      icon="fa-check"
      action={() => acceptKey(id).then(update)}
    />
  );
  const rej = () => (
    <AsyncButton
      disabled={!enabled}
      key="reject"
      title={t("Reject")}
      icon="fa-times"
      action={() => rejectKey(id).then(update)}
    />
  );
  const del = () => (
    <AsyncButton
      disabled={!enabled}
      key="delete"
      title={t("Delete")}
      icon="fa-trash"
      action={() => deleteKey(id).then(update)}
    />
  );
  const mapping = {
    accepted: [del],
    pending: [acc, rej],
    rejected: [del],
    denied: [del],
  };
  return <div className="pull-right btn-group">{mapping[state].map((fn) => fn())}</div>;
}

const stateMapping = {
  accepted: {
    uiName: t("accepted"),
    label: "success",
  },
  pending: {
    uiName: t("pending"),
    label: "info",
  },
  rejected: {
    uiName: t("rejected"),
    label: "warning",
  },
  denied: {
    uiName: t("denied"),
    label: "danger",
  },
};

function labelFor(state) {
  const mapping = stateMapping[state];
  return <span className={"label label-" + mapping.label}>{mapping.uiName}</span>;
}

type Props = {};

type State = {
  keys: any[];
  isOrgAdmin: boolean;
  loading: boolean;
};

class KeyManagement extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      keys: [],
      isOrgAdmin: false,
      loading: true,
    };
    this.reloadKeys();
  }

  reloadKeys = () => {
    this.setState({ loading: true });
    return listKeys().then((data) => {
      this.setState({
        keys: data["minions"],
        isOrgAdmin: data["isOrgAdmin"],
        loading: false,
      });
    });
  };

  rowKey = (rowData) => {
    return rowData.id;
  };

  searchData = (datum, criteria) => {
    if (criteria) {
      return (
        datum.id.toLocaleLowerCase().includes(criteria.toLocaleLowerCase()) ||
        datum.fingerprint.toLocaleLowerCase().includes(criteria.toLocaleLowerCase())
      );
    }
    return true;
  };

  isFiltered(criteria) {
    return criteria && criteria.length > 0;
  }

  render() {
    const panelButtons = (
      <div className="pull-right btn-group">
        <AsyncButton id="reload" icon="fa-refresh" text="Refresh" action={this.reloadKeys} />
      </div>
    );
    return (
      <span>
        <TopPanel title={t("Keys")} icon="fa-desktop" button={panelButtons} helpUrl="reference/salt/salt-keys.html">
          <Table
            data={this.state.keys}
            identifier={this.rowKey}
            initialSortColumnKey="id"
            loading={this.state.loading}
            searchField={<SearchField filter={this.searchData} placeholder={t("Search by name or fingerprint")} />}
          >
            <Column
              columnKey="id"
              width="30%"
              comparator={Utils.sortByText}
              header={t("Name")}
              cell={(row, criteria) => {
                if (typeof row.sid !== "undefined") {
                  return (
                    <a href={"/rhn/manager/systems/" + row.id}>
                      <Highlight enabled={this.isFiltered(criteria)} text={row.id} highlight={criteria} />
                    </a>
                  );
                } else {
                  return <Highlight enabled={this.isFiltered(criteria)} text={row.id} highlight={criteria} />;
                }
              }}
            />
            <Column
              columnKey="fingerprint"
              width="45%"
              comparator={Utils.sortByText}
              header={t("Fingerprint")}
              cell={(row, criteria) => (
                <Highlight enabled={this.isFiltered(criteria)} text={row.fingerprint} highlight={criteria} />
              )}
            />
            <Column
              columnKey="state"
              width="10%"
              columnClass="text-center"
              headerClass="text-center"
              comparator={Utils.sortByText}
              header={t("State")}
              cell={(row) => labelFor(row.state)}
            />
            <Column
              width="15%"
              columnClass="text-right"
              headerClass="text-right"
              header={t("Actions")}
              cell={(row) => actionsFor(row.id, row.state, this.reloadKeys, this.state.isOrgAdmin)}
            />
          </Table>
        </TopPanel>
      </span>
    );
  }
}

export const renderer = (id) => SpaRenderer.renderNavigationReact(<KeyManagement />, document.getElementById(id));
