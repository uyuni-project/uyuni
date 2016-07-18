"use strict";

const React = require("react");
const StatePersistedMixin = require("../components/util").StatePersistedMixin;
const UtilComponent = require("./subscription-matching-util");
const ToolTip = UtilComponent.ToolTip;
const CsvLink = UtilComponent.CsvLink;
const humanReadablePolicy = UtilComponent.humanReadablePolicy;
const WarningIcon =  require("./subscription-matching-util").WarningIcon;
const {Table, Column, SearchField, Highlight, SimpleTableDataModel} = require("../components/table");

const Subscriptions = React.createClass({
  mixins: [StatePersistedMixin],

  rowComparator: function(aRaw, bRaw, columnKey) {
    var result = 0;
    if (columnKey == "policy") {
      const aValue = humanReadablePolicy(aRaw[columnKey]);
      const bValue = humanReadablePolicy(bRaw[columnKey]);
      result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    }
    else if (columnKey == "quantity") {
      const aMatched = aRaw["matchedQuantity"];
      const aTotal = aRaw["totalQuantity"];
      const bMatched = bRaw["matchedQuantity"];
      const bTotal = bRaw["totalQuantity"];
      const aValue =  aMatched / aTotal;
      const bValue =  bMatched / bTotal;
      result = aValue > bValue ? 1 : (aValue < bValue ? -1 : 0);
    }
    else {
      const aValue = aRaw[columnKey];
      const bValue = bRaw[columnKey];
      result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    }

    if (result == 0) {
      const aId = aRaw["id"];
      const bId = bRaw["id"];
      result = aId > bId ? 1 : (aId < bId ? -1 : 0);
    }
    return result;
  },

  searchData: function(data, criteria) {
    return data.filter((row) => row.description.toLowerCase().includes(criteria.toLowerCase()));
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
            rowClassFn={(row) => moment(row.endDate).isBefore(moment()) ? "text-muted" : null }
            loadState={this.props.loadState}
            saveState={this.props.saveState}
            initialSort="partNumber"
            searchPanel={
                <SearchField searchFn={this.searchData}
                    placeholder={t("Filter by description")}/>
            }>
            <Column
                columnKey="partNumber"
                sortFn={this.rowComparator}
                header={t("Part number")}
                cell={ (row) => row.partNumber }
                />
            <Column
                columnKey="description"
                sortFn={this.rowComparator}
                header={t("Description")}
                cell={ (row) => row.description }
                />
           <Column
                columnKey="policy"
                sortFn={this.rowComparator}
                header={t("Policy")}
                cell={ (row) => humanReadablePolicy(row.policy) }
                />
           <Column
                columnKey="quantity"
                sortFn={this.rowComparator}
                header={t("Matched/Total")}
                cell={ (row) =>
                    <QuantityCell matched={row.matchedQuantity} total={row.totalQuantity} /> }
                />
           <Column
                columnKey="startDate"
                sortFn={this.rowComparator}
                header={t("Start date")}
                cell={ (row) =>
                    <ToolTip content={moment(row.startDate).fromNow()}
                                title={moment(row.startDate).format("LL")} />}
                />
           <Column
                columnKey="endDate"
                sortFn={this.rowComparator}
                header={t("End date")}
                cell={ (row) =>
                    <span>
                        <ToolTip content={moment(row.endDate).fromNow()}
                                    title={moment(row.endDate).format("LL")} />
                        { moment(row.endDate).isBefore(moment().add(6, "months")) &&
                          moment(row.endDate).isAfter(moment()) ?
                           <WarningIcon iconOnRight={true} /> : null }
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
  }

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
