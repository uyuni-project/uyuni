import { type ReactNode, Fragment, useEffect, useState } from "react";

import { SectionState } from "components/FormulaForm";
import { Panel } from "components/panels/Panel";
import { Highlight } from "components/table/Highlight";

import { isFiltered } from "./FormulaComponentGenerator";

type Props = {
  id: string;
  sectionsExpanded: SectionState;
  setSectionsExpanded: (SectionState) => void;
  header?: ReactNode;
  help?: ReactNode;
  children?: ReactNode;
  isVisibleByCriteria?: () => boolean;
  criteria: string;
};

const Group = (props: Props) => {
  const [visible, setVisible] = useState(props.sectionsExpanded !== SectionState.Collapsed);
  // console.log("visible", props.sectionsExpanded !== SectionState.Collapsed);
  useEffect(() => {
    if (props.sectionsExpanded !== SectionState.Mixed) {
      setVisible(props.sectionsExpanded !== SectionState.Collapsed);
    }
  }, [props.sectionsExpanded]);

  return props.isVisibleByCriteria?.() ? (
    <Panel
      key={props.id}
      headingLevel="h4"
      className="formula-content-section"
      collapseId={props.id.replace(/[.#\s]/g, "-")}
      collapsClose={!visible}
      header={
        <div className="group-heading">
          <span id={props.id}>
            {isFiltered(props.criteria) ? (
              <Highlight
                enabled={isFiltered(props.criteria)}
                text={props.header?.toString() || ""}
                highlight={props.criteria}
              />
            ) : (
              props.header
            )}
          </span>
        </div>
      }
    >
      <Fragment>
        {props.help !== props.header && <p>{props.help}</p>}
        {props.children}
      </Fragment>
    </Panel>
  ) : null;
};

export default Group;
