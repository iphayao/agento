"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { knowledgeApi } from "@/lib/api";
import type { KnowledgeDocument, KnowledgeChunk } from "@/types";
import KnowledgeForm from "../KnowledgeForm";

export default function EditKnowledgePage() {
  const { id } = useParams<{ id: string }>();
  const [doc, setDoc] = useState<KnowledgeDocument | null>(null);
  const [chunks, setChunks] = useState<KnowledgeChunk[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState<"edit" | "chunks">("edit");

  useEffect(() => {
    Promise.all([knowledgeApi.get(id), knowledgeApi.getChunks(id)])
      .then(([docRes, chunksRes]) => {
        setDoc(docRes.data);
        setChunks(chunksRes.data);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="p-8 text-gray-400">Loading...</div>;
  if (error) return <div className="p-8 text-red-500">{error}</div>;
  if (!doc) return null;

  const embeddedCount = chunks.filter((c) => c.hasEmbedding).length;

  return (
    <div className="p-8 max-w-3xl">
      <div className="mb-6">
        <Link href="/knowledge" className="text-sm text-gray-500 hover:text-gray-700">
          ← Knowledge Base
        </Link>
        <h1 className="text-2xl font-bold text-gray-900 mt-2">{doc.title}</h1>
        <p className="text-gray-500 text-sm mt-1">
          {doc.chunkCount} chunk{doc.chunkCount !== 1 ? "s" : ""} ·{" "}
          {embeddedCount} embedded
        </p>
      </div>

      <div className="flex gap-2 mb-5">
        <button
          onClick={() => setTab("edit")}
          className={`px-4 py-2 rounded text-sm font-medium ${
            tab === "edit"
              ? "bg-blue-600 text-white"
              : "bg-white text-gray-600 border border-gray-200"
          }`}
        >
          Edit Document
        </button>
        <button
          onClick={() => setTab("chunks")}
          className={`px-4 py-2 rounded text-sm font-medium ${
            tab === "chunks"
              ? "bg-blue-600 text-white"
              : "bg-white text-gray-600 border border-gray-200"
          }`}
        >
          View Chunks ({chunks.length})
        </button>
      </div>

      {tab === "edit" && (
        <div className="card">
          <KnowledgeForm existing={doc} />
        </div>
      )}

      {tab === "chunks" && (
        <div className="space-y-3">
          {chunks.length === 0 && (
            <div className="card text-center py-8 text-gray-400">
              No chunks yet. Save the document to generate embeddings.
            </div>
          )}
          {chunks.map((chunk) => (
            <div key={chunk.id} className="card">
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-xs font-medium text-gray-500">
                      Chunk {chunk.chunkIndex + 1}
                    </span>
                    {chunk.hasEmbedding ? (
                      <span className="badge bg-green-50 text-green-700">Embedded</span>
                    ) : (
                      <span className="badge bg-yellow-50 text-yellow-600">No embedding</span>
                    )}
                  </div>
                  <p className="text-sm text-gray-700 whitespace-pre-wrap">{chunk.chunkText}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
