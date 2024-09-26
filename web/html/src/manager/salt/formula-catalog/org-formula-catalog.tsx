import { hot } from "react-hot-loader/root";

import * as React from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { Messages, MessageType, ServerMessageType } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import Network from "utils/network";

type Props = {
  flashMessage?: ServerMessageType;
  warningMessage?: ServerMessageType;
};

type State = {
  serverData: any[];
  messages: any[];
};

class FormulaCatalog extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      serverData: [],
      messages: [],
    };
  }

  refreshServerData = () => {
    Network.get("/rhn/manager/api/formula-catalog/data").then((data) => {
      this.setState({ serverData: data });
    });
  };

  UNSAFE_componentWillMount() {
    this.refreshServerData();
  }

  sortByText = (aRaw = "", bRaw = "", columnKey, sortDirection) => {
    return aRaw.toLowerCase().localeCompare(bRaw.toLowerCase()) * sortDirection;
  };

  rowKey = (rowData) => {
    return rowData;
  };

  searchData = (row: string = "", criteria?: string) => {
    return !criteria || row.toLowerCase().includes(criteria.toLowerCase());
  };

  render() {
    var items: MessageType[] = [
      {
        severity: "info",
        text: (
          <p>
            {t(
              "The formula catalog page enables viewing of currently installed <link>Salt Formulas</link>. Apply these formulas to individual systems or server groups. Formulas allow automatic installation and configuration of software and may be installed via RPM packages.",
              {
                link: (str) => (
                  <a
                    href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    {str}
                  </a>
                ),
              }
            )}
          </p>
        ),
      },
    ];

    if (this.state.messages.length > 0) {
      items = items.concat(
        this.state.messages.map(function (msg) {
          return { severity: "info", text: msg };
        })
      );
    }
    if (this.props.flashMessage) {
      items.push({ severity: "info", text: this.props.flashMessage });
    }
    if (this.props.warningMessage) {
      items.push({ severity: "warning", text: this.props.warningMessage });
    }
    return (
      <TopPanel
        title={t("Formula Catalog")}
        icon="spacewalk-icon-salt-add"
        helpUrl="reference/salt/salt-formula-catalog.html"
      >
        <Messages items={items} />
        <div>
          <Table
            data={this.state.serverData}
            identifier={this.rowKey}
            initialSortColumnKey="name"
            searchField={<SearchField filter={this.searchData} placeholder={t("Filter by formula name")} />}
          >
            <Column
              columnKey="name"
              comparator={this.sortByText}
              header={t("Formula")}
              cell={(s) => <a href={"/rhn/manager/formula-catalog/formula/" + s}>{s}</a>}
            />
          </Table>
        </div>
      </TopPanel>
    );
  }
}

export default hot(withPageWrapper(FormulaCatalog));
