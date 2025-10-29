import { useEffect, useRef } from "react";

import type { Ace } from "ace-builds";

import { useId } from "utils/hooks";

declare global {
  interface Window {
    ace: {
      edit(node: HTMLDivElement): Ace.Editor;
    };
  }
}

type Props = {
  mode?: string;
  minLines?: number;
  maxLines?: number;
  readOnly?: boolean;
  onChange?: (...args: any[]) => any;
  className?: string;
  id?: string;
  content?: React.ReactNode;
};

const AceEditor = ({ minLines = 20, maxLines = 40, readOnly = false, ...props }: Props) => {
  const fallbackId = useId();
  const nodeRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<Ace.Editor>();

  useEffect(() => {
    const s1 = document.createElement("script");
    s1.setAttribute("src", `/javascript/legacy/ace-editor/ace.js?cb=${Math.random() * 100}`);
    s1.addEventListener("load", () => {
      const s2 = document.createElement("script");
      s2.setAttribute("src", `/javascript/legacy/ace-editor/ext-modelist.js?cb=${Math.random() * 100}`);

      s2.addEventListener("load", () => {
        // TODO: Move down
      });
      document.head.appendChild(s2);
    });
    document.head.appendChild(s1);
  }, []);

  useEffect(() => {
    // TODO: Copy over

    try {
      const node = nodeRef.current;
      if (!node) {
        throw new RangeError("Unable to find node for ace-editor");
      }

      if (!editorRef.current) {
        const editor = window.ace.edit(node);
        editor.setTheme("ace/theme/xcode");
        editor.setShowPrintMargin(false);
        editor.on("change", () => props.onChange?.(editor.getSession().getValue()));
        editorRef.current = editor;
      }

      if (props.mode) {
        editorRef.current.getSession().setMode("ace/mode/" + props.mode);
      }
      editorRef.current.setOptions({ minLines, maxLines });
      editorRef.current.setReadOnly(readOnly);
    } catch (error) {
      Loggerhead.error("Failed to configure Ace editor");
      Loggerhead.error(error);
    }
  }, [props.mode, minLines, maxLines, readOnly]);

  return (
    <div ref={nodeRef} className={props.className} id={props.id ?? fallbackId}>
      {props.content}
    </div>
  );
};

export { AceEditor };
