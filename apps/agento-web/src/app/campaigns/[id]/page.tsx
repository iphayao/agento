"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { campaignApi, contentApi } from "@/lib/api";
import type { Campaign, ContentStatus, GeneratedContent } from "@/types";
import { CONTENT_TYPES } from "@/types";

function StatusBadge({ status }: { status: ContentStatus }) {
  if (status === "APPROVED") return <span className="badge-approved">Approved</span>;
  if (status === "REJECTED") return <span className="badge-rejected">Rejected</span>;
  return <span className="badge-draft">Draft</span>;
}

function ComplianceNote({ notes }: { notes?: string }) {
  if (!notes) return null;
  const isWarning = notes.includes("WARNING");
  return (
    <div
      className={`text-xs mt-2 px-2 py-1 rounded ${
        isWarning
          ? "bg-red-50 text-red-700 border border-red-200"
          : "bg-green-50 text-green-700 border border-green-200"
      }`}
    >
      {notes}
    </div>
  );
}

export default function CampaignDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [contentList, setContentList] = useState<GeneratedContent[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [contentType, setContentType] = useState("TIKTOK_CAPTION");
  const [additionalContext, setAdditionalContext] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [generateError, setGenerateError] = useState<string | null>(null);

  const loadContent = useCallback(async () => {
    const res = await contentApi.listByCampaign(id);
    setContentList(res.data);
  }, [id]);

  useEffect(() => {
    Promise.all([campaignApi.get(id), contentApi.listByCampaign(id)])
      .then(([camp, content]) => {
        setCampaign(camp.data);
        setContentList(content.data);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  const handleGenerate = async () => {
    if (!contentType) return;
    setGenerating(true);
    setGenerateError(null);
    try {
      const res = await contentApi.generate(id, {
        contentType,
        additionalContext: additionalContext.trim() || undefined,
      });
      setContentList((prev) => [res.data, ...prev]);
      setAdditionalContext("");
    } catch (e: unknown) {
      setGenerateError(e instanceof Error ? e.message : "Generation failed");
    } finally {
      setGenerating(false);
    }
  };

  const handleApprove = async (contentId: string) => {
    try {
      const res = await contentApi.approve(contentId);
      setContentList((prev) =>
        prev.map((c) => (c.id === contentId ? res.data : c))
      );
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Approve failed");
    }
  };

  const handleReject = async (contentId: string) => {
    try {
      const res = await contentApi.reject(contentId);
      setContentList((prev) =>
        prev.map((c) => (c.id === contentId ? res.data : c))
      );
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Reject failed");
    }
  };

  const handleDelete = async (contentId: string) => {
    if (!confirm("Delete this content?")) return;
    try {
      await contentApi.delete(contentId);
      setContentList((prev) => prev.filter((c) => c.id !== contentId));
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Delete failed");
    }
  };

  if (loading) return <div className="p-8 text-gray-400">Loading...</div>;
  if (error) return <div className="p-8 text-red-500">{error}</div>;
  if (!campaign) return null;

  return (
    <div className="p-8">
      {/* Campaign header */}
      <div className="flex items-start justify-between mb-6">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Link href="/campaigns" className="text-gray-400 hover:text-gray-600 text-sm">
              ← Campaigns
            </Link>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">{campaign.name}</h1>
          <div className="flex gap-2 mt-2">
            {campaign.channel && (
              <span className="badge bg-purple-50 text-purple-700">{campaign.channel}</span>
            )}
            <span
              className={
                campaign.status === "ACTIVE"
                  ? "badge-active"
                  : campaign.status === "COMPLETED"
                  ? "badge-approved"
                  : "badge-draft"
              }
            >
              {campaign.status}
            </span>
          </div>
        </div>
        <Link href={`/campaigns/${id}/edit`} className="btn-secondary btn-sm">
          Edit Campaign
        </Link>
      </div>

      {/* Campaign details */}
      <div className="card mb-6">
        <div className="grid grid-cols-2 gap-4 text-sm">
          {campaign.objective && (
            <div>
              <span className="font-medium text-gray-700">Objective:</span>
              <p className="text-gray-600 mt-1">{campaign.objective}</p>
            </div>
          )}
          {campaign.targetAudience && (
            <div>
              <span className="font-medium text-gray-700">Target Audience:</span>
              <p className="text-gray-600 mt-1">{campaign.targetAudience}</p>
            </div>
          )}
          {campaign.contentAngle && (
            <div className="col-span-2">
              <span className="font-medium text-gray-700">Content Angle:</span>
              <p className="text-gray-600 mt-1">{campaign.contentAngle}</p>
            </div>
          )}
        </div>
      </div>

      {/* Generate Content */}
      <div className="card mb-8 border-blue-200 bg-blue-50">
        <h2 className="text-lg font-semibold text-blue-900 mb-4">Generate Content</h2>
        <div className="flex flex-wrap gap-3 items-end">
          <div>
            <label className="form-label text-blue-800">Content Type</label>
            <select
              className="form-input w-56"
              value={contentType}
              onChange={(e) => setContentType(e.target.value)}
              disabled={generating}
            >
              {CONTENT_TYPES.map((ct) => (
                <option key={ct.value} value={ct.value}>
                  {ct.label}
                </option>
              ))}
            </select>
          </div>
          <div className="flex-1 min-w-48">
            <label className="form-label text-blue-800">Additional Context (optional)</label>
            <input
              className="form-input"
              value={additionalContext}
              onChange={(e) => setAdditionalContext(e.target.value)}
              placeholder="e.g. Focus on office use, mention 900 sheets per pack"
              disabled={generating}
            />
          </div>
          <button
            onClick={handleGenerate}
            disabled={generating}
            className="btn-primary"
          >
            {generating ? (
              <span className="flex items-center gap-2">
                <span className="animate-spin">⏳</span> Generating...
              </span>
            ) : (
              "Generate Content"
            )}
          </button>
        </div>
        {generateError && (
          <p className="text-red-600 text-sm mt-3">{generateError}</p>
        )}
      </div>

      {/* Content list */}
      <div>
        <h2 className="text-lg font-semibold text-gray-800 mb-4">
          Generated Content ({contentList.length})
        </h2>

        {contentList.length === 0 && (
          <div className="card text-center py-8">
            <p className="text-gray-400">No content generated yet. Click Generate Content above.</p>
          </div>
        )}

        <div className="space-y-4">
          {contentList.map((content) => (
            <div key={content.id} className="card">
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-2">
                  <StatusBadge status={content.status} />
                  {content.contentType && (
                    <span className="badge bg-gray-100 text-gray-600 text-xs">
                      {content.contentType}
                    </span>
                  )}
                  {content.aiModel && (
                    <span className="text-xs text-gray-400">{content.aiModel}</span>
                  )}
                </div>
                <div className="flex gap-2">
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
                <div className="mb-2">
                  <span className="text-xs font-medium text-blue-600 uppercase tracking-wide">Hook</span>
                  <p className="text-gray-800 text-sm mt-1">{content.hook}</p>
                </div>
              )}
              {content.body && (
                <div className="mb-2">
                  <span className="text-xs font-medium text-gray-500 uppercase tracking-wide">Body</span>
                  <p className="text-gray-700 text-sm mt-1 whitespace-pre-wrap">{content.body}</p>
                </div>
              )}
              {content.callToAction && (
                <div className="mb-2">
                  <span className="text-xs font-medium text-green-600 uppercase tracking-wide">CTA</span>
                  <p className="text-gray-700 text-sm mt-1">{content.callToAction}</p>
                </div>
              )}
              {content.hashtags && content.hashtags.length > 0 && (
                <div className="mt-2 flex flex-wrap gap-1">
                  {content.hashtags.map((tag, i) => (
                    <span key={i} className="text-xs text-blue-600 font-medium">
                      {tag}
                    </span>
                  ))}
                </div>
              )}
              <ComplianceNote notes={content.complianceNotes} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
