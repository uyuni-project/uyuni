import * as React from "react";
import { ToolTip, CsvLink, humanReadablePolicy } from "./subscription-matching-util";
import { WarningIcon } from "./subscription-matching-util";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Utils } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import { localizedMoment } from "utils";

type SubscriptionsProps = {
  subscriptions: any[];
};

class Subscriptions extends React.Component<SubscriptionsProps> {
  sortByPolicy = (aRaw, bRaw, columnKey, sortDirection) => {
    var result = 0;
    const aValue = humanReadablePolicy(aRaw[columnKey]);
    const bValue = humanReadablePolicy(bRaw[columnKey]);
    result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  };

  sortByQuantity = (aRaw, bRaw, columnKey, sortDirection) => {
    var result = 0;
    const aMatched = aRaw["matchedQuantity"];
    const aTotal = aRaw["totalQuantity"];
    const bMatched = bRaw["matchedQuantity"];
    const bTotal = bRaw["totalQuantity"];
    const aValue = aMatched / aTotal;
    const bValue = bMatched / bTotal;
    result = aValue > bValue ? 1 : aValue < bValue ? -1 : 0;
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  };

  searchData = (datum, criteria) => {
    if (criteria) {
      return datum.description.toLowerCase().includes(criteria.toLowerCase());
    }
    return true;
  };

  buildRows = (subscriptions) => {
    return Object.keys(subscriptions).map((id) => subscriptions[id]);
  };

  render() {
    let body: React.ReactNode = null;
    if (Object.keys(this.props.subscriptions).length > 0) {
      body = (
        <div>
          <Table
            data={this.buildRows(this.props.subscriptions)}
            identifier={(row) => row.id}
            cssClassFunction={(row) => {
              const now = localizedMoment();
              return localizedMoment(row.endDate).isBefore(now) || localizedMoment(row.startDate).isAfter(now)
                ? "text-muted"
                : null;
            }}
            initialSortColumnKey="partNumber"
            initialItemsPerPage={window.userPrefPageSize}
            searchField={<SearchField filter={this.searchData} placeholder={t("Filter by description")} />}
          >
            <Column
              columnKey="partNumber"
              comparator={Utils.sortByText}
              header={t("Part number")}
              cell={(row) => row.partNumber}
            />
            <Column
              columnKey="description"
              comparator={Utils.sortByText}
              header={t("Description")}
              cell={(row) => row.description}
            />
            <Column
              columnKey="policy"
              comparator={this.sortByPolicy}
              header={t("Policy")}
              cell={(row) => humanReadablePolicy(row.policy)}
            />
            <Column
              columnKey="quantity"
              comparator={this.sortByQuantity}
              header={t("Matched/Total")}
              cell={(row) => <QuantityCell matched={row.matchedQuantity} total={row.totalQuantity} />}
            />
            <Column
              columnKey="startDate"
              comparator={Utils.sortByText}
              header={t("Start date")}
              cell={(row) => {
                const startDate = localizedMoment(row.startDate);
                return <ToolTip content={startDate.fromNow()} title={startDate.toUserDateString()} />;
              }}
            />
            <Column
              columnKey="endDate"
              comparator={Utils.sortByText}
              header={t("End date")}
              cell={(row) => {
                const endDate = localizedMoment(row.endDate);
                const isWarning =
                  endDate.isBefore(localizedMoment().add(6, "months")) && endDate.isAfter(localizedMoment());
                return (
                  <span>
                    <ToolTip content={endDate.fromNow()} title={endDate.toUserDateString()} />
                    {isWarning ? <WarningIcon iconOnRight={true} /> : null}
                  </span>
                );
              }}
            />
          </Table>
          <CsvLink name="subscription_report.csv" />
        </div>
      );
    } else {
      body = <p>{t("No subscriptions found.")}</p>;
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Your subscriptions")}</h2>
        {body}
      </div>
    );
  }
}

const QuantityCell = (props) => {
  const matched = props.matched;
  const total = props.total;
  const content = matched + "/" + total;

  if (DEPRECATED_unsafeEquals(matched, total)) {
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

export { Subscriptions };
