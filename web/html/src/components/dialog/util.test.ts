import { hideDialog, showDialog } from "./util";

describe("dialog util", () => {
  let originalModal: unknown;
  let modalMock: jest.Mock;

  beforeEach(() => {
    originalModal = (jQuery.fn as any).modal;
    modalMock = jest.fn(function (this: JQuery) {
      return this;
    });
    (jQuery.fn as any).modal = modalMock;
    document.body.innerHTML = "";
  });

  afterEach(() => {
    document.body.innerHTML = "";

    if (originalModal === undefined) {
      delete (jQuery.fn as any).modal;
    } else {
      (jQuery.fn as any).modal = originalModal;
    }
  });

  test("does not invoke Bootstrap for missing targets", () => {
    showDialog("missing-dialog");
    hideDialog("missing-dialog");

    expect(modalMock).not.toBeCalled();
  });

  test("does not invoke Bootstrap for non-modal targets", () => {
    document.body.innerHTML = '<div id="react-modal-dialog"></div>';

    showDialog("react-modal-dialog");
    hideDialog("react-modal-dialog");

    expect(modalMock).not.toBeCalled();
  });

  test("invokes Bootstrap for modal targets", () => {
    document.body.innerHTML = '<div id="legacy-dialog" class="modal"></div>';

    showDialog("legacy-dialog");
    expect(modalMock).toBeCalledWith("show");

    modalMock.mockClear();
    hideDialog("legacy-dialog");
    expect(modalMock).toBeCalledWith("hide");
  });
});
