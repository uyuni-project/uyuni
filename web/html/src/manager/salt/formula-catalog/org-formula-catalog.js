/* eslint-disable */

import React from "react";
import {Table} from "components/table/Table";
import {Column} from "components/table/Column";
import {SearchField} from "components/table/SearchField";
import { TopPanel } from "components/panels/TopPanel";
import { Messages } from "components/messages";
import Network from "utils/network";
import withPageWrapper from "../../../components/general/with-page-wrapper";
import {hot} from 'react-hot-loader';

class FormulaCatalog extends React.Component {
    constructor(props) {
        super(props);
        var st = {
            "serverData": [],
            "messages": []
        };
        this.state = st;
    }

    refreshServerData = () => {
        Network.get("/rhn/manager/api/formula-catalog/data").promise.then(data => {
          this.setState({"serverData" : data});
        });
    };

    UNSAFE_componentWillMount() {
        this.refreshServerData();
    }

    sortByText = (aRaw, bRaw, columnKey, sortDirection) => {
        return aRaw.toLowerCase().localeCompare(bRaw.toLowerCase()) * sortDirection;
    };

    rowKey = (rowData) => {
        return rowData;
    };

    searchData = (data, criteria) => {
        return data.filter((row) => row.toLowerCase().includes(criteria.toLowerCase()));
    };

    render() {

        var items=[{severity: "info", text:
          <p>The formula catalog page enables viewing of currently installed <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html" target="_blank">Salt Formulas</a>. Apply these formulas to individual systems or server groups. Formulas allow automatic installation and configuration of software and may be installed via RPM packages.</p>
        }];

        if (this.state.messages.length > 0) {
          items = items.concat(this.state.messages.map(function(msg) {
              return {severity: "info", text: msg};
          }));
        }
        if(this.props.flashMessage){
          items.push({severity: "info", text: this.props.flashMessage});
        }
        if(this.props.warningMessage){
          items.push({severity: "warning", text: this.props.warningMessage});
        }
        return (
            <TopPanel title={t("Formula Catalog")} icon="spacewalk-icon-salt-add" helpUrl="/docs/reference/salt/salt-formula-catalog.html">
            <Messages items={items}/>
            <div>
              <Table
                data={this.state.serverData}
                identifier={this.rowKey}
                initialSortColumnKey="name"
                initialItemsPerPage={userPrefPageSize}
                searchPanel={
                    <SearchField filter={this.searchData}
                      placeholder={t("Filter by formula name")} />
                }>
                <Column
                  columnKey="name"
                  comparator={this.sortByText}
                  header={t("Formula")}
                  cell={ (s) =>
                    <a href={"/rhn/manager/formula-catalog/formula/" + s}>{s}</a> }
                />
              </Table>
            </div>
            </TopPanel>
        );
    }
}

export default hot(module)(withPageWrapper(FormulaCatalog));
