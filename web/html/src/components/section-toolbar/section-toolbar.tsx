import { type ReactNode, useEffect } from "react";
type Props = {
  children: ReactNode;
};

export const SectionToolbar = ({ children }: Props) => {
  useEffect(() => {
    handleSst?.();
  }, []);

  return <div className="spacewalk-section-toolbar">{children}</div>;
};
