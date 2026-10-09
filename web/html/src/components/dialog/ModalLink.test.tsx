import { click, render, screen } from "utils/test-utils";

import { ModalLink } from "./ModalLink";
import { showDialog } from "./util";

jest.mock("./util", () => ({
  showDialog: jest.fn(),
}));

describe("ModalLink", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("does not invoke callbacks or open its target when disabled", async () => {
    const onClick = jest.fn();

    render(<ModalLink target="test-dialog" text="Open dialog" disabled onClick={onClick} />);

    const button = screen.getByRole("button", { name: "Open dialog" });
    expect(button.getAttribute("disabled")).not.toBeNull();

    await click(button);

    expect(onClick).not.toBeCalled();
    expect(showDialog).not.toBeCalled();
  });

  test("passes the item to the callback and opens the target", async () => {
    const item = { id: 42 };
    const onClick = jest.fn();

    render(<ModalLink target="test-dialog" text="Open dialog" item={item} onClick={onClick} />);

    await click(screen.getByRole("button", { name: "Open dialog" }));

    expect(onClick).toBeCalledWith(item);
    expect(showDialog).toBeCalledWith("test-dialog");
  });
});
