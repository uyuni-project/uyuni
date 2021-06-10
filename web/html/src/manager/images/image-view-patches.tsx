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

const typeIcons = {
  "Security Advisory": t("fa fa-shield fa-1-5x"),
  "Bug Fix Advisory": t("fa fa-bug fa-1-5x"),
  "Product Enhancement Advisory": t("fa spacewalk-icon-enhancement fa-1-5x"),
  reboot_suggested: t("fa fa-refresh fa-1-5x"),
  restart_suggested: t("fa fa-archive fa-1-5"),
};

const typeTitles = {
  "Security Advisory": t("Security advisory"),
  "Bug Fix Advisory": t("Bug fix advisory"),
  "Product Enhancement Advisory": t("Product enhancement advisory"),
  reboot_suggested: t("Reboot required"),
  restart_suggested: t("Affects package management stack"),
};

type ImageViewPatchesProps = {
  data: any;
};

class ImageViewPatches extends React.Component<ImageViewPatchesProps> {
  constructor(props) {
    super(props);

    ["renderType"].forEach(method => (this[method] = this[method].bind(this)));
  }

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

  renderType(row) {
    let icon = [<i key={row.type} className={typeIcons[row.type]} title={typeTitles[row.type]} />];

    for (let k of row.keywords) {
      icon.push(<i key={k} className={typeIcons[k]} title={typeTitles[k]} />);
    }

    return icon;
  }

  render() {
    const data = this.props.data;
    return (
      <Table
        data={data.patchlist ? data.patchlist : []}
        identifier={p => p.id}
        initialSortColumnKey="name"
        initialItemsPerPage={window.userPrefPageSize}
        searchField={<SearchField filter={this.searchData} />}
      >
        <Column
          columnKey="type"
          width="10%"
          comparator={Utils.sortByText}
          header={t("Type")}
          cell={row => this.renderType(row)}
        />
        <Column
          columnKey="name"
          comparator={Utils.sortByText}
          header={t("Advisory")}
          cell={row => (
            <a href={"/rhn/errata/details/Details.do?eid=" + row.id} title={t("Details")}>
              {row.name}
            </a>
          )}
        />
        <Column columnKey="synopsis" comparator={Utils.sortByText} header={t("Synopsis")} cell={row => row.synopsis} />
        <Column
          columnKey="update"
          comparator={Utils.sortByDate}
          header={t("Updated")}
          cell={row => <FromNow time={row.update} />}
        />
      </Table>
    );
  }
}

export { ImageViewPatches };
