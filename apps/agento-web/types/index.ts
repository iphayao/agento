export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error?: string;
}

export interface BrandProfile {
  id: number;
  brandName: string;
  slogan: string | null;
  toneOfVoice: string | null;
  targetAudience: string | null;
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
  keyMessages?: string[];
  prohibitedClaims?: string[];
}

export interface ProductFact {
  id: number;
  productName: string;
  sku: string | null;
  sheetCount: number | null;
  ply: number | null;
  packSize: number | null;
  cartonSize: number | null;
  keyBenefits: string[];
  proofPoints: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ProductFactRequest {
  productName: string;
  sku?: string;
  sheetCount?: number;
  ply?: number;
  packSize?: number;
  cartonSize?: number;
  keyBenefits?: string[];
  proofPoints?: string[];
}

export interface Campaign {
  id: number;
  name: string;
  objective: string | null;
  channel: string;
  targetAudience: string | null;
  contentAngle: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface CampaignRequest {
  name: string;
  objective?: string;
  channel: string;
  targetAudience?: string;
  contentAngle?: string;
  status?: string;
}

export type ContentStatus = 'DRAFT' | 'APPROVED' | 'REJECTED';

export interface GeneratedContent {
  id: number;
  campaignId: number;
  contentType: string;
  channel: string;
  title: string | null;
  body: string | null;
  hook: string | null;
  callToAction: string | null;
  hashtags: string | null;
  status: ContentStatus;
  aiModel: string | null;
  promptVersion: string | null;
  complianceNotes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface GenerateRequest {
  contentType?: string;
  promptVersion?: string;
}
