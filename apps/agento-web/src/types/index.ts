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

export const CAMPAIGN_STATUSES = [
  { value: "DRAFT", label: "Draft" },
  { value: "ACTIVE", label: "Active" },
  { value: "COMPLETED", label: "Completed" },
  { value: "ARCHIVED", label: "Archived" },
];
