"use client";

import { useEffect, useRef, useState } from "react";
import { calendarApi, campaignApi, exportApi } from "@/lib/api";
import type {
  Campaign,
  ContentCalendar,
  ContentExportRequest,
  ExportJob,
  ExportType,
} from "@/types";
import { CHANNELS, EXPORT_TYPES } from "@/types";

const STATUS_COLORS: Record<string, string> = {
  PENDING:   "bg-gray-100 text-gray-600",
  RUNNING:   "bg-yellow-100 text-yellow-700",
  COMPLETED: "bg-green-100 text-green-700",
  FAILED:    "bg-red-100 text-red-700",
};

export default function ExportsPage() {
  const [jobs, setJobs] = useState<ExportJob[]>([]);
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [calendars, setCalendars] = useState<ContentCalendar[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Form state
  const [exportType, setExportType] = useState<ExportType>("CONTENT_CSV");
  const [calendarId, setCalendarId] = useState("");
  const [campaignId, setCampaignId] = useState("");
  const [channel, setChannel] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [includeDraft, setIncludeDraft] = useState(false);

  // Polling ref for in-flight jobs
  const pollingRef = useRef<Map<string, ReturnType<typeof setInterval>>>(new Map());

  useEffect(() => {
    Promise.all([
      exportApi.list(),
      campaignApi.list(),
      calendarApi.list(),
    ])
      .then(([jobsRes, camRes, calRes]) => {
        setJobs(jobsRes.data);
        setCampaigns(camRes.data);
        setCalendars(calRes.data);
        // Start polling any in-flight jobs
        jobsRes.data
          .filter((j) => j.status === "PENDING" || j.status === "RUNNING")
          .forEach((j) => startPolling(j.id));
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));

    return () => {
      // eslint-disable-next-line react-hooks/exhaustive-deps
      const timers = pollingRef.current;
      timers.forEach((timer) => clearInterval(timer));
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function startPolling(id: string) {
    if (pollingRef.current.has(id)) return;
    const timer = setInterval(async () => {
      try {
        const res = await exportApi.get(id);
        const updated = res.data;
        setJobs((prev) => prev.map((j) => (j.id === id ? updated : j)));
        if (updated.status === "COMPLETED" || updated.status === "FAILED") {
          clearInterval(timer);
          pollingRef.current.delete(id);
        }
      } catch {
        clearInterval(timer);
        pollingRef.current.delete(id);
      }
    }, 2000);
    pollingRef.current.set(id, timer);
  }

  const isCalendarExport = exportType === "CALENDAR_CSV";

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      let job: ExportJob;
      if (isCalendarExport) {
        if (!calendarId) throw new Error("Please select a calendar");
        const res = await exportApi.exportCalendar(calendarId);
        job = res.data;
      } else {
        const req: ContentExportRequest = {
          exportType,
          statuses: includeDraft ? ["APPROVED", "DRAFT"] : ["APPROVED"],
        };
        if (campaignId) req.campaignId = campaignId;
        if (channel) req.channel = channel;
        if (startDate) req.startDate = startDate;
        if (endDate) req.endDate = endDate;
        const res = await exportApi.exportContent(req);
        job = res.data;
      }
      setJobs((prev) => [job, ...prev]);
      startPolling(job.id);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setSubmitting(false);
    }
  }

  function handleDownload(job: ExportJob) {
    const url = exportApi.downloadUrl(job.id);
    const a = document.createElement("a");
    a.href = url;
    a.download = job.fileName ?? "export.csv";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Export & Publishing Prep</h1>
        <p className="text-sm text-gray-500 mt-1">
          Download approved content as CSV for manual publishing on TikTok, Shopee, Lazada, and Facebook.
        </p>
      </div>

      {/* Export Form */}
      <div className="bg-white border border-gray-200 rounded-lg p-5 mb-8">
        <h2 className="font-semibold text-gray-800 mb-4">Create New Export</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Export Type */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Export Type</label>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2">
              {EXPORT_TYPES.map((t) => (
                <label
                  key={t.value}
                  className={`flex items-start gap-2 p-3 border rounded-lg cursor-pointer transition-colors ${
                    exportType === t.value
                      ? "border-blue-500 bg-blue-50"
                      : "border-gray-200 hover:border-gray-300"
                  }`}
                >
                  <input
                    type="radio"
                    name="exportType"
                    value={t.value}
                    checked={exportType === t.value}
                    onChange={() => setExportType(t.value)}
                    className="mt-0.5"
                  />
                  <div>
                    <div className="text-sm font-medium text-gray-800">{t.label}</div>
                    <div className="text-xs text-gray-500">{t.description}</div>
                  </div>
                </label>
              ))}
            </div>
          </div>

          {/* Calendar selector (calendar exports only) */}
          {isCalendarExport && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Calendar</label>
              <select
                value={calendarId}
                onChange={(e) => setCalendarId(e.target.value)}
                className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">Select a calendar…</option>
                {calendars.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} ({c.periodStart} — {c.periodEnd})
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Content filters (non-calendar exports) */}
          {!isCalendarExport && (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Campaign (optional)</label>
                <select
                  value={campaignId}
                  onChange={(e) => setCampaignId(e.target.value)}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">All campaigns</option>
                  {campaigns.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Channel (optional)</label>
                <select
                  value={channel}
                  onChange={(e) => setChannel(e.target.value)}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">All channels</option>
                  {CHANNELS.map((ch) => (
                    <option key={ch.value} value={ch.value}>{ch.label}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Start Date (optional)</label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">End Date (optional)</label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
          )}

          {/* Include drafts toggle (non-calendar) */}
          {!isCalendarExport && (
            <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
              <input
                type="checkbox"
                checked={includeDraft}
                onChange={(e) => setIncludeDraft(e.target.checked)}
                className="rounded"
              />
              Also include draft content (by default only approved content is exported)
            </label>
          )}

          {error && <p className="text-sm text-red-600">{error}</p>}

          <button
            type="submit"
            disabled={submitting}
            className="btn-primary"
          >
            {submitting ? "Starting export…" : "Export Now"}
          </button>
        </form>
      </div>

      {/* Export History */}
      <div>
        <h2 className="font-semibold text-gray-800 mb-3">Export History</h2>

        {loading && <p className="text-gray-500 text-sm">Loading…</p>}

        {!loading && jobs.length === 0 && (
          <div className="text-center py-12 border-2 border-dashed border-gray-200 rounded-lg">
            <p className="text-gray-400 text-sm">No exports yet. Create one above.</p>
          </div>
        )}

        {jobs.length > 0 && (
          <div className="space-y-3">
            {jobs.map((job) => (
              <div key={job.id} className="bg-white border border-gray-200 rounded-lg p-4 flex items-center gap-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-medium text-gray-800 text-sm">
                      {EXPORT_TYPES.find((t) => t.value === job.exportType)?.label ?? job.exportType}
                    </span>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLORS[job.status]}`}>
                      {job.status}
                    </span>
                  </div>

                  <div className="flex flex-wrap gap-x-4 gap-y-0.5 text-xs text-gray-500">
                    {job.channel && <span>Channel: {job.channel}</span>}
                    {job.startDate && <span>From: {job.startDate}</span>}
                    {job.endDate && <span>To: {job.endDate}</span>}
                    {job.rowCount != null && <span>{job.rowCount} rows</span>}
                    {job.fileName && <span className="font-mono">{job.fileName}</span>}
                  </div>

                  {job.errorMessage && (
                    <p className="text-xs text-red-600 mt-1">{job.errorMessage}</p>
                  )}

                  <p className="text-xs text-gray-400 mt-1">
                    {new Date(job.createdAt).toLocaleString()}
                  </p>
                </div>

                <div className="shrink-0">
                  {job.status === "RUNNING" || job.status === "PENDING" ? (
                    <span className="text-xs text-yellow-600 animate-pulse">Processing…</span>
                  ) : job.status === "COMPLETED" ? (
                    <button
                      onClick={() => handleDownload(job)}
                      className="btn-success btn-sm"
                    >
                      Download CSV
                    </button>
                  ) : null}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
