import React, {useEffect} from "react";
//global handleSst

declare var handleSst:function;

export const SectionToolbar =  ({children}) => {
  useEffect(() => {
    handleSst();
  }, [])

  return (
    <div className='spacewalk-section-toolbar'>
      {children}
    </div>
  )
}
