import * as React from "react";
import { useState } from "react";
import { render, screen, type } from "utils/test-utils";
import { SearchField } from "./SearchField";

describe("SearchField", () => {
  /**
   * This component used to switch from uncontrolled to controlled mode and throw an error.
   * The problem happens specifically when the value (the criteria prop) is initially undefined
   * but is then later set to a value.
   * See https://reactjs.org/docs/forms.html#controlled-components and
   */
  test("is consistently controlled", async () => {
    const TestWrapper = () => {
      // This is intentionally undefined, the bug happens specifically on the undefined -> value transition
      const [criteria, setCriteria] = useState<string | undefined>(undefined);

      return <SearchField criteria={criteria} onSearch={(value) => setCriteria(value)} />;
    };

    render(<TestWrapper />);
    const input = screen.getByRole("textbox") as HTMLInputElement;

    const consoleError = jest.spyOn(console, "error");
    // Originally this would trigger the bug, setting the value from undefined to a string
    await type(input, "value");
    expect(consoleError).not.toBeCalled();
  });

  test("is searchable when no criteria is specified", async (done) => {
    render(
      <SearchField
        onSearch={(value) => {
          expect(value).toBe("value");
          done();
        }}
      />
    );
    const input = screen.getByRole("textbox") as HTMLInputElement;
    await type(input, "value");
  });

  test("is searchable when criteria is specified", async () => {
    const TestWrapper = () => {
      const [criteria, setCriteria] = useState("initialValue");
      return <SearchField criteria={criteria} onSearch={(value) => setCriteria(value)} />;
    };

    render(<TestWrapper />);
    const input = screen.getByRole("textbox") as HTMLInputElement;
    await type(input, "newValue");
    expect(input.value).toBe("newValue");
  });
});
