"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { campaignApi } from "@/lib/api";
import type { Campaign } from "@/types";

function StatusBadge({ status }: { status: string }) {
  const cls =
    status === "ACTIVE"
      ? "badge-active"
      : status === "COMPLETED"
      ? "badge-approved"
      : status === "ARCHIVED"
      ? "badge bg-gray-100 text-gray-600"
      : "badge-draft";
  return <span className={cls}>{status}</span>;
}

export default function CampaignsPage() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    campaignApi
      .list()
      .then((res) => setCampaigns(res.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this campaign?")) return;
    try {
      await campaignApi.delete(id);
      setCampaigns((prev) => prev.filter((c) => c.id !== id));
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Delete failed");
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Campaigns</h1>
          <p className="text-gray-500 text-sm mt-1">Create and manage marketing campaigns</p>
        </div>
        <Link href="/campaigns/new" className="btn-primary">
          + New Campaign
        </Link>
      </div>

      {loading && <p className="text-gray-400">Loading...</p>}
      {error && <p className="text-red-500 text-sm">{error}</p>}

      {!loading && campaigns.length === 0 && (
        <div className="card text-center py-12">
          <p className="text-gray-500 mb-4">No campaigns yet.</p>
          <Link href="/campaigns/new" className="btn-primary">
            Create Campaign
          </Link>
        </div>
      )}

      <div className="space-y-4">
        {campaigns.map((campaign) => (
          <div key={campaign.id} className="card hover:shadow-md transition-shadow">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-1">
                  <h2 className="text-lg font-semibold text-gray-900">{campaign.name}</h2>
                  <StatusBadge status={campaign.status} />
                  {campaign.channel && (
                    <span className="badge bg-purple-50 text-purple-700">{campaign.channel}</span>
                  )}
                </div>
                {campaign.objective && (
                  <p className="text-gray-600 text-sm">{campaign.objective}</p>
                )}
                {campaign.contentAngle && (
                  <p className="text-gray-500 text-sm mt-1">
                    <span className="font-medium">Angle:</span> {campaign.contentAngle}
                  </p>
                )}
              </div>
              <div className="flex gap-2 ml-4">
                <Link href={`/campaigns/${campaign.id}`} className="btn-primary btn-sm">
                  Open
                </Link>
                <Link href={`/campaigns/${campaign.id}/edit`} className="btn-secondary btn-sm">
                  Edit
                </Link>
                <button
                  onClick={() => handleDelete(campaign.id)}
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
