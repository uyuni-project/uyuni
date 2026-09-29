import { click, render, screen } from "utils/test-utils";

import { CheckFilterGroup } from "./CheckFilterGroup";

const buildOptions = (overrides = {}) => [
  { label: "API", checked: false, onChange: jest.fn(), ...overrides },
  { label: "Web", checked: true, onChange: jest.fn() },
];

describe("CheckFilterGroup", () => {
  test("reflects the checked state of every option", () => {
    render(<CheckFilterGroup options={buildOptions()} />);

    expect(screen.getByRole<HTMLInputElement>("checkbox", { name: "API" }).checked).toBe(false);
    expect(screen.getByRole<HTMLInputElement>("checkbox", { name: "Web" }).checked).toBe(true);
  });

  test("reports the new state when an option is checked", async () => {
    const options = buildOptions();
    render(<CheckFilterGroup options={options} />);

    await click(screen.getByRole("checkbox", { name: "API" }));

    expect(options[0].onChange).toHaveBeenCalledWith(true);
    expect(options[1].onChange).not.toHaveBeenCalled();
  });

  test("reports the new state when an option is cleared", async () => {
    const options = buildOptions();
    render(<CheckFilterGroup options={options} />);

    await click(screen.getByRole("checkbox", { name: "Web" }));

    expect(options[1].onChange).toHaveBeenCalledWith(false);
  });

  test("renders the default leading label", () => {
    render(<CheckFilterGroup options={buildOptions()} />);

    expect(screen.getByText("Filter by:")).toBeDefined();
  });

  test("falls back to the label as the checkbox id, and prefers an explicit one", () => {
    render(<CheckFilterGroup options={buildOptions({ id: "show-api-only" })} />);

    expect(screen.getByRole("checkbox", { name: "API" }).id).toBe("show-api-only");
    expect(screen.getByRole("checkbox", { name: "Web" }).id).toBe("Web");
  });
});
