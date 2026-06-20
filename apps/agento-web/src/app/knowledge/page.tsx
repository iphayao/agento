"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { knowledgeApi } from "@/lib/api";
import type { KnowledgeDocument, DocumentType } from "@/types";
import { DOCUMENT_TYPES } from "@/types";

const TYPE_COLORS: Record<DocumentType, string> = {
  BRAND_GUIDELINE: "bg-blue-50 text-blue-700",
  PRODUCT_FACT: "bg-green-50 text-green-700",
  APPROVED_CLAIM: "bg-emerald-50 text-emerald-700",
  PROHIBITED_CLAIM: "bg-red-50 text-red-700",
  CUSTOMER_REVIEW: "bg-purple-50 text-purple-700",
  WINNING_CONTENT: "bg-yellow-50 text-yellow-700",
  COMPETITOR_NOTE: "bg-orange-50 text-orange-700",
  MARKET_INSIGHT: "bg-indigo-50 text-indigo-700",
};

export default function KnowledgeListPage() {
  const [docs, setDocs] = useState<KnowledgeDocument[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterType, setFilterType] = useState<string>("ALL");

  useEffect(() => {
    knowledgeApi
      .list()
      .then((res) => setDocs(res.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this knowledge document? This will also delete its chunks and embeddings."))
      return;
    try {
      await knowledgeApi.delete(id);
      setDocs((prev) => prev.filter((d) => d.id !== id));
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Delete failed");
    }
  };

  const filtered =
    filterType === "ALL" ? docs : docs.filter((d) => d.type === filterType);

  const counts = docs.reduce<Record<string, number>>((acc, d) => {
    acc[d.type] = (acc[d.type] ?? 0) + 1;
    return acc;
  }, {});

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Knowledge Base</h1>
          <p className="text-gray-500 text-sm mt-1">
            Brand memory — {docs.length} documents ·{" "}
            {docs.reduce((s, d) => s + d.chunkCount, 0)} chunks embedded
          </p>
        </div>
        <div className="flex gap-2">
          <Link href="/knowledge/search" className="btn-secondary">
            Test Search
          </Link>
          <Link href="/knowledge/new" className="btn-primary">
            + Add Document
          </Link>
        </div>
      </div>

      {/* Type filter tabs */}
      <div className="flex gap-2 flex-wrap mb-5">
        <button
          onClick={() => setFilterType("ALL")}
          className={`px-3 py-1 rounded-full text-xs font-medium border transition-colors ${
            filterType === "ALL"
              ? "bg-gray-800 text-white border-gray-800"
              : "bg-white text-gray-600 border-gray-200 hover:border-gray-400"
          }`}
        >
          All ({docs.length})
        </button>
        {DOCUMENT_TYPES.map((dt) => (
          <button
            key={dt.value}
            onClick={() => setFilterType(dt.value)}
            className={`px-3 py-1 rounded-full text-xs font-medium border transition-colors ${
              filterType === dt.value
                ? "bg-gray-800 text-white border-gray-800"
                : "bg-white text-gray-600 border-gray-200 hover:border-gray-400"
            }`}
          >
            {dt.label} ({counts[dt.value] ?? 0})
          </button>
        ))}
      </div>

      {loading && <p className="text-gray-400">Loading...</p>}
      {error && <p className="text-red-500 text-sm">{error}</p>}

      {!loading && filtered.length === 0 && (
        <div className="card text-center py-12">
          <p className="text-gray-500 mb-4">
            No knowledge documents yet. Add brand guidelines, product facts, approved claims,
            customer reviews, or winning content.
          </p>
          <Link href="/knowledge/new" className="btn-primary">
            Add First Document
          </Link>
        </div>
      )}

      <div className="space-y-3">
        {filtered.map((doc) => (
          <div key={doc.id} className="card">
            <div className="flex items-start justify-between gap-4">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span
                    className={`badge ${TYPE_COLORS[doc.type] ?? "bg-gray-50 text-gray-600"}`}
                  >
                    {DOCUMENT_TYPES.find((d) => d.value === doc.type)?.label ?? doc.type}
                  </span>
                  {doc.status === "ARCHIVED" && (
                    <span className="badge bg-gray-100 text-gray-400">Archived</span>
                  )}
                </div>
                <h2 className="text-base font-semibold text-gray-900 truncate">{doc.title}</h2>
                <p className="text-gray-500 text-sm mt-1 line-clamp-2">
                  {doc.content.slice(0, 150)}
                  {doc.content.length > 150 ? "..." : ""}
                </p>
                <div className="flex items-center gap-3 mt-2 text-xs text-gray-400">
                  <span>{doc.chunkCount} chunk{doc.chunkCount !== 1 ? "s" : ""}</span>
                  {doc.source && <span>Source: {doc.source}</span>}
                  {doc.tags && doc.tags.length > 0 && (
                    <span>{doc.tags.join(", ")}</span>
                  )}
                </div>
              </div>
              <div className="flex gap-2 shrink-0">
                <Link href={`/knowledge/${doc.id}`} className="btn-secondary btn-sm">
                  Edit
                </Link>
                <button
                  onClick={() => handleDelete(doc.id)}
                  className="btn-danger btn-sm"
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
