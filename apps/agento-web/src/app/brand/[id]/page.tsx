"use client";

import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { brandApi } from "@/lib/api";
import type { BrandProfile, BrandProfileRequest } from "@/types";
import BrandForm from "../BrandForm";

export default function EditBrandPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [brand, setBrand] = useState<BrandProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    brandApi
      .get(id)
      .then((res) => setBrand(res.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSubmit = async (data: BrandProfileRequest) => {
    setError(null);
    try {
      await brandApi.update(id, data);
      router.push("/brand");
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Save failed");
    }
  };

  if (loading) return <div className="p-8 text-gray-400">Loading...</div>;
  if (error) return <div className="p-8 text-red-500">{error}</div>;
  if (!brand) return null;

  return (
    <div className="p-8 max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Edit Brand Profile</h1>
      <BrandForm defaultValues={brand} onSubmit={handleSubmit} />
    </div>
  );
}
