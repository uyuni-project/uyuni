import { render, screen } from "utils/test-utils";

import { ProductCheck } from "./ProductCheck";

describe("ProductCheck", () => {
  test.each([
    ["checked", true, false],
    ["partially", false, true],
    ["unchecked", false, false],
  ] as const)("maps selectionState=%s to checked=%s and indeterminate=%s", (selectionState, checked, indeterminate) => {
    render(<ProductCheck selectionState={selectionState} readOnly />);

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(checked);
    expect(checkbox.indeterminate).toBe(indeterminate);
  });

  test("supports the explicit checked state without selectionState", () => {
    render(<ProductCheck checked={true} readOnly />);

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(true);
    expect(checkbox.indeterminate).toBe(false);
  });
});
