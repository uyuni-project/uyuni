type SortType = {
  direction: number;
  column: string;
};

export default class PageControl {
  page: number;
  pageSize: number;
  query?: string | null;
  sort?: SortType | null;

  constructor(
    page: number,
    pageSize: number,
    query?: string | null,
    sortColumn?: string | null,
    sortDirection: number = 1
  ) {
    this.page = page;
    this.pageSize = pageSize;
    this.query = query;
    if (sortColumn) {
      this.sort = {
        direction: sortDirection,
        column: sortColumn,
      };
    }
  }
}
