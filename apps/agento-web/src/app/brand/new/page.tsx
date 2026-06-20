"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { brandApi } from "@/lib/api";
import type { BrandProfileRequest } from "@/types";
import BrandForm from "../BrandForm";

export default function NewBrandPage() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (data: BrandProfileRequest) => {
    setError(null);
    try {
      await brandApi.create(data);
      router.push("/brand");
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Save failed");
    }
  };

  return (
    <div className="p-8 max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">New Brand Profile</h1>
      {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
      <BrandForm onSubmit={handleSubmit} />
    </div>
  );
}
