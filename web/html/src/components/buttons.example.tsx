import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";

export default () => {
  return (
    <StripedStorySection>
      <StoryRow>
        <Button>&lt;Button /&gt;</Button>
        <Button disabled>&lt;Button disabled /&gt;</Button>
      </StoryRow>
      <StoryRow>
        <Button className="btn-link">&lt;Button className="btn-link" /&gt;</Button>
        <Button className="btn-link" disabled>
          &lt;Button className="btn-link" disabled /&gt;
        </Button>
      </StoryRow>
      <StoryRow>
        <Button className="btn-default">&lt;Button className="btn-default" /&gt;</Button>
        <Button className="btn-default" disabled>
          &lt;Button className="btn-default" disabled /&gt;
        </Button>
      </StoryRow>
      <StoryRow>
        <Button className="btn-primary">&lt;Button className="btn-primary" /&gt;</Button>
        <Button className="btn-primary" disabled>
          &lt;Button className="btn-primary" disabled /&gt;
        </Button>
      </StoryRow>
      <StoryRow>
        <Button className="btn-danger">&lt;Button className="btn-danger" /&gt;</Button>
        <Button className="btn-danger" disabled>
          &lt;Button className="btn-danger" disabled /&gt;
        </Button>
      </StoryRow>
    </StripedStorySection>
  );
};
