import { Comparator, PagedData } from "./index";
import PageControl from "./page-control";

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
  selectable?: boolean | ((row: any) => boolean);

  constructor(
    data: Array<any>,
    identifier: (row: any) => any,
    filter?: (row: any, criteria: string) => boolean,
    comparators?: {
      [key: string]: Comparator;
    },
    loading?: boolean,
    selectable: boolean | ((row: any) => boolean) = false
  ) {
    this.data = data;
    this.identifier = identifier;
    this.filter = filter;
    this.comparators = comparators;
    this.loading = loading;
    this.selectable = selectable;
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
    const filtered = this.getFilteredData(criteria);
    const isSelectable = typeof this.selectable === "boolean" ? undefined : this.selectable;
    const selectable = isSelectable != null ? filtered.filter((item) => isSelectable(item)) : filtered;
    callback(Promise.resolve(selectable.map(this.identifier)));
  }

  getFilteredData(criteria: string | null | undefined): Array<any> {
    if (criteria && this.filter) {
      return this.data.filter((row) => !this.filter || this.filter(row, criteria));
    }
    return this.data;
  }
}
