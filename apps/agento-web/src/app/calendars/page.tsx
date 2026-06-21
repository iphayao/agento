"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { calendarApi } from "@/lib/api";
import type { ContentCalendar } from "@/types";

const STATUS_COLORS: Record<string, string> = {
  DRAFT: "bg-gray-100 text-gray-700",
  GENERATING: "bg-yellow-100 text-yellow-700",
  READY_FOR_REVIEW: "bg-blue-100 text-blue-700",
  APPROVED: "bg-green-100 text-green-700",
};

export default function CalendarsPage() {
  const [calendars, setCalendars] = useState<ContentCalendar[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    calendarApi.list()
      .then((r) => setCalendars(r.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this calendar and all its items?")) return;
    try {
      await calendarApi.delete(id);
      setCalendars((prev) => prev.filter((c) => c.id !== id));
    } catch (e: any) {
      alert(e.message);
    }
  };

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Content Calendars</h1>
          <p className="text-sm text-gray-500 mt-1">Plan and batch-generate content by week or month</p>
        </div>
        <Link
          href="/calendars/new"
          className="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700"
        >
          New Calendar
        </Link>
      </div>

      {loading && <p className="text-gray-500">Loading calendars...</p>}
      {error && <p className="text-red-600">Error: {error}</p>}

      {!loading && !error && calendars.length === 0 && (
        <div className="text-center py-16 border-2 border-dashed border-gray-200 rounded-lg">
          <p className="text-gray-400 mb-4">No calendars yet</p>
          <Link href="/calendars/new" className="text-blue-600 hover:underline text-sm">
            Create your first content calendar
          </Link>
        </div>
      )}

      {calendars.length > 0 && (
        <div className="grid gap-4">
          {calendars.map((cal) => (
            <div key={cal.id} className="bg-white border border-gray-200 rounded-lg p-5">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-1">
                    <h2 className="font-semibold text-gray-900">{cal.name}</h2>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLORS[cal.status] ?? "bg-gray-100"}`}>
                      {cal.status.replace("_", " ")}
                    </span>
                  </div>
                  <p className="text-sm text-gray-500">
                    {cal.periodStart} — {cal.periodEnd}
                  </p>
                  {cal.objective && (
                    <p className="text-sm text-gray-600 mt-1 line-clamp-1">{cal.objective}</p>
                  )}
                  <p className="text-xs text-gray-400 mt-2">{cal.itemCount} items</p>
                </div>
                <div className="flex items-center gap-2 ml-4">
                  <Link
                    href={`/calendars/${cal.id}`}
                    className="text-sm text-blue-600 hover:underline"
                  >
                    View
                  </Link>
                  <button
                    onClick={() => handleDelete(cal.id)}
                    className="text-sm text-red-500 hover:underline"
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
