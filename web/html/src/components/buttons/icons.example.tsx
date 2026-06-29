import { StoryRow, StorySection, StripedStorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";

export default () => {
  return (
    <div>
      <h2>Variants</h2>
      <p>
        <strong>Note: </strong>Icon-only buttons should always include a descriptive <code>title / Tooltip</code>{" "}
        attribute to ensure clarity and accessibility for all users.
      </p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-primary" title="Add" icon="fa-plus" />
          <Button className="btn-default" title="Delete" icon="fa-trash" />
          <Button className="btn-primary" icon="fa-plus" text="Primary" />
          <Button className="btn-default" icon="fa-plus" text="Default" />
          <Button className="btn-tertiary" icon="fa-plus" text="Tertiary" />
          <Button className="btn-tertiary" title="Delete" icon="fa-trash" />
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Button className="btn-primary" title="Add" icon="fa-plus" /&gt; <br />
        &lt;Button className="btn-default" title="Delete" icon="fa-trash" /&gt;
        <br />
        &lt;Button className="btn-primary" icon="fa-plus" text="Primary" /&gt;
        <br />
        &lt;Button className="btn-default" icon="fa-plus" text="Default" /&gt;
        <br />
        &lt;Button className="btn-tertiary" icon="fa-plus" text="Tertiary" /&gt;
        <br />
        &lt;Button className="btn-tertiary" title="Delete" icon="fa-trash" /&gt;
      </StorySection>
    </div>
  );
};
