import { StoryRow, StorySection, StripedStorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";

export default () => {
  return (
    <div>
      <h2>Sizes</h2>
      <p>
        We support two button sizes: default and small. Use <code>btn-sm</code> for the small button size.
      </p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-primary btn-sm" text="Small button" />
          <Button className="btn-default btn-sm" text="Small button" />
          <Button className="btn-danger btn-sm" text="Small button" />
          <Button className="btn-default btn-sm" title="Delete" icon="fa-trash" />
          <Button className="btn-primary btn-sm" title="Add" icon="fa-plus" />
          <Button className="btn-tertiary btn-sm" title="Delete" icon="fa-trash" />
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Button className="btn-primary btn-sm" text="Small button" /&gt; <br />
        &lt;Button className="btn-default btn-sm" text="Small button" /&gt;
        <br />
        &lt;Button className="btn-danger btn-sm" text="Small button" /&gt;
        <br />
        &lt;Button className="btn-default btn-sm" title="Delete" icon="fa-trash" /&gt;
        <br />
        &lt;Button className="btn-primary btn-sm" title="Add" icon="fa-plus" /&gt;
        <br />
        &lt;Button className="btn-tertiary btn-sm" title="Delete" icon="fa-trash" /&gt;
        <br />
      </StorySection>
    </div>
  );
};
