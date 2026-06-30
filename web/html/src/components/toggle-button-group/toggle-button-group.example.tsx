import { useState } from "react";

import { StorySection, StripedStorySection } from "manager/storybook/layout";

import { ToggleButtonGroup } from "components/toggle-button-group/toggle-button-group";

export default () => {
  const [textType, setTextType] = useState("Value1");
  const [iconType, setIconType] = useState("left");
  const [sizeBy, setSizeBy] = useState("small1");
  return (
    <div>
      <h2>Toggle button group</h2>
      <p>
        A toggle button group presents a set of mutually exclusive options, allowing users to select and switch between
        states.
      </p>
      <StripedStorySection>
        <ToggleButtonGroup
          value={textType}
          onChange={setTextType}
          options={[
            {
              value: "Value1",
              label: "Value1",
            },
            {
              value: "Value2",
              label: "Value2",
            },
            {
              value: "Value3",
              label: "Value3",
            },
          ]}
        />
      </StripedStorySection>
      <StorySection>
        &lt;ToggleButtonGroup <br />
        &nbsp;&nbsp;value="Value1" <br />
        &nbsp;&nbsp;onChange=&#123;&#40;&#41; =&gt; &#123;&#125;&#125; <br />
        &nbsp;&nbsp;options=&#123;&#91; <br />
        &nbsp;&nbsp;&nbsp;&nbsp;&#123; value: "Value1", label: "Value1" &#125;, <br />
        &nbsp;&nbsp;&nbsp;&nbsp;&#123; value: "Value2", label: "Value2"&#125;, <br />
        &nbsp;&nbsp;&nbsp;&nbsp;&#123; value: "Value3", label: "Value3" &#125; <br />
        &nbsp;&nbsp;&#93;&#125; <br />
        /&gt;
      </StorySection>

      <h4>Only icon</h4>
      <StripedStorySection>
        <ToggleButtonGroup
          value={iconType}
          onChange={setIconType}
          options={[
            {
              value: "left",
              icon: "fa-solid fa-align-left",
              tooltip: "Left align",
            },

            {
              value: "center",
              icon: "fa-solid fa-align-center",
              tooltip: "Center align",
            },
            {
              value: "right",
              icon: "fa-solid fa-align-right",
              tooltip: "Right align",
            },
          ]}
        />
      </StripedStorySection>
      <h4>Size</h4>
      <StripedStorySection>
        <ToggleButtonGroup
          value={sizeBy}
          onChange={setSizeBy}
          size="sm"
          options={[
            {
              value: "small1",
              icon: "fa-solid fa-align-left",
            },

            {
              value: "small2",
              icon: "fa-solid fa-align-center",
            },
            {
              value: "small3",
              icon: "fa-solid fa-align-right",
              disabled: true,
            },
          ]}
        />
      </StripedStorySection>
    </div>
  );
};
