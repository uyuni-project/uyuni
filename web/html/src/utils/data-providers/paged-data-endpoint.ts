import { debounce } from "lodash";
import Network from "utils/network";
import PageControl from "./page-control";

import { Cancelable } from "utils/functions";

/**
 * Provides a data endpoint with pagination, filtering and sorting capabilities
 */
export default class PagedDataEndpoint {
  uri: URL;
  get: (callback: (promise: Promise<any>) => any, pageControl: PageControl) => void;
  curReq: Cancelable | null | undefined;

  constructor(uri: URL) {
    this.uri = uri;
    this.get = debounce(this.doGet, 300);
  }

  /**
   * Makes the request to the endpoint to retrieve a page of data
   *
   * The callback function is called with the Promise object as the only
   * parameter as soon as the call is made.
   *
   * Important: This method is wrapped with a debounced version called 'get'.
   * To use this method, call 'PagedDataEndpoint#get()' instead.
   */
  doGet(callback: (promise: Promise<any>) => any, pageControl: PageControl) {
    this.setPage(pageControl);
    this.request(callback);
  }

  /**
   * Prepares and sends a 'select all' request to the endpoint, optionally with
   * a filter criteria string
   */
  selectAll(callback: (promise: Promise<any>) => any, criteria?: string) {
    this.setSelectAll(criteria);
    this.request(callback);
  }

  /**
   * Does the actual request to the server
   */
  request(callback: (promise: Promise<any>) => any) {
    if (this.curReq) {
      this.curReq.cancel("The request is cancelled due to subsequent calls");
    }
    this.curReq = Network.get(this.uri.toString());
    this.curReq.finally(() => (this.curReq = null));
    callback(this.curReq);
  }

  /**
   * Sets the pagination parameters for the request
   *
   * The parameters are;
   *  - p: First item index on the page
   *  - ps: Number of items to retrieve
   *  - q: The query string for filter
   */
  setPage(pageControl?: PageControl) {
    const query = this.getNewQuery();
    if (pageControl) {
      query.set("p", String(pageControl.page));
      query.set("ps", String(pageControl.pageSize));
      if (pageControl.query) {
        query.set("q", pageControl.query);
      }

      const sort = pageControl.sort;
      if (sort) {
        query.set("s", String(sort.direction));
        query.set("sc", sort.column);
      }
    }
  }

  /**
   * Sets the 'select all' query parameters for the request
   *
   * The parameters are;
   *  - q: The query string for the filter (optional)
   *  - f=id: Specifying the 'select all' function
   */
  setSelectAll(criteria?: string) {
    const query = this.getNewQuery();
    query.set("f", "id");
    if (criteria) {
      query.set("q", criteria);
    }
  }

  /**
   * Resets the query string and returns an empty one
   */
  getNewQuery() {
    this.uri.search = "";
    return this.uri.searchParams;
  }
}
