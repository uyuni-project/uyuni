/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { SectionState } from "components/FormulaForm";

import { render, screen } from "utils/test-utils";

import Group from "./Group";

type GroupProps = Parameters<typeof Group>[0];

const defaultProps: GroupProps = {
  id: "group",
  header: "Network",
  sectionsExpanded: SectionState.Expanded,
  setSectionsExpanded: jest.fn(),
  isVisibleByCriteria: () => true,
  criteria: "",
};

const renderGroup = (overrides: Partial<GroupProps> = {}) => {
  const props = { ...defaultProps, ...overrides };
  const utils = render(<Group {...props} />);
  return {
    ...utils,
    // Keep the already applied props so tests only have to state what changes
    rerenderGroup: (next: Partial<GroupProps>) => utils.rerender(<Group {...props} {...next} />),
  };
};

const collapsePanel = (container: HTMLElement) => container.querySelector(".panel-collapse");

describe("Group", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("visibility", () => {
    test("renders nothing when the search criteria filters the group out", () => {
      const { container } = renderGroup({ criteria: "nomatch", isVisibleByCriteria: () => false });

      expect(container.innerHTML).toBe("");
    });

    test("renders nothing when no visibility callback is provided", () => {
      const { container } = renderGroup({ isVisibleByCriteria: undefined });

      expect(container.innerHTML).toBe("");
    });
  });

  describe("header", () => {
    test("renders the header in a heading carrying the group id", () => {
      const { container } = renderGroup({ id: "network.settings", header: "Network" });

      const heading = container.querySelector("h4#network\\.settings");
      expect(heading?.textContent).toBe("Network");
    });

    test("renders arbitrary nodes as the header when no criteria is set", () => {
      renderGroup({ header: <span data-testid="custom-header">Network</span> });

      expect(screen.getByTestId("custom-header")).toBeTruthy();
    });

    test("highlights the matching part of the header when a criteria is set", () => {
      renderGroup({ header: "Network", criteria: "two" });

      expect(screen.getByText("two").tagName).toBe("MARK");
      expect(screen.getByText("Ne")).toBeTruthy();
      expect(screen.getByText("rk")).toBeTruthy();
    });

    test("renders the header unhighlighted when the criteria does not match it", () => {
      const { container } = renderGroup({ header: "Network", criteria: "storage" });

      expect(screen.getByText("Network")).toBeTruthy();
      expect(container.querySelector("mark")).toBeNull();
    });

    test("does not highlight anything without a criteria", () => {
      const { container } = renderGroup({ header: "Network", criteria: "" });

      expect(screen.getByText("Network")).toBeTruthy();
      expect(container.querySelector("mark")).toBeNull();
    });
  });

  describe("body", () => {
    test("renders the children", () => {
      renderGroup({ children: <input data-testid="child-field" /> });

      expect(screen.getByTestId("child-field")).toBeTruthy();
    });

    test("renders the help text when it differs from the header", () => {
      renderGroup({ header: "Network", help: "Configure the network interfaces" });

      expect(screen.getByText("Configure the network interfaces")).toBeTruthy();
    });

    test("does not render the help text when it is identical to the header", () => {
      const { container } = renderGroup({ header: "Network", help: "Network" });

      expect(container.querySelectorAll("p")).toHaveLength(0);
    });

    test("does not render an empty paragraph when there is no help text", () => {
      const { container } = renderGroup({ help: undefined });

      expect(container.querySelectorAll("p")).toHaveLength(0);
    });
  });

  describe("collapsing", () => {
    test("replaces dots, hashes and whitespace in the id to build the collapse target", () => {
      const { container } = renderGroup({ id: "net.work#0 settings" });

      expect(container.querySelector("#net-work-0-settings-panel-closable")).toBeTruthy();
    });

    test("starts expanded when the form sections are expanded", () => {
      const { container } = renderGroup({ sectionsExpanded: SectionState.Expanded });

      expect(collapsePanel(container)?.classList.contains("show")).toBe(true);
    });

    test("starts expanded when the form sections are mixed", () => {
      const { container } = renderGroup({ sectionsExpanded: SectionState.Mixed });

      expect(collapsePanel(container)?.classList.contains("show")).toBe(true);
    });

    test("starts collapsed when the form sections are collapsed", () => {
      const { container } = renderGroup({ sectionsExpanded: SectionState.Collapsed });

      expect(collapsePanel(container)?.classList.contains("show")).toBe(false);
    });

    test("collapses when the form sections switch to collapsed", () => {
      const { container, rerenderGroup } = renderGroup({ sectionsExpanded: SectionState.Expanded });

      rerenderGroup({ sectionsExpanded: SectionState.Collapsed });

      expect(collapsePanel(container)?.classList.contains("show")).toBe(false);
    });

    test("expands when the form sections switch to expanded", () => {
      const { container, rerenderGroup } = renderGroup({ sectionsExpanded: SectionState.Collapsed });

      rerenderGroup({ sectionsExpanded: SectionState.Expanded });

      expect(collapsePanel(container)?.classList.contains("show")).toBe(true);
    });

    test("keeps the current state when the form sections switch to mixed", () => {
      const { container, rerenderGroup } = renderGroup({ sectionsExpanded: SectionState.Collapsed });

      rerenderGroup({ sectionsExpanded: SectionState.Mixed });

      expect(collapsePanel(container)?.classList.contains("show")).toBe(false);
    });
  });
});
