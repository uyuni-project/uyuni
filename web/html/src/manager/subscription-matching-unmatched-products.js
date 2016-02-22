"use strict";

var React = require("react");
var TableComponent = require("../components/table");
var Table = TableComponent.Table;
var TableCell = TableComponent.TableCell;
var TableRow = TableComponent.TableRow;
var StatePersistedMixin = require("../components/util").StatePersistedMixin;
var UtilComponent = require("./subscription-matching-util");
var CsvLink = UtilComponent.CsvLink;
var SystemLabel = UtilComponent.SystemLabel;
var PopUp = require("../components/popup").PopUp;

var UnmatchedProducts = React.createClass({
  mixins: [StatePersistedMixin],

  getInitialState: function() {
    return {selectedProductId: null};
  },

  rowComparator: function(a, b, columnIndex, ascending) {
    var columnKeyInRawData = ["productName", "systemCount"];
    var columnKey = columnKeyInRawData[columnIndex];
    var orderCondition = ascending ? 1 : -1;
    var result = 0;
    var aValue = a.props["rawData"][columnKey];
    var bValue = b.props["rawData"][columnKey];
    if (columnKey == "systemCount") {
      result = aValue > bValue ? 1 : (aValue < bValue ? -1 : 0);
    }
    else {
      result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    }

    if (result == 0) {
      var aId = a.props["rawData"]["id"];
      var bId = b.props["rawData"]["id"];
      result = aId > bId ? 1 : (aId < bId ? -1 : 0);
    }

    return result * orderCondition;
  },

  buildRows: function() {
    var products = this.props.products;
    return this.props.unmatchedProductIds.map((pid) => {
      var productName = products[pid].productName;
      var systemCount = products[pid].unmatchedSystemCount;
      var listButton =
          <button
            className="btn btn-default btn-cell"
            onClick={() => {this.showPopUp(pid);}}
            data-toggle="modal"
            data-target="#unmatchedProductsPopUp">
            {t("Show system list")}
          </button>;

      var columns = [
        <TableCell key="name" content={productName} />,
        <TableCell key="cpuCount" content={systemCount} />,
        <TableCell key="button" content={listButton} />,
      ];

      var rawData = {
        id: pid,
        productName: productName,
        systemCount: systemCount
      };

      return <TableRow key={pid} columns={columns} rawData={rawData} />
    });
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
          <Table headers={[t("Product name"), t("Unmatched system count"), ""]}
            rows={this.buildRows()}
            loadState={this.props.loadState}
            saveState={this.props.saveState}
            rowComparator={this.rowComparator}
            sortableColumnIndexes={[0, 1]}
          />
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

var UnmatchedSystemPopUp = React.createClass({
  rowComparator: function(a, b, columnIndex, ascending) {
    var columnKeyInRawData = ["systemName"];
    var columnKey = columnKeyInRawData[columnIndex];
    var orderCondition = ascending ? 1 : -1;
    var result = 0;
    var aValue = a.props["rawData"][columnKey];
    var bValue = b.props["rawData"][columnKey];
    result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    return result * orderCondition;
  },

  buildRows: function(product, systems) {
    return product.unmatchedSystemIds.map((sid) => {
      var systemName = systems[sid].name;
      var column = <TableCell key="system" content={<SystemLabel type={systems[sid].type} name={systemName} />} />;
      var rawData = {systemName: systemName};

      return <TableRow key={sid} columns={[column]} rawData={rawData} />
    });
  },

  render: function() {
    var popUpContent = this.props.selectedProductId == null ?
      null :
      <Table
        headers={[t("System name")]}
        rows={this.buildRows(this.props.products[this.props.selectedProductId], this.props.systems)}
        rowComparator={this.rowComparator}
        sortableColumnIndexes={[0]}
        rowFilter={(tableRow, searchValue) => tableRow.props["rawData"]["systemName"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
        filterPlaceholder={t("Filter by name")}
      />
    ;

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
