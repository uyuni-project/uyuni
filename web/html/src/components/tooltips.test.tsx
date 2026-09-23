import { initializeTooltips } from "./tooltips";

describe("initializeTooltips", () => {
  test("initializes each trigger and its event listeners only once", () => {
    const mutationObserver = jest.spyOn(globalThis, "MutationObserver").mockImplementation(
      () =>
        ({
          observe: jest.fn(),
          disconnect: jest.fn(),
          takeRecords: () => [],
        }) as unknown as MutationObserver
    );
    document.body.innerHTML = '<button data-bs-toggle="tooltip">Help</button>';
    const hide = jest.fn();
    const getOrCreateInstance = jest.fn(() => ({ hide }));
    (globalThis as any).bootstrap = {
      Tooltip: { getOrCreateInstance },
    };

    initializeTooltips();
    initializeTooltips();

    expect(getOrCreateInstance).toBeCalledTimes(1);

    document.querySelector("button")?.dispatchEvent(new Event("click"));
    expect(hide).toBeCalledTimes(1);

    mutationObserver.mockRestore();
  });
});
