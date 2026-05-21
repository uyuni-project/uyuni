import { Panel } from "./Panel";
import { Button, DropdownButton } from "components/buttons";
import SectionToggle from "../formulas/SectionToggle";
import "../formulas/formula-form.css";
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
      <p>Panels in Project:</p>

      <Panel
        key="panel-id"
        collapseId="panel-id"
        header={<h4 id="headerid">Header test</h4>}
        collapsClose={false}
        buttons={<Button className="btn-tertiary btn-sm" title={t("Delete")} icon="fa-trash-o" />}
      >
        <div>Test data</div>
      </Panel>

      <p>Panels in Formulas:</p>

      <Panel
        key="panel-id"
        headingLevel="h5"
        collapseId="headerid"
        collapsClose={false}
        header={<h4 id="headerid">Header</h4>}
      >
        <div>Test data Panels in Formula</div>
      </Panel>
    </>
  );
};
