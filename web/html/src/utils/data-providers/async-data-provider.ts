import PagedDataEndpoint from "./paged-data-endpoint";
import PageControl from "./page-control";

import { PagedData } from "./index";

export default class AsyncDataProvider {
  endpoint: PagedDataEndpoint;

  constructor(uri: string) {
    this.endpoint = new PagedDataEndpoint(new URL(uri, window.location.origin));
  }

  get(callback: (promise: Promise<PagedData>) => any, pageControl: PageControl) {
    this.endpoint.get(callback, pageControl);
  }

  getIds(callback: (promise: Promise<Array<any>>) => any, criteria?: string) {
    this.endpoint.selectAll(callback, criteria);
  }
}
