import { StoryRow, StorySection, StripedStorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";

export default () => (
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
