"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { performanceApi } from "@/lib/api";
import type {
  ContentInsight,
  ContentPerformance,
  ContentPerformanceRequest,
  DashboardStats,
  InsightType,
  PerformanceSummary,
  TopContent,
} from "@/types";
import { CHANNELS } from "@/types";

// ─── Formatting helpers ──────────────────────────────────────────────────────

function pct(v?: number | null) {
  if (v == null) return "—";
  return (Number(v) * 100).toFixed(2) + "%";
}
function currency(v?: number | null) {
  if (v == null) return "—";
  return "฿" + Number(v).toLocaleString("th-TH", { minimumFractionDigits: 2 });
}
function num(v?: number | null) {
  if (v == null) return "—";
  return Number(v).toLocaleString();
}
function roasFmt(v?: number | null) {
  if (v == null) return "—";
  return Number(v).toFixed(2) + "x";
}

const INSIGHT_LABELS: Record<InsightType, string> = {
  WINNING_HOOK: "Winning Hook",
  WINNING_ANGLE: "Winning Angle",
  LOW_PERFORMING_ANGLE: "Low Angle",
  STRONG_CTA: "Strong CTA",
  WEAK_CTA: "Weak CTA",
  AUDIENCE_SIGNAL: "Audience Signal",
  CHANNEL_SIGNAL: "Channel Signal",
};

const INSIGHT_COLORS: Record<InsightType, string> = {
  WINNING_HOOK: "#16a34a",
  WINNING_ANGLE: "#2563eb",
  LOW_PERFORMING_ANGLE: "#dc2626",
  STRONG_CTA: "#7c3aed",
  WEAK_CTA: "#f97316",
  AUDIENCE_SIGNAL: "#0891b2",
  CHANNEL_SIGNAL: "#d97706",
};

// ─── Stat Card ────────────────────────────────────────────────────────────────

function StatCard({
  label,
  value,
}: {
  label: string;
  value: string | number;
}) {
  return (
    <div
      style={{
        background: "white",
        border: "1px solid #e5e7eb",
        borderRadius: 8,
        padding: "16px 20px",
        minWidth: 140,
      }}
    >
      <div style={{ fontSize: 12, color: "#6b7280", marginBottom: 4 }}>{label}</div>
      <div style={{ fontSize: 22, fontWeight: 700, color: "#111827" }}>{value}</div>
    </div>
  );
}

// ─── Add/Edit Modal ───────────────────────────────────────────────────────────

const EMPTY_FORM: ContentPerformanceRequest = {
  generatedContentId: "",
  channel: "tiktok",
  impressions: 0,
  views: 0,
  clicks: 0,
  likes: 0,
  comments: 0,
  shares: 0,
  orders: 0,
  revenue: 0,
  cost: 0,
  notes: "",
};

function PerformanceModal({
  initial,
  onSave,
  onClose,
}: {
  initial: ContentPerformanceRequest;
  onSave: (data: ContentPerformanceRequest) => Promise<void>;
  onClose: () => void;
}) {
  const [form, setForm] = useState<ContentPerformanceRequest>(initial);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const field = (
    key: keyof ContentPerformanceRequest,
    label: string,
    type: "text" | "number" = "number"
  ) => (
    <div style={{ marginBottom: 10 }}>
      <label style={{ display: "block", fontSize: 12, marginBottom: 3, color: "#374151" }}>
        {label}
      </label>
      <input
        type={type}
        value={form[key] as string | number}
        onChange={(e) =>
          setForm((f) => ({
            ...f,
            [key]: type === "number" ? Number(e.target.value) : e.target.value,
          }))
        }
        style={{
          width: "100%",
          padding: "6px 10px",
          border: "1px solid #d1d5db",
          borderRadius: 6,
          fontSize: 14,
          boxSizing: "border-box",
        }}
      />
    </div>
  );

  const handleSubmit = async () => {
    if (!form.generatedContentId.trim()) {
      setError("Content ID is required");
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await onSave(form);
      onClose();
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to save");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,0.4)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 50,
      }}
    >
      <div
        style={{
          background: "white",
          borderRadius: 10,
          padding: 24,
          width: 480,
          maxHeight: "90vh",
          overflowY: "auto",
        }}
      >
        <h3 style={{ margin: "0 0 16px", fontSize: 16, fontWeight: 700 }}>
          Performance Record
        </h3>
        {error && (
          <div
            style={{
              background: "#fef2f2",
              border: "1px solid #fca5a5",
              color: "#b91c1c",
              padding: "8px 12px",
              borderRadius: 6,
              marginBottom: 12,
              fontSize: 13,
            }}
          >
            {error}
          </div>
        )}
        {field("generatedContentId", "Content ID (UUID)", "text")}
        <div style={{ marginBottom: 10 }}>
          <label style={{ display: "block", fontSize: 12, marginBottom: 3, color: "#374151" }}>
            Channel
          </label>
          <select
            value={form.channel}
            onChange={(e) => setForm((f) => ({ ...f, channel: e.target.value }))}
            style={{
              width: "100%",
              padding: "6px 10px",
              border: "1px solid #d1d5db",
              borderRadius: 6,
              fontSize: 14,
            }}
          >
            {CHANNELS.map((ch) => (
              <option key={ch.value} value={ch.value}>
                {ch.label}
              </option>
            ))}
          </select>
        </div>
        <div style={{ marginBottom: 10 }}>
          <label style={{ display: "block", fontSize: 12, marginBottom: 3, color: "#374151" }}>
            Published At
          </label>
          <input
            type="datetime-local"
            value={form.publishedAt ?? ""}
            onChange={(e) => setForm((f) => ({ ...f, publishedAt: e.target.value || undefined }))}
            style={{
              width: "100%",
              padding: "6px 10px",
              border: "1px solid #d1d5db",
              borderRadius: 6,
              fontSize: 14,
              boxSizing: "border-box",
            }}
          />
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8 }}>
          {field("impressions", "Impressions")}
          {field("views", "Views")}
          {field("clicks", "Clicks")}
          {field("likes", "Likes")}
          {field("comments", "Comments")}
          {field("shares", "Shares")}
          {field("orders", "Orders")}
          {field("revenue", "Revenue (฿)")}
          {field("cost", "Cost (฿)")}
        </div>
        {field("notes", "Notes", "text")}
        <div className="flex gap-2 justify-end mt-2">
          <button onClick={onClose} className="btn-secondary">Cancel</button>
          <button onClick={handleSubmit} disabled={saving} className="btn-primary">
            {saving ? "Saving…" : "Save"}
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── Main Page ─────────────────────────────────────────────────────────────────

export default function PerformancePage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [records, setRecords] = useState<ContentPerformance[]>([]);
  const [top, setTop] = useState<TopContent | null>(null);
  const [insights, setInsights] = useState<ContentInsight[]>([]);
  const [summaries, setSummaries] = useState<PerformanceSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [editRecord, setEditRecord] = useState<ContentPerformance | null>(null);
  const [analyzing, setAnalyzing] = useState(false);
  const [analyzeMsg, setAnalyzeMsg] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"records" | "top" | "insights" | "summaries">(
    "records"
  );

  const loadAll = async () => {
    try {
      const [statsRes, recordsRes, topRes, insightsRes, summariesRes] = await Promise.all([
        performanceApi.getDashboard(),
        performanceApi.list(),
        performanceApi.getTop(5),
        performanceApi.getInsights(),
        performanceApi.getSummaries(),
      ]);
      setStats(statsRes.data);
      setRecords(recordsRes.data);
      setTop(topRes.data);
      setInsights(insightsRes.data);
      setSummaries(summariesRes.data);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to load performance data");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const handleCreate = async (data: ContentPerformanceRequest) => {
    await performanceApi.create(data);
    await loadAll();
  };

  const handleUpdate = async (data: ContentPerformanceRequest) => {
    if (!editRecord) return;
    await performanceApi.update(editRecord.id, data);
    await loadAll();
    setEditRecord(null);
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this performance record?")) return;
    await performanceApi.delete(id);
    setRecords((prev) => prev.filter((r) => r.id !== id));
  };

  const handleAnalyze = async () => {
    setAnalyzing(true);
    setAnalyzeMsg(null);
    try {
      const res = await performanceApi.analyze({ topN: 50 });
      setAnalyzeMsg(res.data.status === "dispatched"
        ? `Analysis dispatched — ${res.data.recordCount} records sent to analyst.`
        : "No data to analyze.");
    } catch (e: unknown) {
      setAnalyzeMsg(e instanceof Error ? e.message : "Analysis failed");
    } finally {
      setAnalyzing(false);
    }
  };

  if (loading) return <div style={{ padding: 32 }}>Loading performance data…</div>;
  if (error) return <div style={{ padding: 32, color: "#dc2626" }}>Error: {error}</div>;

  const tabStyle = (tab: typeof activeTab) => ({
    padding: "8px 16px",
    border: "none",
    borderBottom: activeTab === tab ? "2px solid #18181b" : "2px solid transparent",
    background: "none",
    cursor: "pointer",
    fontWeight: activeTab === tab ? 700 : 400,
    color: activeTab === tab ? "#18181b" : "#6b7280",
    fontSize: 14,
  });

  return (
    <div style={{ padding: 32, maxWidth: 1100, margin: "0 auto" }}>
      {/* Header */}
      <div
        style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}
      >
        <div>
          <h1 style={{ margin: 0, fontSize: 22, fontWeight: 700 }}>Performance Learning</h1>
          <p style={{ margin: "4px 0 0", color: "#6b7280", fontSize: 14 }}>
            Track content performance and generate AI insights.
          </p>
        </div>
        <div className="flex gap-2">
          <Link href="/performance/import" className="btn-secondary">
            Import CSV
          </Link>
          <button
            onClick={handleAnalyze}
            disabled={analyzing || records.length === 0}
            className="btn-primary bg-violet-600 hover:bg-violet-700 focus:ring-violet-600"
          >
            {analyzing ? "Analyzing…" : "Generate Insights"}
          </button>
          <button onClick={() => setShowModal(true)} className="btn-primary">
            + Add Record
          </button>
        </div>
      </div>

      {analyzeMsg && (
        <div
          style={{
            background: "#f0fdf4",
            border: "1px solid #86efac",
            color: "#166534",
            padding: "10px 14px",
            borderRadius: 6,
            marginBottom: 16,
            fontSize: 13,
          }}
        >
          {analyzeMsg}
        </div>
      )}

      {/* Dashboard Stats */}
      {stats && (
        <div
          style={{ display: "flex", gap: 12, flexWrap: "wrap", marginBottom: 24 }}
        >
          <StatCard label="Total Records" value={num(stats.totalRecords)} />
          <StatCard label="Total Impressions" value={num(stats.totalImpressions)} />
          <StatCard label="Total Clicks" value={num(stats.totalClicks)} />
          <StatCard label="Total Orders" value={num(stats.totalOrders)} />
          <StatCard label="Total Revenue" value={currency(stats.totalRevenue)} />
          <StatCard label="Total Cost" value={currency(stats.totalCost)} />
          <StatCard label="Avg Engagement" value={pct(stats.averageEngagementRate)} />
          <StatCard label="Avg ROAS" value={roasFmt(stats.averageRoas)} />
        </div>
      )}

      {/* Tabs */}
      <div style={{ borderBottom: "1px solid #e5e7eb", marginBottom: 20 }}>
        <button style={tabStyle("records")} onClick={() => setActiveTab("records")}>
          All Records ({records.length})
        </button>
        <button style={tabStyle("top")} onClick={() => setActiveTab("top")}>
          Top Content
        </button>
        <button style={tabStyle("insights")} onClick={() => setActiveTab("insights")}>
          Insights ({insights.length})
        </button>
        <button style={tabStyle("summaries")} onClick={() => setActiveTab("summaries")}>
          Summaries ({summaries.length})
        </button>
      </div>

      {/* ── Records Tab ──────────────────────────────────────────────────────── */}
      {activeTab === "records" && (
        <div style={{ overflowX: "auto" }}>
          {records.length === 0 ? (
            <div style={{ textAlign: "center", padding: 40, color: "#9ca3af" }}>
              No performance records yet. Add one manually or import a CSV.
            </div>
          ) : (
            <table
              style={{
                width: "100%",
                borderCollapse: "collapse",
                fontSize: 13,
                background: "white",
              }}
            >
              <thead>
                <tr style={{ background: "#f9fafb" }}>
                  {[
                    "Channel",
                    "Impressions",
                    "Views",
                    "Clicks",
                    "Orders",
                    "Revenue",
                    "Cost",
                    "Eng%",
                    "Conv%",
                    "ROAS",
                    "",
                  ].map((h) => (
                    <th
                      key={h}
                      style={{
                        padding: "10px 12px",
                        textAlign: "left",
                        borderBottom: "1px solid #e5e7eb",
                        fontWeight: 600,
                        color: "#374151",
                      }}
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {records.map((r) => (
                  <tr
                    key={r.id}
                    style={{ borderBottom: "1px solid #f3f4f6" }}
                    onMouseEnter={(e) =>
                      (e.currentTarget.style.background = "#f9fafb")
                    }
                    onMouseLeave={(e) =>
                      (e.currentTarget.style.background = "white")
                    }
                  >
                    <td style={{ padding: "10px 12px" }}>
                      <span
                        style={{
                          background: "#f4f4f5",
                          color: "#18181b",
                          padding: "2px 8px",
                          borderRadius: 4,
                          fontSize: 11,
                          fontWeight: 600,
                          textTransform: "uppercase",
                        }}
                      >
                        {r.channel}
                      </span>
                    </td>
                    <td style={{ padding: "10px 12px" }}>{num(r.impressions)}</td>
                    <td style={{ padding: "10px 12px" }}>{num(r.views)}</td>
                    <td style={{ padding: "10px 12px" }}>{num(r.clicks)}</td>
                    <td style={{ padding: "10px 12px" }}>{num(r.orders)}</td>
                    <td style={{ padding: "10px 12px" }}>{currency(r.revenue)}</td>
                    <td style={{ padding: "10px 12px" }}>{currency(r.cost)}</td>
                    <td style={{ padding: "10px 12px" }}>{pct(r.engagementRate)}</td>
                    <td style={{ padding: "10px 12px" }}>{pct(r.conversionRate)}</td>
                    <td style={{ padding: "10px 12px" }}>{roasFmt(r.roas)}</td>
                    <td style={{ padding: "10px 12px" }}>
                      <div className="flex gap-1.5">
                        <button
                          onClick={() => setEditRecord(r)}
                          className="btn-secondary btn-sm"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleDelete(r.id)}
                          className="btn-danger btn-sm"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* ── Top Content Tab ───────────────────────────────────────────────────── */}
      {activeTab === "top" && top && (
        <div>
          <h3 style={{ margin: "0 0 12px", fontSize: 15, fontWeight: 700 }}>
            Top by Revenue
          </h3>
          <TopTable records={top.byRevenue} metric="revenue" fmt={currency} />

          <h3 style={{ margin: "24px 0 12px", fontSize: 15, fontWeight: 700 }}>
            Top by Engagement Rate
          </h3>
          <TopTable records={top.byEngagement} metric="engagementRate" fmt={pct} />

          <h3 style={{ margin: "24px 0 12px", fontSize: 15, fontWeight: 700 }}>
            Top by ROAS
          </h3>
          <TopTable records={top.byRoas} metric="roas" fmt={roasFmt} />

          <h3 style={{ margin: "24px 0 12px", fontSize: 15, fontWeight: 700 }}>
            Channel Breakdown
          </h3>
          <ChannelTable rows={top.byChannel} />
        </div>
      )}

      {/* ── Insights Tab ─────────────────────────────────────────────────────── */}
      {activeTab === "insights" && (
        <div>
          {insights.length === 0 ? (
            <div style={{ textAlign: "center", padding: 40, color: "#9ca3af" }}>
              No insights yet. Add performance data and click{" "}
              <strong>Generate Insights</strong>.
            </div>
          ) : (
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fill, minmax(320px, 1fr))",
                gap: 12,
              }}
            >
              {insights.map((ins) => (
                <InsightCard key={ins.id} insight={ins} />
              ))}
            </div>
          )}
        </div>
      )}

      {/* ── Summaries Tab ────────────────────────────────────────────────────── */}
      {activeTab === "summaries" && (
        <div>
          {summaries.length === 0 ? (
            <div style={{ textAlign: "center", padding: 40, color: "#9ca3af" }}>
              No summaries yet. Generate insights first.
            </div>
          ) : (
            summaries.map((s) => <SummaryCard key={s.id} summary={s} />)
          )}
        </div>
      )}

      {/* Add Modal */}
      {showModal && (
        <PerformanceModal
          initial={EMPTY_FORM}
          onSave={handleCreate}
          onClose={() => setShowModal(false)}
        />
      )}

      {/* Edit Modal */}
      {editRecord && (
        <PerformanceModal
          initial={{
            generatedContentId: editRecord.generatedContentId,
            channel: editRecord.channel,
            publishedAt: editRecord.publishedAt,
            impressions: editRecord.impressions,
            views: editRecord.views,
            clicks: editRecord.clicks,
            likes: editRecord.likes,
            comments: editRecord.comments,
            shares: editRecord.shares,
            orders: editRecord.orders,
            revenue: editRecord.revenue,
            cost: editRecord.cost,
            notes: editRecord.notes,
          }}
          onSave={handleUpdate}
          onClose={() => setEditRecord(null)}
        />
      )}
    </div>
  );
}

// ─── Sub-components ────────────────────────────────────────────────────────────

function TopTable({
  records,
  metric,
  fmt,
}: {
  records: ContentPerformance[];
  metric: keyof ContentPerformance;
  fmt: (v?: number | null) => string;
}) {
  if (records.length === 0)
    return <div style={{ color: "#9ca3af", fontSize: 13 }}>No data.</div>;
  return (
    <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13, background: "white" }}>
      <thead>
        <tr style={{ background: "#f9fafb" }}>
          {["#", "Channel", "Content ID", metric].map((h) => (
            <th
              key={h}
              style={{
                padding: "8px 12px",
                textAlign: "left",
                borderBottom: "1px solid #e5e7eb",
                fontWeight: 600,
                color: "#374151",
              }}
            >
              {h}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {records.map((r, i) => (
          <tr key={r.id} style={{ borderBottom: "1px solid #f3f4f6" }}>
            <td style={{ padding: "8px 12px", color: "#6b7280" }}>{i + 1}</td>
            <td style={{ padding: "8px 12px" }}>
              <span
                style={{
                  background: "#f4f4f5",
                  color: "#18181b",
                  padding: "2px 8px",
                  borderRadius: 4,
                  fontSize: 11,
                  fontWeight: 600,
                  textTransform: "uppercase",
                }}
              >
                {r.channel}
              </span>
            </td>
            <td style={{ padding: "8px 12px", fontFamily: "monospace", fontSize: 11, color: "#6b7280" }}>
              {r.generatedContentId.slice(0, 8)}…
            </td>
            <td style={{ padding: "8px 12px", fontWeight: 700 }}>
              {fmt(r[metric] as number | null | undefined)}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function ChannelTable({ rows }: { rows: { channel: string; count: number; impressions: number; clicks: number; revenue: number }[] }) {
  if (rows.length === 0)
    return <div style={{ color: "#9ca3af", fontSize: 13 }}>No channel data.</div>;
  return (
    <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13, background: "white" }}>
      <thead>
        <tr style={{ background: "#f9fafb" }}>
          {["Channel", "Posts", "Impressions", "Clicks", "Revenue"].map((h) => (
            <th
              key={h}
              style={{
                padding: "8px 12px",
                textAlign: "left",
                borderBottom: "1px solid #e5e7eb",
                fontWeight: 600,
                color: "#374151",
              }}
            >
              {h}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.map((r) => (
          <tr key={r.channel} style={{ borderBottom: "1px solid #f3f4f6" }}>
            <td style={{ padding: "8px 12px", fontWeight: 600 }}>{r.channel}</td>
            <td style={{ padding: "8px 12px" }}>{r.count}</td>
            <td style={{ padding: "8px 12px" }}>{Number(r.impressions).toLocaleString()}</td>
            <td style={{ padding: "8px 12px" }}>{Number(r.clicks).toLocaleString()}</td>
            <td style={{ padding: "8px 12px" }}>
              ฿{Number(r.revenue).toLocaleString("th-TH", { minimumFractionDigits: 2 })}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function InsightCard({ insight }: { insight: ContentInsight }) {
  const color = INSIGHT_COLORS[insight.insightType] || "#374151";
  const label = INSIGHT_LABELS[insight.insightType] || insight.insightType;
  return (
    <div
      style={{
        background: "white",
        border: "1px solid #e5e7eb",
        borderLeft: `4px solid ${color}`,
        borderRadius: 8,
        padding: 16,
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 8,
        }}
      >
        <span
          style={{
            fontSize: 11,
            fontWeight: 700,
            color,
            textTransform: "uppercase",
            letterSpacing: "0.04em",
          }}
        >
          {label}
        </span>
        <span style={{ fontSize: 11, color: "#6b7280" }}>
          {(Number(insight.confidenceScore) * 100).toFixed(0)}% confidence
        </span>
      </div>
      <p style={{ margin: 0, fontSize: 13, color: "#111827", lineHeight: 1.5 }}>
        {insight.insightText}
      </p>
    </div>
  );
}

function SummaryCard({ summary }: { summary: PerformanceSummary }) {
  return (
    <div
      style={{
        background: "white",
        border: "1px solid #e5e7eb",
        borderRadius: 8,
        padding: 20,
        marginBottom: 16,
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 12,
        }}
      >
        <div style={{ fontWeight: 700, fontSize: 15 }}>
          {summary.periodStart} → {summary.periodEnd}
          {summary.channel && (
            <span
              style={{
                marginLeft: 8,
                background: "#f4f4f5",
                color: "#18181b",
                padding: "2px 8px",
                borderRadius: 4,
                fontSize: 11,
                fontWeight: 600,
                textTransform: "uppercase",
              }}
            >
              {summary.channel}
            </span>
          )}
        </div>
      </div>
      <p style={{ margin: "0 0 16px", fontSize: 13, color: "#374151", lineHeight: 1.6 }}>
        {summary.summaryText}
      </p>
      {summary.recommendedHooks.length > 0 && (
        <RecommendationList label="Recommended Hooks" items={summary.recommendedHooks} color="#16a34a" />
      )}
      {summary.recommendedAngles.length > 0 && (
        <RecommendationList label="Recommended Angles" items={summary.recommendedAngles} color="#18181b" />
      )}
      {summary.recommendedCTAs.length > 0 && (
        <RecommendationList label="Recommended CTAs" items={summary.recommendedCTAs} color="#7c3aed" />
      )}
      {summary.avoidPatterns.length > 0 && (
        <RecommendationList label="Patterns to Avoid" items={summary.avoidPatterns} color="#dc2626" />
      )}
    </div>
  );
}

function RecommendationList({
  label,
  items,
  color,
}: {
  label: string;
  items: string[];
  color: string;
}) {
  return (
    <div style={{ marginBottom: 12 }}>
      <div
        style={{ fontSize: 12, fontWeight: 700, color, marginBottom: 4, textTransform: "uppercase" }}
      >
        {label}
      </div>
      <ul style={{ margin: 0, padding: "0 0 0 16px" }}>
        {items.map((item, i) => (
          <li key={i} style={{ fontSize: 13, color: "#374151", marginBottom: 3 }}>
            {item}
          </li>
        ))}
      </ul>
    </div>
  );
}
