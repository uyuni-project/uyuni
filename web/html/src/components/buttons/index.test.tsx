import { render, screen } from "utils/test-utils";

import { LinkButton } from "./index";

describe("LinkButton", () => {
  test("renders the Senna opt-out attribute", () => {
    render(<LinkButton text="Download CSV" href="/report.csv" disableSenna />);

    const link = screen.getByRole("link", { name: "Download CSV" });
    expect(link.getAttribute("data-senna-off")).toEqual("true");
  });

  test("does not render the Senna opt-out attribute when it is disabled", () => {
    render(<LinkButton text="Regular link" href="/regular" disableSenna={false} />);

    const link = screen.getByRole("link", { name: "Regular link" });
    expect(link.hasAttribute("data-senna-off")).toBe(false);
  });

  test("does not implicitly open download links in a new tab", () => {
    render(<LinkButton text="Download CSV" href="/report.csv" download="report.csv" />);

    const link = screen.getByRole("link", { name: "Download CSV" });
    expect(link.getAttribute("download")).toEqual("report.csv");
    expect(link.hasAttribute("target")).toBe(false);
  });

  test("adds safe relationship attributes for links that explicitly open a new tab", () => {
    render(<LinkButton text="External link" href="https://example.com" target="_blank" />);

    const link = screen.getByRole("link", { name: "External link" });
    expect(link.getAttribute("target")).toEqual("_blank");
    expect(link.getAttribute("rel")).toEqual("noopener noreferrer");
  });
});
