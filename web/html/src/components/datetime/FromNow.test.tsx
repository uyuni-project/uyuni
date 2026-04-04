import { render, screen } from "utils/test-utils";

import { FromNow } from "./FromNow";

describe("FromNow component", () => {
  const validISOString = "2020-01-30T23:00:00.000Z";

  test("renders with basic input", () => {
    render(<FromNow value={validISOString} />);
    // Title is given in user's configured time zone
    const element = screen.getByTitle("2020-01-30 15:00 PST");
    expect(element).toBeDefined();
    expect((element.innerHTML || "").trim()).not.toEqual("");
  });
});
