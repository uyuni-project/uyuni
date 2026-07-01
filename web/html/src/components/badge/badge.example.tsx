import { StoryRow, StorySection, StripedStorySection } from "manager/storybook/layout";

import Badge from "components/badge/Badge";
import { CounterBadge } from "components/badge/CounterBadge";
import { BootstrapPanel } from "components/panels/BootstrapPanel";
export default () => {
  return (
    <>
      <h2>Badge</h2>
      <p>
        The Badge component is used to display short, non-interactive labels that help communicate status, category, or
        metadata.
      </p>
      <h4>Status Badge</h4>
      <p>
        Use <code>status</code> variant for informational or system states.
      </p>
      <strong>When to use:</strong>
      <ul>
        <li>Representing application state</li>
        <li>Status indicators</li>
        <li>Background process states</li>
        <li>Read-only metadata</li>
      </ul>
      <p>
        <strong>Examples: </strong>
        Active / Inactive / Success / Error / Warning / Running / Failed / Pending
      </p>
      <StripedStorySection>
        <StoryRow>
          <Badge text="Default" />
          <Badge text="Success" icon="fa-check" color="success" />
          <Badge text="Warning" icon="fa-exclamation-triangle" color="warning" />
          <Badge text="Error" icon="fa-ban" color="error" />
          <Badge text="Info" icon="fa-info-circle" color="info" />
          <Badge text="Running" icon="fa-spinner fa-spin" color="running" />
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Badge text="Default" /&gt; <br />
        &lt;Badge text="Success" icon="fa-check" color="success" /&gt;
        <br />
        &lt;Badge text="Warning" icon="fa-exclamation-triangle" color="warning" /&gt;
        <br />
        &lt;Badge text="Error" icon="fa-ban" color="error" /&gt;
        <br />
        &lt;Badge text="Info" icon="fa-info-circle" color="info" /&gt;
        <br />
        &lt;Badge text="Running" icon="fa-spinner fa-spin" color="running" /&gt;
      </StorySection>
      <h4>Special Badge</h4>
      <p>
        Use <code>special</code> variant for attention-grabbing labels or highlights.
      </p>
      <strong>When to use:</strong>
      <ul>
        <li>Highlighting important or promotional labels</li>
        <li>Feature flags (Beta, New, Premium)</li>
        <li>Standout UI elements</li>
        <li>Non-system descriptive tags</li>
      </ul>
      <p>
        <strong>Examples: </strong>
        New, Premium, Featured, Beta
      </p>
      <StripedStorySection>
        <StoryRow>
          <Badge text="Default" variant="special" />
          <Badge text="New" color="green" variant="special" />
          <Badge text="Removed" color="red" variant="special" />
          <Badge text="Beta" color="yellow" variant="special" />
          <Badge text="Internal" color="blue" variant="special" />
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Badge text="Default" variant="special" /&gt; <br />
        &lt;Badge text="New" color="green" variant="special" /&gt;
        <br />
        &lt;Badge text="Removed" color="red" variant="special" /&gt;
        <br />
        &lt;Badge text="Beta" color="yellow" variant="special" /&gt;
        <br />
        &lt;Badge text="Internal" color="blue" variant="special" /&gt;
      </StorySection>
      <h4 className="mt-5">Counter Badge</h4>
      <p>
        The Counter Badge is used to display a numeric value associated with another UI element, such as a button, tab,
        or navigation item.
      </p>
      <strong>Best Practices: </strong>
      <ul>
        <li>Use only for numeric values.</li>
        <li>Keep the count concise (e.g. use 99+ instead of large numbers).</li>
        <li>Place the badge close to the element it describes (icon, button, tab, etc.).</li>
      </ul>
      <StripedStorySection>
        <StoryRow>
          <CounterBadge count="1" />
          <CounterBadge count="999+" status="highlight" />
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;CounterBadge count="1" /&gt; <br />
        &lt;CounterBadge count="999+" status="highlight" /&gt;
      </StorySection>
      <h4 className="mt-5">Badge in Panel:</h4>
      <BootstrapPanel title="Panel">
        <Badge text="Default" />
        <Badge text="Success" icon="fa-check" color="success" />
        <Badge text="Warning" icon="fa-exclamation-triangle" color="warning" />
        <Badge text="Error" icon="fa-ban" color="error" />
        <Badge text="Info" icon="fa-info-circle" color="info" />
        <Badge text="Running" icon="fa-spinner fa-spin" color="running" />
        <h4 className="mt-5">Counter Badge</h4>
        <CounterBadge count="1" />
        <CounterBadge count="999+" status="highlight" />
        <h4 className="mt-5">Special Badge</h4>
        <Badge text="Default" variant="special" />
        <Badge text="New" color="green" variant="special" />
        <Badge text="Removed" color="red" variant="special" />
        <Badge text="Beta" color="yellow" variant="special" />
        <Badge text="Internal" color="blue" variant="special" />
      </BootstrapPanel>
    </>
  );
};
