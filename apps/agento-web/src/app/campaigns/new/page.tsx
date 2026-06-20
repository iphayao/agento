"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { campaignApi } from "@/lib/api";
import type { CampaignRequest } from "@/types";
import CampaignForm from "../CampaignForm";

export default function NewCampaignPage() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (data: CampaignRequest) => {
    setError(null);
    try {
      const res = await campaignApi.create(data);
      router.push(`/campaigns/${res.data.id}`);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Save failed");
    }
  };

  return (
    <div className="p-8 max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">New Campaign</h1>
      {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
      <CampaignForm onSubmit={handleSubmit} />
    </div>
  );
}
