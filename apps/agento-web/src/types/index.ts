export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export interface BrandProfile {
  id: string;
  brandName: string;
  slogan?: string;
  toneOfVoice?: string;
  targetAudience?: string;
  keyMessages: string[];
  prohibitedClaims: string[];
  createdAt: string;
  updatedAt: string;
}

export interface BrandProfileRequest {
  brandName: string;
  slogan?: string;
  toneOfVoice?: string;
  targetAudience?: string;
  keyMessages: string[];
  prohibitedClaims: string[];
}

export interface ProductFact {
  id: string;
  productName: string;
  sku?: string;
  sheetCount: number;
  ply: number;
  packSize: number;
  cartonSize: number;
  keyBenefits: string[];
  proofPoints: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ProductFactRequest {
  productName: string;
  sku?: string;
  sheetCount: number;
  ply: number;
  packSize: number;
  cartonSize: number;
  keyBenefits: string[];
  proofPoints: string[];
}

export interface Campaign {
  id: string;
  name: string;
  objective?: string;
  channel?: string;
  targetAudience?: string;
  contentAngle?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface CampaignRequest {
  name: string;
  objective?: string;
  channel?: string;
  targetAudience?: string;
  contentAngle?: string;
  status?: string;
}

export type ContentStatus = "DRAFT" | "APPROVED" | "REJECTED";

export interface GeneratedContent {
  id: string;
  campaignId: string;
  contentType?: string;
  channel?: string;
  title?: string;
  body?: string;
  hook?: string;
  callToAction?: string;
  hashtags: string[];
  status: ContentStatus;
  aiModel?: string;
  promptVersion?: string;
  complianceNotes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface GenerateContentRequest {
  contentType: string;
  additionalContext?: string;
}

export const CHANNELS = [
  { value: "tiktok", label: "TikTok Shop" },
  { value: "shopee", label: "Shopee" },
  { value: "lazada", label: "Lazada" },
  { value: "facebook", label: "Facebook" },
  { value: "reseller", label: "Reseller Store" },
];

export const CONTENT_TYPES = [
  { value: "TIKTOK_CAPTION", label: "TikTok Caption" },
  { value: "TIKTOK_SCRIPT", label: "TikTok Video Script" },
  { value: "SHOPEE_DESCRIPTION", label: "Shopee Product Description" },
  { value: "LAZADA_DESCRIPTION", label: "Lazada Product Description" },
  { value: "FACEBOOK_POST", label: "Facebook Post" },
  { value: "RESELLER_POST", label: "Reseller Sales Post" },
];

// ── Agent Workflow ──────────────────────────────────────────────────────────

export type AgentWorkflowStatus = "PENDING" | "RUNNING" | "COMPLETED" | "FAILED" | "CANCELLED";
export type AgentStepStatus = "PENDING" | "RUNNING" | "COMPLETED" | "FAILED" | "SKIPPED";

export interface AgentStepResult {
  id: string;
  workflowId: string;
  stepName: string;
  status: AgentStepStatus;
  outputPayload?: string;
  errorMessage?: string;
  startedAt?: string;
  completedAt?: string;
  createdAt?: string;
}

export interface AgentWorkflow {
  id: string;
  campaignId: string;
  status: AgentWorkflowStatus;
  currentStep?: string;
  errorMessage?: string;
  createdAt: string;
  updatedAt: string;
  steps: AgentStepResult[];
  generatedContent?: {
    contentId: string;
    title?: string;
    status: string;
    complianceWarnings: string[];
  };
}

export const AGENT_STEP_LABELS: Record<string, string> = {
  retrieval: "Knowledge Retrieval",
  brand_strategist: "Brand Strategist",
  customer_insight: "Customer Insight",
  content_writer: "Content Writer",
  claim_compliance: "Claim & Compliance",
  editor: "Editor",
  final_formatter: "Final Formatter",
};

export const AGENT_STEPS = [
  "retrieval",
  "brand_strategist",
  "customer_insight",
  "content_writer",
  "claim_compliance",
  "editor",
  "final_formatter",
];

// ── Knowledge Base ──────────────────────────────────────────────────────────

export type DocumentType =
  | "BRAND_GUIDELINE"
  | "PRODUCT_FACT"
  | "APPROVED_CLAIM"
  | "PROHIBITED_CLAIM"
  | "CUSTOMER_REVIEW"
  | "WINNING_CONTENT"
  | "COMPETITOR_NOTE"
  | "MARKET_INSIGHT";

export type DocumentStatus = "ACTIVE" | "ARCHIVED";

export interface KnowledgeDocument {
  id: string;
  title: string;
  type: DocumentType;
  content: string;
  source?: string;
  tags: string[];
  status: DocumentStatus;
  chunkCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface KnowledgeDocumentRequest {
  title: string;
  type: DocumentType;
  content: string;
  source?: string;
  tags: string[];
}

export interface KnowledgeChunk {
  id: string;
  documentId: string;
  chunkIndex: number;
  chunkText: string;
  hasEmbedding: boolean;
  createdAt: string;
}

export interface KnowledgeSearchRequest {
  query: string;
  documentType?: DocumentType;
  topK?: number;
  minScore?: number;
}

export interface KnowledgeSearchResult {
  query: string;
  results: KnowledgeChunkMatch[];
}

export interface KnowledgeChunkMatch {
  chunkId: string;
  documentId: string;
  documentTitle: string;
  documentType: string;
  chunkText: string;
  score: number;
  chunkIndex: number;
}

// ── Performance Learning ────────────────────────────────────────────────────

export interface ContentPerformance {
  id: string;
  generatedContentId: string;
  channel: string;
  publishedAt?: string;
  impressions: number;
  views: number;
  clicks: number;
  likes: number;
  comments: number;
  shares: number;
  orders: number;
  revenue: number;
  conversionRate?: number;
  engagementRate?: number;
  cost: number;
  roas?: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ContentPerformanceRequest {
  generatedContentId: string;
  channel: string;
  publishedAt?: string;
  impressions: number;
  views: number;
  clicks: number;
  likes: number;
  comments: number;
  shares: number;
  orders: number;
  revenue: number;
  cost: number;
  notes?: string;
}

export type InsightType =
  | "WINNING_HOOK"
  | "WINNING_ANGLE"
  | "LOW_PERFORMING_ANGLE"
  | "STRONG_CTA"
  | "WEAK_CTA"
  | "AUDIENCE_SIGNAL"
  | "CHANNEL_SIGNAL";

export interface ContentInsight {
  id: string;
  generatedContentId?: string;
  campaignId?: string;
  insightType: InsightType;
  insightText: string;
  confidenceScore: number;
  createdAt: string;
}

export interface PerformanceSummary {
  id: string;
  periodStart: string;
  periodEnd: string;
  channel?: string;
  summaryText: string;
  recommendedAngles: string[];
  recommendedHooks: string[];
  recommendedCTAs: string[];
  avoidPatterns: string[];
  createdAt: string;
}

export interface DashboardStats {
  totalRecords: number;
  totalImpressions: number;
  totalClicks: number;
  totalOrders: number;
  totalRevenue: number;
  totalCost: number;
  averageEngagementRate: number;
  averageRoas: number;
}

export interface TopContent {
  byRevenue: ContentPerformance[];
  byEngagement: ContentPerformance[];
  byRoas: ContentPerformance[];
  byChannel: ChannelBreakdown[];
}

export interface ChannelBreakdown {
  channel: string;
  count: number;
  impressions: number;
  clicks: number;
  revenue: number;
}

export interface AnalyzeRequest {
  contentIds?: string[];
  channel?: string;
  topN?: number;
}

export const DOCUMENT_TYPES: { value: DocumentType; label: string }[] = [
  { value: "BRAND_GUIDELINE", label: "Brand Guideline" },
  { value: "PRODUCT_FACT", label: "Product Fact" },
  { value: "APPROVED_CLAIM", label: "Approved Claim" },
  { value: "PROHIBITED_CLAIM", label: "Prohibited Claim" },
  { value: "CUSTOMER_REVIEW", label: "Customer Review" },
  { value: "WINNING_CONTENT", label: "Winning Content" },
  { value: "COMPETITOR_NOTE", label: "Competitor Note" },
  { value: "MARKET_INSIGHT", label: "Market Insight" },
];

export const CAMPAIGN_STATUSES = [
  { value: "DRAFT", label: "Draft" },
  { value: "ACTIVE", label: "Active" },
  { value: "COMPLETED", label: "Completed" },
  { value: "ARCHIVED", label: "Archived" },
];
