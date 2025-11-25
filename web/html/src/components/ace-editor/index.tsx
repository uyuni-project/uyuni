import { useEffect, useRef } from "react";

import type { Ace } from "ace-builds";

import { useId } from "utils/hooks";

declare global {
  interface Window {
    // See java/code/webapp/WEB-INF/decorators/layout_head.jsp
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
  onChange?: (content: string) => void;
  className?: string;
  id?: string;
  content?: React.ReactNode;
};

const AceEditor = ({ minLines = 20, maxLines = 40, readOnly = false, content = "", onChange, ...props }: Props) => {
  const fallbackId = useId();
  const nodeRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<Ace.Editor>();

  useEffect(() => {
    try {
      const node = nodeRef.current;
      if (!node) {
        throw new RangeError("Unable to find node for ace-editor");
      }
      if (!editorRef.current) {
        const editor = window.ace.edit(node);
        editor.setTheme("ace/theme/xcode");
        editor.setShowPrintMargin(false);
        editor.getSession().setValue(content || "");
        editor.on("change", () => {
          onChange?.(editor.getSession().getValue());
        });

        editorRef.current = editor;
      }
    } catch (error) {
      Loggerhead.error("Failed to configure Ace editor");
      Loggerhead.error(error);
    }
  }, []);

  // Update editor value when `content` prop changes in edit mode
  useEffect(() => {
    const editor = editorRef.current;
    if (!editor) return;
    const currentValue = editor.getSession().getValue();
    if (currentValue !== content) {
      editor.setValue(content || "");
    }
  }, [content]);

  useEffect(() => {
    if (!editorRef.current) return;

    if (props.mode) {
      editorRef.current.getSession().setMode("ace/mode/" + props.mode);
    }
    editorRef.current.setOptions({ minLines, maxLines });
    editorRef.current.setReadOnly(readOnly);
  }, [props.mode, minLines, maxLines, readOnly]);

  return <div ref={nodeRef} className={props.className} id={props.id ?? fallbackId} />;
};

export { AceEditor };
