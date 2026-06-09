import { click, render, screen } from "utils/test-utils";

import { Check } from "./Check";

describe("Check", () => {
  test("syncs the indeterminate DOM property to true", () => {
    render(<Check indeterminate readOnly />);

    expect((screen.getByRole("checkbox") as HTMLInputElement).indeterminate).toBe(true);
  });

  test("clears the indeterminate DOM property when the prop becomes undefined", () => {
    const { rerender } = render(<Check indeterminate readOnly />);
    expect((screen.getByRole("checkbox") as HTMLInputElement).indeterminate).toBe(true);

    rerender(<Check readOnly />);

    expect((screen.getByRole("checkbox") as HTMLInputElement).indeterminate).toBe(false);
  });

  test("invokes onChange with the new boolean value when toggled", async () => {
    const onChange = jest.fn();
    render(<Check checked={false} onChange={onChange} />);

    await click(screen.getByRole("checkbox"));

    expect(onChange).toHaveBeenCalledTimes(1);
    expect(onChange).toHaveBeenCalledWith(true);
  });

  test("applies a custom inputClass to the input, overriding the default", () => {
    render(<Check inputClass="custom-check" readOnly />);

    const checkbox = screen.getByRole("checkbox");
    expect(checkbox.className).toBe("custom-check");
    expect(checkbox.className).not.toContain("form-check-input");
  });

  test("renders the label wrapper by default", () => {
    const { container } = render(<Check readOnly />);

    expect(container.querySelector("label")).not.toBeNull();
  });
});
