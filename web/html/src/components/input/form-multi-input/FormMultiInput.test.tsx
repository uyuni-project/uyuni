/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import type { ComponentProps } from "react";

import { render } from "utils/test-utils";

import { Form } from "../form/Form";
import { Text } from "../text/Text";
import { type FormMultiInputVariant, FormMultiInput } from "./FormMultiInput";

type ExtraProps = Partial<Omit<ComponentProps<typeof FormMultiInput>, "variant" | "children">>;

describe("FormMultiInput", () => {
  function renderVariant(variant: FormMultiInputVariant, extraProps: ExtraProps = {}) {
    // Form mutates its model, so each test needs a fresh one
    const model = { item0_a: "a0", item0_b: "b0", item1_a: "a1", item1_b: "b1" };
    return render(
      <Form model={model} onChange={() => {}}>
        <FormMultiInput
          id="items"
          title="Items"
          prefix="item"
          onAdd={() => {}}
          onRemove={() => {}}
          variant={variant}
          {...extraProps}
        >
          {(index) => (
            <>
              <Text name={`item${index}_a`} label="A" />
              <Text name={`item${index}_b`} label="B" />
            </>
          )}
        </FormMultiInput>
      </Form>
    );
  }

  const removeButton = (container: HTMLElement, index: number) =>
    container.querySelector(`#remove_item${index}`) as HTMLButtonElement;

  test("add button passes the next free index", () => {
    const onAdd = jest.fn();
    const { container } = renderVariant("singleLine", { onAdd });
    (container.querySelector("#add_item") as HTMLButtonElement).click();
    expect(onAdd).toHaveBeenCalledWith(2);
  });

  describe("singleLine variant", () => {
    test("puts each item on one line with the remove button at the end", () => {
      const { container } = renderVariant("singleLine");
      const lines = container.querySelectorAll(".multi-field-singleline");
      expect(lines).toHaveLength(2);
      lines.forEach((line, i) => {
        expect(line.querySelector(`#item${i}_a`)).not.toBeNull();
        expect(line.lastElementChild?.id).toBe(`remove_item${i}`);
      });
    });

    test("applies rowClass and removes the clicked item", () => {
      const onRemove = jest.fn();
      const { container } = renderVariant("singleLine", { onRemove, rowClass: "custom-row" });
      expect(container.querySelectorAll(".multi-field-singleline.custom-row")).toHaveLength(2);
      removeButton(container, 0).click();
      expect(onRemove).toHaveBeenCalledWith(0);
    });
  });

  describe("group variant", () => {
    test("puts the remove button above the fields and separates groups", () => {
      const { container } = renderVariant("group");
      const groups = container.querySelectorAll(".multi-field-group");
      expect(groups).toHaveLength(2);
      groups.forEach((group, i) => {
        expect(group.firstElementChild?.querySelector(`#remove_item${i}`)).not.toBeNull();
        expect(group.querySelector(`#item${i}_a`)).not.toBeNull();
        expect(group.querySelector(`#item${i}_b`)).not.toBeNull();
      });
      expect(groups[0].classList.contains("border-bottom")).toBe(true);
      expect(groups[1].classList.contains("border-bottom")).toBe(false);
    });

    test("applies rowClass and removes the whole group", () => {
      const onRemove = jest.fn();
      const { container } = renderVariant("group", { onRemove, rowClass: "custom-group" });
      expect(container.querySelectorAll(".multi-field-group.custom-group")).toHaveLength(2);
      removeButton(container, 1).click();
      expect(onRemove).toHaveBeenCalledWith(1);
    });
  });

  describe("panel variant", () => {
    const itemPanels = (container: HTMLElement) => container.querySelectorAll(".panel-body .panel");

    test("wraps each item in a Panel with the remove button in its heading", () => {
      const { container } = renderVariant("panel");
      const panels = itemPanels(container);
      expect(panels).toHaveLength(2);
      panels.forEach((panel, i) => {
        expect(panel.querySelector(`.panel-heading #remove_item${i}`)).not.toBeNull();
        expect(panel.querySelector(`.panel-body #item${i}_a`)).not.toBeNull();
      });
    });

    test("uses a default title without panelTitle or panelIcon", () => {
      const { container } = renderVariant("panel");
      const titles = Array.from(itemPanels(container)).map((panel) => panel.querySelector("h3")?.textContent);
      expect(titles).toEqual(["Item 1", "Item 2"]);
    });

    test("uses panelTitle and panelIcon when given", () => {
      const { container } = renderVariant("panel", {
        panelTitle: (index) => `Database ${index + 1}`,
        panelIcon: () => "fa-database",
      });
      itemPanels(container).forEach((panel, i) => {
        expect(panel.querySelector("h3")?.textContent).toBe(`Database ${i + 1}`);
        expect(panel.querySelector("h3 i.fa-database")).not.toBeNull();
      });
    });

    test("removes the clicked item", () => {
      const onRemove = jest.fn();
      const { container } = renderVariant("panel", { onRemove });
      removeButton(container, 1).click();
      expect(onRemove).toHaveBeenCalledWith(1);
    });
  });

  test.each<FormMultiInputVariant>(["singleLine", "group", "panel"])(
    "%s variant disables remove buttons",
    (variant) => {
      const { container } = renderVariant(variant, { disabled: true });
      expect(removeButton(container, 0).disabled).toBe(true);
      expect(removeButton(container, 1).disabled).toBe(true);
    }
  );
});
