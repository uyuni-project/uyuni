import React from "react";
import { render, screen } from "@testing-library/react";
import Badge from "./Badge";

describe("Badge Component", () => {
  it("renders the badge with the correct text", () => {
    render(<Badge text="Active" />);
    expect(screen.getByText("Active")).toBeTruthy();
  });

  it("applies the correct status variant class", () => {
    render(<Badge text="Active" variant="status" color="success" />);
    const badge = screen.getByText("Active");

    expect(badge.className).toContain("badge");
    expect(badge.className).toContain("badge--status");
    expect(badge.className).toContain("badge--success");
  });
  it("applies the correct special variant class", () => {
    render(<Badge text="Active" variant="special" color="green" />);
    const badge = screen.getByText("Active");

    expect(badge.className).toContain("badge");
    expect(badge.className).toContain("badge--special");
    expect(badge.className).toContain("badge--green");
  });

  it("renders an icon when provided", () => {
    render(<Badge text="Active" icon="fa-check" />);
    expect(document.querySelector(".fa-check")).toBeTruthy();
  });
  it("does not render an icon when none is provided", () => {
    render(<Badge text="Active" />);
    expect(document.querySelector("i")).toBeNull();
  });
});
