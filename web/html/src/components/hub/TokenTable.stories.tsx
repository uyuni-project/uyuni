import { useRef } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { localizedMoment } from "utils";
import { Utils } from "utils/functions";
import Network from "utils/network";

import { AddTokenButton, AddTokenMethod } from "./AddTokenButton";
import { createAccessTokens } from "./story-fixtures";
import { TokenTable } from "./TokenTable";

type TokenTableProps = React.ComponentProps<typeof TokenTable>;

const mockNetworkGet = (() => {
  const items = createAccessTokens();
  return Utils.cancelable(Promise.resolve({ items, total: items.length }));
}) as typeof Network.get;

const mockNetworkPost = ((url: string, data?: { valid?: boolean }) => {
  if (url.endsWith("/validity")) {
    return Utils.cancelable(
      Promise.resolve({ data: data?.valid ? localizedMoment("2027-09-23T12:00:00Z").toDate() : null })
    );
  }
  return Utils.cancelable(Promise.resolve({ data: "eyJhbGciOiJIUzI1NiJ9.storybook-token" }));
}) as typeof Network.post;

const mockNetworkDelete = (() => Utils.cancelable(Promise.resolve())) as typeof Network.del;

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
  beforeEach: () => {
    const originalNetworkGet = Network.get;
    const originalNetworkPost = Network.post;
    const originalNetworkDelete = Network.del;
    Network.get = mockNetworkGet;
    Network.post = mockNetworkPost;
    Network.del = mockNetworkDelete;

    return () => {
      if (Network.get === mockNetworkGet) {
        Network.get = originalNetworkGet;
      }
      if (Network.post === mockNetworkPost) {
        Network.post = originalNetworkPost;
      }
      if (Network.del === mockNetworkDelete) {
        Network.del = originalNetworkDelete;
      }
    };
  },
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
