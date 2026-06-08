import { createElement } from "react";

import { screen } from "@testing-library/react";

import { render } from "utils/test-utils";

import { type ProductLike, getProductSelectionState } from "./product-check/product-selection.utils";
import { CheckListItem } from "./products";
import { searchCriteriaInExtension } from "./products.utils";

const extension = {
  label: "suse base 1 2 asd",
  channels: [{ summary: "suse base 1 channel 1  " }, { summary: "suse base 1 channel 2  " }],
  extensions: [
    {
      label: "suse extension 1 child 1",
      channels: [{ summary: "suse extension 1 child 1 channel 1" }, { summary: "suse extension 1 child 1 channel 2" }],
      extensions: [],
    },
    {
      label: "suse extension 1 child 2",
      channels: [{ summary: "suse extension 1 child 2 channel 1" }, { summary: "suse extension 1 child 2 channel 2" }],
      extensions: [
        {
          label: "suse extension 2 child 1",
          channels: [
            { summary: "suse extension 2 child 1 channel 1" },
            { summary: "suse extension 2 child 1 channel 2" },
          ],
          extensions: [
            {
              label: "suse extension 3 child 1",
              channels: [
                { summary: "suse extension 3 child 1 channel 1" },
                { summary: "suse extension 3 child 1 channel 2" },
              ],
              extensions: [],
            },
          ],
        },
      ],
    },
  ],
};

describe("Testing searchCriteriaInExtension", () => {
  test("check criteria in base extension label and channels", () => {
    const criteriaLabel = "base 1";
    const criteriaChannel1 = "base 1 channel 1";
    const criteriaChannel2 = "base 1 channel 2";
    const criteriaWrong = "redhat";

    expect(searchCriteriaInExtension(extension, criteriaLabel)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel1)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel2)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaWrong)).toBeFalsy();
  });

  test("check criteria in first level extension label and channels", () => {
    const criteriaLabel = "extension 1 child 1";
    const criteriaChannel1 = "extension 1 child 1 channel 1";
    const criteriaChannel2 = "extension 1 child 1 channel 1";
    const criteriaWrong = "redhat";

    expect(searchCriteriaInExtension(extension, criteriaLabel)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel1)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel2)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaWrong)).toBeFalsy();
  });

  test("check criteria in second level extension label and channels", () => {
    const criteriaLabel = "suse extension 2 child 1";
    const criteriaChannel1 = "extension 2 child 1 channel 1";
    const criteriaChannel2 = "suse extension 2 child 1 channel 2";
    const criteriaWrong = "redhat";

    expect(searchCriteriaInExtension(extension, criteriaLabel)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel1)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel2)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaWrong)).toBeFalsy();
  });

  test("check criteria in third level extension label and channels", () => {
    const criteriaLabel = "extension 3";
    const criteriaChannel1 = "extension 3";
    const criteriaChannel2 = "extension 3";
    const criteriaWrong = "redhat 3 extension";

    expect(searchCriteriaInExtension(extension, criteriaLabel)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel1)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel2)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaWrong)).toBeFalsy();
  });
});

describe("getProductSelectionState", () => {
  const productTree: ProductLike = {
    identifier: "base",
    status: "AVAILABLE",
    extensions: [
      {
        identifier: "recommended-child",
        status: "AVAILABLE",
        extensions: [
          {
            identifier: "grandchild",
            status: "AVAILABLE",
            extensions: [],
          },
        ],
      },
      {
        identifier: "optional-child",
        status: "AVAILABLE",
        extensions: [],
      },
    ],
  };

  test("returns partially when only recommended children are auto-selected", () => {
    const selectedItems = [{ identifier: "base" }, { identifier: "recommended-child" }];

    expect(getProductSelectionState(productTree, selectedItems)).toBe("partially");
  });

  test("returns checked when the whole subtree is selected", () => {
    const selectedItems = [
      { identifier: "base" },
      { identifier: "recommended-child" },
      { identifier: "grandchild" },
      { identifier: "optional-child" },
    ];

    expect(getProductSelectionState(productTree, selectedItems)).toBe("checked");
  });

  test("returns unchecked when neither the product nor its children are selected", () => {
    expect(getProductSelectionState(productTree, [])).toBe("unchecked");
  });

  test("returns checked for a child when all of its direct children are selected", () => {
    const selectedItems = [{ identifier: "recommended-child" }, { identifier: "grandchild" }];

    expect(getProductSelectionState(productTree.extensions![0], selectedItems)).toBe("checked");
  });

  test("keeps the parent partially selected when a direct child is only partially selected", () => {
    const selectedItems = [
      { identifier: "base" },
      { identifier: "recommended-child" },
      { identifier: "optional-child" },
    ];

    expect(getProductSelectionState(productTree, selectedItems)).toBe("partially");
  });

  test("keeps a selected non-root parent partially selected when not all of its own children are selected", () => {
    const selectedItems = [{ identifier: "recommended-child" }];

    expect(getProductSelectionState(productTree.extensions![0], selectedItems)).toBe("partially");
  });

  test("returns partially when a direct child is selected but the parent is not", () => {
    const selectedItems = [{ identifier: "optional-child" }];

    expect(getProductSelectionState(productTree, selectedItems)).toBe("partially");
  });

  test("treats installed direct children as selected", () => {
    const installedTree: ProductLike = {
      identifier: "installed-parent",
      status: "AVAILABLE",
      extensions: [
        {
          identifier: "installed-child",
          status: "INSTALLED",
          extensions: [],
        },
      ],
    };

    const selectedItems = [{ identifier: "installed-parent" }];

    expect(getProductSelectionState(installedTree, selectedItems)).toBe("checked");
  });

  test("ignores unavailable direct children when evaluating selection state", () => {
    const unavailableTree: ProductLike = {
      identifier: "unavailable-parent",
      status: "AVAILABLE",
      extensions: [
        {
          identifier: "available-child",
          status: "AVAILABLE",
          extensions: [],
        },
        {
          identifier: "unavailable-child",
          status: "UNAVAILABLE",
          extensions: [],
        },
      ],
    };

    const selectedItems = [{ identifier: "unavailable-parent" }, { identifier: "available-child" }];

    expect(getProductSelectionState(unavailableTree, selectedItems)).toBe("checked");
  });

  test("matches the rendered list behavior for a multi-level recommended products tree", () => {
    const renderedTree: ProductLike = {
      identifier: "sle-hpc-15-sp2",
      status: "AVAILABLE",
      extensions: [
        {
          identifier: "ltss-15-sp2",
          status: "AVAILABLE",
          extensions: [],
        },
        {
          identifier: "basesystem-15-sp2",
          status: "AVAILABLE",
          extensions: [
            {
              identifier: "python2-15-sp2",
              status: "AVAILABLE",
              extensions: [],
            },
            {
              identifier: "desktop-applications-15-sp2",
              status: "AVAILABLE",
              extensions: [],
            },
            {
              identifier: "server-applications-15-sp2",
              status: "AVAILABLE",
              extensions: [],
            },
          ],
        },
      ],
    };

    const selectedItems: ProductLike[] = [
      { identifier: "sle-hpc-15-sp2" },
      { identifier: "ltss-15-sp2" },
      { identifier: "basesystem-15-sp2" },
      { identifier: "python2-15-sp2" },
      { identifier: "desktop-applications-15-sp2" },
    ];

    expect(getProductSelectionState(renderedTree, selectedItems)).toBe("partially");
    expect(getProductSelectionState(renderedTree.extensions?.[1] as ProductLike, selectedItems)).toBe("partially");
  });
});

describe("CheckListItem checkbox rendering", () => {
  const cols = {
    selector: { width: 2, um: "em" },
    showSubList: { width: 2, um: "em" },
    description: { width: "", um: "" },
    arch: { width: 5, um: "em" },
    channels: { width: 7, um: "em" },
    recommended: { width: 9, um: "em" },
    mix: { width: 4, um: "em" },
  };

  const buildBypassProps = (selectedItems) => ({
    nestedKey: "extensions",
    isSelectable: true,
    selectedItems,
    listStyleClass: "",
    showChannelsfor: jest.fn(),
    cols,
    resyncProduct: jest.fn(),
    scheduledItems: [],
    scheduleResyncItems: [],
    handleVisibleSublist: jest.fn(),
    visibleSubList: [],
    readOnlyMode: false,
  });

  const item = {
    identifier: "base",
    label: "SUSE Linux Enterprise Desktop 15 SP1",
    arch: "x86_64",
    status: "AVAILABLE",
    channels: [],
    extensions: [
      {
        identifier: "basesystem",
        label: "Basesystem Module 15 SP1",
        status: "AVAILABLE",
        channels: [],
        extensions: [
          {
            identifier: "python2",
            label: "Python 2 Module 15 SP1",
            status: "AVAILABLE",
            channels: [],
            extensions: [],
          },
          {
            identifier: "desktop-applications",
            label: "Desktop Applications Module 15 SP1",
            status: "AVAILABLE",
            channels: [],
            extensions: [
              {
                identifier: "workstation-extension",
                label: "SUSE Linux Enterprise Workstation Extension 15 SP1",
                status: "AVAILABLE",
                channels: [],
                extensions: [],
              },
            ],
          },
        ],
      },
    ],
  };

  test("renders an indeterminate checkbox when a direct child is only partially selected", () => {
    render(
      createElement(CheckListItem, {
        item,
        bypassProps: buildBypassProps([
          { identifier: "base" },
          { identifier: "basesystem" },
          { identifier: "python2" },
          { identifier: "desktop-applications" },
        ]),
        handleSelectedItems: jest.fn(),
        handleUnselectedItems: jest.fn(),
        treeLevel: 1,
        childrenDisabled: false,
        index: 0,
      })
    );

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(false);
    expect(checkbox.indeterminate).toBe(true);
  });

  test("renders a checked checkbox when the whole visible subtree is selected", () => {
    render(
      createElement(CheckListItem, {
        item,
        bypassProps: buildBypassProps([
          { identifier: "base" },
          { identifier: "basesystem" },
          { identifier: "python2" },
          { identifier: "desktop-applications" },
          { identifier: "workstation-extension" },
        ]),
        handleSelectedItems: jest.fn(),
        handleUnselectedItems: jest.fn(),
        treeLevel: 1,
        childrenDisabled: false,
        index: 0,
      })
    );

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(true);
    expect(checkbox.indeterminate).toBe(false);
  });

  test("renders an installed product as a checked disabled checkbox", () => {
    render(
      createElement(CheckListItem, {
        item: {
          ...item,
          identifier: "installed-base",
          status: "INSTALLED",
          extensions: [],
        },
        bypassProps: buildBypassProps([]),
        handleSelectedItems: jest.fn(),
        handleUnselectedItems: jest.fn(),
        treeLevel: 1,
        childrenDisabled: false,
        index: 0,
      })
    );

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(true);
    expect(checkbox.indeterminate).toBe(false);
    expect(checkbox.disabled).toBe(true);
  });
});
