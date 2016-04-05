"use strict";

const React = require("react");
const TableComponent = require("../components/table");
const Table = TableComponent.Table;
const TableCell = TableComponent.TableCell;
const TableRow = TableComponent.TableRow;
const StatePersistedMixin = require("../components/util").StatePersistedMixin;
const UtilComponent = require("./subscription-matching-util");
const ToolTip = UtilComponent.ToolTip;
const CsvLink = UtilComponent.CsvLink;
const humanReadablePolicy = UtilComponent.humanReadablePolicy;
const WarningIcon =  require("./subscription-matching-util").WarningIcon;

const Subscriptions = React.createClass({
  mixins: [StatePersistedMixin],

  rowComparator: function(a, b, columnIndex, ascending) {
    const columnKeyInRawData=["partNumber", "description", "policy", "quantity", "startDate", "endDate"];
    const columnKey = columnKeyInRawData[columnIndex];
    const orderCondition = ascending ? 1 : -1;
    const aRaw = a.props["rawData"];
    const bRaw = b.props["rawData"];
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
    return result * orderCondition;
  },

  buildRows: function(subscriptions) {
    return Object.keys(subscriptions).map((id) => {
      const s = subscriptions[id];
      const className = moment(s.endDate).isBefore(moment()) ? "text-muted" : null;
      const warningIcon = moment(s.endDate).isBefore(moment().add(6, "months")) && moment(s.endDate).isAfter(moment()) ?
        <WarningIcon iconOnRight={true} /> : null;

      const columns = [
        <TableCell key="partNumber" content={s.partNumber} />,
        <TableCell key="description" content={s.description} />,
        <TableCell key="policy" content={humanReadablePolicy(s.policy)} />,
        <QuantityCell key="quantity" matched={s.matchedQuantity} total={s.totalQuantity} />,
        <TableCell key="startDate" content={
          <ToolTip content={moment(s.startDate).fromNow()}
            title={moment(s.startDate).format("LL")} />}
        />,
        <TableCell key="endDate" content={
          <span>
            <ToolTip content={moment(s.endDate).fromNow()}
              title={moment(s.endDate).format("LL")} />
            {warningIcon}
          </span>
        } />,
      ];

      return <TableRow key={id} className={className} columns={columns} rawData={s} />
    });
  },

  render: function() {
    var body;
    if (Object.keys(this.props.subscriptions).length > 0) {
      body = (
        <div>
          <Table headers={[t("Part number"), t("Description"), t("Policy"), t("Matched/Total"), t("Start date"), t("End date")]}
            rows={this.buildRows(this.props.subscriptions)}
            loadState={this.props.loadState}
            saveState={this.props.saveState}
            rowFilter={(tableRow, searchValue) => tableRow.props["rawData"]["description"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
            filterPlaceholder={t("Filter by description")}
            rowComparator={this.rowComparator}
            sortableColumnIndexes={[0,1,2,3,4,5]}
          />
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
      <TableCell content={
        <span>
          <strong>{content}</strong>
          &nbsp;
          <i className="fa fa-exclamation-triangle text-warning"></i>
        </span>
      } />
    );
  }
  return <TableCell content={content} />;
}

module.exports = {
  Subscriptions: Subscriptions,
}
