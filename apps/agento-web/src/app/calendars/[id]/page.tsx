"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { calendarApi, contentApi } from "@/lib/api";
import type {
  BatchGenerationJob,
  CalendarItem,
  CalendarItemUpdateRequest,
  ContentCalendar,
} from "@/types";

const STATUS_COLORS: Record<string, string> = {
  PLANNED: "bg-gray-100 text-gray-600",
  GENERATING: "bg-yellow-100 text-yellow-700",
  COMPLETED: "bg-green-100 text-green-700",
  FAILED: "bg-red-100 text-red-700",
};

const CALENDAR_STATUS_COLORS: Record<string, string> = {
  DRAFT: "bg-gray-100 text-gray-700",
  GENERATING: "bg-yellow-100 text-yellow-700",
  READY_FOR_REVIEW: "bg-blue-100 text-blue-700",
  APPROVED: "bg-green-100 text-green-700",
};

const CHANNELS = ["tiktok", "shopee", "lazada", "facebook", "reseller"];
const CONTENT_TYPES = [
  "TIKTOK_CAPTION", "TIKTOK_SCRIPT", "SHOPEE_DESCRIPTION",
  "LAZADA_DESCRIPTION", "FACEBOOK_POST", "RESELLER_POST",
];

export default function CalendarDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const [calendar, setCalendar] = useState<ContentCalendar | null>(null);
  const [items, setItems] = useState<CalendarItem[]>([]);
  const [batchJob, setBatchJob] = useState<BatchGenerationJob | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [planning, setPlanning] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [editingItem, setEditingItem] = useState<string | null>(null);
  const [editForm, setEditForm] = useState<CalendarItemUpdateRequest>({});
  const pollRef = useRef<NodeJS.Timeout | null>(null);

  const loadAll = useCallback(async () => {
    const [calRes, itemsRes] = await Promise.all([
      calendarApi.get(id),
      calendarApi.listItems(id),
    ]);
    setCalendar(calRes.data);
    setItems(itemsRes.data);
    return calRes.data;
  }, [id]);

  const loadBatchJob = useCallback(async () => {
    try {
      const res = await calendarApi.getBatchJob(id);
      setBatchJob(res.data);
      return res.data;
    } catch {
      return null;
    }
  }, [id]);

  useEffect(() => {
    loadAll()
      .then((cal) => {
        if (cal.status === "GENERATING") {
          loadBatchJob();
          startPolling();
        }
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));

    return () => stopPolling();
  }, [id]);

  const startPolling = () => {
    if (pollRef.current) return;
    pollRef.current = setInterval(async () => {
      const [job] = await Promise.all([loadBatchJob(), loadAll()]);
      if (job && (job.status === "COMPLETED" || job.status === "FAILED")) {
        stopPolling();
      }
    }, 4000);
  };

  const stopPolling = () => {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
  };

  const handlePlan = async () => {
    if (!confirm("This will replace all PLANNED items with AI suggestions. Continue?")) return;
    setPlanning(true);
    setError(null);
    try {
      const res = await calendarApi.plan(id);
      setItems(res.data);
      await loadAll();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setPlanning(false);
    }
  };

  const handleGenerate = async () => {
    if (!confirm("Start batch generation for all PLANNED items?")) return;
    setGenerating(true);
    setError(null);
    try {
      const res = await calendarApi.generate(id);
      setBatchJob(res.data);
      await loadAll();
      startPolling();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setGenerating(false);
    }
  };

  const handleDeleteItem = async (itemId: string) => {
    if (!confirm("Delete this item?")) return;
    try {
      await calendarApi.deleteItem(id, itemId);
      setItems((prev) => prev.filter((i) => i.id !== itemId));
    } catch (e: any) {
      alert(e.message);
    }
  };

  const handleEditItem = (item: CalendarItem) => {
    setEditingItem(item.id);
    setEditForm({
      plannedDate: item.plannedDate,
      channel: item.channel,
      contentType: item.contentType,
      contentAngle: item.contentAngle,
      targetAudience: item.targetAudience,
      hookDirection: item.hookDirection,
      ctaDirection: item.ctaDirection,
    });
  };

  const handleSaveItem = async (itemId: string) => {
    try {
      const res = await calendarApi.updateItem(id, itemId, editForm);
      setItems((prev) => prev.map((i) => (i.id === itemId ? res.data : i)));
      setEditingItem(null);
    } catch (e: any) {
      alert(e.message);
    }
  };

  const handleApproveContent = async (contentId: string) => {
    try {
      await contentApi.approve(contentId);
      await loadAll();
    } catch (e: any) {
      alert(e.message);
    }
  };

  const handleRejectContent = async (contentId: string) => {
    try {
      await contentApi.reject(contentId);
      await loadAll();
    } catch (e: any) {
      alert(e.message);
    }
  };

  // Group items by week for the calendar view
  const itemsByDate = items.reduce((acc, item) => {
    const date = item.plannedDate;
    if (!acc[date]) acc[date] = [];
    acc[date].push(item);
    return acc;
  }, {} as Record<string, CalendarItem[]>);

  const plannedCount = items.filter((i) => i.status === "PLANNED").length;
  const completedCount = items.filter((i) => i.status === "COMPLETED").length;
  const failedCount = items.filter((i) => i.status === "FAILED").length;

  if (loading) return <div className="p-6 text-gray-500">Loading calendar...</div>;
  if (error && !calendar) return <div className="p-6 text-red-600">Error: {error}</div>;
  if (!calendar) return <div className="p-6 text-gray-500">Calendar not found</div>;

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-start justify-between mb-6">
        <div>
          <div className="flex items-center gap-3 mb-1">
            <h1 className="text-2xl font-bold text-gray-900">{calendar.name}</h1>
            <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${CALENDAR_STATUS_COLORS[calendar.status] ?? ""}`}>
              {calendar.status.replace("_", " ")}
            </span>
          </div>
          <p className="text-sm text-gray-500">
            {calendar.periodStart} — {calendar.periodEnd}
          </p>
          {calendar.objective && (
            <p className="text-sm text-gray-600 mt-1">{calendar.objective}</p>
          )}
          <div className="flex gap-4 mt-2 text-xs text-gray-500">
            <span>{items.length} total</span>
            <span className="text-green-600">{completedCount} completed</span>
            <span className="text-gray-500">{plannedCount} planned</span>
            {failedCount > 0 && <span className="text-red-500">{failedCount} failed</span>}
          </div>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={handlePlan}
            disabled={planning}
            className="px-3 py-2 border border-blue-600 text-blue-600 text-sm rounded-md hover:bg-blue-50 disabled:opacity-50"
          >
            {planning ? "Planning..." : "AI Plan"}
          </button>
          <button
            onClick={handleGenerate}
            disabled={generating || plannedCount === 0 || calendar.status === "GENERATING"}
            className="px-3 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 disabled:opacity-50"
          >
            {generating ? "Starting..." : `Generate All (${plannedCount})`}
          </button>
        </div>
      </div>

      {error && <p className="mb-4 text-red-600 text-sm">{error}</p>}

      {/* Batch Progress */}
      {batchJob && (batchJob.status === "RUNNING" || batchJob.status === "COMPLETED") && (
        <div className="mb-6 bg-white border border-gray-200 rounded-lg p-4">
          <div className="flex items-center justify-between mb-2">
            <span className="text-sm font-medium text-gray-700">
              Batch Generation{" "}
              <span className={batchJob.status === "COMPLETED" ? "text-green-600" : "text-yellow-600"}>
                {batchJob.status}
              </span>
            </span>
            <span className="text-xs text-gray-500">
              {batchJob.completedItems + batchJob.failedItems} / {batchJob.totalItems}
            </span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div
              className={`h-2 rounded-full transition-all ${batchJob.status === "COMPLETED" ? "bg-green-500" : "bg-blue-500"}`}
              style={{
                width: batchJob.totalItems > 0
                  ? `${Math.round(((batchJob.completedItems + batchJob.failedItems) / batchJob.totalItems) * 100)}%`
                  : "0%",
              }}
            />
          </div>
          <div className="flex gap-4 mt-1 text-xs text-gray-500">
            <span className="text-green-600">{batchJob.completedItems} done</span>
            {batchJob.failedItems > 0 && (
              <span className="text-red-500">{batchJob.failedItems} failed</span>
            )}
          </div>
        </div>
      )}

      {/* Add Item */}
      <div className="mb-4 flex justify-end">
        <AddItemForm calendarId={id} onAdded={(item) => setItems((prev) => [...prev, item])} />
      </div>

      {/* Calendar Items Table */}
      {items.length === 0 ? (
        <div className="text-center py-12 border-2 border-dashed border-gray-200 rounded-lg">
          <p className="text-gray-400 mb-2">No calendar items yet</p>
          <p className="text-sm text-gray-400">
            Click <strong>AI Plan</strong> to generate suggestions, or add items manually
          </p>
        </div>
      ) : (
        <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase">Date</th>
                <th className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase">Channel</th>
                <th className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase">Type</th>
                <th className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase">Angle</th>
                <th className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase">Status</th>
                <th className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {items
                .sort((a, b) => a.plannedDate.localeCompare(b.plannedDate))
                .map((item) => (
                  <ItemRow
                    key={item.id}
                    item={item}
                    isEditing={editingItem === item.id}
                    editForm={editForm}
                    onEdit={() => handleEditItem(item)}
                    onSave={() => handleSaveItem(item.id)}
                    onCancel={() => setEditingItem(null)}
                    onDelete={() => handleDeleteItem(item.id)}
                    onEditFormChange={(patch) => setEditForm((prev) => ({ ...prev, ...patch }))}
                    onApprove={item.generatedContentId ? () => handleApproveContent(item.generatedContentId!) : undefined}
                    onReject={item.generatedContentId ? () => handleRejectContent(item.generatedContentId!) : undefined}
                  />
                ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

// ── Sub-components ───────────────────────────────────────────────────────────

function AddItemForm({
  calendarId,
  onAdded,
}: {
  calendarId: string;
  onAdded: (item: CalendarItem) => void;
}) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ plannedDate: "", channel: "tiktok", contentType: "", contentAngle: "" });
  const [saving, setSaving] = useState(false);

  if (!open) {
    return (
      <button onClick={() => setOpen(true)} className="text-sm text-blue-600 hover:underline">
        + Add Item
      </button>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const res = await calendarApi.addItem(calendarId, {
        plannedDate: form.plannedDate,
        channel: form.channel,
        contentType: form.contentType || undefined,
        contentAngle: form.contentAngle || undefined,
      });
      onAdded(res.data);
      setOpen(false);
      setForm({ plannedDate: "", channel: "tiktok", contentType: "", contentAngle: "" });
    } catch (e: any) {
      alert(e.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex items-end gap-2 flex-wrap">
      <div>
        <label className="block text-xs text-gray-500 mb-1">Date</label>
        <input
          type="date"
          required
          value={form.plannedDate}
          onChange={(e) => setForm((p) => ({ ...p, plannedDate: e.target.value }))}
          className="border border-gray-300 rounded px-2 py-1.5 text-sm"
        />
      </div>
      <div>
        <label className="block text-xs text-gray-500 mb-1">Channel</label>
        <select
          value={form.channel}
          onChange={(e) => setForm((p) => ({ ...p, channel: e.target.value }))}
          className="border border-gray-300 rounded px-2 py-1.5 text-sm"
        >
          {CHANNELS.map((ch) => <option key={ch} value={ch}>{ch}</option>)}
        </select>
      </div>
      <div>
        <label className="block text-xs text-gray-500 mb-1">Angle</label>
        <input
          value={form.contentAngle}
          onChange={(e) => setForm((p) => ({ ...p, contentAngle: e.target.value }))}
          placeholder="ฝุ่นน้อย เนียนนุ่ม..."
          className="border border-gray-300 rounded px-2 py-1.5 text-sm w-48"
        />
      </div>
      <button
        type="submit"
        disabled={saving}
        className="px-3 py-1.5 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 disabled:opacity-50"
      >
        {saving ? "Adding..." : "Add"}
      </button>
      <button type="button" onClick={() => setOpen(false)} className="text-sm text-gray-500 hover:text-gray-700">
        Cancel
      </button>
    </form>
  );
}

function ItemRow({
  item,
  isEditing,
  editForm,
  onEdit,
  onSave,
  onCancel,
  onDelete,
  onEditFormChange,
  onApprove,
  onReject,
}: {
  item: CalendarItem;
  isEditing: boolean;
  editForm: CalendarItemUpdateRequest;
  onEdit: () => void;
  onSave: () => void;
  onCancel: () => void;
  onDelete: () => void;
  onEditFormChange: (patch: Partial<CalendarItemUpdateRequest>) => void;
  onApprove?: () => void;
  onReject?: () => void;
}) {
  if (isEditing) {
    return (
      <tr className="bg-blue-50">
        <td className="px-4 py-2">
          <input
            type="date"
            value={editForm.plannedDate ?? ""}
            onChange={(e) => onEditFormChange({ plannedDate: e.target.value })}
            className="border border-gray-300 rounded px-2 py-1 text-xs w-32"
          />
        </td>
        <td className="px-4 py-2">
          <select
            value={editForm.channel ?? ""}
            onChange={(e) => onEditFormChange({ channel: e.target.value })}
            className="border border-gray-300 rounded px-2 py-1 text-xs"
          >
            {CHANNELS.map((ch) => <option key={ch} value={ch}>{ch}</option>)}
          </select>
        </td>
        <td className="px-4 py-2">
          <select
            value={editForm.contentType ?? ""}
            onChange={(e) => onEditFormChange({ contentType: e.target.value })}
            className="border border-gray-300 rounded px-2 py-1 text-xs"
          >
            <option value="">—</option>
            {CONTENT_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
        </td>
        <td className="px-4 py-2">
          <input
            value={editForm.contentAngle ?? ""}
            onChange={(e) => onEditFormChange({ contentAngle: e.target.value })}
            className="border border-gray-300 rounded px-2 py-1 text-xs w-full"
          />
        </td>
        <td className="px-4 py-2 text-xs">—</td>
        <td className="px-4 py-2">
          <div className="flex gap-2">
            <button onClick={onSave} className="text-xs text-blue-600 hover:underline">Save</button>
            <button onClick={onCancel} className="text-xs text-gray-500 hover:underline">Cancel</button>
          </div>
        </td>
      </tr>
    );
  }

  return (
    <tr className="hover:bg-gray-50">
      <td className="px-4 py-3 text-gray-700 font-mono text-xs">{item.plannedDate}</td>
      <td className="px-4 py-3 text-gray-700 capitalize">{item.channel}</td>
      <td className="px-4 py-3 text-gray-500 text-xs">{item.contentType ?? "—"}</td>
      <td className="px-4 py-3 text-gray-600 max-w-xs truncate">{item.contentAngle ?? "—"}</td>
      <td className="px-4 py-3">
        <span className={`text-xs px-2 py-0.5 rounded-full ${STATUS_COLORS[item.status]}`}>
          {item.status}
        </span>
      </td>
      <td className="px-4 py-3">
        <div className="flex items-center gap-2">
          {item.status === "PLANNED" && (
            <button onClick={onEdit} className="text-xs text-blue-600 hover:underline">Edit</button>
          )}
          {item.generatedContentId && (
            <>
              <Link
                href={`/content`}
                className="text-xs text-purple-600 hover:underline"
              >
                View Content
              </Link>
              {onApprove && (
                <button onClick={onApprove} className="text-xs text-green-600 hover:underline">Approve</button>
              )}
              {onReject && (
                <button onClick={onReject} className="text-xs text-red-500 hover:underline">Reject</button>
              )}
            </>
          )}
          {item.status === "PLANNED" && (
            <button onClick={onDelete} className="text-xs text-red-400 hover:underline">Delete</button>
          )}
        </div>
      </td>
    </tr>
  );
}
