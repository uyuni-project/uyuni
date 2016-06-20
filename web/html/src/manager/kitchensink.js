'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Buttons = require("../components/buttons");
const Button = Buttons.Button;
const Panel = require("../components/panel").Panel;
const Table = require("../components/table2").Table;
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Comparators = Functions.Comparators;
const Filters = Functions.Filters;
const Renderer = Functions.Renderer;
const STables = require("../components/tableng.js");
const STable = STables.STable;
const SColumn = STables.SColumn;
const SHeader = STables.SHeader;
const SCell = STables.SCell;



function Highlight(props) {
  var text = props.text;
  var high = props.highlight;

  if (!props.enabled) {
    return <span>{text}</span>
  }

  var chunks = text.split(new RegExp("^" + high));

  return <span>{chunks
    .map((e, i) => e == "" ? <span key="highlight" style={{backgroundColor: "#f0ad4e", borderRadius: "2px"}}>{high}</span> : <span key={i}>{e}</span>)}</span>;
}


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

  sortById(data, direction) {
    return data.sort((a, b) => direction * (a - b));
  }

  sortByName(data, direction) {
    return data.sort((a, b) => direction * a.name.toLowerCase().localeCompare(b.name.toLowerCase()));
  }

  sortByAddress(data, direction) {
    return data.sort((a, b) => direction * a.address.toLowerCase().localeCompare(b.address.toLowerCase()));
  }


  searchData(data, criteria) {
      if (!criteria || criteria == "") {
        return data;
      }
      return data.filter((e) => e.name.startsWith(criteria) || e.address.startsWith(criteria));
  }

  render() {
    return (
    <span>
        <h4>You have selected {this.state.selectedName}</h4>
        <Panel title="Table demo" icon="fa-desktop">
            <STable data={this.state.tableData} searchFn={this.searchData} rowKeyFn={this.rowKey} pageSize={15}>
              <SColumn columnKey="id" width="10%">
                <SHeader sortFn={this.sortById}>{t('Id')}</SHeader>
                <SCell value={ (row) => row.id } />
              </SColumn>
              <SColumn columnKey="name" width="30%">
                <SHeader sortFn={this.sortByName}>{t('Name')}</SHeader>
                <SCell value={ (row, table) => <Highlight enabled={table.state.dataModel.filtered}
                            text={row.name}
                            highlight={table.state.dataModel.filteredText}/>
                    }/>
              </SColumn>
              <SColumn columnKey="address" width="50%">
                <SHeader sortFn={this.sortByAddress}>{t('Address')}</SHeader>
                <SCell value={ (row, table) => <Highlight enabled={table.state.dataModel.filtered}
                            text={row.address}
                            highlight={table.state.dataModel.filteredText}/> } />
              </SColumn>
              <SColumn width="10%">
                <SHeader>{t('Actions')}</SHeader>
                <SCell value={ (row) => <Button text="Select" handler={() => this.setState({selectedName: row.name}) }/> } />
              </SColumn>
            </STable>
        </Panel>
    </span>
    );
  }

}

ReactDOM.render(
  <TableDemo />,
  document.getElementById('tabledemo')
);
