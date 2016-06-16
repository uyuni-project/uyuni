"use strict";

const React = require("react");
const TableComponent = require("../components/table");
const Table = TableComponent.Table;
const TableCell = TableComponent.TableCell;
const TableRow = TableComponent.TableRow;
const StatePersistedMixin = require("../components/util").StatePersistedMixin;
const UtilComponent = require("./subscription-matching-util");
const CsvLink = UtilComponent.CsvLink;
const SystemLabel = UtilComponent.SystemLabel;
const PopUp = require("../components/popup").PopUp;
const STables = require("../components/tableng.js");
const STable = STables.STable;
const SColumn = STables.SColumn;
const SHeader = STables.SHeader;
const SCell = STables.SCell;

const UnmatchedProducts = React.createClass({
  mixins: [StatePersistedMixin],

  getInitialState: function() {
    return {
        selectedProductId: null,
        tableData: this.buildData(this.props)
    };

  },

  buildData: function(props) {
      const products = props.products;
      return props.unmatchedProductIds.map((pid) => {
          const productName = products[pid].productName;
          const systemCount = products[pid].unmatchedSystemCount;
          return {
                 id: pid,
                 productName: productName,
                 systemCount: systemCount
               };
       });
  },

  rowKey: function(rowData) {
    return rowData.id;
  },

  sortByName: function(data, direction) {
    return data.sort((a, b) => direction *
        a.productName.toLowerCase().localeCompare(b.productName.toLowerCase())
    );
  },

  sortByCpuCount: function(data, direction) {
    return data.sort((a, b) => direction * (a.systemCount - b.systemCount));
  },

  showPopUp: function(id) {
    this.setState({selectedProductId: id});
  },

  closePopUp: function() {
    this.setState({selectedProductId: null});
  },

  render: function() {
    var body;
    if (this.props.unmatchedProductIds.length > 0) {
      body = (
        <div>

          <STable data={this.state.tableData} rowKeyFn={this.rowKey}>
            <SColumn columnKey="name">
                <SHeader sortFn={this.sortByName}>{t("Product name")}</SHeader>
                <SCell value={ (row) => row.productName } />
            </SColumn>
            <SColumn columnKey="cpuCount">
                <SHeader sortFn={this.sortByCpuCount}>{t("Unmatched system count")}</SHeader>
                <SCell value={ (row) => row.systemCount } />
            </SColumn>
            <SColumn>
                <SCell value={ (row) =>  <button
                                className="btn btn-default btn-cell"
                                onClick={() => {this.showPopUp(row.id);}}
                                data-toggle="modal"
                                data-target="#unmatchedProductsPopUp">
                                {t("Show system list")}
                              </button>
                 } />
            </SColumn>
          </STable>

          <CsvLink name="unmatched_product_report.csv" />

          <UnmatchedSystemPopUp
            systems={this.props.systems}
            products={this.props.products}
            selectedProductId={this.state.selectedProductId}
            onClosePopUp={this.closePopUp}
          />
        </div>
      );
    }
    else {
      body = <p>{t("No unmatching products are found.")}</p>
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Unmatched Products")}</h2>
        {body}
      </div>
    );
  }
});


const UnmatchedSystemPopUp = React.createClass({

  getInitialState: function() {
    return {
        tableData: this.buildTableData(this.props)
    };
  },

  buildTableData: function(props) {
    console.log("UnmatchedSystemPopUp.buildTableData");
    if (!props.selectedProductId) {
        return null;
    }
    const product = props.products[props.selectedProductId];
    const systems = props.systems;
    return product.unmatchedSystemIds.map((sid) => {
      return {
        id: sid,
        systemName: systems[sid].name,
        type: systems[sid].type
      }
    });
  },

  sortByName: function(data, direction) {
    return data.sort((a, b) => direction *
        a.systemName.toLowerCase().localeCompare(b.systemName.toLowerCase())
    );
  },

  rowKey: function(rowData) {
    return rowData.id;
  },

  componentWillReceiveProps: function(nextProps) {
    if (!this.state.tableData ||
        this.props.selectedProductId != nextProps.selectedProductId ||
        this.props.products != nextProps.products ||
        this.props.systems != nextProps.systems) {

        this.setState({tableData: this.buildTableData(nextProps)})
    }
  },

  render: function() {
    const popUpContent = !this.state.tableData ?
      null :
      <STable data={this.state.tableData} rowKeyFn={this.rowKey}>
        <SColumn columnKey="name">
            <SHeader sortFn={this.sortByName}>{t("System name")}</SHeader>
            <SCell value={ (row) => <SystemLabel type={row.type} name={row.systemName} /> } />
        </SColumn>
      </STable>;

    return (
      <PopUp title={t("Unmatched systems")}
        id="unmatchedProductsPopUp"
        content={popUpContent}
        onClosePopUp={this.props.onClosePopUp}
      />
    );
  }
});

module.exports = {
  UnmatchedProducts: UnmatchedProducts,
}
