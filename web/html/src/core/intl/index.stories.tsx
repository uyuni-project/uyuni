import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { t } from "./index";

const meta = {
  title: "Components/Utilities/Internationalization",
  parameters: {
    docs: {
      description: {
        component:
          "Uyuni's translation helper supports ICU message syntax, plural rules, placeholders, and rich-text replacement functions.",
      },
    },
  },
} satisfies Meta;

export default meta;

type Story = StoryObj<typeof meta>;

export const RichText: Story = {
  render: () => (
    <p>
      {t("Please see <link>Format.JS docs</link> for the message syntax.", {
        link: (text) => (
          <a
            href="https://formatjs.github.io/docs/core-concepts/icu-syntax/"
            target="_blank"
            rel="noopener noreferrer"
            key={text}
          >
            {text}
          </a>
        ),
      })}
    </p>
  ),
};

export const Plurals: Story = {
  render: () => (
    <div>
      {[0, 1, 2, 25].map((itemCount) => (
        <p key={itemCount}>{t(`{itemCount, plural, one {# item selected} other {# items selected}}`, { itemCount })}</p>
      ))}
    </div>
  ),
};
