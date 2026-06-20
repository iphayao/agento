"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import type { BrandProfile, BrandProfileRequest } from "@/types";

interface Props {
  defaultValues?: BrandProfile;
  onSubmit: (data: BrandProfileRequest) => Promise<void>;
}

function parseLines(text: string): string[] {
  return text
    .split("\n")
    .map((s) => s.trim())
    .filter(Boolean);
}

export default function BrandForm({ defaultValues, onSubmit }: Props) {
  const router = useRouter();
  const [saving, setSaving] = useState(false);

  const [brandName, setBrandName] = useState(defaultValues?.brandName ?? "");
  const [slogan, setSlogan] = useState(defaultValues?.slogan ?? "");
  const [toneOfVoice, setToneOfVoice] = useState(defaultValues?.toneOfVoice ?? "");
  const [targetAudience, setTargetAudience] = useState(defaultValues?.targetAudience ?? "");
  const [keyMessages, setKeyMessages] = useState(
    defaultValues?.keyMessages?.join("\n") ?? ""
  );
  const [prohibitedClaims, setProhibitedClaims] = useState(
    defaultValues?.prohibitedClaims?.join("\n") ?? ""
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!brandName.trim()) return;
    setSaving(true);
    try {
      await onSubmit({
        brandName: brandName.trim(),
        slogan: slogan.trim() || undefined,
        toneOfVoice: toneOfVoice.trim() || undefined,
        targetAudience: targetAudience.trim() || undefined,
        keyMessages: parseLines(keyMessages),
        prohibitedClaims: parseLines(prohibitedClaims),
      });
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <div>
        <label className="form-label">Brand Name *</label>
        <input
          className="form-input"
          value={brandName}
          onChange={(e) => setBrandName(e.target.value)}
          placeholder="e.g. SoClean"
          required
        />
      </div>

      <div>
        <label className="form-label">Slogan</label>
        <input
          className="form-input"
          value={slogan}
          onChange={(e) => setSlogan(e.target.value)}
          placeholder="e.g. สะอาด เนียนนุ่ม ฝุ่นน้อย"
        />
      </div>

      <div>
        <label className="form-label">Tone of Voice</label>
        <textarea
          className="form-textarea"
          rows={2}
          value={toneOfVoice}
          onChange={(e) => setToneOfVoice(e.target.value)}
          placeholder="e.g. Warm, honest, practical. Speaks Thai naturally. Never over-promises."
        />
      </div>

      <div>
        <label className="form-label">Target Audience</label>
        <textarea
          className="form-textarea"
          rows={2}
          value={targetAudience}
          onChange={(e) => setTargetAudience(e.target.value)}
          placeholder="e.g. Women Gen Y with purchasing power, households, office buyers"
        />
      </div>

      <div>
        <label className="form-label">Key Messages (one per line)</label>
        <textarea
          className="form-textarea"
          rows={4}
          value={keyMessages}
          onChange={(e) => setKeyMessages(e.target.value)}
          placeholder={"ฝุ่นน้อย\nเนียนนุ่ม\nคุ้มค่า\nเหมาะสำหรับบ้านและออฟฟิศ"}
        />
      </div>

      <div>
        <label className="form-label">Additional Prohibited Claims (one per line)</label>
        <textarea
          className="form-textarea"
          rows={3}
          value={prohibitedClaims}
          onChange={(e) => setProhibitedClaims(e.target.value)}
          placeholder="Add any brand-specific terms to avoid..."
        />
        <p className="text-xs text-gray-400 mt-1">
          Standard prohibited terms (dust-free, antibacterial, etc.) are enforced automatically.
        </p>
      </div>

      <div className="flex gap-3 pt-2">
        <button type="submit" disabled={saving} className="btn-primary">
          {saving ? "Saving..." : "Save Brand Profile"}
        </button>
        <button type="button" onClick={() => router.back()} className="btn-secondary">
          Cancel
        </button>
      </div>
    </form>
  );
}
