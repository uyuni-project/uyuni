import { Fragment } from "react";

import { render, screen } from "utils/test-utils";

import { Messages, Utils as MessagesUtils } from "./messages";

describe("Messages", () => {
  test("uses existing React element keys for list messages", () => {
    const consoleError = jest.spyOn(console, "error").mockImplementation(() => undefined);

    try {
      const items = MessagesUtils.error(
        [<Fragment key="first">First error</Fragment>, <Fragment key="second">Second error</Fragment>],
        true
      );

      render(<Messages items={items} />);

      expect(screen.getAllByRole("listitem")).toHaveLength(2);
      expect(consoleError).not.toHaveBeenCalled();
    } finally {
      consoleError.mockRestore();
    }
  });
});
