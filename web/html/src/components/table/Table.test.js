/**
 * Generic sanity checks for the table component.
 * Module specific tests belong on the submodule instead.
 */

import * as React from "react";
import {
  render,
  waitForElementToBeRemoved,
  screen,
  server
} from "utils/test-utils";

import { Table } from "./Table";
import { Column } from "./Column";

describe("Table component", () => {
  // TODO: Would be nice to infer types here to ensure this stays in sync with the component
  // Minimal required props based on the table's types
  const baseProps = {
    data: [],
    identifier: item => item.value,
    // This is used to await loading states
    loadingText: "LOADING_TEXT"
  };

  /** Render and wait for the first load to be done */
  async function renderAndLoad(...args) {
    const result = render(...args);
    await waitForElementToBeRemoved(() =>
      screen.queryByText(baseProps.loadingText, { exact: false })
    );
    return result;
  }

  test("renders with minimal props", async done => {
    expect(async () => {
      await renderAndLoad(<Table {...baseProps} />);
      done();
    }).not.toThrow();
  });

  test("renders static data", async () => {
    const data = [
      {
        value: "Value 0"
      },
      {
        value: "Value 1"
      },
      {
        value: "Value 2"
      }
    ];

    await renderAndLoad(
      <Table {...baseProps} data={data}>
        <Column columnKey="value" header={"Header"} cell={item => item.value} />
      </Table>
    );

    data.forEach(item => {
      expect(screen.queryByText(item.value)).not.toBe(null);
    });
  });

  test("renders async data", async () => {
    const data = [
      {
        value: "Value 0"
      },
      {
        value: "Value 1"
      },
      {
        value: "Value 2"
      }
    ];
    server.mockGet("/getData", {
      items: data,
      total: data.length
    });

    await renderAndLoad(
      <Table {...baseProps} data={"/getData"}>
        <Column columnKey="value" header={"Header"} cell={item => item.value} />
      </Table>
    );

    data.forEach(item => {
      expect(screen.queryByText(item.value)).not.toBe(null);
    });
  });
});
