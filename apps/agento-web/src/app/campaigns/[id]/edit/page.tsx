"use client";

import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { campaignApi } from "@/lib/api";
import type { Campaign, CampaignRequest } from "@/types";
import CampaignForm from "../../CampaignForm";

export default function EditCampaignPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    campaignApi
      .get(id)
      .then((res) => setCampaign(res.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSubmit = async (data: CampaignRequest) => {
    setError(null);
    try {
      await campaignApi.update(id, data);
      router.push(`/campaigns/${id}`);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Save failed");
    }
  };

  if (loading) return <div className="p-8 text-gray-400">Loading...</div>;
  if (error) return <div className="p-8 text-red-500">{error}</div>;
  if (!campaign) return null;

  return (
    <div className="p-8 max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Edit Campaign</h1>
      <CampaignForm defaultValues={campaign} onSubmit={handleSubmit} />
    </div>
  );
}
