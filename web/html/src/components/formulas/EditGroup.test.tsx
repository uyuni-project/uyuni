import { SectionState } from "components/FormulaForm";

import { click, render, screen } from "utils/test-utils";

import EditGroup from "./EditGroup";
import { ElementDefinition } from "./FormulaComponentGenerator";

/** Minimal stand-in for the `FormulaForm` instance `EditGroup` talks to */
const mockFormulaForm = (scope = "system", values: any = {}) => ({
  props: { scope },
  handleChange: jest.fn(),
  getFormulaValues: () => values,
});

/** `edit-group` holding a plain list of primitive values */
const primitiveListElement = (overrides: Partial<ElementDefinition> = {}): ElementDefinition => ({
  $type: "edit-group",
  $name: "Servers",
  $scope: "system",
  $minItems: 0,
  $maxItems: 3,
  $newItemValue: "",
  $prototype: { $type: "text", $name: "Server", $scope: "system" },
  ...overrides,
});

/** `edit-group` holding [key, value] pairs of primitive values */
const primitiveDictionaryElement = (overrides: Partial<ElementDefinition> = {}): ElementDefinition => ({
  $type: "edit-group",
  $name: "Labels",
  $scope: "system",
  $minItems: 0,
  $maxItems: 3,
  $newItemValue: ["", ""],
  $prototype: {
    $type: "text",
    $name: "Value",
    $scope: "system",
    $key: { $type: "text", $name: "Key", $scope: "system" },
  },
  ...overrides,
});

/** `edit-group` holding a list of dictionaries, each rendered as its own sub-panel */
const dictionaryListElement = (overrides: Partial<ElementDefinition> = {}): ElementDefinition => ({
  $type: "edit-group",
  $name: "Mount",
  $scope: "system",
  $minItems: 0,
  $maxItems: 3,
  $newItemValue: { path: "", size: "" },
  $prototype: {
    $type: "group",
    $name: "Mount",
    $scope: "system",
    path: { $id: "path", $type: "text", $name: "Path", $scope: "system" },
    size: { $id: "size", $type: "text", $name: "Size", $scope: "system" },
  },
  ...overrides,
});

type RenderOptions = {
  element: ElementDefinition;
  value: any;
  formulaForm?: ReturnType<typeof mockFormulaForm>;
  setSectionsExpanded?: jest.Mock;
  sectionsExpanded?: SectionState;
  disabled?: boolean;
  criteria?: string;
  isVisibleByCriteria?: () => boolean;
};

const renderEditGroup = ({
  element,
  value,
  formulaForm = mockFormulaForm(),
  setSectionsExpanded = jest.fn(),
  sectionsExpanded = SectionState.Expanded,
  disabled = false,
  criteria = "",
  isVisibleByCriteria = () => true,
}: RenderOptions) => {
  const utils = render(
    <EditGroup
      id="group"
      key={element.$name}
      element={element}
      value={value}
      formulaForm={formulaForm}
      disabled={disabled}
      sectionsExpanded={sectionsExpanded}
      setSectionsExpanded={setSectionsExpanded}
      isVisibleByCriteria={isVisibleByCriteria}
      criteria={criteria}
    />
  );
  return { ...utils, formulaForm, setSectionsExpanded };
};

describe("EditGroup", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("visibility", () => {
    test("renders nothing when the search criteria filters the group out", () => {
      const { container } = renderEditGroup({
        element: primitiveListElement(),
        value: ["alpha"],
        criteria: "nomatch",
        isVisibleByCriteria: () => false,
      });

      expect(container.innerHTML).toBe("");
    });

    test("renders nothing when no visibility callback is provided", () => {
      const { container } = render(
        <EditGroup
          id="group"
          key="Servers"
          element={primitiveListElement()}
          value={["alpha"]}
          formulaForm={mockFormulaForm()}
          sectionsExpanded={SectionState.Expanded}
          setSectionsExpanded={jest.fn()}
          criteria=""
        />
      );

      expect(container.innerHTML).toBe("");
    });

    test("highlights the matching part of the group name when a criteria is set", () => {
      renderEditGroup({ element: primitiveListElement(), value: [], criteria: "erv" });

      const mark = screen.getByText("erv");
      expect(mark.tagName).toBe("MARK");
    });

    test("does not highlight the group name without a criteria", () => {
      const { container } = renderEditGroup({ element: primitiveListElement(), value: [] });

      expect(screen.getByText("Servers")).toBeTruthy();
      expect(container.querySelector("mark")).toBeNull();
    });
  });

  describe("help text", () => {
    test("renders $help when it differs from the group name", () => {
      renderEditGroup({
        element: primitiveListElement({ $help: "One entry per server" }),
        value: [],
      });

      expect(screen.getByText("One entry per server")).toBeTruthy();
    });

    test("does not render $help when it is identical to the group name", () => {
      const { container } = renderEditGroup({
        element: primitiveListElement({ $help: "Servers" }),
        value: [],
      });

      expect(container.querySelectorAll("p")).toHaveLength(0);
    });
  });

  describe("collapsing", () => {
    test("starts collapsed when the form sections are collapsed", () => {
      const { container } = renderEditGroup({
        element: primitiveListElement(),
        value: [],
        sectionsExpanded: SectionState.Collapsed,
      });

      expect(container.querySelector(".panel-collapse")?.classList.contains("show")).toBe(false);
    });

    test("expands when the form sections switch to expanded", () => {
      const element = primitiveListElement();
      const { container, rerender } = renderEditGroup({
        element,
        value: [],
        sectionsExpanded: SectionState.Collapsed,
      });

      rerender(
        <EditGroup
          id="group"
          key={element.$name}
          element={element}
          value={[]}
          formulaForm={mockFormulaForm()}
          sectionsExpanded={SectionState.Expanded}
          setSectionsExpanded={jest.fn()}
          isVisibleByCriteria={() => true}
          criteria=""
        />
      );

      expect(container.querySelector(".panel-collapse")?.classList.contains("show")).toBe(true);
    });

    test("keeps the current state when the form sections switch to mixed", () => {
      const element = primitiveListElement();
      const { container, rerender } = renderEditGroup({
        element,
        value: [],
        sectionsExpanded: SectionState.Collapsed,
      });

      rerender(
        <EditGroup
          id="group"
          key={element.$name}
          element={element}
          value={[]}
          formulaForm={mockFormulaForm()}
          sectionsExpanded={SectionState.Mixed}
          setSectionsExpanded={jest.fn()}
          isVisibleByCriteria={() => true}
          criteria=""
        />
      );

      expect(container.querySelector(".panel-collapse")?.classList.contains("show")).toBe(false);
    });
  });

  describe("adding items", () => {
    test("appends a copy of $newItemValue and reports the change", async () => {
      const value = ["alpha"];
      const { formulaForm, setSectionsExpanded } = renderEditGroup({
        element: primitiveListElement({ $newItemValue: "default" }),
        value,
      });

      await click(screen.getByRole("button", { name: "Add Servers" }));

      expect(formulaForm.handleChange).toHaveBeenCalledWith({ id: "group", value: ["alpha", "default"] });
      expect(setSectionsExpanded).toHaveBeenCalledWith(SectionState.Mixed);
    });

    test("deep copies object item templates so items do not share state", async () => {
      const newItemValue = { path: "", size: "" };
      const { formulaForm } = renderEditGroup({
        element: dictionaryListElement({ $newItemValue: newItemValue }),
        value: [],
      });

      await click(screen.getByRole("button", { name: "Add Mount" }));

      const added = formulaForm.handleChange.mock.calls[0][0].value[0];
      expect(added).toEqual(newItemValue);
      expect(added).not.toBe(newItemValue);
    });

    test("disables the add button with an explanatory title once $maxItems is reached", () => {
      renderEditGroup({
        element: primitiveListElement({ $maxItems: 2 }),
        value: ["alpha", "beta"],
      });

      // Once a title is set it becomes the button's accessible name, so the label is no longer queryable by name
      const addButton = screen.getByTitle("Max number of items reached") as HTMLButtonElement;
      expect(addButton.disabled).toBe(true);
      expect(addButton.textContent).toBe("Add Servers");
    });

    test("does not add an item when $maxItems is reached", async () => {
      const { formulaForm } = renderEditGroup({
        element: primitiveListElement({ $maxItems: 1 }),
        value: ["alpha"],
      });

      await click(screen.getByTitle("Max number of items reached"));

      expect(formulaForm.handleChange).not.toHaveBeenCalled();
    });

    test("does not add an item for a readonly element", async () => {
      const { formulaForm } = renderEditGroup({
        element: primitiveListElement({ $scope: "readonly" }),
        value: [],
      });

      await click(screen.getByRole("button", { name: "Add Servers" }));

      expect(formulaForm.handleChange).not.toHaveBeenCalled();
    });

    test("does not add an item when the element scope does not match the form scope", async () => {
      const { formulaForm } = renderEditGroup({
        element: primitiveListElement({ $scope: "group" }),
        value: [],
        formulaForm: mockFormulaForm("system"),
      });

      await click(screen.getByRole("button", { name: "Add Servers" }));

      expect(formulaForm.handleChange).not.toHaveBeenCalled();
    });
  });

  describe("list of primitive values", () => {
    test("renders one input per item", () => {
      const { container } = renderEditGroup({
        element: primitiveListElement(),
        value: ["alpha", "beta"],
      });

      const inputs = container.querySelectorAll<HTMLInputElement>("#group\\$elements input");
      expect(Array.from(inputs).map((input) => input.value)).toEqual(["alpha", "beta"]);
    });

    test("shows the empty state instead of items when there is nothing to show", () => {
      renderEditGroup({ element: primitiveListElement(), value: [] });

      expect(screen.getByText("No items added yet. Use", { exact: false })).toBeTruthy();
      expect(screen.getByText("Add Servers", { selector: "strong" })).toBeTruthy();
    });

    test("ignores the $meta key when listing items", () => {
      const value: any = ["alpha"];
      value.$meta = { some: "metadata" };

      const { container } = renderEditGroup({ element: primitiveListElement(), value });

      expect(container.querySelectorAll("#group\\$elements input")).toHaveLength(1);
    });

    test("removes the item belonging to the clicked remove button", async () => {
      const value = ["alpha", "beta", "gamma"];
      const { formulaForm } = renderEditGroup({ element: primitiveListElement(), value });

      const removeButtons = screen.getAllByRole("button", { name: "Remove item" });
      await click(removeButtons[1]);

      expect(formulaForm.handleChange).toHaveBeenCalledWith({ id: "group", value: ["alpha", "gamma"] });
    });

    test("disables the remove buttons once $minItems is reached", () => {
      renderEditGroup({
        element: primitiveListElement({ $minItems: 2 }),
        value: ["alpha", "beta"],
      });

      const removeButtons = screen.getAllByRole("button", { name: "Min number of items reached" });
      expect(removeButtons).toHaveLength(2);
      removeButtons.forEach((button) => expect((button as HTMLButtonElement).disabled).toBe(true));
    });
  });

  describe("dictionary of primitive values", () => {
    test("renders a key and a value input per pair", () => {
      const { container } = renderEditGroup({
        element: primitiveDictionaryElement(),
        value: [
          ["env", "prod"],
          ["tier", "web"],
        ],
      });

      const inputs = container.querySelectorAll<HTMLInputElement>(".pair-group input");
      expect(Array.from(inputs).map((input) => input.value)).toEqual(["env", "prod", "tier", "web"]);
    });

    test("labels the key and value inputs using the prototype names", () => {
      renderEditGroup({ element: primitiveDictionaryElement(), value: [["env", "prod"]] });

      expect(screen.getByText("Key", { exact: false })).toBeTruthy();
      expect(screen.getByText("Value", { exact: false })).toBeTruthy();
    });

    test("shows the empty state when there are no pairs", () => {
      renderEditGroup({ element: primitiveDictionaryElement(), value: [] });

      expect(screen.getByText("Add Labels", { selector: "strong" })).toBeTruthy();
    });

    test("removes the pair belonging to the clicked remove button", async () => {
      const { formulaForm } = renderEditGroup({
        element: primitiveDictionaryElement(),
        value: [
          ["env", "prod"],
          ["tier", "web"],
        ],
      });

      await click(screen.getAllByRole("button", { name: "Remove item" })[0]);

      expect(formulaForm.handleChange).toHaveBeenCalledWith({ id: "group", value: [["tier", "web"]] });
    });
  });

  describe("list of dictionaries", () => {
    test("renders a sub panel per item with a generated heading", () => {
      renderEditGroup({
        element: dictionaryListElement(),
        value: [
          { path: "/srv", size: "10G" },
          { path: "/var", size: "20G" },
        ],
      });

      expect(screen.getByText("Mount-1")).toBeTruthy();
      expect(screen.getByText("Mount-2")).toBeTruthy();
    });

    test("renders an input per prototype field of every item", () => {
      const { container } = renderEditGroup({
        element: dictionaryListElement(),
        value: [{ path: "/srv", size: "10G" }],
      });

      const inputs = container.querySelectorAll<HTMLInputElement>("#group\\$elements input");
      expect(Array.from(inputs).map((input) => input.value)).toEqual(["/srv", "10G"]);
    });

    test("expands the ${i} and field placeholders of $itemName in the heading", () => {
      renderEditGroup({
        element: dictionaryListElement({ $itemName: "${i}: ${path}" }),
        value: [
          { path: "/srv", size: "10G" },
          { path: "/var", size: "20G" },
        ],
      });

      expect(screen.getByText("1: /srv")).toBeTruthy();
      expect(screen.getByText("2: /var")).toBeTruthy();
    });

    test("expands the ${productName} placeholder of $itemName in the heading", () => {
      renderEditGroup({
        element: dictionaryListElement({ $itemName: "Mount on ${productName}" }),
        value: [{ path: "/srv", size: "10G" }],
      });

      expect(screen.getByText("Mount on Uyuni")).toBeTruthy();
    });

    test("keeps an unknown field placeholder as-is in the heading", () => {
      renderEditGroup({
        element: dictionaryListElement({ $itemName: "${missing}" }),
        value: [{ path: "/srv", size: "10G" }],
      });

      expect(screen.getByText("${missing}")).toBeTruthy();
    });

    test("shows the empty state when there are no items", () => {
      renderEditGroup({ element: dictionaryListElement(), value: [] });

      expect(screen.getByText("Add Mount", { selector: "strong" })).toBeTruthy();
    });

    test("removes the item belonging to the clicked remove button", async () => {
      const { formulaForm } = renderEditGroup({
        element: dictionaryListElement(),
        value: [
          { path: "/srv", size: "10G" },
          { path: "/var", size: "20G" },
        ],
      });

      await click(screen.getByRole("button", { name: "Remove Mount-1" }));

      expect(formulaForm.handleChange).toHaveBeenCalledWith({ id: "group", value: [{ path: "/var", size: "20G" }] });
    });

    test("disables the remove buttons once $minItems is reached", () => {
      renderEditGroup({
        element: dictionaryListElement({ $minItems: 1 }),
        value: [{ path: "/srv", size: "10G" }],
      });

      const removeButton = screen.getByRole("button", { name: "Min number of items reached" }) as HTMLButtonElement;
      expect(removeButton.disabled).toBe(true);
    });
  });
});
