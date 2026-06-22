"use client";

import Link from "next/link";
import { useState } from "react";
import { knowledgeApi } from "@/lib/api";
import type { DocumentType, KnowledgeChunkMatch } from "@/types";
import { DOCUMENT_TYPES } from "@/types";

export default function KnowledgeSearchPage() {
  const [query, setQuery] = useState("");
  const [docType, setDocType] = useState<DocumentType | "">("");
  const [topK, setTopK] = useState(5);
  const [minScore, setMinScore] = useState(0.0);
  const [results, setResults] = useState<KnowledgeChunkMatch[]>([]);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searched, setSearched] = useState(false);

  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    if (!query.trim()) return;
    setError(null);
    setSearching(true);
    setSearched(false);
    try {
      const res = await knowledgeApi.search({
        query: query.trim(),
        documentType: docType || undefined,
        topK,
        minScore,
      });
      setResults(res.data.results);
      setSearched(true);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Search failed");
    } finally {
      setSearching(false);
    }
  }

  function scoreColor(score: number) {
    if (score >= 0.85) return "text-green-700 bg-green-50";
    if (score >= 0.7) return "text-blue-700 bg-blue-50";
    if (score >= 0.5) return "text-yellow-700 bg-yellow-50";
    return "text-gray-500 bg-gray-50";
  }

  return (
    <div className="p-8 max-w-3xl">
      <div className="mb-6">
        <Link href="/knowledge" className="text-sm text-gray-500 hover:text-gray-700">
          ← Knowledge Base
        </Link>
        <h1 className="text-2xl font-bold text-gray-900 mt-2">Semantic Search</h1>
        <p className="text-gray-500 text-sm mt-1">
          Test the vector search used by AI agents during content generation.
        </p>
      </div>

      <div className="card mb-6">
        <form onSubmit={handleSearch} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Search Query
            </label>
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="e.g. brand voice and tone of voice guidelines"
              className="input-field"
              required
            />
          </div>

          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Document Type
              </label>
              <select
                value={docType}
                onChange={(e) => setDocType(e.target.value as DocumentType | "")}
                className="input-field"
              >
                <option value="">All types</option>
                {DOCUMENT_TYPES.map((dt) => (
                  <option key={dt.value} value={dt.value}>
                    {dt.label}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Top K
              </label>
              <input
                type="number"
                min={1}
                max={20}
                value={topK}
                onChange={(e) => setTopK(Number(e.target.value))}
                className="input-field"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Min Score (0–1)
              </label>
              <input
                type="number"
                min={0}
                max={1}
                step={0.05}
                value={minScore}
                onChange={(e) => setMinScore(Number(e.target.value))}
                className="input-field"
              />
            </div>
          </div>

          <button type="submit" disabled={searching} className="btn-primary">
            {searching ? "Searching..." : "Search"}
          </button>
        </form>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm mb-4">
          {error}
        </div>
      )}

      {searched && results.length === 0 && (
        <div className="card text-center py-8 text-gray-400">
          No results found. Try a different query or lower the min score.
        </div>
      )}

      {results.length > 0 && (
        <div className="space-y-3">
          <p className="text-sm text-gray-500 font-medium">{results.length} result{results.length !== 1 ? "s" : ""}</p>
          {results.map((match, idx) => (
            <div key={match.chunkId} className="card">
              <div className="flex items-start justify-between gap-3 mb-2">
                <div>
                  <span className="text-xs font-medium text-gray-500">#{idx + 1} · </span>
                  <Link
                    href={`/knowledge/${match.documentId}`}
                    className="text-sm font-medium text-zinc-700 hover:text-zinc-900 hover:underline"
                  >
                    {match.documentTitle}
                  </Link>
                  <span className="text-xs text-gray-400 ml-2">{match.documentType}</span>
                </div>
                <span className={`badge font-mono text-xs ${scoreColor(match.score)}`}>
                  {match.score.toFixed(4)}
                </span>
              </div>
              <p className="text-sm text-gray-700 whitespace-pre-wrap">{match.chunkText}</p>
              <p className="text-xs text-gray-400 mt-2">Chunk {match.chunkIndex + 1}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
