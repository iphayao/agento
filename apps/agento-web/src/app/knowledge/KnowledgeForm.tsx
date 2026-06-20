"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { knowledgeApi } from "@/lib/api";
import type { KnowledgeDocument, KnowledgeDocumentRequest, DocumentType } from "@/types";
import { DOCUMENT_TYPES } from "@/types";

interface Props {
  existing?: KnowledgeDocument;
}

export default function KnowledgeForm({ existing }: Props) {
  const router = useRouter();
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [title, setTitle] = useState(existing?.title ?? "");
  const [type, setType] = useState<DocumentType>(existing?.type ?? "BRAND_GUIDELINE");
  const [content, setContent] = useState(existing?.content ?? "");
  const [source, setSource] = useState(existing?.source ?? "");
  const [tagsInput, setTagsInput] = useState(existing?.tags?.join(", ") ?? "");

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSaving(true);

    const data: KnowledgeDocumentRequest = {
      title: title.trim(),
      type,
      content: content.trim(),
      source: source.trim() || undefined,
      tags: tagsInput
        .split(",")
        .map((t) => t.trim())
        .filter(Boolean),
    };

    try {
      if (existing) {
        await knowledgeApi.update(existing.id, data);
      } else {
        await knowledgeApi.create(data);
      }
      router.push("/knowledge");
      router.refresh();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Save failed");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm">
          {error}
        </div>
      )}

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Title <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
          maxLength={500}
          placeholder="e.g. SoClean Brand Voice Guidelines"
          className="input-field"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Document Type <span className="text-red-500">*</span>
        </label>
        <select
          value={type}
          onChange={(e) => setType(e.target.value as DocumentType)}
          className="input-field"
          required
        >
          {DOCUMENT_TYPES.map((dt) => (
            <option key={dt.value} value={dt.value}>
              {dt.label}
            </option>
          ))}
        </select>
        <p className="text-xs text-gray-400 mt-1">
          {typeHint(type)}
        </p>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Content <span className="text-red-500">*</span>
        </label>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          required
          rows={10}
          placeholder="Paste or type the knowledge content here..."
          className="input-field font-mono text-sm"
        />
        <p className="text-xs text-gray-400 mt-1">
          Content will be automatically chunked and embedded for semantic search.
        </p>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Source
        </label>
        <input
          type="text"
          value={source}
          onChange={(e) => setSource(e.target.value)}
          placeholder="e.g. Customer survey Q1 2024, TikTok analytics report"
          className="input-field"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Tags
        </label>
        <input
          type="text"
          value={tagsInput}
          onChange={(e) => setTagsInput(e.target.value)}
          placeholder="soclean, brand-voice, 2024 (comma separated)"
          className="input-field"
        />
      </div>

      <div className="flex gap-3 pt-2">
        <button type="submit" disabled={saving} className="btn-primary">
          {saving ? "Saving..." : existing ? "Update Document" : "Create Document"}
        </button>
        <button
          type="button"
          onClick={() => router.back()}
          className="btn-secondary"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}

function typeHint(type: DocumentType): string {
  const hints: Record<DocumentType, string> = {
    BRAND_GUIDELINE: "Brand voice, tone of voice, writing style, visual identity rules",
    PRODUCT_FACT: "Product specifications, proof points, verified product claims",
    APPROVED_CLAIM: "Marketing claims that are approved and safe to use",
    PROHIBITED_CLAIM: "Claims and language that must never appear in content",
    CUSTOMER_REVIEW: "Real customer reviews and testimonials",
    WINNING_CONTENT: "Past content that performed well — use as inspiration",
    COMPETITOR_NOTE: "Competitor positioning, gaps, and differentiation opportunities",
    MARKET_INSIGHT: "Market trends, channel insights, audience research",
  };
  return hints[type] ?? "";
}
