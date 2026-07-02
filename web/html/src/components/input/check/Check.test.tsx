import { render, screen } from "utils/test-utils";

import { Check } from "./Check";

describe("Check", () => {
  test("applies the default input class to the input", () => {
    render(<Check readOnly />);

    expect(screen.getByRole("checkbox").className).toContain("form-check-input");
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

  test("applies className to the label wrapper", () => {
    const { container } = render(<Check className="wrapper-check" readOnly />);

    expect(container.querySelector("label")?.className).toContain("wrapper-check");
  });
});
