import type {
  AgentWorkflow,
  AgentStepResult,
  AnalyzeRequest,
  ApiResponse,
  BrandProfile,
  BrandProfileRequest,
  Campaign,
  CampaignRequest,
  ChannelBreakdown,
  ContentInsight,
  ContentPerformance,
  ContentPerformanceRequest,
  DashboardStats,
  GeneratedContent,
  GenerateContentRequest,
  KnowledgeChunk,
  KnowledgeDocument,
  KnowledgeDocumentRequest,
  KnowledgeSearchRequest,
  KnowledgeSearchResult,
  PerformanceSummary,
  ProductFact,
  ProductFactRequest,
  TopContent,
} from "@/types";

// All API calls go through the Next.js proxy at /api/[...path].
// The proxy injects the API key server-side, so the key never reaches the browser.
const BASE_URL = "/api";

async function apiFetch<T>(
  path: string,
  options?: RequestInit
): Promise<ApiResponse<T>> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
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

// Knowledge Base
export const knowledgeApi = {
  list: () => apiFetch<KnowledgeDocument[]>("/knowledge"),
  get: (id: string) => apiFetch<KnowledgeDocument>(`/knowledge/${id}`),
  getChunks: (id: string) => apiFetch<KnowledgeChunk[]>(`/knowledge/${id}/chunks`),
  create: (data: KnowledgeDocumentRequest) =>
    apiFetch<KnowledgeDocument>("/knowledge", { method: "POST", body: JSON.stringify(data) }),
  update: (id: string, data: KnowledgeDocumentRequest) =>
    apiFetch<KnowledgeDocument>(`/knowledge/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  archive: (id: string) =>
    apiFetch<void>(`/knowledge/${id}/archive`, { method: "PUT" }),
  delete: (id: string) =>
    apiFetch<void>(`/knowledge/${id}`, { method: "DELETE" }),
  search: (data: KnowledgeSearchRequest) =>
    apiFetch<KnowledgeSearchResult>("/knowledge/search", {
      method: "POST",
      body: JSON.stringify(data),
    }),
};

// Performance Learning
export const performanceApi = {
  list: () => apiFetch<ContentPerformance[]>("/performance"),
  get: (id: string) => apiFetch<ContentPerformance>(`/performance/${id}`),
  listByContent: (contentId: string) =>
    apiFetch<ContentPerformance[]>(`/performance/content/${contentId}`),
  create: (data: ContentPerformanceRequest) =>
    apiFetch<ContentPerformance>("/performance", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  update: (id: string, data: ContentPerformanceRequest) =>
    apiFetch<ContentPerformance>(`/performance/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  delete: (id: string) =>
    apiFetch<void>(`/performance/${id}`, { method: "DELETE" }),
  importCsv: (file: File) => {
    const form = new FormData();
    form.append("file", file);
    return fetch(`${BASE_URL}/performance/import/csv`, {
      method: "POST",
      body: form,
    }).then((r) => r.json() as Promise<ApiResponse<{ imported: number }>>);
  },
  getDashboard: () => apiFetch<DashboardStats>("/performance/analytics/dashboard"),
  getTop: (n = 5) =>
    apiFetch<TopContent>(`/performance/analytics/top?n=${n}`),
  getInsights: () => apiFetch<ContentInsight[]>("/performance/insights"),
  getSummaries: () => apiFetch<PerformanceSummary[]>("/performance/summaries"),
  analyze: (data: AnalyzeRequest) =>
    apiFetch<{ status: string; recordCount: number }>("/performance/analyze", {
      method: "POST",
      body: JSON.stringify(data),
    }),
};

// Agent Workflows
export const workflowApi = {
  start: (campaignId: string) =>
    apiFetch<AgentWorkflow>(`/campaigns/${campaignId}/agent-workflows`, { method: "POST" }),
  listByCampaign: (campaignId: string) =>
    apiFetch<AgentWorkflow[]>(`/campaigns/${campaignId}/agent-workflows`),
  get: (workflowId: string) =>
    apiFetch<AgentWorkflow>(`/agent-workflows/${workflowId}`),
  getSteps: (workflowId: string) =>
    apiFetch<AgentStepResult[]>(`/agent-workflows/${workflowId}/steps`),
  retry: (workflowId: string) =>
    apiFetch<AgentWorkflow>(`/agent-workflows/${workflowId}/retry`, { method: "POST" }),
  cancel: (workflowId: string) =>
    apiFetch<AgentWorkflow>(`/agent-workflows/${workflowId}/cancel`, { method: "POST" }),
};
