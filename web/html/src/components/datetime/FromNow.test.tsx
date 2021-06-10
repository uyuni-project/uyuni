import * as React from "react";
import { render, screen } from "utils/test-utils";

import { FromNow } from './FromNow';

describe("FromNow component", () => {
    const validISOString = "2020-01-30T23:00:00.000Z";

    test("renders with basic input", () => {
        render(<FromNow time={validISOString} />);
        // Title is given in user's configured time zone
        const span = screen.getByTitle("2020-01-30 15:00 America/Los_Angeles");
        expect(span).toBeDefined();
        expect((span.innerHTML || "").trim()).toBeDefined();
    });
});
