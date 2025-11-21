import { Fragment } from "react";

import ReactDOMServer from "react-dom/server";

import { t } from "./index";

describe("new t()", () => {
  test("passthrough", () => {
    expect(t("foo")).toEqual("foo");
    expect(t("undefined")).toEqual("undefined");
    expect(t("")).toEqual("");
    // In case someone acidentally passes an `any` typed variable with a non-string value, try and recover
    expect(t(undefined as any)).toEqual("");
    expect(t(null as any)).toEqual("");
  });

  test("named placeholders", () => {
    expect(t("foo {insert} bar", { insert: "something" })).toEqual("foo something bar");
    expect(t("foo {insert} bar", { insert: undefined })).toEqual("foo  bar");
  });

  test("tags", () => {
    const input = "foo <link>bar</link>";
    const inputArgs = {
      link: (str) => <a href="/">{str}</a>,
    };
    const expected = 'foo <a href="/">bar</a>';

    expect(ReactDOMServer.renderToStaticMarkup(<Fragment key="key">{t(input, inputArgs)}</Fragment>)).toEqual(expected);
  });

  test("tags with named placeholders", () => {
    const input = "foo <link>{insert}</link> bar";
    const inputArgs = {
      insert: "something",
      link: (str) => <a href="/">{str}</a>,
    };
    const expected = 'foo <a href="/">something</a> bar';

    expect(ReactDOMServer.renderToStaticMarkup(<Fragment key="key">{t(input, inputArgs)}</Fragment>)).toEqual(expected);
  });

  // This behavior allows existing `handleResponseError` implementations to pass `{ arg: undefined }` even when there is no arg
  test("extra args are ignored", () => {
    const input = "foo bar";
    const inputArgs = { tea: "cup", and: undefined };
    const expected = "foo bar";

    expect(t(input, inputArgs)).toEqual(expected);
  });
});
