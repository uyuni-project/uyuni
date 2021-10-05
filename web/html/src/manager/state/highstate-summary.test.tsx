import { render, server, within, click, waitForElementToBeRemoved, screen } from "utils/test-utils";
import HighstateSummary from "manager/state/highstate-summary";

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
    await screen.findByRole("table");

    const rows = screen.getAllByRole("row");

    // Table headers
    within(rows[0]).getByRole("columnheader", { name: "State Source" });
    within(rows[0]).getByRole("columnheader", { name: "Type" });
    within(rows[0]).getByRole("columnheader", { name: "Inherited From" });

    within(rows[1]).getByText("Internal states");
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
    await screen.findByRole("table");

    const rows = screen.getAllByRole("row");
    expect(rows.length).toBe(4);

    // 1: Directly assigned state channel
    within(rows[1]).getByRole("link", { name: "My state channel" });
    within(rows[1]).getByText("State channel");
    within(rows[1]).getByText("-");

    // 2: Formula inherited from system group
    within(rows[2]).getByRole("link", { name: "My formula" });
    within(rows[2]).getByText("Formula");
    within(rows[2]).getByRole("link", { name: "My group" });

    // 3: Config channel inherited from org
    within(rows[3]).getByRole("link", { name: "My config channel" });
    within(rows[3]).getByText("Config channel");
    within(rows[3]).getByRole("link", { name: "My org" });
  });
});

describe("Highstate output", () => {
  test("Render highstate output", async () => {
    server.mockGetJson(API_SUMMARY, []);
    server.mockGetJson(API_HIGHSTATE, "my-example-highstate-string");

    render(<HighstateSummary minionId={1000} />);
    await screen.findByText("There are no entries to show.");

    const highstateLink = screen.getByRole("button", { name: "Show full highstate output" }) as HTMLButtonElement;
    expect(highstateLink.disabled).toBeFalsy();
    click(highstateLink);
    expect(highstateLink.disabled).toBeTruthy();
    await waitForElementToBeRemoved(highstateLink);

    screen.getByText("my-example-highstate-string");
  });
});
