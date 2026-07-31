import { click, render, screen } from "utils/test-utils";

import { CheckInput } from "./CheckInput";

describe("CheckInput", () => {
  test("syncs the indeterminate DOM property when the prop changes", () => {
    const { rerender } = render(<CheckInput indeterminate readOnly />);
    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;

    expect(checkbox.indeterminate).toBe(true);

    rerender(<CheckInput readOnly />);

    expect(checkbox.indeterminate).toBe(false);
  });

  test("maps the native change event to a boolean onChange value", async () => {
    const onChange = jest.fn();
    render(<CheckInput checked={false} onChange={onChange} />);

    await click(screen.getByRole("checkbox"));

    expect(onChange).toHaveBeenCalledTimes(1);
    expect(onChange).toHaveBeenCalledWith(true);
  });
});
