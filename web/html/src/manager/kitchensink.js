'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Buttons = require("../components/buttons");
const Button = Buttons.Button;
const Panel = require("../components/panel").Panel;
const Functions = require("../utils/functions");
const Comparators = Functions.Comparators;
const Filters = Functions.Filters;
const Renderer = Functions.Renderer;
const {Table, Column, SearchField, Highlight} = require("../components/tableng.js");

var tableData = [];

var names = ["Larry", "Moe", "Curly"];
var ni = 0;

for(var i = 0; i < 200; i++) {
    if (ni > names.length-1) {
       ni = 0;
    }
    tableData.push({
        id: i,
        name: names[ni] + "_" + i,
        address: names[ni] + "'s address " + i
    });
    ni++;
}


class TableDemo extends React.Component {

  constructor(props) {
    super();
    ["rowKey", "sortById", "sortByName", "sortByAddress", "searchData"].forEach(method => this[method] = this[method].bind(this));
    this.state = {
        selectedName: null,
        tableData: tableData
    };
  }


  rowKey(rowData) {
    return rowData.id;
  }

  sortById(a, b) {
      if (a.id < b.id) {
        return -1;
      }
      if (a.id > b.id) {
        return 1;
      }
      return 0;
  }

  sortByName(a, b) {
    return a.name.toLowerCase().localeCompare(b.name.toLowerCase());
  }

  sortByAddress(a, b) {
    return a.address.toLowerCase().localeCompare(b.address.toLowerCase());
  }


  searchData(data, criteria) {
      return data.filter((e) => e.name.toLocaleLowerCase().includes(criteria.toLocaleLowerCase()) || e.address.toLocaleLowerCase().includes(criteria.toLocaleLowerCase()));
  }

  render() {
    return (
    <span>
        <h4>You have selected {this.state.selectedName}</h4>
        <Panel title="Table demo" icon="fa-desktop">
            <Table
                data={this.state.tableData}
                rowKeyFn={this.rowKey}
                pageSize={15}
                searchPanel={
                    <SearchField searchFn={this.searchData} placeholder="Search by name or address"/>
                }>
              <Column
                    columnKey="id"
                    width="10%"
                    sortFn={this.sortById}
                    header={<span style={{color: 'red'}}>{t('Id')}</span>}
                    cell={ (row) => row.id }
                    />
              <Column
                    columnKey="name"
                    width="30%"
                    header={<span style={{color: 'yellow'}}>{t('Name')}</span>}
                    sortFn={this.sortByName}
                    cell={(row, table) => <Highlight enabled={table.state.dataModel.filtered}
                                              text={row.name}
                                              highlight={table.state.dataModel.criteria}/>}
                    />
              <Column
                    columnKey="address"
                    width="50%"
                    header={<span style={{color: 'blue'}}>{t('Address')}</span>}
                    sortFn={this.sortByAddress}
                    cell={ (row, table) => <Highlight enabled={table.state.dataModel.filtered}
                                             text={row.address}
                                             highlight={table.state.dataModel.criteria}/> }
                    />
              <Column width="10%"
                    header={t('Actions')}
                    cell={ (row) => <Button text="Select" handler={() => this.setState({selectedName: row.name}) }/> }
                    />
            </Table>
        </Panel>
    </span>
    );
  }

}

ReactDOM.render(
  <TableDemo />,
  document.getElementById('tabledemo')
);
