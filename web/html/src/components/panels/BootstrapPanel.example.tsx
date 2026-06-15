import { StorySection } from "manager/storybook/layout";

import { BootstrapPanel } from "./BootstrapPanel";
export default () => {
  return (
    <>
      <h2>Panels:</h2>
      <p>
        Panels are flexible containers used to organize and group related content, actions, or form elements into
        structured and manageable sections.
      </p>

      <p>
        <strong>Do</strong>
      </p>
      <ul>
        <li>Use concise and meaningful titles/headers.</li>
        <li>Keep related content grouped within a single panel.</li>
        <li>
          Use collapsible panels to group related content and reduce visual clutter, especially when users need to
          manage multiple sections, repeated data, or advanced settings without overwhelming the interface.
        </li>
        <li>Place primary actions (Save, Add, Edit) consistently in the button area.</li>
      </ul>
      <p>
        <strong>Don't</strong>
      </p>
      <ul>
        <li>Avoid deeply nested panels (more than 2 levels).</li>
        <li>Do not place too many actions in the panel header.</li>
        <li>Avoid hiding critical information inside collapsed panels by default.</li>
        <li>Avoid long or cluttered headers with excessive text/icons.</li>
      </ul>
      <br></br>
      <BootstrapPanel title="Static Panel Header" footer="Footer content">
        stuff
      </BootstrapPanel>
      <StorySection>
        &lt;BootstrapPanel title="Static Panel Header" footer="Footer content"&gt; <br />
        &nbsp; &nbsp;stuff <br />
        &lt;/BootstrapPanel&gt;
      </StorySection>
    </>
  );
};
