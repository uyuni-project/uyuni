// @flow

import PageControl from './page-control';

type PagedData = {
  items: Array<any>,
  total: number
};

export default class SimpleDataProvider {
  data: Array<any>;
  identifier: (row: any) => any;
  filter: ?(row: any, criteria: string) => boolean;
  comparators: ?{column: (a: any, b: any, key: string, direction: number) => number};

  constructor(data: Array<any>, identifier: (row: any) => any,
      filter?: (row: any, criteria: string) => boolean,
      comparators?: {[string]: (a: any, b: any, key: string, direction: number) => number}) {
    this.data = data;
    this.identifier = identifier;
    this.filter = filter;
    this.comparators = comparators;
  }

  get(callback: (promise: Promise<PagedData>) => any, pageControl?: PageControl): void {
    let data = this.data;
    let total = data.length;

    if(pageControl) {
      data = this.getFilteredData(pageControl.query);

      if(this.comparators && pageControl.sort) {
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

    callback(Promise.resolve({items: data, total: total}));
  }

  getIds(callback: (promise: Promise<Array<any>>) => any, criteria?: string) {
    const ids = this.getFilteredData(criteria).map(this.identifier);
    callback(Promise.resolve(ids));
  }

  getFilteredData(criteria: ?string): Array<any> {
    if (criteria && this.filter) {
      return this.data.filter((row) => !this.filter || this.filter(row, criteria));
    }
    return this.data;
  }
}
