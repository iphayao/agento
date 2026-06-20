"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { brandApi } from "@/lib/api";
import type { BrandProfile } from "@/types";

export default function BrandListPage() {
  const [brands, setBrands] = useState<BrandProfile[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    brandApi
      .list()
      .then((res) => setBrands(res.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this brand profile?")) return;
    try {
      await brandApi.delete(id);
      setBrands((prev) => prev.filter((b) => b.id !== id));
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Delete failed");
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Brand Profiles</h1>
          <p className="text-gray-500 text-sm mt-1">Manage the SoClean brand identity</p>
        </div>
        <Link href="/brand/new" className="btn-primary">
          + New Brand Profile
        </Link>
      </div>

      {loading && <p className="text-gray-400">Loading...</p>}
      {error && <p className="text-red-500 text-sm">{error}</p>}

      {!loading && brands.length === 0 && (
        <div className="card text-center py-12">
          <p className="text-gray-500 mb-4">No brand profiles yet.</p>
          <Link href="/brand/new" className="btn-primary">
            Create Brand Profile
          </Link>
        </div>
      )}

      <div className="space-y-4">
        {brands.map((brand) => (
          <div key={brand.id} className="card">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <h2 className="text-lg font-semibold text-gray-900">{brand.brandName}</h2>
                {brand.slogan && (
                  <p className="text-gray-500 text-sm mt-1 italic">{brand.slogan}</p>
                )}
                {brand.toneOfVoice && (
                  <p className="text-gray-600 text-sm mt-2">
                    <span className="font-medium">Tone:</span> {brand.toneOfVoice}
                  </p>
                )}
                {brand.targetAudience && (
                  <p className="text-gray-600 text-sm">
                    <span className="font-medium">Audience:</span> {brand.targetAudience}
                  </p>
                )}
                {brand.keyMessages.length > 0 && (
                  <div className="mt-2 flex flex-wrap gap-1">
                    {brand.keyMessages.map((msg, i) => (
                      <span key={i} className="badge bg-blue-50 text-blue-700">
                        {msg}
                      </span>
                    ))}
                  </div>
                )}
              </div>
              <div className="flex gap-2 ml-4">
                <Link href={`/brand/${brand.id}`} className="btn-secondary btn-sm">
                  Edit
                </Link>
                <button
                  onClick={() => handleDelete(brand.id)}
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
