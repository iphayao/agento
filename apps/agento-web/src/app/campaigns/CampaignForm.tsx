"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import type { Campaign, CampaignRequest, CHANNELS, CAMPAIGN_STATUSES } from "@/types";
import { CHANNELS as channels, CAMPAIGN_STATUSES as statuses } from "@/types";

interface Props {
  defaultValues?: Campaign;
  onSubmit: (data: CampaignRequest) => Promise<void>;
}

export default function CampaignForm({ defaultValues, onSubmit }: Props) {
  const router = useRouter();
  const [saving, setSaving] = useState(false);

  const [name, setName] = useState(defaultValues?.name ?? "");
  const [objective, setObjective] = useState(defaultValues?.objective ?? "");
  const [channel, setChannel] = useState(defaultValues?.channel ?? "");
  const [targetAudience, setTargetAudience] = useState(defaultValues?.targetAudience ?? "");
  const [contentAngle, setContentAngle] = useState(defaultValues?.contentAngle ?? "");
  const [status, setStatus] = useState(defaultValues?.status ?? "DRAFT");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    setSaving(true);
    try {
      await onSubmit({
        name: name.trim(),
        objective: objective.trim() || undefined,
        channel: channel || undefined,
        targetAudience: targetAudience.trim() || undefined,
        contentAngle: contentAngle.trim() || undefined,
        status,
      });
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <div>
        <label className="form-label">Campaign Name *</label>
        <input
          className="form-input"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="e.g. June TikTok Campaign — ฝุ่นน้อย Focus"
          required
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="form-label">Channel</label>
          <select
            className="form-input"
            value={channel}
            onChange={(e) => setChannel(e.target.value)}
          >
            <option value="">Select channel...</option>
            {channels.map((c) => (
              <option key={c.value} value={c.value}>
                {c.label}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="form-label">Status</label>
          <select
            className="form-input"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          >
            {statuses.map((s) => (
              <option key={s.value} value={s.value}>
                {s.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div>
        <label className="form-label">Objective</label>
        <textarea
          className="form-textarea"
          rows={2}
          value={objective}
          onChange={(e) => setObjective(e.target.value)}
          placeholder="e.g. Drive TikTok Shop sales for June — target 200 orders"
        />
      </div>

      <div>
        <label className="form-label">Target Audience</label>
        <input
          className="form-input"
          value={targetAudience}
          onChange={(e) => setTargetAudience(e.target.value)}
          placeholder="e.g. Women Gen Y in Bangkok, household buyers"
        />
      </div>

      <div>
        <label className="form-label">Content Angle</label>
        <textarea
          className="form-textarea"
          rows={2}
          value={contentAngle}
          onChange={(e) => setContentAngle(e.target.value)}
          placeholder="e.g. ฝุ่นน้อย เหมาะกับออฟฟิศและบ้าน — emphasize low-dust benefit"
        />
      </div>

      <div className="flex gap-3 pt-2">
        <button type="submit" disabled={saving} className="btn-primary">
          {saving ? "Saving..." : "Save Campaign"}
        </button>
        <button type="button" onClick={() => router.back()} className="btn-secondary">
          Cancel
        </button>
      </div>
    </form>
  );
}
