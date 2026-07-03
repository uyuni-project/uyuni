import { render, screen } from "@testing-library/react";

import { CounterBadge } from "./CounterBadge";

describe("CounterBadge Component", () => {
  test("renders the counter badge with the correct count", () => {
    render(<CounterBadge count={1} />);
    expect(screen.getByText("1")).toBeTruthy();
  });

  test("renders the counter badge with a large number value", () => {
    render(<CounterBadge count={999} />);
    expect(screen.getByText("999+")).toBeTruthy();
  });

  test("uses the default status class", () => {
    render(<CounterBadge count={1} />);
    const badge = screen.getByText("1");

    expect(badge.className).toContain("badge");
    expect(badge.className).toContain("counter-badge");
    expect(badge.className).toContain("badge--default");
  });

  test("applies the highlight status class", () => {
    render(<CounterBadge count={999} status="highlight" />);
    const badge = screen.getByText("999+");

    expect(badge.className).toContain("badge");
    expect(badge.className).toContain("counter-badge");
    expect(badge.className).toContain("badge--highlight");
  });
});
