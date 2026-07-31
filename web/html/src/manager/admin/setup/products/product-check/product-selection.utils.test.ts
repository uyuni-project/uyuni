import { type ProductLike, getProductSelectionState } from "./product-selection.utils";

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

  const recommendedChild = productTree.extensions?.[0] as ProductLike;
  const select = (...identifiers: string[]): ProductLike[] => identifiers.map((identifier) => ({ identifier }));

  test.each([
    {
      name: "returns partially when only recommended children are auto-selected",
      item: productTree,
      selectedItems: select("base", "recommended-child"),
      expected: "partially",
    },
    {
      name: "returns checked when the whole subtree is selected",
      item: productTree,
      selectedItems: select("base", "recommended-child", "grandchild", "optional-child"),
      expected: "checked",
    },
    {
      name: "returns unchecked when neither the product nor its children are selected",
      item: productTree,
      selectedItems: select(),
      expected: "unchecked",
    },
    {
      name: "returns checked for a child when all of its direct children are selected",
      item: recommendedChild,
      selectedItems: select("recommended-child", "grandchild"),
      expected: "checked",
    },
    {
      name: "keeps the parent partially selected when a direct child is only partially selected",
      item: productTree,
      selectedItems: select("base", "recommended-child", "optional-child"),
      expected: "partially",
    },
    {
      name: "keeps a selected non-root parent partially selected when not all of its own children are selected",
      item: recommendedChild,
      selectedItems: select("recommended-child"),
      expected: "partially",
    },
    {
      name: "returns partially when a direct child is selected but the parent is not",
      item: productTree,
      selectedItems: select("optional-child"),
      expected: "partially",
    },
  ] as const)("$name", ({ item, selectedItems, expected }) => {
    expect(getProductSelectionState(item, selectedItems)).toBe(expected);
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
