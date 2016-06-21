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
const Tables = require("../components/tableng.js");
const Table = Tables.Table;
const Column = Tables.Column;
const Header = Tables.Header;
const Cell = Tables.Cell;
const SearchPanel = Tables.SearchPanel;
const SearchField = Tables.SearchField;
const Highlight = Tables.Highlight;

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
      // a must be equal to b
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
            <Table data={this.state.tableData} rowKeyFn={this.rowKey} pageSize={15}>
              <SearchPanel>
                <SearchField searchFn={this.searchData}/>
              </SearchPanel>
              <Column columnKey="id" width="10%">
                <Header sortFn={this.sortById}>{t('Id')}</Header>
                <Cell content={ (row) => row.id } />
              </Column>
              <Column columnKey="name" width="30%">
                <Header sortFn={this.sortByName}>{t('Name')}</Header>
                <Cell content={ (row, table) => <Highlight enabled={table.state.dataModel.filtered}
                            text={row.name}
                            highlight={table.state.dataModel.criteria}/>
                    }/>
              </Column>
              <Column columnKey="address" width="50%">
                <Header sortFn={this.sortByAddress}>{t('Address')}</Header>
                <Cell content={ (row, table) => <Highlight enabled={table.state.dataModel.filtered}
                            text={row.address}
                            highlight={table.state.dataModel.criteria}/> } />
              </Column>
              <Column width="10%">
                <Header>{t('Actions')}</Header>
                <Cell content={ (row) => <Button text="Select" handler={() => this.setState({selectedName: row.name}) }/> } />
              </Column>
            </Table>
        </Panel>
    </span>
    );

//    return (
//    <span>
//        <h4>You have selected {this.state.selectedName}</h4>
//        <Panel title="Table demo" icon="fa-desktop">
//            <Table
//                data={this.state.tableData}
//                rowKeyFn={this.rowKey}
//                pageSize={15}
//                searchPanel={
//                    <SearchField searchFn={this.searchData}/>
//                }>
//              <Column
//                    columnKey="id"
//                    width="10%"
//                    sortFn={this.sortById}
//                    header={t('Id')}
//                    cell={ (row) => row.id }>
//              </Column>
//              <Column
//                    columnKey="name"
//                    width="30%"
//                    header=>{t('Name')}
//                    sortFn={this.sortByName}
//                    cell={(row, table) => <Highlight enabled={table.state.dataModel.filtered}
//                                              text={row.name}
//                                              highlight={table.state.dataModel.criteria}/>}>
//              </Column>
//              <Column columnKey="address" width="50%">
//                <Header sortFn={this.sortByAddress}>{t('Address')}</Header>
//                <Cell content={ (row, table) => <Highlight enabled={table.state.dataModel.filtered}
//                            text={row.address}
//                            highlight={table.state.dataModel.criteria}/> } />
//              </Column>
//              <Column width="10%">
//                <Header>{t('Actions')}</Header>
//                <Cell content={ (row) => <Button text="Select" handler={() => this.setState({selectedName: row.name}) }/> } />
//              </Column>
//            </Table>
//        </Panel>
//    </span>
//    );
  }

}

ReactDOM.render(
  <TableDemo />,
  document.getElementById('tabledemo')
);
