import { render, screen } from "@testing-library/react";

import { ComplianceBadge } from "./ComplianceBadge";

describe("ComplianceBadge", () => {
  test("shows the default badge when there are no scans", () => {
    render(<ComplianceBadge percentage={0} compliant={0} total={0} />);

    expect(screen.getByText("No Scans").classList).toContain("badge--default");
  });

  test.each([
    [80, 8, "badge--success"],
    [50, 5, "badge--warning"],
    [49, 4, "badge--error"],
  ])("uses the correct color for %s percent", (percentage, compliant, expectedClass) => {
    render(<ComplianceBadge percentage={percentage} compliant={compliant} total={10} />);

    expect(screen.getByText(`${compliant}/10 (${percentage}%)`).classList).toContain(expectedClass);
  });

  test("rounds the displayed percentage", () => {
    render(<ComplianceBadge percentage={66.6} compliant={2} total={3} />);

    expect(screen.getByText("2/3 (67%)")).toBeTruthy();
  });
});
