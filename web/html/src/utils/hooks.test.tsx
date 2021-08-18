import { usePrevious } from "./hooks";
import { render, screen } from "utils/test-utils";

describe("usePrevious", () => {
  const TestComponent = (props: { value: string }) => {
    const prevValue = usePrevious(props.value);
    return <p>{prevValue ?? typeof prevValue}</p>;
  };

  test("yields undefined on first render", () => {
    render(<TestComponent value="1" />);
    expect(screen.getByText("undefined"));
  });

  test("yields previous value on rerenders", () => {
    const { rerender } = render(<TestComponent value="1" />);
    rerender(<TestComponent value="2" />);
    expect(screen.getByText("1"));

    rerender(<TestComponent value="3" />);
    expect(screen.getByText("2"));
  });
});
