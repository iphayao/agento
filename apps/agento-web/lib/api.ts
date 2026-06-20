import {
  ApiResponse,
  BrandProfile,
  BrandProfileRequest,
  Campaign,
  CampaignRequest,
  GeneratedContent,
  GenerateRequest,
  ProductFact,
  ProductFactRequest,
} from '@/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  });

  const json: ApiResponse<T> = await res.json();

  if (!json.success || !res.ok) {
    throw new Error(json.error || `Request failed: ${res.status}`);
  }

  return json.data as T;
}

// --- Brand Profile ---

export const brandApi = {
  get: () => request<BrandProfile | null>('/api/v1/brand'),
  create: (data: BrandProfileRequest) =>
    request<BrandProfile>('/api/v1/brand', { method: 'POST', body: JSON.stringify(data) }),
  update: (id: number, data: BrandProfileRequest) =>
    request<BrandProfile>(`/api/v1/brand/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
};

// --- Product Facts ---

export const productsApi = {
  list: () => request<ProductFact[]>('/api/v1/products'),
  get: (id: number) => request<ProductFact>(`/api/v1/products/${id}`),
  create: (data: ProductFactRequest) =>
    request<ProductFact>('/api/v1/products', { method: 'POST', body: JSON.stringify(data) }),
  update: (id: number, data: ProductFactRequest) =>
    request<ProductFact>(`/api/v1/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id: number) => request<null>(`/api/v1/products/${id}`, { method: 'DELETE' }),
};

// --- Campaigns ---

export const campaignsApi = {
  list: () => request<Campaign[]>('/api/v1/campaigns'),
  get: (id: number) => request<Campaign>(`/api/v1/campaigns/${id}`),
  create: (data: CampaignRequest) =>
    request<Campaign>('/api/v1/campaigns', { method: 'POST', body: JSON.stringify(data) }),
  update: (id: number, data: CampaignRequest) =>
    request<Campaign>(`/api/v1/campaigns/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id: number) => request<null>(`/api/v1/campaigns/${id}`, { method: 'DELETE' }),
  generate: (id: number, data: GenerateRequest) =>
    request<GeneratedContent>(`/api/v1/campaigns/${id}/generate`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),
};

// --- Generated Content ---

export const contentApi = {
  list: (campaignId?: number) =>
    request<GeneratedContent[]>(
      `/api/v1/content${campaignId ? `?campaignId=${campaignId}` : ''}`
    ),
  get: (id: number) => request<GeneratedContent>(`/api/v1/content/${id}`),
  approve: (id: number) =>
    request<GeneratedContent>(`/api/v1/content/${id}/approve`, { method: 'POST' }),
  reject: (id: number) =>
    request<GeneratedContent>(`/api/v1/content/${id}/reject`, { method: 'POST' }),
};
