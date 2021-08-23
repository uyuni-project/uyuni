import * as React from "react";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Utils } from "utils/functions";
import { FromNow } from "components/datetime";

// See java/code/src/com/suse/manager/webui/templates/content_management/view.jade
declare global {
  interface Window {
    imageId?: any;
    isAdmin?: any;
    timezone?: any;
    localTime?: any;
    isRuntimeInfoEnabled?: any;
  }
}

type Props = {
  data: any;
};

class ImageViewPackages extends React.Component<Props> {
  searchData(row, criteria) {
    if (criteria) {
      return (
        row.name.toLocaleLowerCase().includes(criteria.toLocaleLowerCase()) ||
        row.arch.toLocaleLowerCase().includes(criteria.toLocaleLowerCase())
      );
    }
    return true;
  }

  isFiltered(criteria) {
    return criteria && criteria.length > 0;
  }

  render() {
    const data = this.props.data;
    return (
      <Table
        data={data.packagelist ? data.packagelist : []}
        identifier={p => p.name + p.arch}
        initialSortColumnKey="name"
        initialItemsPerPage={window.userPrefPageSize}
        searchField={<SearchField filter={this.searchData} />}
      >
        <Column columnKey="name" comparator={Utils.sortByText} header={t("Package Name")} cell={row => row.name} />
        <Column columnKey="arch" comparator={Utils.sortByText} header={t("Architecture")} cell={row => row.arch} />
        <Column
          columnKey="installed"
          comparator={Utils.sortByDate}
          header={t("Installed")}
          cell={row => <FromNow time={row.installed} />}
        />
      </Table>
    );
  }
}

export { ImageViewPackages };
