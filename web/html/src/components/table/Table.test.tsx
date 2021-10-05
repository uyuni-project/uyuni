/**
 * Generic sanity checks for the table component.
 * Module specific tests belong on the submodule instead.
 */

import * as React from "react";
import { render, waitForElementToBeRemoved, screen, server, type, RenderOptions } from "utils/test-utils";

import { Table } from "./Table";
import { Column } from "./Column";
import { SearchField } from "./SearchField";

describe("Table component", () => {
  // IMPROVE: Would be nice to infer types here to ensure this stays in sync with the component
  // Minimal required props based on the table's types
  const baseProps = {
    data: [],
    identifier: (item) => item.value,
    // Only used to await loading states here in tests
    loadingText: "LOADING_TEXT",
  };

  /** Render and wait for the first load to be done */
  async function renderAndLoad(ui: React.ReactElement, options?: Omit<RenderOptions, "queries">) {
    const result = render(ui, options);
    await waitForElementToBeRemoved(() => screen.queryByText(baseProps.loadingText));
    return result;
  }

  test("renders with minimal props", async (done) => {
    expect(async () => {
      await renderAndLoad(<Table {...baseProps}>{null}</Table>);
      done();
    }).not.toThrow();
  });

  test("renders static data", async () => {
    const data = [
      {
        value: "Value 0",
      },
      {
        value: "Value 1",
      },
      {
        value: "Value 2",
      },
    ];

    await renderAndLoad(
      <Table {...baseProps} data={data}>
        <Column columnKey="value" cell={(item) => item.value} />
      </Table>
    );

    data.forEach((item) => {
      expect(screen.queryByText(item.value)).not.toBe(null);
    });
  });

  test("renders async data", async () => {
    const data = [
      {
        value: "Value 0",
      },
      {
        value: "Value 1",
      },
      {
        value: "Value 2",
      },
    ];
    server.mockGetJson("/getData", {
      items: data,
      total: data.length,
    });

    await renderAndLoad(
      <Table {...baseProps} data={"/getData"}>
        <Column columnKey="value" cell={(item) => item.value} />
      </Table>
    );

    data.forEach((item) => {
      expect(screen.queryByText(item.value)).not.toBe(null);
    });
  });

  test("renders correct number of pages for multiple pages of data", async () => {
    const itemsPerPage = 1;
    const totalItems = 1234;

    server.mockGetJson("/getData", {
      items: [{ value: "Value" }],
      total: totalItems,
    });

    await renderAndLoad(
      <Table {...baseProps} data={"/getData"} initialItemsPerPage={itemsPerPage}>
        <Column columnKey="value" cell={(item) => item.value} />
      </Table>
    );

    expect(
      screen.queryByText(`${totalItems}`, {
        selector: "option",
      })
    ).not.toBe(null);
  });

  test("loading indicator for tables using SimpleDataProvider", async () => {
    const data = [{ value: "Value 0" }];

    const { rerender } = render(
      <Table {...baseProps} loading={true}>
        {null}
      </Table>
    );

    // Check if loading indicator appears
    expect(screen.queryByText(baseProps.loadingText)).not.toBe(null);

    rerender(
      <Table {...baseProps} data={data} loading={false}>
        <Column columnKey="value" cell={(item) => item.value} />
      </Table>
    );
    await waitForElementToBeRemoved(() => screen.queryByText(baseProps.loadingText));

    // Check if loading indicator disappears and data is displayed
    expect(screen.queryByText(baseProps.loadingText)).toBe(null);
    expect(screen.queryByText(data[0].value)).not.toBe(null);
  });

  test("is filtered by a basic SearchField", async () => {
    const data = [
      {
        value: "Value 0",
      },
      {
        value: "Value 1",
      },
      {
        value: "Value 2",
      },
    ];
    const filter = (datum, criteria) => {
      if (criteria) {
        return datum.value.indexOf(criteria) !== -1;
      }
      return true;
    };
    await renderAndLoad(
      <Table {...baseProps} data={data} searchField={<SearchField filter={filter} />}>
        <Column columnKey="value" cell={(item) => item.value} />
      </Table>
    );
    const input = screen.getByRole("textbox");
    await type(input, "Value 1");

    expect(screen.queryByText("Value 0")).toBe(null);
    expect(screen.queryByText("Value 1")).not.toBe(null);
    expect(screen.queryByText("Value 2")).toBe(null);
  });
});
