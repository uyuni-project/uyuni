import type { Meta, StoryObj } from "@storybook/react-webpack5";

const meta = {
  title: "Components/AceEditor",
  parameters: {
    docs: {
      description: {
        component: `
Code editor component powered by Ace Editor. Supports syntax highlighting for multiple languages, read-only mode, and customizable height. Used for editing scripts, configuration files, and code snippets.

**Note:** This component cannot be rendered in Storybook because it requires the global \`ace\` object from ace-builds which is not loaded in the Storybook environment. The component works correctly in the application.

## Usage Example

\`\`\`tsx
import { AceEditor } from "components/ace-editor";

<AceEditor
  mode="jsx"
  content={code}
  onChange={(newCode) => setCode(newCode)}
  minLines={10}
  maxLines={30}
/>
\`\`\`

## Supported Modes

- Programming: \`javascript\`, \`typescript\`, \`jsx\`, \`python\`, \`ruby\`
- Shell: \`sh\`, \`dockerfile\`, \`powershell\`
- Data: \`json\`, \`yaml\`, \`xml\`, \`sql\`
- Web: \`html\`, \`css\`
- Other: \`text\`
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
          Component Cannot Be Rendered in Storybook
        </h3>
        <p style={{ marginBottom: 0 }}>
          AceEditor requires the global <code>ace</code> object from ace-builds which is not available in the Storybook
          environment. The component works correctly in the application.
        </p>
      </div>

      <div style={{ padding: "20px", background: "#f8f9fa", borderRadius: "4px", marginBottom: "20px" }}>
        <h4>Usage Example</h4>
        <pre
          style={{
            background: "#fff",
            padding: "15px",
            borderRadius: "4px",
            border: "1px solid #dee2e6",
            overflow: "auto",
          }}
        >
          {`import { AceEditor } from "components/ace-editor";

<AceEditor
  mode="jsx"
  content={code}
  onChange={(newCode) => setCode(newCode)}
  minLines={10}
  maxLines={30}
/>`}
        </pre>
      </div>

      <div style={{ padding: "20px", background: "#f8f9fa", borderRadius: "4px" }}>
        <h4>Supported Syntax Modes</h4>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px", marginTop: "10px" }}>
          <div>
            <strong>Programming Languages:</strong>
            <ul>
              <li>JavaScript, TypeScript, JSX</li>
              <li>Python, Ruby</li>
            </ul>
          </div>
          <div>
            <strong>Shell & Config:</strong>
            <ul>
              <li>Bash (sh), Dockerfile</li>
              <li>PowerShell</li>
            </ul>
          </div>
          <div>
            <strong>Data Formats:</strong>
            <ul>
              <li>JSON, YAML, XML</li>
              <li>SQL</li>
            </ul>
          </div>
          <div>
            <strong>Web:</strong>
            <ul>
              <li>HTML, CSS</li>
              <li>Plain text</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "⚠️ This component cannot be rendered in Storybook. See the documentation above for usage instructions.",
      },
    },
  },
};
