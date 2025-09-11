import { StoryRow, StorySection, StripedStorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";

import { ActionStatus } from "../action/ActionStatus";

export default () => {
  return (
    <div>
      <h1>Tooltip</h1>
      <ul>
        <li>
          UI elements without visible text—such as icon buttons, icon tabs, should include a tooltip for better clarity
          and usability.
        </li>
        <li>Provide more context or explanation for specific element to help users make informed choices.</li>
      </ul>
      <h3>Usage</h3>
      <p>
        Add <code>data-bs-toggle="tooltip"</code> and a <code>title</code> attribute to the element.
      </p>
      <p>
        Some components — such as <strong>Buttons</strong> and <strong>ActionStatus</strong> — already include the{" "}
        <code>data-bs-toggle</code> attribute.{" "}
      </p>
      <p>
        For these, you only need to add a <code>title</code> (or use the defined props for the title) to enable a
        tooltip.
      </p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-default" title="Add" icon="fa-plus" />
          <Button className="btn-tertiary" title="Delete" icon="fa-trash" />
          <div className="my-2 mx-3">
            <ActionStatus data-bs-placement="right" serverId="server123" actionId="456" status="Queued" />
          </div>
          <a
            href="/rhn/manager/storybook?tab=tooltip"
            className="my-2 mx-3"
            data-bs-toggle="tooltip"
            title="Tooltip on link"
          >
            Link
          </a>
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Button className="btn-default" title="Add" icon="fa-plus" /&gt; <br />
        &lt;Button className="btn-tertiary" title="Delete" icon="fa-trash" /&gt; <br />
        &lt;ActionStatus serverId="server123" actionId="456" status="Queued" /&gt; <br />
        &lt;a href="/rhn/manager/storybook?tab=tooltip" data-bs-toggle="tooltip" title="Tooltip on link"&gt;Link
        &lt;/a&gt;
      </StorySection>
      <h3>Placement</h3>
      <p>
        Use <code>data-bs-placement</code> attibute (top by default) to set direction:{" "}
        <code>top | right | bottom | left.</code>
      </p>
      <p>
        <strong>Note:</strong> Buttons already include the <code>tooltipPlacement</code> prop to control tooltip
        position.
      </p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-default" text="Top" tooltipPlacement="top" title="Top Tooltip" />
          <Button className="btn-default" text="Right" tooltipPlacement="right" title="Right Tooltip" />
          <Button className="btn-default" text="Left" tooltipPlacement="left" title="Left Tooltip" />
          <Button className="btn-default" text="Bottom" tooltipPlacement="bottom" title="Bottom Tooltip" />
        </StoryRow>
        <StoryRow>
          <a
            href="/rhn/manager/storybook?tab=tooltip"
            className="mx-4"
            data-bs-toggle="tooltip"
            data-bs-placement="top"
            title="Top Tooltip"
          >
            Top{" "}
          </a>
          <a
            href="/rhn/manager/storybook?tab=tooltip"
            className="mx-4"
            data-bs-toggle="tooltip"
            data-bs-placement="right"
            title="Right Tooltip"
          >
            Right{" "}
          </a>
          <a
            href="/rhn/manager/storybook?tab=tooltip"
            className="mx-4"
            data-bs-toggle="tooltip"
            data-bs-placement="left"
            title="Left Tooltip"
          >
            Left{" "}
          </a>
          <a
            href="/rhn/manager/storybook?tab=tooltip"
            className="mx-4"
            data-bs-toggle="tooltip"
            data-bs-placement="bottom"
            title="Bottom Tooltip"
          >
            Bottom{" "}
          </a>
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Button className="btn-default" text="Top" tooltipPlacement="top" title="Top Tooltip" /&gt; <br />
        &lt;a href="/rhn/manager/storybook?tab=tooltip" data-bs-toggle="tooltip" data-bs-placement="top" title="Bottom
        Top"&gt;Top &lt;/a&gt;
        <br />
        &lt;a href="/rhn/manager/storybook?tab=tooltip" data-bs-toggle="tooltip" data-bs-placement="right" title="Bottom
        Right"&gt;Right &lt;/a&gt;
        <br />
        &lt;a href="/rhn/manager/storybook?tab=tooltip" data-bs-toggle="tooltip" data-bs-placement="left" title="Bottom
        Left"&gt;Left &lt;/a&gt;
        <br />
        &lt;a href="/rhn/manager/storybook?tab=tooltip" data-bs-toggle="tooltip" data-bs-placement="bottom"
        title="Bottom Tooltip"&gt;Bottom &lt;/a&gt;
      </StorySection>

      <h3>Option</h3>
      <p>
        Tooltip with multiple lines or a wider layout. Use <code>data-bs-custom-class="wide-tooltip"</code> to apply a
        custom CSS class.
      </p>
      <StripedStorySection>
        <StoryRow>
          <i
            className="fa fa-info-circle fa-1-5x"
            data-bs-toggle="tooltip"
            data-bs-custom-class="wide-tooltip"
            title={`Required channels:

            SLE-Module-Basesystem15-SP5 - aarch64
            SLE-Product-SLES15-SP5 - aarch64`}
          >
            {" "}
          </i>
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;i className="fa fa-info-circle fa-1-5x" data-bs-toggle="tooltip" data-bs-custom-class="wide-tooltip"
        title=`Required channels:\n SLE-Module-Basesystem15-SP5 - aarch64\n SLE-Product-SLES15-SP5 - aarch64`&gt;
        &lt;/i&gt;
      </StorySection>
    </div>
  );
};
