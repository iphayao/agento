"use client";

import { useEffect, useState } from "react";
import { contentApi } from "@/lib/api";
import type { ContentStatus, GeneratedContent } from "@/types";

function StatusBadge({ status }: { status: ContentStatus }) {
  if (status === "APPROVED") return <span className="badge-approved">Approved</span>;
  if (status === "REJECTED") return <span className="badge-rejected">Rejected</span>;
  return <span className="badge-draft">Draft</span>;
}

const FILTERS: { label: string; value: ContentStatus | "ALL" }[] = [
  { label: "All", value: "ALL" },
  { label: "Draft", value: "DRAFT" },
  { label: "Approved", value: "APPROVED" },
  { label: "Rejected", value: "REJECTED" },
];

export default function ContentReviewPage() {
  const [all, setAll] = useState<GeneratedContent[]>([]);
  const [filter, setFilter] = useState<ContentStatus | "ALL">("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    contentApi
      .list()
      .then((res) => setAll(res.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const displayed = filter === "ALL" ? all : all.filter((c) => c.status === filter);

  const handleApprove = async (id: string) => {
    try {
      const res = await contentApi.approve(id);
      setAll((prev) => prev.map((c) => (c.id === id ? res.data : c)));
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Failed");
    }
  };

  const handleReject = async (id: string) => {
    try {
      const res = await contentApi.reject(id);
      setAll((prev) => prev.map((c) => (c.id === id ? res.data : c)));
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Failed");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this content?")) return;
    try {
      await contentApi.delete(id);
      setAll((prev) => prev.filter((c) => c.id !== id));
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Failed");
    }
  };

  const counts = {
    ALL: all.length,
    DRAFT: all.filter((c) => c.status === "DRAFT").length,
    APPROVED: all.filter((c) => c.status === "APPROVED").length,
    REJECTED: all.filter((c) => c.status === "REJECTED").length,
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Content Review</h1>
        <p className="text-gray-500 text-sm mt-1">
          Review, approve, or reject AI-generated content
        </p>
      </div>

      {/* Filter tabs */}
      <div className="flex gap-2 mb-6">
        {FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => setFilter(f.value)}
            className={`px-4 py-2 rounded-full text-sm font-medium transition-colors ${
              filter === f.value
                ? "bg-zinc-900 text-white"
                : "bg-white text-zinc-600 border border-zinc-200 hover:bg-zinc-50"
            }`}
          >
            {f.label}{" "}
            <span
              className={`ml-1 ${filter === f.value ? "text-zinc-300" : "text-zinc-400"}`}
            >
              {counts[f.value]}
            </span>
          </button>
        ))}
      </div>

      {loading && <p className="text-gray-400">Loading...</p>}
      {error && <p className="text-red-500 text-sm">{error}</p>}

      {!loading && displayed.length === 0 && (
        <div className="card text-center py-12">
          <p className="text-gray-400">No content in this category.</p>
        </div>
      )}

      <div className="space-y-4">
        {displayed.map((content) => (
          <div key={content.id} className="card">
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-2 flex-wrap">
                <StatusBadge status={content.status} />
                {content.contentType && (
                  <span className="badge bg-gray-100 text-gray-600 text-xs">
                    {content.contentType}
                  </span>
                )}
                {content.channel && (
                  <span className="badge bg-purple-50 text-purple-700 text-xs">
                    {content.channel}
                  </span>
                )}
                {content.aiModel && (
                  <span className="text-xs text-gray-400">{content.aiModel}</span>
                )}
                <span className="text-xs text-gray-400">
                  {new Date(content.createdAt).toLocaleDateString("th-TH", {
                    day: "numeric",
                    month: "short",
                    year: "numeric",
                    hour: "2-digit",
                    minute: "2-digit",
                  })}
                </span>
              </div>
              <div className="flex gap-2 ml-4">
                {content.status === "DRAFT" && (
                  <>
                    <button
                      onClick={() => handleApprove(content.id)}
                      className="btn-success btn-sm"
                    >
                      Approve
                    </button>
                    <button
                      onClick={() => handleReject(content.id)}
                      className="btn-danger btn-sm"
                    >
                      Reject
                    </button>
                  </>
                )}
                <button
                  onClick={() => handleDelete(content.id)}
                  className="btn-secondary btn-sm"
                >
                  Delete
                </button>
              </div>
            </div>

            {content.title && (
              <h3 className="font-semibold text-gray-900 mb-2">{content.title}</h3>
            )}
            {content.hook && (
              <p className="text-zinc-800 text-sm font-medium mb-1">{content.hook}</p>
            )}
            {content.body && (
              <p className="text-gray-700 text-sm whitespace-pre-wrap mb-2">{content.body}</p>
            )}
            {content.callToAction && (
              <p className="text-green-700 text-sm font-medium mb-2">{content.callToAction}</p>
            )}
            {content.hashtags && content.hashtags.length > 0 && (
              <div className="flex flex-wrap gap-1 mb-2">
                {content.hashtags.map((tag, i) => (
                  <span key={i} className="text-xs text-zinc-500 font-medium">
                    {tag}
                  </span>
                ))}
              </div>
            )}
            {content.complianceNotes && (
              <div
                className={`text-xs px-2 py-1 rounded mt-1 ${
                  content.complianceNotes.includes("WARNING")
                    ? "bg-red-50 text-red-700 border border-red-200"
                    : "bg-green-50 text-green-700 border border-green-200"
                }`}
              >
                {content.complianceNotes}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
