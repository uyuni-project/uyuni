import * as React from "react";
import { useEffect, useState } from "react";

import SectionToggle from "./SectionToggle";

type Props = {
  id: string;
  sectionsExpanded: string;
  setSectionsExpanded: (string) => void;
  header?: React.ReactNode;
  help?: React.ReactNode;
  children?: React.ReactNode;
  isVisibleByCriteria?: any;
};

const Group = (props: Props) => {
  const [visible, setVisible] = useState(props.sectionsExpanded === "expanded");

  useEffect(() => {
    if (props.sectionsExpanded !== "mixed") {
      setVisible(props.sectionsExpanded === "expanded");
    }
  }, [props.sectionsExpanded]);

  const isVisible = () => {
    return visible;
  };

  const setVisibility = (index, visible) => {
    setVisible(visible);
    props.setSectionsExpanded("mixed");
  };

  return (
    props.isVisibleByCriteria?.() ?
      <div
        className={
          visible ? "formula-content-section-open group-heading" : "formula-content-section-closed group-heading"
        }
      >
        <SectionToggle setVisible={setVisibility} isVisible={isVisible}>
          <h4 id={props.id} key={props.id}>
            {props.header}
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
      : null
  );
};

export default Group;
