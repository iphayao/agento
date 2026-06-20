"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import type { ProductFact, ProductFactRequest } from "@/types";

interface Props {
  defaultValues?: ProductFact;
  onSubmit: (data: ProductFactRequest) => Promise<void>;
}

function parseLines(text: string): string[] {
  return text
    .split("\n")
    .map((s) => s.trim())
    .filter(Boolean);
}

export default function ProductForm({ defaultValues, onSubmit }: Props) {
  const router = useRouter();
  const [saving, setSaving] = useState(false);

  const [productName, setProductName] = useState(defaultValues?.productName ?? "");
  const [sku, setSku] = useState(defaultValues?.sku ?? "");
  const [sheetCount, setSheetCount] = useState(defaultValues?.sheetCount ?? 180);
  const [ply, setPly] = useState(defaultValues?.ply ?? 2);
  const [packSize, setPackSize] = useState(defaultValues?.packSize ?? 5);
  const [cartonSize, setCartonSize] = useState(defaultValues?.cartonSize ?? 50);
  const [keyBenefits, setKeyBenefits] = useState(
    defaultValues?.keyBenefits?.join("\n") ?? ""
  );
  const [proofPoints, setProofPoints] = useState(
    defaultValues?.proofPoints?.join("\n") ?? ""
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!productName.trim()) return;
    setSaving(true);
    try {
      await onSubmit({
        productName: productName.trim(),
        sku: sku.trim() || undefined,
        sheetCount,
        ply,
        packSize,
        cartonSize,
        keyBenefits: parseLines(keyBenefits),
        proofPoints: parseLines(proofPoints),
      });
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="form-label">Product Name *</label>
          <input
            className="form-input"
            value={productName}
            onChange={(e) => setProductName(e.target.value)}
            placeholder="e.g. SoClean Facial Tissue"
            required
          />
        </div>
        <div>
          <label className="form-label">SKU</label>
          <input
            className="form-input"
            value={sku}
            onChange={(e) => setSku(e.target.value)}
            placeholder="e.g. SC-180-2P"
          />
        </div>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div>
          <label className="form-label">Sheets per Box</label>
          <input
            className="form-input"
            type="number"
            value={sheetCount}
            onChange={(e) => setSheetCount(Number(e.target.value))}
            min={1}
          />
        </div>
        <div>
          <label className="form-label">Ply</label>
          <input
            className="form-input"
            type="number"
            value={ply}
            onChange={(e) => setPly(Number(e.target.value))}
            min={1}
          />
        </div>
        <div>
          <label className="form-label">Pack Size (boxes)</label>
          <input
            className="form-input"
            type="number"
            value={packSize}
            onChange={(e) => setPackSize(Number(e.target.value))}
            min={1}
          />
        </div>
        <div>
          <label className="form-label">Carton Size (packs)</label>
          <input
            className="form-input"
            type="number"
            value={cartonSize}
            onChange={(e) => setCartonSize(Number(e.target.value))}
            min={1}
          />
        </div>
      </div>

      <div>
        <label className="form-label">Key Benefits (one per line)</label>
        <textarea
          className="form-textarea"
          rows={4}
          value={keyBenefits}
          onChange={(e) => setKeyBenefits(e.target.value)}
          placeholder={"ฝุ่นน้อย\nเนียนนุ่ม\nให้สัมผัสสะอาด\nเหมาะกับการใช้งานทุกวัน"}
        />
      </div>

      <div>
        <label className="form-label">Proof Points (one per line)</label>
        <textarea
          className="form-textarea"
          rows={3}
          value={proofPoints}
          onChange={(e) => setProofPoints(e.target.value)}
          placeholder={"2-ply construction for added thickness\n180 sheets for extended use\nPack of 5 boxes = 900 sheets total"}
        />
      </div>

      <div className="flex gap-3 pt-2">
        <button type="submit" disabled={saving} className="btn-primary">
          {saving ? "Saving..." : "Save Product"}
        </button>
        <button type="button" onClick={() => router.back()} className="btn-secondary">
          Cancel
        </button>
      </div>
    </form>
  );
}
