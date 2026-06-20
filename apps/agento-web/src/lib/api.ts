import type {
  ApiResponse,
  BrandProfile,
  BrandProfileRequest,
  Campaign,
  CampaignRequest,
  GeneratedContent,
  GenerateContentRequest,
  ProductFact,
  ProductFactRequest,
} from "@/types";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";
const API_KEY = process.env.NEXT_PUBLIC_API_KEY || "";

async function apiFetch<T>(
  path: string,
  options?: RequestInit
): Promise<ApiResponse<T>> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(API_KEY ? { "X-Api-Key": API_KEY } : {}),
    ...(options?.headers as Record<string, string>),
  };
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers,
  });
  const json = await res.json();
  if (!res.ok) {
    throw new Error(json.message || `API error ${res.status}`);
  }
  return json as ApiResponse<T>;
}

// Brand Profile
export const brandApi = {
  list: () => apiFetch<BrandProfile[]>("/brands"),
  get: (id: string) => apiFetch<BrandProfile>(`/brands/${id}`),
  create: (data: BrandProfileRequest) =>
    apiFetch<BrandProfile>("/brands", { method: "POST", body: JSON.stringify(data) }),
  update: (id: string, data: BrandProfileRequest) =>
    apiFetch<BrandProfile>(`/brands/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (id: string) =>
    apiFetch<void>(`/brands/${id}`, { method: "DELETE" }),
};

// Product Facts
export const productApi = {
  list: () => apiFetch<ProductFact[]>("/products"),
  get: (id: string) => apiFetch<ProductFact>(`/products/${id}`),
  create: (data: ProductFactRequest) =>
    apiFetch<ProductFact>("/products", { method: "POST", body: JSON.stringify(data) }),
  update: (id: string, data: ProductFactRequest) =>
    apiFetch<ProductFact>(`/products/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (id: string) =>
    apiFetch<void>(`/products/${id}`, { method: "DELETE" }),
};

// Campaigns
export const campaignApi = {
  list: () => apiFetch<Campaign[]>("/campaigns"),
  get: (id: string) => apiFetch<Campaign>(`/campaigns/${id}`),
  create: (data: CampaignRequest) =>
    apiFetch<Campaign>("/campaigns", { method: "POST", body: JSON.stringify(data) }),
  update: (id: string, data: CampaignRequest) =>
    apiFetch<Campaign>(`/campaigns/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (id: string) =>
    apiFetch<void>(`/campaigns/${id}`, { method: "DELETE" }),
};

// Generated Content
export const contentApi = {
  list: () => apiFetch<GeneratedContent[]>("/content"),
  listByCampaign: (campaignId: string) =>
    apiFetch<GeneratedContent[]>(`/campaigns/${campaignId}/content`),
  get: (id: string) => apiFetch<GeneratedContent>(`/content/${id}`),
  generate: (campaignId: string, data: GenerateContentRequest) =>
    apiFetch<GeneratedContent>(`/campaigns/${campaignId}/content/generate`, {
      method: "POST",
      body: JSON.stringify(data),
    }),
  approve: (id: string) =>
    apiFetch<GeneratedContent>(`/content/${id}/approve`, { method: "PUT" }),
  reject: (id: string) =>
    apiFetch<GeneratedContent>(`/content/${id}/reject`, { method: "PUT" }),
  delete: (id: string) =>
    apiFetch<void>(`/content/${id}`, { method: "DELETE" }),
};
