import PageControl from "./page-control";

import { PagedData, Comparator } from "./index";

export default class SimpleDataProvider {
  data: Array<any>;
  identifier: (row: any) => any;
  filter: ((row: any, criteria: string) => boolean) | null | undefined;
  comparators:
    | {
        [key: string]: Comparator;
      }
    | null
    | undefined;
  loading: boolean | null | undefined;

  constructor(
    data: Array<any>,
    identifier: (row: any) => any,
    filter?: (row: any, criteria: string) => boolean,
    comparators?: {
      [key: string]: Comparator;
    },
    loading?: boolean
  ) {
    this.data = data;
    this.identifier = identifier;
    this.filter = filter;
    this.comparators = comparators;
    this.loading = loading;
  }

  get(callback: (promise: Promise<PagedData>) => any, pageControl?: PageControl): void {
    // Only proceed if loading has finished
    if (this.loading) {
      return;
    }

    let data = this.data;
    let total = data.length;

    if (pageControl) {
      data = this.getFilteredData(pageControl.query);

      if (this.comparators && pageControl.sort) {
        const sort = pageControl.sort;
        const compare = this.comparators[sort.column];
        if (compare) {
          data = data.sort((a, b) => compare(a, b, sort.column, sort.direction));
        }
      }

      total = data.length;
      const firstItemIndex = pageControl.page - 1;
      data = data.slice(firstItemIndex, firstItemIndex + pageControl.pageSize);
    }

    callback(Promise.resolve({ items: data, total: total }));
  }

  getIds(callback: (promise: Promise<Array<any>>) => any, criteria?: string) {
    const ids = this.getFilteredData(criteria).map(this.identifier);
    callback(Promise.resolve(ids));
  }

  getFilteredData(criteria: string | null | undefined): Array<any> {
    if (criteria && this.filter) {
      return this.data.filter((row) => !this.filter || this.filter(row, criteria));
    }
    return this.data;
  }
}
