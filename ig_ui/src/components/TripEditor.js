"use client";

import {LexicalComposer} from "@lexical/react/LexicalComposer";
import {RichTextPlugin} from "@lexical/react/LexicalRichTextPlugin";
import {ContentEditable} from "@lexical/react/LexicalContentEditable";
import {HistoryPlugin} from "@lexical/react/LexicalHistoryPlugin";
import {OnChangePlugin} from "@lexical/react/LexicalOnChangePlugin";
import {LexicalErrorBoundary} from "@lexical/react/LexicalErrorBoundary";
import {ListPlugin} from "@lexical/react/LexicalListPlugin";
import {LinkPlugin} from "@lexical/react/LexicalLinkPlugin";
import {useLexicalComposerContext} from "@lexical/react/LexicalComposerContext";
import {HeadingNode, QuoteNode, $createHeadingNode, $createQuoteNode} from "@lexical/rich-text";
import {ListNode, ListItemNode, INSERT_ORDERED_LIST_COMMAND, INSERT_UNORDERED_LIST_COMMAND} from "@lexical/list";
import {LinkNode, $toggleLink} from "@lexical/link";
import {$createParagraphNode, $getSelection, $isRangeSelection, $setSelection, FORMAT_TEXT_COMMAND} from "lexical";
import {$setBlocksType} from "@lexical/selection";
import {Pilcrow, Heading2, Heading3, Quote, Bold, Italic, List, ListOrdered, Link2} from "lucide-react";

const nodes = [HeadingNode, QuoteNode, ListNode, ListItemNode, LinkNode];
const theme = {
  paragraph: "mb-3",
  heading: {h2: "mt-7 text-2xl font-bold", h3: "mt-6 text-xl font-bold"},
  quote: "my-5 border-l-4 border-amber-500 pl-4 italic",
  list: {ul: "my-3 list-disc pl-7", ol: "my-3 list-decimal pl-7"},
  link: "text-amber-700 underline",
  text: {bold: "font-bold", italic: "italic"},
};
const buttonClass = "rounded border border-black bg-white p-2 transition-colors hover:border-border-btn hover:bg-amber-100 focus:border-border-btn focus:outline-none focus:ring-2 focus:ring-border-btn/30";

function Toolbar() {
  const [editor] = useLexicalComposerContext();
  function block(factory) {
    editor.update(() => {
      const selection = $getSelection();
      if ($isRangeSelection(selection)) $setBlocksType(selection, factory);
    });
  }
  function link() {
    const selection = editor.getEditorState().read(() => {
      const current = $getSelection();
      return $isRangeSelection(current) ? current.clone() : null;
    });
    if (!selection) return;
    const url = window.prompt("Link URL (https://...)");
    if (url === null) return;
    if (url && !/^https?:\/\//i.test(url)) {
      window.alert("Use an http or https link.");
      return;
    }
    editor.update(() => {
      $setSelection(selection);
      $toggleLink(url || null);
    });
  }
  return <div role="toolbar" aria-label="Text formatting" onMouseDown={(event) => event.preventDefault()} className="flex flex-wrap gap-2 border-b border-black p-2">
    <button type="button" className={buttonClass} aria-label="Paragraph" title="Paragraph" onClick={() => block(() => $createParagraphNode())}><Pilcrow size={20} aria-hidden="true" /></button>
    <button type="button" className={buttonClass} aria-label="Heading" title="Heading" onClick={() => block(() => $createHeadingNode("h2"))}><Heading2 size={20} aria-hidden="true" /></button>
    <button type="button" className={buttonClass} aria-label="Subheading" title="Subheading" onClick={() => block(() => $createHeadingNode("h3"))}><Heading3 size={20} aria-hidden="true" /></button>
    <button type="button" className={buttonClass} aria-label="Quote" title="Quote" onClick={() => block(() => $createQuoteNode())}><Quote size={20} aria-hidden="true" /></button>
    <button type="button" className={buttonClass} aria-label="Bold" title="Bold" onClick={() => editor.dispatchCommand(FORMAT_TEXT_COMMAND, "bold")}><Bold size={20} aria-hidden="true" /></button>
    <button type="button" className={buttonClass} aria-label="Italic" title="Italic" onClick={() => editor.dispatchCommand(FORMAT_TEXT_COMMAND, "italic")}><Italic size={20} aria-hidden="true" /></button>
    <button type="button" className={buttonClass} aria-label="Bulleted list" title="Bulleted list" onClick={() => editor.dispatchCommand(INSERT_UNORDERED_LIST_COMMAND, undefined)}><List size={20} aria-hidden="true" /></button>
    <button type="button" className={buttonClass} aria-label="Numbered list" title="Numbered list" onClick={() => editor.dispatchCommand(INSERT_ORDERED_LIST_COMMAND, undefined)}><ListOrdered size={20} aria-hidden="true" /></button>
    <button type="button" className={buttonClass} aria-label="Link" title="Link" onClick={link}><Link2 size={20} aria-hidden="true" /></button>
  </div>;
}

export default function TripEditor({onChange}) {
  return <LexicalComposer initialConfig={{namespace: "TripEditor", nodes, theme, onError: (error) => {throw error;}}}>
    <div className="rounded-lg border-2 border-black bg-white transition-colors hover:border-border-btn focus-within:border-border-btn focus-within:ring-2 focus-within:ring-border-btn/30">
      <Toolbar />
      <div className="group relative min-h-48 p-3">
        <RichTextPlugin contentEditable={<ContentEditable aria-label="Trip story" className="min-h-40 text-lg leading-relaxed outline-none" />}
          placeholder={<p className="pointer-events-none absolute left-3 top-3 text-lg leading-relaxed text-gray-500 group-focus-within:hidden">Tell the story of your trip...</p>}
          ErrorBoundary={LexicalErrorBoundary} />
      </div>
      <HistoryPlugin />
      <ListPlugin />
      <LinkPlugin />
      <OnChangePlugin onChange={(state) => onChange(state.toJSON())} />
    </div>
  </LexicalComposer>;
}
