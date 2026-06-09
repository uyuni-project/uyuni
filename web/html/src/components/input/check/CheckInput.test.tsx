import { click, render, screen } from "utils/test-utils";

import { CheckInput } from "./CheckInput";

describe("CheckInput", () => {
  test("syncs the indeterminate DOM property to true", () => {
    render(<CheckInput indeterminate readOnly />);

    expect((screen.getByRole("checkbox") as HTMLInputElement).indeterminate).toBe(true);
  });

  test("clears the indeterminate DOM property when the prop becomes undefined", () => {
    const { rerender } = render(<CheckInput indeterminate readOnly />);
    expect((screen.getByRole("checkbox") as HTMLInputElement).indeterminate).toBe(true);

    rerender(<CheckInput readOnly />);

    expect((screen.getByRole("checkbox") as HTMLInputElement).indeterminate).toBe(false);
  });

  test("invokes onChange with the new boolean value when toggled", async () => {
    const onChange = jest.fn();
    render(<CheckInput checked={false} onChange={onChange} />);

    await click(screen.getByRole("checkbox"));

    expect(onChange).toHaveBeenCalledTimes(1);
    expect(onChange).toHaveBeenCalledWith(true);
  });
});
