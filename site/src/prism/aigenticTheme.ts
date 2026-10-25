import type { PrismTheme } from "prism-react-renderer";

export const aigenticTheme: PrismTheme = {
  plain: {
    color: "#e6e1d6",
    backgroundColor: "#161616",
  },
  styles: [
    {
      types: ["comment", "prolog", "doctype", "cdata"],
      style: { color: "#8a857a", fontStyle: "italic" },
    },
    {
      types: ["keyword", "selector", "important", "atrule"],
      style: { color: "#f0bc42" },
    },
    {
      types: ["annotation"],
      style: { color: "#f0bc42", fontStyle: "italic" },
    },
    {
      types: ["function", "function-variable", "method"],
      style: { color: "#8fd0c4" },
    },
    {
      types: ["string", "char", "template-string", "attr-value", "regex"],
      style: { color: "#eba57f" },
    },
    {
      types: ["number", "boolean", "constant", "symbol"],
      style: { color: "#eba57f" },
    },
    {
      types: ["class-name", "tag", "property"],
      style: { color: "#f6dca0" },
    },
    {
      types: ["attr-name", "variable", "parameter"],
      style: { color: "#e6e1d6" },
    },
    {
      types: ["operator", "entity", "url"],
      style: { color: "#c9c3b6" },
    },
    {
      types: ["punctuation"],
      style: { color: "#9a958a" },
    },
    {
      types: ["deleted"],
      style: { color: "#e5786d" },
    },
    {
      types: ["inserted"],
      style: { color: "#8fd0c4" },
    },
  ],
};
