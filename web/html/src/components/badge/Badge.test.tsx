import { render, screen } from "@testing-library/react";

import { Badge } from "./Badge";

describe("Badge Component", () => {
  test("renders the badge with the correct text", () => {
    render(<Badge text="Active" />);
    expect(screen.getByText("Active")).toBeTruthy();
  });
  test("uses the default status variant and color", () => {
    render(<Badge text="Active" />);
    const badge = screen.getByText("Active");

    expect(badge.className).toContain("badge--status");
    expect(badge.className).toContain("badge--default");
  });

  test("renders the small badge with the correct CSS class", () => {
    render(<Badge text="Active" small />);
    const badge = screen.getByText("Active");

    expect(badge.className).toContain("badge--sm");
  });

  test("uses the default special color", () => {
    render(<Badge text="Active" variant="special" />);
    const badge = screen.getByText("Active");

    expect(badge.className).toContain("badge--special");
    expect(badge.className).toContain("badge--gray");
  });

  test("applies the correct status variant class", () => {
    render(<Badge text="Active" variant="status" color="success" />);
    const badge = screen.getByText("Active");

    expect(badge.className).toContain("badge");
    expect(badge.className).toContain("badge--status");
    expect(badge.className).toContain("badge--success");
  });
  test("applies the correct special variant class", () => {
    render(<Badge text="Active" variant="special" color="green" />);
    const badge = screen.getByText("Active");

    expect(badge.className).toContain("badge");
    expect(badge.className).toContain("badge--special");
    expect(badge.className).toContain("badge--green");
  });

  test("renders an icon when provided", () => {
    render(<Badge text="Active" icon="fa-check" />);
    expect(document.querySelector(".fa-check")).toBeTruthy();
  });
  test("does not render an icon when none is provided", () => {
    render(<Badge text="Active" />);
    expect(document.querySelector("i")).toBeNull();
  });
});
