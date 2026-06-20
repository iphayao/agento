'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { campaignsApi, contentApi } from '@/lib/api';
import { Campaign, GeneratedContent } from '@/types';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';

export default function CampaignDetailPage() {
  const params = useParams();
  const id = Number(params.id);

  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [content, setContent] = useState<GeneratedContent[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [contentType, setContentType] = useState('tiktok_caption');
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const load = () =>
    Promise.all([
      campaignsApi.get(id),
      contentApi.list(id),
    ])
      .then(([c, ct]) => { setCampaign(c); setContent(ct); })
      .catch(() => setMessage({ type: 'error', text: 'Failed to load campaign' }))
      .finally(() => setLoading(false));

  useEffect(() => { load(); }, [id]);

  const handleGenerate = async () => {
    setGenerating(true);
    setMessage(null);
    try {
      const generated = await campaignsApi.generate(id, { contentType, promptVersion: 'v1' });
      setContent((prev) => [generated, ...prev]);
      setMessage({ type: 'success', text: 'Content generated! Review below.' });
    } catch (err: unknown) {
      setMessage({ type: 'error', text: err instanceof Error ? err.message : 'Generation failed' });
    } finally {
      setGenerating(false);
    }
  };

  const handleApprove = async (contentId: number) => {
    const updated = await contentApi.approve(contentId);
    setContent((prev) => prev.map((c) => c.id === contentId ? updated : c));
  };

  const handleReject = async (contentId: number) => {
    const updated = await contentApi.reject(contentId);
    setContent((prev) => prev.map((c) => c.id === contentId ? updated : c));
  };

  if (loading) return <div className="text-sm text-gray-500">Loading...</div>;
  if (!campaign) return <div className="text-sm text-red-600">Campaign not found</div>;

  return (
    <div>
      <div className="flex items-center gap-3 mb-1">
        <Link href="/campaigns" className="text-sm text-gray-500 hover:text-gray-700">← Campaigns</Link>
      </div>
      <div className="flex items-start justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{campaign.name}</h1>
          <div className="flex items-center gap-2 mt-1">
            <Badge status={campaign.status} />
            <span className="text-sm text-gray-500">{campaign.channel}</span>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <select
            value={contentType}
            onChange={(e) => setContentType(e.target.value)}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none"
          >
            <option value="tiktok_caption">TikTok Caption</option>
            <option value="tiktok_script">TikTok Script</option>
            <option value="facebook_post">Facebook Post</option>
            <option value="shopee_description">Shopee Description</option>
          </select>
          <Button onClick={handleGenerate} loading={generating}>
            Generate Content
          </Button>
        </div>
      </div>

      {campaign.objective && (
        <div className="bg-white rounded-lg border border-gray-200 p-4 mb-6 grid grid-cols-2 gap-4 text-sm">
          {campaign.objective && <div><span className="text-gray-500">Objective: </span>{campaign.objective}</div>}
          {campaign.targetAudience && <div><span className="text-gray-500">Audience: </span>{campaign.targetAudience}</div>}
          {campaign.contentAngle && <div className="col-span-2"><span className="text-gray-500">Angle: </span><em>{campaign.contentAngle}</em></div>}
        </div>
      )}

      {message && (
        <div className={`mb-4 p-3 rounded-md text-sm ${message.type === 'success' ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'}`}>
          {message.text}
        </div>
      )}

      <h2 className="text-base font-semibold text-gray-800 mb-3">Generated Content ({content.length})</h2>

      {content.length === 0 ? (
        <div className="bg-white rounded-lg border border-dashed border-gray-300 p-8 text-center">
          <p className="text-gray-500 text-sm">No content generated yet. Click <strong>Generate Content</strong> above.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {content.map((c) => (
            <ContentCard key={c.id} content={c} onApprove={handleApprove} onReject={handleReject} />
          ))}
        </div>
      )}
    </div>
  );
}

function ContentCard({
  content,
  onApprove,
  onReject,
}: {
  content: GeneratedContent;
  onApprove: (id: number) => Promise<void>;
  onReject: (id: number) => Promise<void>;
}) {
  const [loading, setLoading] = useState<'approve' | 'reject' | null>(null);

  const act = async (action: 'approve' | 'reject') => {
    setLoading(action);
    try {
      action === 'approve' ? await onApprove(content.id) : await onReject(content.id);
    } finally {
      setLoading(null);
    }
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-5">
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center gap-2">
          <Badge status={content.status} />
          <span className="text-xs text-gray-500">{content.contentType} · {content.aiModel}</span>
        </div>
        <span className="text-xs text-gray-400">{new Date(content.createdAt).toLocaleString('th-TH')}</span>
      </div>

      {content.title && <div className="font-semibold text-gray-900 mb-2">{content.title}</div>}
      {content.hook && <div className="text-sm text-brand-700 mb-2 italic">{content.hook}</div>}
      {content.body && <div className="text-sm text-gray-700 mb-3 whitespace-pre-wrap">{content.body}</div>}
      {content.callToAction && (
        <div className="text-sm font-medium text-gray-800 mb-2">CTA: {content.callToAction}</div>
      )}
      {content.hashtags && <div className="text-xs text-blue-600 mb-3">{content.hashtags}</div>}

      {content.complianceNotes && (
        <div className="bg-yellow-50 border border-yellow-200 rounded p-2 text-xs text-yellow-800 mb-3">
          ⚠️ {content.complianceNotes}
        </div>
      )}

      {content.status === 'DRAFT' && (
        <div className="flex gap-2 pt-2 border-t border-gray-100">
          <Button variant="success" className="text-xs" loading={loading === 'approve'} onClick={() => act('approve')}>
            Approve
          </Button>
          <Button variant="danger" className="text-xs" loading={loading === 'reject'} onClick={() => act('reject')}>
            Reject
          </Button>
        </div>
      )}
    </div>
  );
}
