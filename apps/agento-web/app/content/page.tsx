'use client';

import { useEffect, useState } from 'react';
import { contentApi } from '@/lib/api';
import { GeneratedContent } from '@/types';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
import Link from 'next/link';

type Filter = 'ALL' | 'DRAFT' | 'APPROVED' | 'REJECTED';

export default function ContentReviewPage() {
  const [content, setContent] = useState<GeneratedContent[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<Filter>('DRAFT');

  useEffect(() => {
    contentApi.list().then(setContent).finally(() => setLoading(false));
  }, []);

  const handleApprove = async (id: number) => {
    const updated = await contentApi.approve(id);
    setContent((prev) => prev.map((c) => c.id === id ? updated : c));
  };

  const handleReject = async (id: number) => {
    const updated = await contentApi.reject(id);
    setContent((prev) => prev.map((c) => c.id === id ? updated : c));
  };

  const filtered = filter === 'ALL' ? content : content.filter((c) => c.status === filter);
  const counts = {
    ALL: content.length,
    DRAFT: content.filter((c) => c.status === 'DRAFT').length,
    APPROVED: content.filter((c) => c.status === 'APPROVED').length,
    REJECTED: content.filter((c) => c.status === 'REJECTED').length,
  };

  if (loading) return <div className="text-sm text-gray-500">Loading...</div>;

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Content Review</h1>
      <p className="text-sm text-gray-500 mb-6">Review and approve AI-generated content across all campaigns</p>

      <div className="flex gap-2 mb-6">
        {(['ALL', 'DRAFT', 'APPROVED', 'REJECTED'] as Filter[]).map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
              filter === f ? 'bg-brand-600 text-white' : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
            }`}
          >
            {f} ({counts[f]})
          </button>
        ))}
      </div>

      {filtered.length === 0 ? (
        <div className="bg-white rounded-lg border border-dashed border-gray-300 p-8 text-center">
          <p className="text-gray-500 text-sm">No {filter.toLowerCase()} content.</p>
          {filter === 'DRAFT' && (
            <p className="text-sm text-gray-400 mt-1">
              Go to a <Link href="/campaigns" className="text-brand-600 hover:underline">campaign</Link> and click Generate Content.
            </p>
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {filtered.map((c) => (
            <ContentReviewCard
              key={c.id}
              content={c}
              onApprove={handleApprove}
              onReject={handleReject}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function ContentReviewCard({
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
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <Badge status={content.status} />
          <span className="text-xs text-gray-500">{content.contentType}</span>
          <Link href={`/campaigns/${content.campaignId}`}
            className="text-xs text-brand-600 hover:underline">
            Campaign #{content.campaignId}
          </Link>
        </div>
        <span className="text-xs text-gray-400">{new Date(content.createdAt).toLocaleString('th-TH')}</span>
      </div>

      {content.title && <div className="font-semibold text-gray-900 mb-2">{content.title}</div>}
      {content.hook && <div className="text-sm text-brand-700 mb-2 italic">{content.hook}</div>}
      {content.body && <div className="text-sm text-gray-700 mb-3 whitespace-pre-wrap">{content.body}</div>}
      {content.callToAction && <div className="text-sm font-medium text-gray-800 mb-2">CTA: {content.callToAction}</div>}
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
