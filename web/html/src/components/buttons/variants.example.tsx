import { StoryRow, StorySection, StripedStorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";

export default () => {
  return (
    <div>
      <h2>Buttons</h2>
      <p>Different button styling variants tailored for various scenarios as required.</p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-primary" text="Primary" />
          <Button className="btn-default" text="Default" />
          <Button className="btn-danger" text="Danger" />
          <Button className="btn-tertiary" text="Tertiary" />
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Button className="btn-primary" text="Primary" /&gt; <br />
        &lt;Button className="btn-default" text="Default" /&gt;
        <br />
        &lt;Button className="btn-danger" text="Danger" /&gt;
        <br />
        &lt;Button className="btn-tertiary" text="Tertiary" /&gt;
      </StorySection>
    </div>
  );
};
