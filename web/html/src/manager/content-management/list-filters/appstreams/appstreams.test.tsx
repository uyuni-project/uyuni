import { render, screen, server } from "utils/test-utils";

import AppStreamsForm from "./appstreams";

describe("AppStreams filter form", () => {
  test("Render form", () => {
    const { rerender } = render(<AppStreamsForm matcher={null} />);

    expect(screen.queryByLabelText("Module Name")).toBeNull();
    expect(screen.queryByLabelText("Stream")).toBeNull();

    rerender(<AppStreamsForm matcher={"module_none"} />);

    expect(screen.queryByLabelText("Module Name")).toBeNull();
    expect(screen.queryByLabelText("Stream")).toBeNull();

    rerender(<AppStreamsForm matcher={"equals"} />);

    expect(screen.getByLabelText("Module Name")).toBeTruthy();
    expect(screen.getByLabelText("Stream")).toBeTruthy();
  });

  test("Browse available modules", () => {
    server.mockGetJson("/rhn/manager/api/channels/modular", {
      success: true,
      data: [],
    });

    render(<AppStreamsForm matcher={"equals"} />);

    screen.getByRole("button", { name: "Browse available modules" }).click();
    expect(screen.findByLabelText("Channel", { exact: false })).toBeTruthy();
  });
});
