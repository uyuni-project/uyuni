/* eslint-disable */
"use strict";

const React = require("react");
const createReactClass = require('create-react-class');
const StatePersistedMixin = require("components/util").StatePersistedMixin;
const UtilComponent = require("./subscription-matching-util");
const ToolTip = UtilComponent.ToolTip;
const CsvLink = UtilComponent.CsvLink;
const humanReadablePolicy = UtilComponent.humanReadablePolicy;
const WarningIcon =  require("./subscription-matching-util").WarningIcon;
const {Table, Column, SearchField, Highlight} = require("components/table");
const Functions = require("utils/functions");
const Utils = Functions.Utils;

const Subscriptions = createReactClass({
  displayName: 'Subscriptions',
  mixins: [StatePersistedMixin],

  sortByPolicy: function(aRaw, bRaw, columnKey, sortDirection) {
    var result = 0;
    const aValue = humanReadablePolicy(aRaw[columnKey]);
    const bValue = humanReadablePolicy(bRaw[columnKey]);
    result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  },

  sortByQuantity: function(aRaw, bRaw, columnKey, sortDirection) {
    var result = 0;
    const aMatched = aRaw["matchedQuantity"];
    const aTotal = aRaw["totalQuantity"];
    const bMatched = bRaw["matchedQuantity"];
    const bTotal = bRaw["totalQuantity"];
    const aValue =  aMatched / aTotal;
    const bValue = bMatched / bTotal;
    result = aValue > bValue ? 1 : (aValue < bValue ? -1 : 0);
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  },

  searchData: function(datum, criteria) {
    if (criteria) {
      return datum.description.toLowerCase().includes(criteria.toLowerCase());
    }
    return true;
  },

  buildRows: function(subscriptions) {
    return Object.keys(subscriptions).map((id) => subscriptions[id]);
  },

  render: function() {
    let body = null;
    if (Object.keys(this.props.subscriptions).length > 0) {
      body = (
        <div>
          <Table
            data={this.buildRows(this.props.subscriptions)}
            identifier={(row) => row.id}
            cssClassFunction={(row) => moment(row.endDate).isBefore(moment()) ? "text-muted" : null }
            loadState={this.props.loadState}
            saveState={this.props.saveState}
            initialSortColumnKey="partNumber"
            initialItemsPerPage={userPrefPageSize}
            searchField={
                <SearchField filter={this.searchData}
                    criteria={""}
                    placeholder={t("Filter by description")} />
            }>
            <Column
              columnKey="partNumber"
              comparator={Utils.sortByText}
              header={t("Part number")}
              cell={ (row) => row.partNumber }
            />
            <Column
              columnKey="description"
              comparator={Utils.sortByText}
              header={t("Description")}
              cell={ (row) => row.description }
            />
            <Column
              columnKey="policy"
              comparator={this.sortByPolicy}
              header={t("Policy")}
              cell={ (row) => humanReadablePolicy(row.policy) }
            />
            <Column
              columnKey="quantity"
              comparator={this.sortByQuantity}
              header={t("Matched/Total")}
              cell={ (row) => <QuantityCell matched={row.matchedQuantity} total={row.totalQuantity} /> }
            />
            <Column
              columnKey="startDate"
              comparator={Utils.sortByText}
              header={t("Start date")}
              cell={ (row) =>
                  <ToolTip content={moment(row.startDate).fromNow()}
                    title={moment(row.startDate).format("LL")} />
              }
            />
            <Column
              columnKey="endDate"
              comparator={Utils.sortByText}
              header={t("End date")}
              cell={ (row) =>
                  <span>
                    <ToolTip content={moment(row.endDate).fromNow()}
                      title={moment(row.endDate).format("LL")} />
                    {
                      moment(row.endDate).isBefore(moment().add(6, "months")) &&
                        moment(row.endDate).isAfter(moment()) ?
                      <WarningIcon iconOnRight={true} /> : null
                    }
                  </span>
              }
            />
          </Table>
          <CsvLink name="subscription_report.csv" />
        </div>
      );
    }
    else {
      body = <p>{t("No subscriptions found.")}</p>
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Your subscriptions")}</h2>
        {body}
      </div>
    );
  },
});

const QuantityCell = (props) => {
  const matched = props.matched;
  const total = props.total;
  const content = matched + "/" + total;

  if (matched == total) {
    return (
        <span>
          <strong>{content}</strong>
          &nbsp;
          <i className="fa fa-exclamation-triangle text-warning"></i>
        </span>
        );
  }
  return <span>{content}</span>;
};

module.exports = {
  Subscriptions: Subscriptions,
}
