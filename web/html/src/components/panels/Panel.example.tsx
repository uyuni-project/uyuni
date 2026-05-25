import { Panel } from "./Panel";
import { Button, DropdownButton } from "components/buttons";
import { StorySection } from "manager/storybook/layout";
import { useRef, useReducer } from "react";
export default () => {
  const visible = useRef(new Map());
  const [, rerender] = useReducer((x) => x + 1, 0);

  const isVisible = (index) => {
    return visible.current.get(index) !== false;
  };

  const setVisible = (index, isVisible) => {
    visible.current.set(index, isVisible);
    rerender(); // needed if not using state
  };
  return (
    <>
      <h3>Collapsible Panel</h3>
      <p></p>

      <Panel
        key="panel-id"
        collapseId="panel-id"
        header={<h4 id="headerid">Collapsible Panel</h4>}
        collapsClose={false}
        buttons={<Button className="btn-tertiary btn-sm" title={t("Delete")} icon="fa-trash-o" />}
      >
        <div>Collapsible Panel content</div>
      </Panel>
      <StorySection>
        &lt;Panel
        <br /> &nbsp; key="panel-id"
        <br /> &nbsp; collapseId="panel-id"
        <br /> &nbsp; header=&#123;&lt;h4&gt;Collapsible Panel&lt;/h4&gt;&#125;
        <br /> &nbsp; collapsClose=&#123;false&#125;
        <br /> &nbsp; buttons=&#123;&lt;Button className="btn-tertiary btn-sm" title="Delete" icon="fa-trash-o"
        /&gt;&#125;
        <br /> &gt;
        <br /> &nbsp; &nbsp;&lt;div&gt;Collapsible Panel content&lt;/div&gt;
        <br /> &lt;/Panel&gt;
      </StorySection>
      <hr></hr>

      <Panel
        key="panel-id"
        headingLevel="h4"
        collapseId="headerid"
        collapsClose={false}
        title="Nested Collapsible Panel"
      >
        <Panel
          key="panel-id2"
          headingLevel="h5"
          collapseId="headerid2"
          collapsClose={false}
          title="Nested panel two"
          buttons={<Button className="btn-tertiary btn-sm" title="Delete" icon="fa-trash-o" />}
        >
          <div>Nested Collapsible Panel content </div>
        </Panel>
      </Panel>
      <StorySection>
        &lt;Panel
        <br /> &nbsp; key="panel-id"
        <br /> &nbsp; headingLevel="h4"
        <br /> &nbsp; collapseId="headerid"
        <br /> &nbsp; collapsClose=&#123;false&#125;
        <br /> &nbsp; title="Nested Collapsible Panel"
        <br /> &gt;
        <br /> &nbsp; &nbsp;&lt;Panel
        <br /> &nbsp; &nbsp; key="panel-id2"
        <br /> &nbsp; &nbsp; headingLevel="h5"
        <br /> &nbsp; &nbsp; collapseId="headerid2"
        <br /> &nbsp; &nbsp; collapsClose=&#123;false&#125;
        <br /> &nbsp; &nbsp; title="Nested panel two"
        <br /> &nbsp; &nbsp; buttons=&#123;&lt;Button className="btn-tertiary btn-sm" title="Delete" icon="fa-trash-o"
        /&gt;&#125;
        <br />
        &nbsp; &gt;
        <br /> &nbsp; &nbsp; &nbsp;&lt;div&gt;Nested Collapsible Panel content&lt;/div&gt;
        <br /> &nbsp; &lt;/Panel&gt;
        <br /> &lt;/Panel&gt;
      </StorySection>
    </>
  );
};
