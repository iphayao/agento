"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import { campaignApi, contentApi, workflowApi } from "@/lib/api";
import type {
  AgentStepStatus,
  AgentWorkflow,
  AgentWorkflowStatus,
  Campaign,
  ContentStatus,
  GeneratedContent,
} from "@/types";
import { AGENT_STEP_LABELS, AGENT_STEPS, CONTENT_TYPES } from "@/types";

// ─── Status helpers ────────────────────────────────────────────────────────────

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

function WorkflowStatusBadge({ status }: { status: AgentWorkflowStatus }) {
  const map: Record<AgentWorkflowStatus, string> = {
    PENDING: "bg-gray-100 text-gray-600",
    RUNNING: "bg-blue-100 text-blue-700",
    COMPLETED: "bg-green-100 text-green-700",
    FAILED: "bg-red-100 text-red-700",
    CANCELLED: "bg-orange-100 text-orange-700",
  };
  return (
    <span className={`text-xs font-semibold px-2 py-0.5 rounded ${map[status]}`}>
      {status}
    </span>
  );
}

function StepStatusIcon({ status }: { status: AgentStepStatus }) {
  if (status === "COMPLETED") return <span className="text-green-500">✓</span>;
  if (status === "FAILED") return <span className="text-red-500">✗</span>;
  if (status === "RUNNING") return <span className="animate-spin inline-block">⏳</span>;
  if (status === "SKIPPED") return <span className="text-gray-400">—</span>;
  return <span className="text-gray-300">○</span>;
}

// ─── Agent Workflow Panel ─────────────────────────────────────────────────────

function AgentWorkflowPanel({ campaignId, onContentCreated }: {
  campaignId: string;
  onContentCreated: () => void;
}) {
  const [workflows, setWorkflows] = useState<AgentWorkflow[]>([]);
  const [running, setRunning] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);
  // Tracks whether we were previously polling so we know when to call onContentCreated
  const wasPollRef = useRef(false);

  const load = useCallback(async () => {
    try {
      const res = await workflowApi.listByCampaign(campaignId);
      setWorkflows(res.data);
    } catch {
      // silently ignore — campaign page handles main errors
    }
  }, [campaignId]);

  useEffect(() => {
    load();
  }, [load]);

  // Poll active workflows every 3 s until they complete, fail, or are cancelled.
  // When polling stops (transition from active → not active), refresh the content list.
  useEffect(() => {
    const hasActive = workflows.some(
      (w) => w.status === "PENDING" || w.status === "RUNNING"
    );

    if (hasActive && !pollRef.current) {
      pollRef.current = setInterval(load, 3000);
      wasPollRef.current = true;
    }

    if (!hasActive && pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }

    // Fire onContentCreated once when polling transitions from active → stopped
    if (!hasActive && wasPollRef.current) {
      wasPollRef.current = false;
      onContentCreated();
    }

    return () => {
      if (pollRef.current) {
        clearInterval(pollRef.current);
        pollRef.current = null;
      }
    };
  }, [workflows, load, onContentCreated]);

  const handleStart = async () => {
    setRunning(true);
    setError(null);
    try {
      const res = await workflowApi.start(campaignId);
      setWorkflows((prev) => [res.data, ...prev]);
      setExpandedId(res.data.id);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to start workflow");
    } finally {
      setRunning(false);
    }
  };

  const handleRetry = async (workflowId: string) => {
    try {
      const res = await workflowApi.retry(workflowId);
      setWorkflows((prev) =>
        prev.map((w) => (w.id === workflowId ? res.data : w))
      );
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Retry failed");
    }
  };

  const handleCancel = async (workflowId: string) => {
    if (!confirm("Cancel this workflow?")) return;
    try {
      const res = await workflowApi.cancel(workflowId);
      setWorkflows((prev) =>
        prev.map((w) => (w.id === workflowId ? res.data : w))
      );
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Cancel failed");
    }
  };

  return (
    <div className="card mb-8 border-indigo-200 bg-indigo-50">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-indigo-900">Agent Workflow (Phase 2)</h2>
        <button
          onClick={handleStart}
          disabled={running}
          className="btn-primary bg-indigo-600 hover:bg-indigo-700"
        >
          {running ? (
            <span className="flex items-center gap-2">
              <span className="animate-spin">⏳</span> Starting…
            </span>
          ) : (
            "Run Agent Workflow"
          )}
        </button>
      </div>

      {error && <p className="text-red-600 text-sm mb-3">{error}</p>}

      {workflows.length === 0 && (
        <p className="text-sm text-indigo-600">
          No workflows yet. Click &ldquo;Run Agent Workflow&rdquo; to start a 7-step AI content generation.
        </p>
      )}

      <div className="space-y-4">
        {workflows.map((wf) => (
          <WorkflowCard
            key={wf.id}
            workflow={wf}
            expanded={expandedId === wf.id}
            onToggle={() => setExpandedId(expandedId === wf.id ? null : wf.id)}
            onRetry={() => handleRetry(wf.id)}
            onCancel={() => handleCancel(wf.id)}
          />
        ))}
      </div>
    </div>
  );
}

function WorkflowCard({
  workflow,
  expanded,
  onToggle,
  onRetry,
  onCancel,
}: {
  workflow: AgentWorkflow;
  expanded: boolean;
  onToggle: () => void;
  onRetry: () => void;
  onCancel: () => void;
}) {
  const stepMap = Object.fromEntries(
    (workflow.steps || []).map((s) => [s.stepName, s])
  );

  return (
    <div className="bg-white rounded-lg border border-indigo-200 overflow-hidden">
      {/* Header row */}
      <button
        className="w-full flex items-center justify-between px-4 py-3 text-left hover:bg-indigo-50"
        onClick={onToggle}
      >
        <div className="flex items-center gap-3">
          <WorkflowStatusBadge status={workflow.status} />
          <span className="text-xs text-gray-500">
            {new Date(workflow.createdAt).toLocaleString()}
          </span>
          {workflow.currentStep && workflow.status === "RUNNING" && (
            <span className="text-xs text-indigo-600 animate-pulse">
              → {AGENT_STEP_LABELS[workflow.currentStep] ?? workflow.currentStep}
            </span>
          )}
        </div>
        <div className="flex items-center gap-2">
          {workflow.status === "FAILED" && (
            <button
              onClick={(e) => { e.stopPropagation(); onRetry(); }}
              className="btn-secondary btn-sm text-xs"
            >
              Retry
            </button>
          )}
          {(workflow.status === "PENDING" || workflow.status === "RUNNING") && (
            <button
              onClick={(e) => { e.stopPropagation(); onCancel(); }}
              className="btn-danger btn-sm text-xs"
            >
              Cancel
            </button>
          )}
          <span className="text-gray-400">{expanded ? "▲" : "▼"}</span>
        </div>
      </button>

      {/* Error message */}
      {workflow.errorMessage && (
        <div className="px-4 py-2 bg-red-50 text-red-700 text-xs border-t border-red-200">
          {workflow.errorMessage}
        </div>
      )}

      {/* Expanded: step timeline */}
      {expanded && (
        <div className="px-4 py-3 border-t border-indigo-100">
          <div className="space-y-2">
            {AGENT_STEPS.map((stepKey) => {
              const step = stepMap[stepKey];
              const status: AgentStepStatus = step?.status ?? "PENDING";
              return (
                <StepRow
                  key={stepKey}
                  stepKey={stepKey}
                  status={status}
                  output={step?.outputPayload}
                  error={step?.errorMessage}
                  completedAt={step?.completedAt}
                />
              );
            })}
          </div>

          {/* Compliance warnings */}
          {workflow.generatedContent?.complianceWarnings?.length ? (
            <div className="mt-3 p-2 bg-red-50 border border-red-200 rounded text-xs text-red-700">
              <strong>Compliance warnings:</strong>{" "}
              {workflow.generatedContent.complianceWarnings.join(", ")}
            </div>
          ) : workflow.status === "COMPLETED" ? (
            <div className="mt-3 p-2 bg-green-50 border border-green-200 rounded text-xs text-green-700">
              Claim-safe — no prohibited terms detected
            </div>
          ) : null}

          {workflow.status === "COMPLETED" && (
            <p className="mt-3 text-xs text-indigo-700">
              Content saved as DRAFT. See the Generated Content section below to review and approve.
            </p>
          )}
        </div>
      )}
    </div>
  );
}

function StepRow({
  stepKey,
  status,
  output,
  error,
  completedAt,
}: {
  stepKey: string;
  status: AgentStepStatus;
  output?: string;
  error?: string;
  completedAt?: string;
}) {
  const [showOutput, setShowOutput] = useState(false);
  const label = AGENT_STEP_LABELS[stepKey] ?? stepKey;

  let parsedOutput: Record<string, unknown> | null = null;
  if (output) {
    try {
      parsedOutput = JSON.parse(output);
    } catch {
      parsedOutput = null;
    }
  }

  return (
    <div className="flex flex-col gap-1">
      <button
        className="flex items-center gap-2 text-sm text-left w-full hover:bg-gray-50 rounded px-1 py-0.5"
        onClick={() => output && setShowOutput(!showOutput)}
        disabled={!output}
      >
        <StepStatusIcon status={status} />
        <span
          className={
            status === "COMPLETED"
              ? "text-gray-800"
              : status === "RUNNING"
              ? "text-zinc-900 font-medium"
              : status === "FAILED"
              ? "text-red-700"
              : "text-gray-400"
          }
        >
          {label}
        </span>
        {completedAt && (
          <span className="text-xs text-gray-400 ml-auto">
            {new Date(completedAt).toLocaleTimeString()}
          </span>
        )}
        {output && (
          <span className="text-xs text-indigo-500 ml-1">{showOutput ? "▲" : "▼"}</span>
        )}
      </button>

      {error && (
        <p className="text-xs text-red-600 ml-6 mt-0.5">{error}</p>
      )}

      {showOutput && parsedOutput && (
        <div className="ml-6 mt-1 text-xs bg-gray-50 border border-gray-200 rounded p-2 space-y-1">
          {Object.entries(parsedOutput).map(([k, v]) => (
            <div key={k}>
              <span className="font-medium text-gray-600">{k}:</span>{" "}
              <span className="text-gray-700 whitespace-pre-wrap">
                {typeof v === "string" ? v : JSON.stringify(v, null, 2)}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────

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

      {/* Agent Workflow Panel (Phase 2) */}
      <AgentWorkflowPanel campaignId={id} onContentCreated={loadContent} />

      {/* Quick Generate (Phase 1) */}
      <div className="card mb-8 border-zinc-200 bg-zinc-50">
        <h2 className="text-lg font-semibold text-zinc-900 mb-4">Quick Generate (Single Step)</h2>
        <div className="flex flex-wrap gap-3 items-end">
          <div>
            <label className="form-label text-zinc-700">Content Type</label>
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
            <label className="form-label text-zinc-700">Additional Context (optional)</label>
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
            <p className="text-gray-400">No content generated yet.</p>
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
                  <span className="text-xs font-medium text-zinc-500 uppercase tracking-wide">Hook</span>
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
                    <span key={i} className="text-xs text-zinc-600 font-medium">
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
