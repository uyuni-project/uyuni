import SpaRenderer from "core/spa/spa-renderer";

import { renderer } from "./coco-settings.renderer";

jest.mock("core/spa/spa-renderer", () => ({
  __esModule: true,
  default: {
    renderNavigationReact: jest.fn(),
  },
}));

jest.mock("./coco-settings", () => ({
  CoCoSettings: () => null,
}));

type LegacyCocoSettingsGlobals = {
  serverId?: number;
  availableEnvironmentTypes?: Record<string, string>;
};

const legacyWindow = window as Window & LegacyCocoSettingsGlobals;
const renderNavigationReact = SpaRenderer.renderNavigationReact as jest.Mock;

describe("CoCo settings renderer", () => {
  beforeEach(() => {
    document.body.innerHTML = '<div id="coco-settings"></div>';
    delete legacyWindow.serverId;
    delete legacyWindow.availableEnvironmentTypes;
    renderNavigationReact.mockClear();
  });

  test("uses renderer properties", () => {
    const availableEnvironmentTypes = { kvm: "KVM" };

    renderer("coco-settings", { serverId: 123, availableEnvironmentTypes });

    const [element, container] = renderNavigationReact.mock.calls[0];
    expect(element.props).toMatchObject({ serverId: 123, availableEnvironmentTypes });
    expect(container).toBe(document.getElementById("coco-settings"));
  });

  test("supports legacy page globals during mixed-version development", () => {
    legacyWindow.serverId = 456;
    legacyWindow.availableEnvironmentTypes = { amdsev: "AMD SEV" };

    renderer("coco-settings");

    const [element] = renderNavigationReact.mock.calls[0];
    expect(element.props).toMatchObject({
      serverId: 456,
      availableEnvironmentTypes: { amdsev: "AMD SEV" },
    });
  });

  test("does not render without settings data", () => {
    renderer("coco-settings");

    expect(renderNavigationReact).not.toHaveBeenCalled();
  });
});
