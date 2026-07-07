import ReactModal from "react-modal";

import { click, render, screen } from "utils/test-utils";

import { Dialog } from "./Dialog";

describe("Dialog", () => {
  beforeAll(() => {
    ReactModal.setAppElement(document.body);
  });

  test("closes ReactModal without Bootstrap dismiss handling", async () => {
    const onClose = jest.fn();

    render(<Dialog id="test-dialog" isOpen title="Title" content={<p>Content</p>} onClose={onClose} />);

    const closeButton = screen.getByLabelText("Close");
    expect(closeButton.getAttribute("data-bs-dismiss")).toBeNull();

    await click(closeButton);

    expect(onClose).toBeCalledTimes(1);
  });
});
