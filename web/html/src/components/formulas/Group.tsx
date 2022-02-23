import * as React from "react";
import { useEffect, useState } from "react";

import { Highlight } from "components/table/Highlight";

import { isFiltered } from "./FormulaComponentGenerator";
import SectionToggle from "./SectionToggle";

type Props = {
  id: string;
  sectionsExpanded: string;
  setSectionsExpanded: (string) => void;
  header?: React.ReactNode;
  help?: React.ReactNode;
  children?: React.ReactNode;
  isVisibleByCriteria?: any;
  criteria: string;
};

const Group = (props: Props) => {
  const [visible, setVisible] = useState(props.sectionsExpanded !== "collapsed");

  useEffect(() => {
    if (props.sectionsExpanded !== "mixed") {
      setVisible(props.sectionsExpanded !== "collapsed");
    }
  }, [props.sectionsExpanded]);

  const isVisible = () => {
    return visible;
  };

  const setVisibility = (index, visible) => {
    setVisible(visible);
    props.setSectionsExpanded("mixed");
  };

  return props.isVisibleByCriteria?.() ? (
    <div
      className={
        visible ? "formula-content-section-open group-heading" : "formula-content-section-closed group-heading"
      }
    >
      <SectionToggle setVisible={setVisibility} isVisible={isVisible}>
        <h4 id={props.id} key={props.id}>
          {isFiltered(props.criteria) ? (
            <Highlight
              enabled={isFiltered(props.criteria)}
              text={props.header ? props.header.toString() : ""}
              highlight={props.criteria}
            />
          ) : (
            props.header
          )}
        </h4>
      </SectionToggle>
      <div>
        {visible ? (
          <React.Fragment>
            {props.help ? <p>{props.help}</p> : null}
            {props.children}
          </React.Fragment>
        ) : null}
      </div>
    </div>
  ) : null;
};

export default Group;
