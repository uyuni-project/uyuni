/* eslint-disable */
"use strict";

const React = require("react");
const createReactClass = require('create-react-class');
const StatePersistedMixin = require("components/util").StatePersistedMixin;
const UtilComponent = require("./subscription-matching-util");
const CsvLink = UtilComponent.CsvLink;
const SystemLabel = UtilComponent.SystemLabel;
const PopUp = require("components/popup").PopUp;
const ModalButton = require("components/dialog/ModalButton").ModalButton;
const {Table, Column, SearchField} = require("components/table");
const Functions = require("utils/functions");
const Utils = Functions.Utils;

const UnmatchedProducts = createReactClass({
  displayName: 'UnmatchedProducts',
  mixins: [StatePersistedMixin],

  getInitialState: function() {
    return {
        selectedProductId: null
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

  sortBySystemCount: function(a, b, columnKey, sortDirection) {
    var result = a[columnKey]- b[columnKey];
    return (result || Utils.sortById(a, b)) * sortDirection;
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
          <Table
            data={this.buildData(this.props)}
            identifier={(row) => row.id}
            loadState={this.props.loadState}
            saveState={this.props.saveState}
            initialSortColumnKey="productName"
            initialItemsPerPage={userPrefPageSize}
            >
            <Column
                columnKey="productName"
                comparator={Utils.sortByText}
                header={t("Product name")}
                cell={ (row) => row.productName }
                />
            <Column
                columnKey="systemCount"
                comparator={this.sortBySystemCount}
                header={t("Unmatched system count")}
                cell={ (row) => row.systemCount }
                />
            <Column
                cell={ (row) => <ModalButton
                                className="btn-default btn-cell"
                                title={t("Show system list")}
                                text={t("Show system list")}
                                target="unmatchedProductsPopUp"
                                onClick={() => this.showPopUp(row.id)}
                                /> }
                />
          </Table>

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
  },
});


class UnmatchedSystemPopUp extends React.Component {
  buildTableData = (props) => {
    if (!props.selectedProductId) {
        return [];
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
  };

  searchData = (datum, criteria) => {
    if (criteria) {
      return datum.systemName.toLowerCase().includes(criteria.toLowerCase());
    }
    return true;
  };

  render() {
    const popUpContent = <Table
        data={this.buildTableData(this.props)}
        identifier={(row) => row.id}
        loadState={this.props.loadState}
        saveState={this.props.saveState}
        initialSortColumnKey="systemName"
        initialItemsPerPage={userPrefPageSize}
        searchField={
            <SearchField filter={this.searchData}
              criteria={""}
              placeholder={t("Filter by name")} />
        }>
        <Column
            columnKey="systemName"
            comparator={Utils.sortByText}
            header={t("System name")}
            cell={ (row) => <SystemLabel type={row.type} name={row.systemName} /> } />
      </Table>;

    return (
      <PopUp title={t("Unmatched systems")}
        id="unmatchedProductsPopUp"
        content={popUpContent}
        onClosePopUp={this.props.onClosePopUp}
      />
    );
  }
}

module.exports = {
  UnmatchedProducts: UnmatchedProducts,
}
