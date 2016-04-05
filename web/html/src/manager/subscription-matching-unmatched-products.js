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

const UnmatchedProducts = React.createClass({
  mixins: [StatePersistedMixin],

  getInitialState: function() {
    return {selectedProductId: null};
  },

  rowComparator: function(a, b, columnIndex, ascending) {
    const columnKeyInRawData = ["productName", "systemCount"];
    const columnKey = columnKeyInRawData[columnIndex];
    const orderCondition = ascending ? 1 : -1;
    const aValue = a.props["rawData"][columnKey];
    const bValue = b.props["rawData"][columnKey];

    var result = 0;
    if (columnKey == "systemCount") {
      result = aValue > bValue ? 1 : (aValue < bValue ? -1 : 0);
    }
    else {
      result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    }

    if (result == 0) {
      const aId = a.props["rawData"]["id"];
      const bId = b.props["rawData"]["id"];
      result = aId > bId ? 1 : (aId < bId ? -1 : 0);
    }

    return result * orderCondition;
  },

  buildRows: function() {
    const products = this.props.products;
    return this.props.unmatchedProductIds.map((pid) => {
      const productName = products[pid].productName;
      const systemCount = products[pid].unmatchedSystemCount;
      const listButton =
          <button
            className="btn btn-default btn-cell"
            onClick={() => {this.showPopUp(pid);}}
            data-toggle="modal"
            data-target="#unmatchedProductsPopUp">
            {t("Show system list")}
          </button>;

      const columns = [
        <TableCell key="name" content={productName} />,
        <TableCell key="cpuCount" content={systemCount} />,
        <TableCell key="button" content={listButton} />,
      ];

      const rawData = {
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

const UnmatchedSystemPopUp = React.createClass({
  rowComparator: function(a, b, columnIndex, ascending) {
    const columnKeyInRawData = ["systemName"];
    const columnKey = columnKeyInRawData[columnIndex];
    const orderCondition = ascending ? 1 : -1;
    const aValue = a.props["rawData"][columnKey];
    const bValue = b.props["rawData"][columnKey];
    const result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    return result * orderCondition;
  },

  buildRows: function(product, systems) {
    return product.unmatchedSystemIds.map((sid) => {
      const systemName = systems[sid].name;
      const column = <TableCell key="system" content={<SystemLabel type={systems[sid].type} name={systemName} />} />;
      const rawData = {systemName: systemName};

      return <TableRow key={sid} columns={[column]} rawData={rawData} />
    });
  },

  render: function() {
    const popUpContent = this.props.selectedProductId == null ?
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
