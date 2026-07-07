import HighstateSummary from "manager/state/highstate-summary";

import { click, render, screen, server, waitForElementToBeRemoved, within } from "utils/test-utils";

const API_SUMMARY = "/rhn/manager/api/states/summary?sid=1000";
const API_HIGHSTATE = "/rhn/manager/api/states/highstate?sid=1000";

describe("Highstate summary", () => {
  test("Render summary table", async () => {
    const data = [
      {
        type: "INTERNAL",
      },
    ];
    server.mockGetJson(API_SUMMARY, data);

    render(<HighstateSummary minionId={1000} />);
    expect(await screen.findByRole("table")).toBeDefined();

    const rows = screen.getAllByRole("row");

    // Table headers
    expect(within(rows[0]).getByRole("columnheader", { name: "State Source" })).toBeDefined();
    expect(within(rows[0]).getByRole("columnheader", { name: "Type" })).toBeDefined();
    expect(within(rows[0]).getByRole("columnheader", { name: "Inherited From" })).toBeDefined();

    expect(within(rows[1]).getByText("Internal states")).toBeDefined();
  });

  test("Render state sources", async () => {
    const data = [
      {
        id: 1,
        name: "My state channel",
        type: "STATE",
        sourceId: 1,
        sourceName: "My system",
        sourceType: "SYSTEM",
      },
      {
        id: 2,
        name: "My formula",
        type: "FORMULA",
        sourceId: 2,
        sourceName: "My group",
        sourceType: "GROUP",
      },
      {
        id: 3,
        name: "My config channel",
        type: "CONFIG",
        sourceId: 3,
        sourceName: "My org",
        sourceType: "ORG",
      },
    ];
    server.mockGetJson(API_SUMMARY, data);

    render(<HighstateSummary minionId={1000} />);
    expect(await screen.findByRole("table")).toBeDefined();

    const rows = screen.getAllByRole("row");
    expect(rows.length).toBe(4);

    // 1: Directly assigned state channel
    expect(within(rows[1]).getByRole("link", { name: "My state channel" })).toBeDefined();
    expect(within(rows[1]).getByText("State channel")).toBeDefined();
    expect(within(rows[1]).getByText("-")).toBeDefined();

    // 2: Formula inherited from system group
    expect(within(rows[2]).getByRole("link", { name: "My formula" })).toBeDefined();
    expect(within(rows[2]).getByText("Formula")).toBeDefined();
    expect(within(rows[2]).getByRole("link", { name: "My group" })).toBeDefined();

    // 3: Config channel inherited from org
    expect(within(rows[3]).getByRole("link", { name: "My config channel" })).toBeDefined();
    expect(within(rows[3]).getByText("Config channel")).toBeDefined();
    expect(within(rows[3]).getByRole("link", { name: "My org" })).toBeDefined();
  });
});

describe("Highstate output", () => {
  test("Render highstate output", async () => {
    server.mockGetJson(API_SUMMARY, []);
    server.mockGetJson(API_HIGHSTATE, "my-example-highstate-string");

    render(<HighstateSummary minionId={1000} />);
    expect(await screen.findByText("There are no entries to show.")).toBeDefined();

    const highstateLink = screen.getByRole("button", { name: "Show full highstate output" }) as HTMLButtonElement;
    expect(highstateLink.disabled).toBeFalsy();
    await click(highstateLink);
    expect(highstateLink.disabled).toBeTruthy();
    await waitForElementToBeRemoved(highstateLink);

    expect(await screen.findByText("my-example-highstate-string")).toBeDefined();
  });
});
