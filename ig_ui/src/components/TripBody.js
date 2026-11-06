import React from "react";

function renderNode(node, key) {
  if (!node || typeof node !== "object") return null;
  if (node.type === "text") {
    let content = node.text;
    if (node.format & 1) content = <strong>{content}</strong>;
    if (node.format & 2) content = <em>{content}</em>;
    return <React.Fragment key={key}>{content}</React.Fragment>;
  }
  if (node.type === "linebreak") return <br key={key} />;
  const children = node.children?.map((child, index) => renderNode(child, index));
  switch (node.type) {
    case "paragraph": return <p key={key} className="my-3">{children}</p>;
    case "heading": return node.tag === "h3"
      ? <h3 key={key} className="mt-6 text-xl font-bold">{children}</h3>
      : <h2 key={key} className="mt-7 text-2xl font-bold">{children}</h2>;
    case "quote": return <blockquote key={key} className="my-5 border-l-4 border-amber-500 pl-4 italic">{children}</blockquote>;
    case "list": return node.listType === "number"
      ? <ol key={key} className="my-3 list-decimal pl-7">{children}</ol>
      : <ul key={key} className="my-3 list-disc pl-7">{children}</ul>;
    case "listitem": return <li key={key}>{children}</li>;
    case "link": return <a key={key} href={node.url} rel="noopener noreferrer" className="text-amber-700 underline">{children}</a>;
    default: return null;
  }
}

export default function TripBody({body, description}) {
  if (!body?.root?.children) return <p className="whitespace-pre-wrap text-lg leading-relaxed my-7">{description}</p>;
  return <div className="my-7 text-lg leading-relaxed">{body.root.children.map((node, index) => renderNode(node, index))}</div>;
}
