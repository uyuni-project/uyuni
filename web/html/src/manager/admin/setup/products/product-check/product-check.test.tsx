import { render, screen } from "utils/test-utils";

import { ProductCheck } from "./product-check";

describe("ProductCheck", () => {
  test("renders a checked checkbox", () => {
    render(<ProductCheck selectionState="checked" readOnly />);

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(true);
    expect(checkbox.indeterminate).toBe(false);
  });

  test("renders an indeterminate checkbox", () => {
    render(<ProductCheck selectionState="partially" readOnly />);

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(false);
    expect(checkbox.indeterminate).toBe(true);
  });

  test("renders an unchecked checkbox", () => {
    render(<ProductCheck selectionState="unchecked" readOnly />);

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(false);
    expect(checkbox.indeterminate).toBe(false);
  });

  test("clears the indeterminate state when rerendered as checked", () => {
    const { rerender } = render(<ProductCheck selectionState="partially" readOnly />);

    rerender(<ProductCheck selectionState="checked" readOnly />);

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(true);
    expect(checkbox.indeterminate).toBe(false);
  });

  test("still supports an explicitly checked disabled checkbox", () => {
    render(<ProductCheck checked={true} disabled readOnly />);

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(true);
    expect(checkbox.indeterminate).toBe(false);
    expect(checkbox.disabled).toBe(true);
  });
});
