import DOMPurify from "dompurify";
import parse from "html-react-parser";

DOMPurify.setConfig({
  // Only allow HTML content to be generated, disallow SVG etc
  USE_PROFILES: { html: true },
});

DOMPurify.addHook("uponSanitizeElement", (node, data) => {
  /**
   * If we encounter a bracketed email e.g. "DOM string <linux-bugs@example.com> example", don't treat it as a node and
   * insert it as text instead (bsc#1211469).
   */
  if (data.tagName.includes("@")) {
    return (node.textContent = data.tagName + node.textContent);
  }
  // Otherwise keep default behavior
  return undefined;
});

/** Convert a HTML string to React elements */
export const stringToReact = (input: string | null | undefined) => {
  return parse(DOMPurify.sanitize(input ?? ""));
};
