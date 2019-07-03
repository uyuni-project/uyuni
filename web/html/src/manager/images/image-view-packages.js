/* eslint-disable */
'use strict';

const React = require("react");
const {Table, Column, SearchField} = require("components/table");
const Utils = require("utils/functions").Utils;
const DateTime = require("components/datetime").DateTime;

class ImageViewPackages extends React.Component {
  constructor(props) {
    super(props);
  }

  searchData(row, criteria) {
    if (criteria) {
      return row.name.toLocaleLowerCase().includes(criteria.toLocaleLowerCase()) ||
                row.arch.toLocaleLowerCase().includes(criteria.toLocaleLowerCase());

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
        initialItemsPerPage={userPrefPageSize}
        searchField={<SearchField filter={this.searchData} criteria={""} />}
      >
        <Column
          columnKey="name"
          comparator={Utils.sortByText}
          header={t("Package Name")}
          cell={(row) => row.name}
        />
        <Column
          columnKey="arch"
          comparator={Utils.sortByText}
          header={t("Architecture")}
          cell={(row) => row.arch}
        />
        <Column
          columnKey="installed"
          comparator={Utils.sortByDate}
          header={t("Installed")}
          cell={(row) => <DateTime time={row.installed}/>}
        />
      </Table>
    );
  }
}

module.exports = {
  ImageViewPackages: ImageViewPackages
}
