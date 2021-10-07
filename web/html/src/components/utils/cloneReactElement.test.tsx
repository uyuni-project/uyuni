import * as React from "react";
import { render, screen } from "utils/test-utils";
import { cloneReactElement } from "./cloneReactElement";

describe("cloneReactElement", () => {
  type Props<T> = T & {
    children?: React.ReactNode;
  };
  function Wrapper<T>(props: Props<T>) {
    const { children, ...rest } = props;
    return (
      <div data-testid="wrapper">
        {React.Children.toArray(props.children).map((child) => cloneReactElement(child, rest))}
      </div>
    );
  }

  test("passes regular DOM nodes through unaffected", () => {
    const children = (
      <>
        <span className="span-class">span content</span>
        <a href="link-href" className="link-class">
          link content
        </a>
        <div className="div-class">
          nested
          <div className="div-class">content</div>
        </div>
      </>
    );
    const unwrappedHTML = render(children).container.innerHTML;
    render(
      <Wrapper foo="foo" bar={42}>
        {children}
      </Wrapper>
    );
    expect(screen.getByTestId("wrapper").innerHTML).toEqual(unwrappedHTML);
  });

  test("passes custom props through to React components", () => {
    type ChildProps = { foo?: string; bar?: number };
    const Child = (props: ChildProps) => (
      <>
        foo value: {props.foo}, bar value: {props.bar}
      </>
    );
    render(
      <Wrapper foo="foo" bar={42}>
        {<Child />}
      </Wrapper>
    );
    expect(screen.getByText("foo value: foo, bar value: 42"));
  });
});
