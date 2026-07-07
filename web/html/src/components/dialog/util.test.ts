import { hideDialog, showDialog } from "./util";

function renderModalTarget(id: string) {
  document.body.innerHTML = `<div id="${id}" class="modal"></div>`;
}

function renderNonModalTarget(id: string) {
  document.body.innerHTML = `<div id="${id}"></div>`;
}

function mockBootstrapModalPlugin() {
  const originalModal = (jQuery.fn as any).modal;
  const modalMock = jest.fn(function (this: JQuery) {
    return this;
  });

  (jQuery.fn as any).modal = modalMock;

  return {
    modalMock,
    restore: () => {
      if (originalModal === undefined) {
        delete (jQuery.fn as any).modal;
      } else {
        (jQuery.fn as any).modal = originalModal;
      }
    },
  };
}

describe("dialog util", () => {
  let modalMock: jest.Mock;
  let restoreModalPlugin: () => void;

  beforeEach(() => {
    const bootstrapModalPlugin = mockBootstrapModalPlugin();
    modalMock = bootstrapModalPlugin.modalMock;
    restoreModalPlugin = bootstrapModalPlugin.restore;
    document.body.innerHTML = "";
  });

  afterEach(() => {
    document.body.innerHTML = "";
    restoreModalPlugin();
  });

  test("does not invoke Bootstrap for missing targets", () => {
    showDialog("missing-dialog");
    hideDialog("missing-dialog");

    expect(modalMock).not.toBeCalled();
  });

  test("does not invoke Bootstrap for non-modal targets", () => {
    renderNonModalTarget("react-modal-dialog");

    showDialog("react-modal-dialog");
    hideDialog("react-modal-dialog");

    expect(modalMock).not.toBeCalled();
  });

  test("invokes Bootstrap for modal targets", () => {
    renderModalTarget("legacy-dialog");

    showDialog("legacy-dialog");
    expect(modalMock).toBeCalledWith("show");

    modalMock.mockClear();
    hideDialog("legacy-dialog");
    expect(modalMock).toBeCalledWith("hide");
  });
});
