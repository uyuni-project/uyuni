import type { Meta, StoryObj } from "@storybook/react-webpack5";

const meta = {
  title: "Deprecated/DateTime/DEPRECATED_DateTimePicker",
  parameters: {
    docs: {
      description: {
        component: `
**⚠️ DEPRECATED - Do not use in new code**

This component is deprecated. Use \`import { DateTimePicker } from "components/datetime"\` instead.

**Note:** This component cannot be rendered in Storybook because it depends on jQuery datepicker/timepicker plugins that are not loaded in this environment. This is a legacy component that will be removed in a future release.

## Replacement Component

Use the modern DateTimePicker component instead:

\`\`\`tsx
import { DateTimePicker } from "components/datetime";

<DateTimePicker
  serverTimeZone={false}
  value={moment()}
  onChange={(newValue) => console.log(newValue)}
/>
\`\`\`

## Why This Component is Deprecated

- **jQuery Dependency**: Relies on external jQuery plugins (bootstrap-datepicker, bootstrap-timepicker)
- **Poor Performance**: Heavy library dependencies
- **Accessibility Issues**: jQuery plugins have limited ARIA support
- **Maintenance**: jQuery plugins are no longer actively maintained

The new DateTimePicker uses react-datepicker with better:
- Performance (modern React implementation)
- Accessibility (better ARIA support)
- Better integration with React state
- Smaller bundle size

## Props

- \`serverTimeZone\`: boolean - Whether to use server timezone (default: false)
- \`value\`: moment.Moment - Current date/time value
- \`onChange\`: (value: moment.Moment) => void - Callback when value changes

## Migration Guide

**Before:**
\`\`\`tsx
<DEPRECATED_DateTimePicker
  value={this.state.date}
  onChange={this.handleDateChange}
/>
\`\`\`

**After:**
\`\`\`tsx
<DateTimePicker
  value={this.state.date}
  onChange={this.handleDateChange}
  serverTimeZone={false}
/>
\`\`\`

The API is similar - change the import and component name. Use \`serverTimeZone\` prop if you need server timezone handling.
        `,
      },
    },
  },
} satisfies Meta;

export default meta;

type Story = StoryObj<typeof meta>;

export const Documentation: Story = {
  render: () => (
    <div style={{ padding: "40px", maxWidth: "800px", margin: "0 auto" }}>
      <div
        style={{
          padding: "20px",
          background: "#fff3cd",
          border: "1px solid #ffc107",
          borderRadius: "4px",
          marginBottom: "20px",
        }}
      >
        <h3 style={{ marginTop: 0, color: "#856404" }}>
          <i className="fa fa-exclamation-triangle" style={{ marginRight: "8px" }} />
          Component Cannot Be Rendered
        </h3>
        <p style={{ marginBottom: 0 }}>
          This deprecated component requires jQuery datepicker/timepicker plugins that are not available in Storybook.
          Please use the modern <strong>DateTimePicker</strong> component instead.
        </p>
      </div>

      <div style={{ padding: "20px", background: "#f8f9fa", borderRadius: "4px" }}>
        <h4>Quick Migration</h4>
        <p>Replace this component with the modern DateTimePicker:</p>
        <pre
          style={{
            background: "#fff",
            padding: "15px",
            borderRadius: "4px",
            border: "1px solid #dee2e6",
            overflow: "auto",
          }}
        >
          {`import { DateTimePicker } from "components/datetime";

// Use serverTimeZone (boolean) to display in server timezone
<DateTimePicker
  serverTimeZone
  value={date}
  onChange={handleChange}
/>`}
        </pre>
      </div>
    </div>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "⚠️ This component is deprecated and cannot be rendered in Storybook. See the documentation above for migration instructions.",
      },
    },
  },
};
