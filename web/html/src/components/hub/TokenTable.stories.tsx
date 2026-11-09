/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { useRef } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { localizedMoment } from "utils";
import { Utils } from "utils/functions";
import Network from "utils/network";
import { mockNetwork } from "utils/storybook/mock-network";

import { AddTokenButton, AddTokenMethod } from "./AddTokenButton";
import { createAccessTokens } from "./story-fixtures";
import { TokenTable } from "./TokenTable";
import { CreateTokenRequest, TokenType, ValidityRequest } from "./types";

type TokenTableProps = React.ComponentProps<typeof TokenTable>;

const installTokenMocks = () => {
  let items = createAccessTokens();
  let nextId = Math.max(...items.map((token) => token.id)) + 1;
  const get = (() =>
    Utils.cancelable(
      Promise.resolve({
        items: items.map((token) => ({ ...token })),
        total: items.length,
      })
    )) as typeof Network.get;
  const post = ((url: string, data: CreateTokenRequest | ValidityRequest) => {
    if (url.endsWith("/validity") && "valid" in data) {
      const id = Number(url.split("/").slice(-2)[0]);
      const token = items.find((item) => item.id === id);
      const expirationDate = data.valid ? localizedMoment().add(1, "year").toDate() : null;
      if (token) {
        token.valid = data.valid;
        token.expirationDate = expirationDate;
      }
      return Utils.cancelable(Promise.resolve({ data: expirationDate }));
    }
    if ("type" in data) {
      const now = localizedMoment().toDate();
      items.push({
        id: nextId++,
        serverFqdn: data.fqdn ?? "new.example.com",
        type: data.type,
        localizedType: data.type === TokenType.ISSUED ? "Issued" : "Consumed",
        valid: true,
        expirationDate: null,
        creationDate: now,
        modificationDate: now,
        hubId: null,
        peripheralId: null,
      });
    }
    return Utils.cancelable(Promise.resolve({ data: "eyJhbGciOiJIUzI1NiJ9.storybook-token" }));
  }) as typeof Network.post;
  const del = ((url: string) => {
    const id = Number(url.split("/").slice(-1)[0]);
    items = items.filter((item) => item.id !== id);
    return Utils.cancelable(Promise.resolve());
  }) as typeof Network.del;
  return mockNetwork({ get, post, del });
};

const TokenManagementStory = (props: TokenTableProps) => {
  const tableRef = useRef<TokenTable>(null);

  return (
    <div style={{ width: "100%", maxWidth: "1400px", minHeight: "700px", padding: "24px" }}>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: "16px" }}>
        <AddTokenButton
          method={AddTokenMethod.IssueAndStore}
          onCreated={() => {
            action("token created")();
            tableRef.current?.refresh();
          }}
        />
      </div>
      <TokenTable ref={tableRef} {...props} />
    </div>
  );
};

const meta = {
  title: "Compositions/Hub/TokenTable",
  component: TokenTable,
  beforeEach: installTokenMocks,
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Lists issued and consumed Hub access tokens and demonstrates the related add-token action. Local fixtures cover linked, expiring, invalid, and deletable tokens without contacting a backend.",
      },
    },
  },
  args: {
    allowToggleValidity: true,
    allowDeletion: true,
  },
  argTypes: {
    allowToggleValidity: {
      control: "boolean",
      description: "Shows actions for validating or invalidating tokens.",
    },
    allowDeletion: {
      control: "boolean",
      description: "Shows deletion actions; tokens currently in use remain protected.",
    },
  },
  render: (args) => <TokenManagementStory {...args} />,
} satisfies Meta<typeof TokenTable>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
