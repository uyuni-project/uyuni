import { type ReactNode, useEffect } from "react";
type Props = {
  children: ReactNode;
  top?: string;
};

export const SectionToolbar = ({ children, top }: Props) => {
  useEffect(() => {
    handleSst?.();
  }, []);

  return (
    <div className="spacewalk-section-toolbar" style={{ top: `${top}px` }}>
      {children}
    </div>
  );
};
