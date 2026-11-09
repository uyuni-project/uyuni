/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Utils } from "utils/functions";
import Network from "utils/network";
import { mockNetwork } from "utils/storybook/mock-network";

import { PeripheralsList } from "./PeripheralsList";
import { peripherals } from "./story-fixtures";

const installPeripheralMocks = () => {
  let rows = peripherals.map((peripheral) => ({ ...peripheral }));
  const get = ((url: string) => {
    const criteria = new URL(url, window.location.origin).searchParams.get("q")?.toLocaleLowerCase() ?? "";
    const items = rows
      .filter((peripheral) => peripheral.fqdn.toLocaleLowerCase().includes(criteria))
      .map((peripheral) => ({ ...peripheral }));
    return Utils.cancelable(Promise.resolve({ items, total: items.length }));
  }) as typeof Network.get;
  const del = ((url: string) => {
    const id = Number(new URL(url, window.location.origin).pathname.split("/").slice(-1)[0]);
    rows = rows.filter((peripheral) => peripheral.id !== id);
    return Utils.cancelable(Promise.resolve());
  }) as typeof Network.del;
  return mockNetwork({ get, del });
};

const meta = {
  title: "Compositions/Hub/PeripheralsList",
  component: PeripheralsList,
  beforeEach: installPeripheralMocks,
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Lists registered peripheral servers with synchronized-content counts, Root CA downloads, detail links, filtering, and deregistration. Requests are served from local fixtures.",
      },
    },
  },
  render: () => (
    <div style={{ width: "100%", maxWidth: "1400px", minHeight: "700px", padding: "24px" }}>
      <PeripheralsList />
    </div>
  ),
} satisfies Meta<typeof PeripheralsList>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
