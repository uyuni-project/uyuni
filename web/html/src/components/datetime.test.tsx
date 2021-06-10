import * as React from "react";
import { render, screen } from "utils/test-utils";

import { DateTime } from './datetime';

describe("Datetime component", () => {
    const validISOString = "2020-01-30T23:00:00.000Z";

    test("renders with basic input", () => {
        render(<DateTime time={validISOString} />);
        // Title is given in user's configured time zone
        const span = screen.getByTitle("2020-01-30T15:00:00.000-08:00");
        expect(span).toBeDefined();
        expect((span.innerHTML || "").trim()).toBeDefined();
    });
});
