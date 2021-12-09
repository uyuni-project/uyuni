import { mocked } from "ts-jest/utils";
import { Utils } from "utils/functions";
import Network from "utils/network";

import PagedDataEndpoint from "./paged-data-endpoint";
import PageControl from "./page-control";

jest.mock("../network");

const MockedNetwork = mocked(Network, true);

const PATH = "/my/test/url";
const ORIGIN = "https://my.domain";

describe("paged data endpoint", () => {
  test("Page query parameters", () => {
    const pageControl = new PageControl(1, 10);
    const endpoint = new PagedDataEndpoint(new URL(PATH, ORIGIN));
    endpoint.setPage(pageControl);

    const queryParams = endpoint.uri.searchParams;
    expect(queryParams.get("p")).toBe("1");
    expect(queryParams.get("ps")).toBe("10");
    expect(queryParams.has("q")).toBe(false);
    expect(queryParams.has("s")).toBe(false);
    expect(queryParams.has("sc")).toBe(false);

    endpoint.setPage();
    // Should clear the query parameters
    expect(endpoint.uri.search).toBe("");
  });

  test("Page query filter parameters", () => {
    const pageControl = new PageControl(1, 10, "mystring", "mycolumn");
    const endpoint = new PagedDataEndpoint(new URL(PATH, ORIGIN));
    endpoint.setPage(pageControl);

    const queryParams = endpoint.uri.searchParams;
    expect(queryParams.get("q")).toBe("mystring");
    expect(queryParams.get("s")).toBe("1");
    expect(queryParams.get("sc")).toBe("mycolumn");
  });

  test("Page query sort parameters", () => {
    const pageControl = new PageControl(1, 10, "mystring", "mycolumn", -1);
    const endpoint = new PagedDataEndpoint(new URL(PATH, ORIGIN));
    endpoint.setPage(pageControl);

    const queryParams = endpoint.uri.searchParams;
    expect(queryParams.get("s")).toBe("-1");
    expect(queryParams.get("sc")).toBe("mycolumn");
  });

  test("'Select all' function parameters", () => {
    const endpoint = new PagedDataEndpoint(new URL(PATH, ORIGIN));
    const pageControl = new PageControl(1, 10, "mypagequery", "mycolumn");

    // 'setPage' should be ignored by the 'setSelectAll' call
    endpoint.setPage(pageControl);
    endpoint.setSelectAll();

    const queryParams = endpoint.uri.searchParams;
    expect(queryParams.get("f")).toBe("id");
    expect(queryParams.has("q")).toBe(false);

    // Following page parameters should be absent
    expect(queryParams.has("p")).toBe(false);
    expect(queryParams.has("ps")).toBe(false);
    expect(queryParams.has("q")).toBe(false);
    expect(queryParams.has("s")).toBe(false);
    expect(queryParams.has("sc")).toBe(false);
  });

  test("'Select all' function with filter parameters", () => {
    const endpoint = new PagedDataEndpoint(new URL(PATH, ORIGIN));
    const pageControl = new PageControl(1, 10, "mypagequery", "mycolumn");

    // 'setPage' should be ignored by the 'setSelectAll' call
    endpoint.setPage(pageControl);
    endpoint.setSelectAll("myselectallquery");

    const queryParams = endpoint.uri.searchParams;
    expect(queryParams.get("f")).toEqual("id");
    expect(queryParams.get("q")).toEqual("myselectallquery");
  });

  test("Request call with pagination", (done) => {
    const placeholderResponse = [];
    const endpoint = new PagedDataEndpoint(new URL(PATH, ORIGIN));
    const pageControl = new PageControl(1, 10, "mypagequery", "mycolumn");

    // Mock the 'Network.get' method
    const mockPromise = Promise.resolve(placeholderResponse);
    const mockCancelCallback = jest.fn();
    MockedNetwork.get.mockReturnValue(Utils.cancelable(mockPromise, mockCancelCallback));

    // The test is done when the request promise is resolved
    const mockCallback = jest.fn((promise) =>
      promise.then((response) => {
        // The callback should be called with the Promise object from 'Network.get'
        expect(mockCallback).toBeCalledTimes(1);
        expect(response).toEqual(placeholderResponse);
        expect(mockCancelCallback).not.toBeCalled();
        done();
      })
    );
    endpoint.doGet(mockCallback, pageControl);
  });

  test("Cancelling obsolete requests", (done) => {
    const endpoint = new PagedDataEndpoint(new URL(PATH, ORIGIN));
    const pageControl = new PageControl(1, 10, "mypagequery", "mycolumn");

    // Mock the 'Network.get' method
    const mockPendingPromise = new Promise(() => {});
    const mockCancelCallback = jest.fn();
    MockedNetwork.get.mockReturnValue(Utils.cancelable(mockPendingPromise, mockCancelCallback));

    // Suppress rejections in the mock callback
    // The test is done when the cancelled promise rejection is catched
    const mockCallback = jest.fn((promise) =>
      promise.catch((reason) => {
        expect(reason).toBe("The request is cancelled due to subsequent calls");
        // Cancel callback should've been called exactly once
        expect(mockCancelCallback).toBeCalledTimes(1);
        done();
      })
    );
    endpoint.doGet(mockCallback, pageControl);

    const mockPromise = Promise.resolve();
    MockedNetwork.get.mockReturnValue(Utils.cancelable(mockPromise));
    // The subsequent call should cancel the first promise
    endpoint.doGet(mockCallback, pageControl);
  });
});
