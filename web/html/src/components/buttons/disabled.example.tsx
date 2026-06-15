import { StoryRow, StorySection, StripedStorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";

export default () => {
  return (
    <div>
      <h2>Disabled</h2>
      <p>
        Make buttons look inactive by adding the disabled boolean attribute to any <code>Button</code> element.
      </p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-primary" text="Primary" disabled />
          <Button className="btn-default" text="Default" disabled />
          <Button className="btn-danger" text="Danger" disabled />
          <Button className="btn-tertiary" text="Tertiary" disabled />
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Button className="btn-primary" text="Primary" disabled /&gt; <br />
        &lt;Button className="btn-default" text="Default" disabled /&gt;
        <br />
        &lt;Button className="btn-danger" text="Danger" disabled /&gt;
        <br />
        &lt;Button className="btn-tertiary" text="Tertiary" disabled /&gt;
        <br />
      </StorySection>
    </div>
  );
};
