import { render } from "utils/test-utils";

import { IconTag } from "./icontag";

/** `<i>` carries no implicit ARIA role, so there is nothing for `screen.getByRole` to match on */
const renderIcon = (ui: React.ReactElement) => render(ui).container.querySelector("i") as HTMLElement;

describe("IconTag", () => {
  test("resolves a semantic type to its icon class", () => {
    expect(renderIcon(<IconTag type="system-unknown" />).className).toEqual("fa fa-question-circle");
    // Spacewalk icons carry no `fa-` prefix, but still need the shared `fa` base class
    expect(renderIcon(<IconTag type="errata-enhance" />).className).toEqual("fa spacewalk-icon-enhancement");
  });

  test("adds the base fa class without duplicating it", () => {
    expect(renderIcon(<IconTag icon="fa-flask" />).className).toEqual("fa fa-flask");
    expect(renderIcon(<IconTag icon="fa fa-question-circle icon-size-lg" />).className).toEqual(
      "fa fa-question-circle icon-size-lg"
    );
  });

  test("appends className to a semantic type", () => {
    expect(renderIcon(<IconTag type="header-info" className="mt-1" />).className).toEqual("fa fa-info-circle mt-1");
  });

  test("appends className to a raw icon", () => {
    expect(renderIcon(<IconTag icon="fa-question-circle" className="icon-size-lg" />).className).toEqual(
      "fa fa-question-circle icon-size-lg"
    );
  });

  test("does not repeat a class the semantic type already resolves to", () => {
    expect(renderIcon(<IconTag type="system-unknown" className="fa-question-circle mt-1" />).className).toEqual(
      "fa fa-question-circle mt-1"
    );
  });

  test("maps every size to its icon-size class", () => {
    const sizes = ["sm", "md", "lg", "xl", "2xl"] as const;

    sizes.forEach((size) => {
      expect(renderIcon(<IconTag icon="fa-flask" size={size} />).className).toEqual(`fa fa-flask icon-size-${size}`);
    });
  });

  test("maps every status to its text colour class", () => {
    const statuses = ["danger", "warning", "success", "info", "muted"] as const;

    statuses.forEach((status) => {
      expect(renderIcon(<IconTag icon="fa-flask" status={status} />).className).toEqual(`fa fa-flask text-${status}`);
    });
  });

  test("appends size and status after the classes of a semantic type", () => {
    expect(renderIcon(<IconTag type="header-info" size="xl" status="warning" />).className).toEqual(
      "fa fa-info-circle icon-size-xl text-warning"
    );
  });

  test("combines size and status with className", () => {
    expect(renderIcon(<IconTag icon="fa-flask" className="mt-1" size="sm" status="muted" />).className).toEqual(
      "fa fa-flask mt-1 icon-size-sm text-muted"
    );
  });

  test("does not repeat a size or status className already spells out", () => {
    expect(renderIcon(<IconTag type="system-unknown" className="icon-size-lg" size="lg" />).className).toEqual(
      "fa fa-question-circle icon-size-lg"
    );
    expect(renderIcon(<IconTag type="system-warn" className="text-warning" status="warning" />).className).toEqual(
      "fa fa-exclamation-triangle text-warning"
    );
  });

  test("keeps both classes when size or status disagrees with className", () => {
    // The later class wins in the stylesheet, so the prop overrides what className asks for
    expect(renderIcon(<IconTag type="system-unknown" className="icon-size-lg" size="sm" />).className).toEqual(
      "fa fa-question-circle icon-size-lg icon-size-sm"
    );
    expect(renderIcon(<IconTag type="item-disabled" className="text-muted" status="danger" />).className).toEqual(
      "fa fa-circle-o text-muted text-danger"
    );
  });

  test("sets the id when one is given", () => {
    expect(renderIcon(<IconTag icon="fa-flask" id="experimental-icon" />).getAttribute("id")).toEqual(
      "experimental-icon"
    );
    expect(renderIcon(<IconTag icon="fa-flask" />).hasAttribute("id")).toBe(false);
  });

  test("hides the icon from assistive technology by default", () => {
    expect(renderIcon(<IconTag icon="fa-flask" />).getAttribute("aria-hidden")).toEqual("true");
  });

  test("exposes the icon to assistive technology when ariaHidden is false", () => {
    expect(renderIcon(<IconTag icon="fa-flask" ariaHidden={false} />).getAttribute("aria-hidden")).toEqual("false");
  });

  test("names the icon for assistive technology when an ariaLabel is given", () => {
    expect(renderIcon(<IconTag icon="fa-flask" ariaLabel="Experimental" />).getAttribute("aria-label")).toEqual(
      "Experimental"
    );
    expect(renderIcon(<IconTag icon="fa-flask" />).hasAttribute("aria-label")).toBe(false);
  });

  test("keeps a labelled icon hidden until the caller also unsets ariaHidden", () => {
    // An aria-label is never announced on a hidden element, so a meaningful icon needs both props
    expect(renderIcon(<IconTag icon="fa-flask" ariaLabel="Experimental" />).getAttribute("aria-hidden")).toEqual(
      "true"
    );

    const labelled = renderIcon(<IconTag icon="fa-flask" ariaLabel="Experimental" ariaHidden={false} />);

    expect(labelled.getAttribute("aria-label")).toEqual("Experimental");
    expect(labelled.getAttribute("aria-hidden")).toEqual("false");
  });

  test("renders the tooltip attributes when a title is given", () => {
    const icon = renderIcon(<IconTag icon="fa-flask" title="Experimental" tooltipPlacement="left" />);

    expect(icon.getAttribute("title")).toEqual("Experimental");
    expect(icon.getAttribute("data-bs-toggle")).toEqual("tooltip");
    expect(icon.getAttribute("data-bs-placement")).toEqual("left");
  });

  test("renders the wide tooltip class when tooltipWide is set together with a title", () => {
    const icon = renderIcon(<IconTag icon="fa-flask" title="Experimental" tooltipWide />);

    expect(icon.getAttribute("data-bs-custom-class")).toEqual("wide-tooltip");
  });

  test("omits the wide tooltip class for a regular tooltip", () => {
    const icon = renderIcon(<IconTag icon="fa-flask" title="Experimental" />);

    expect(icon.hasAttribute("data-bs-custom-class")).toBe(false);
  });

  test("ignores tooltipWide without a title", () => {
    const icon = renderIcon(<IconTag icon="fa-flask" tooltipWide />);

    expect(icon.hasAttribute("data-bs-custom-class")).toBe(false);
    expect(icon.hasAttribute("data-bs-toggle")).toBe(false);
  });

  test("renders no tooltip attributes without a title", () => {
    const icon = renderIcon(<IconTag icon="fa-flask" />);

    expect(icon.hasAttribute("title")).toBe(false);
    expect(icon.hasAttribute("data-bs-toggle")).toBe(false);
  });

  test("omits the placement when only a title is given", () => {
    const icon = renderIcon(<IconTag icon="fa-flask" title="Experimental" />);

    expect(icon.getAttribute("data-bs-toggle")).toEqual("tooltip");
    expect(icon.hasAttribute("data-bs-placement")).toBe(false);
  });

  test("ignores tooltipPlacement without a title", () => {
    const icon = renderIcon(<IconTag icon="fa-flask" tooltipPlacement="left" />);

    expect(icon.hasAttribute("data-bs-placement")).toBe(false);
    expect(icon.hasAttribute("data-bs-toggle")).toBe(false);
  });

  test("keeps a tooltip icon hidden from assistive technology unless the caller says otherwise", () => {
    expect(renderIcon(<IconTag icon="fa-flask" title="Experimental" />).getAttribute("aria-hidden")).toEqual("true");
    expect(
      renderIcon(<IconTag icon="fa-flask" title="Experimental" ariaHidden={false} />).getAttribute("aria-hidden")
    ).toEqual("false");
  });

  test("falls back to the base class rather than a literal undefined for an unknown type", () => {
    expect(renderIcon(<IconTag type="no-such-icon" className="mt-1" />).className).toEqual("fa mt-1");
    expect(renderIcon(<IconTag type="no-such-icon" />).className).toEqual("fa");
  });

  test("resolves every semantic type to the base class plus at least one icon class", () => {
    // A spot check across the map, so a bad edit to an entry shows up as a failing test rather than a blank icon
    const types = [
      "action-ok",
      "errata-security",
      "external-link",
      "header-help",
      "header-multiorg-big",
      "item-del",
      "nav-page-next",
      "spinner",
      "system-bare-metal",
      "experimental",
    ];

    types.forEach((type) => {
      const classNames = renderIcon(<IconTag type={type} />).className.split(" ");

      expect(classNames[0]).toEqual("fa");
      expect(classNames.length).toBeGreaterThan(1);
      expect(classNames).not.toContain("undefined");
    });
  });
});
