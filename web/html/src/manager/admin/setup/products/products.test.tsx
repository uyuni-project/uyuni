import { screen } from "@testing-library/react";

import { render } from "utils/test-utils";

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
    const criteriaChannel2 = "extension 1 child 1 channel 2";
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
      <CheckListItem
        item={item}
        bypassProps={buildBypassProps([
          { identifier: "base" },
          { identifier: "basesystem" },
          { identifier: "python2" },
          { identifier: "desktop-applications" },
        ])}
        handleSelectedItems={jest.fn()}
        handleUnselectedItems={jest.fn()}
        treeLevel={1}
        childrenDisabled={false}
        index={0}
      />
    );

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(false);
    expect(checkbox.indeterminate).toBe(true);
  });

  test("renders a checked checkbox when the whole visible subtree is selected", () => {
    render(
      <CheckListItem
        item={item}
        bypassProps={buildBypassProps([
          { identifier: "base" },
          { identifier: "basesystem" },
          { identifier: "python2" },
          { identifier: "desktop-applications" },
          { identifier: "workstation-extension" },
        ])}
        handleSelectedItems={jest.fn()}
        handleUnselectedItems={jest.fn()}
        treeLevel={1}
        childrenDisabled={false}
        index={0}
      />
    );

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(true);
    expect(checkbox.indeterminate).toBe(false);
  });

  test("renders an installed product as a checked disabled checkbox", () => {
    render(
      <CheckListItem
        item={{
          ...item,
          identifier: "installed-base",
          status: "INSTALLED",
          extensions: [],
        }}
        bypassProps={buildBypassProps([])}
        handleSelectedItems={jest.fn()}
        handleUnselectedItems={jest.fn()}
        treeLevel={1}
        childrenDisabled={false}
        index={0}
      />
    );

    const checkbox = screen.getByRole("checkbox") as HTMLInputElement;
    expect(checkbox.checked).toBe(true);
    expect(checkbox.indeterminate).toBe(false);
    expect(checkbox.disabled).toBe(true);
  });
});
