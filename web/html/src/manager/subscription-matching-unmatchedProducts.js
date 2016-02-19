"use strict";

var React = require("react");
var TableComponent = require("../components/table");
var Table = TableComponent.Table;
var TableCell = TableComponent.TableCell;
var TableRow = TableComponent.TableRow;
var StatePersistedMixin = require("../components/util").StatePersistedMixin;
var CsvLink = require("./subscription-matching-util").CsvLink;
var PopUp = require("../components/popup").PopUp;

var UnmatchedProducts = React.createClass({
  mixins: [StatePersistedMixin],

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

  systemsComparator: function(a, b, columnIndex, ascending) {
    var columnKeyInRawData = ["systemName"];
    var columnKey = columnKeyInRawData[columnIndex];
    var orderCondition = ascending ? 1 : -1;
    var result = 0;
    var aValue = a.props["rawData"][columnKey];
    var bValue = b.props["rawData"][columnKey];
    result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    return result * orderCondition;
  },

  getInitialState: function() {
    return {selectedProductId: null};
  },

  showPopUp: function(id) {
    this.setState({selectedProductId: id});
  },

  closePopUp: function() {
    this.setState({selectedProductId: null});
  },

  render: function() {
    var body;
    if (this.props.unmatchedProductIds != null) {
      if (this.props.unmatchedProductIds.length > 0) {
        var popUpContent = this.state.selectedProductId == null ?
          null :
          <Table
            headers={[t("System name")]}
            rows={unmatchedSystemsToRows(this.props.products[this.state.selectedProductId], this.props.systems)}
            rowComparator={this.systemsComparator}
            sortableColumnIndexes={[0]}
            rowFilter={(tableRow, searchValue) => tableRow.props["rawData"]["systemName"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
            filterPlaceholder={t("Filter by name")}
          />
        ;

        body = (
          <div>
            <Table headers={[t("Product name"), t("Unmatched system count"), ""]}
              rows={unmatchedProductsToRows(this)}
              loadState={this.props.loadState}
              saveState={this.props.saveState}
              rowComparator={this.rowComparator}
              sortableColumnIndexes={[0, 1]}
            />
            <CsvLink name="unmatched_product_report.csv" />

            <PopUp title={t("Unmatched systems")}
              id="unmatchedProductsPopUp"
              content={popUpContent}
              onClosePopUp={this.closePopUp}
            />
          </div>
        );
      }
      else {
        body = <p>{t("No unmatching products are found.")}</p>
      }
    }
    else {
      body = <p>{t("Loading...")}</p>
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Unmatched Products")}</h2>
        {body}
      </div>
    );
  }
});

function unmatchedSystemsToRows(product, systems) {
  return product.unmatchedSystemIds.map((sid) => {
    var systemName = systems[sid].name;
    var column = <TableCell content={systemName}/>;
    var rawData = {systemName: systemName};

    return <TableRow columns={[column]} rawData={rawData} />
  });
}

function unmatchedProductsToRows(myParent) {
  var products = myParent.props.products;
  return myParent.props.unmatchedProductIds.map((pid) => {
    var productName = products[pid].productName;
    var systemCount = products[pid].unmatchedSystemCount;
    var listButton =
        <button
          className="btn btn-default btn-cell"
          onClick={function() {myParent.showPopUp(pid);}}
          data-toggle="modal"
          data-target="#unmatchedProductsPopUp">
          {t("Show system list")}
        </button>;

    var columns = [
      <TableCell content={productName} />,
      <TableCell content={systemCount} />,
      <TableCell content={listButton} />,
    ];

    var rawData = {
      id: pid,
      productName: productName,
      systemCount: systemCount
    };

    return <TableRow columns={columns} rawData={rawData} />
  });
}

module.exports = {
  UnmatchedProducts: UnmatchedProducts,
}
