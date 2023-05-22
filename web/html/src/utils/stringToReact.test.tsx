import ReactDOMServer from "react-dom/server";

import { stringToReact } from "./stringToReact";

describe("stringToReact", () => {
  const expectRenderToEqual = (
    a: string | JSX.Element | (string | JSX.Element)[],
    b: string | JSX.Element | (string | JSX.Element)[]
  ) => {
    const aResult = ReactDOMServer.renderToStaticMarkup(<>{a}</>);
    const bResult = ReactDOMServer.renderToStaticMarkup(<>{b}</>);

    return expect(aResult).toEqual(bResult);
  };

  test("pure strings pass through unaltered", () => {
    const input = "foo bar 123";
    const expected = input;

    expectRenderToEqual(stringToReact(input), expected);
  });

  test("regular DOM nodes are kept", () => {
    const input = 'preceding text <a href="https://example.com">link</a> following text';
    const expected = ["preceding text ", <a href="https://example.com">link</a>, " following text"];

    expectRenderToEqual(stringToReact(input), expected);
  });

  test("bracketed email addresses are not parsed as tags (bsc#1211469)", () => {
    const input = "preceding text <linux-bugs@example.com> following text";
    const expected = "preceding text linux-bugs@example.com following text";

    expectRenderToEqual(stringToReact(input), expected);
  });

  test("bracketed email addresses don't break surrounding DOM (bsc#1211469)", () => {
    const input = "preceding <b>text <linux-bugs@example.com> following</b> text";
    const expected = ["preceding ", <b>text linux-bugs@example.com following</b>, " text"];

    expectRenderToEqual(stringToReact(input), expected);
  });

  test("bracketed email matcher doesn't break default sanitizer behavior (bsc#1211469)", () => {
    expectRenderToEqual(
      stringToReact("<img src=x onerror=alert(1)//>"),
      // eslint-disable-next-line jsx-a11y/alt-text
      <img src="x" />
    );

    expectRenderToEqual(stringToReact('<script src="foo" />'), "");

    expectRenderToEqual(stringToReact('<script>var a = "foo"</script>'), "");
  });
});
