import * as React from "react";
import { useEffect } from "react";

type Props = {
  children: React.ReactNode;
};

export const SectionToolbar = ({ children }: Props) => {
  useEffect(() => {
    handleSst?.();
  }, []);

  return <div className="spacewalk-section-toolbar">{children}</div>;
};
