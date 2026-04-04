import type { ReactNode } from "react";

import { DEPRECATED_onClick } from "components/utils";

type Props = {
  index?: any;
  isVisible: (index: any) => boolean;
  setVisible: (index: any, isVisible: boolean) => any;
  children?: ReactNode;
};

const SectionToggle = (props: Props) => {
  const toggleSection = () => {
    let visible = props.isVisible(props.index);
    if (visible === undefined) {
      visible = true; // initially is open
    }
    props.setVisible(props.index, !visible);
  };

  return (
    <span {...DEPRECATED_onClick(() => toggleSection())}>
      <i
        className={"fa " + (props.isVisible(props.index) ? "fa-angle-up" : "fa-angle-down")}
        title={t("Toggle section visibility")}
      />
      {props.children}
    </span>
  );
};

export default SectionToggle;
