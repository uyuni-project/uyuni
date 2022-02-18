import { render, screen } from "utils/test-utils";

import { usePrevious } from "./hooks";

describe("usePrevious", () => {
  const TestComponent = (props: { value: string }) => {
    const prevValue = usePrevious(props.value);
    return <p>{prevValue ?? typeof prevValue}</p>;
  };

  test("yields undefined on first render", () => {
    render(<TestComponent value="1" />);
    screen.getByText("undefined");
  });

  test("yields previous value on rerenders", () => {
    const { rerender } = render(<TestComponent value="1" />);
    rerender(<TestComponent value="2" />);
    screen.getByText("1");

    rerender(<TestComponent value="3" />);
    screen.getByText("2");
  });
});
