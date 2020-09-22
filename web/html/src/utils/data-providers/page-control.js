// @flow

type SortType = {
  direction: number,
  column: string
}

export default class PageControl {
  page: number;
  pageSize: number;
  query: ?string;
  sort: ?SortType;

  constructor(page: number, pageSize: number, query: ?string,
      sortColumn: ?string, sortDirection: number = 1) {
    this.page = page;
    this.pageSize = pageSize;
    this.query = query;
    if (sortColumn) {
      this.sort = {
        direction: sortDirection,
        column: sortColumn
      };
    }
  }
}

