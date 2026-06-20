'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { campaignsApi } from '@/lib/api';
import { Campaign } from '@/types';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';

export default function CampaignsPage() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    campaignsApi.list().then(setCampaigns).finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="text-sm text-gray-500">Loading...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Campaigns</h1>
          <p className="text-sm text-gray-500 mt-0.5">Manage your marketing campaigns</p>
        </div>
        <Link href="/campaigns/new"><Button>+ New Campaign</Button></Link>
      </div>

      {campaigns.length === 0 ? (
        <div className="bg-white rounded-lg border border-dashed border-gray-300 p-8 text-center">
          <p className="text-gray-500 text-sm mb-3">No campaigns yet.</p>
          <Link href="/campaigns/new"><Button variant="secondary">Create First Campaign</Button></Link>
        </div>
      ) : (
        <div className="space-y-3">
          {campaigns.map((c) => (
            <Link key={c.id} href={`/campaigns/${c.id}`}
              className="block bg-white rounded-lg border border-gray-200 p-4 hover:border-brand-300 transition-colors">
              <div className="flex items-start justify-between">
                <div>
                  <div className="font-medium text-gray-900">{c.name}</div>
                  <div className="text-sm text-gray-500 mt-0.5">
                    {c.channel} {c.objective && `· ${c.objective}`}
                  </div>
                  {c.contentAngle && (
                    <div className="text-xs text-gray-400 mt-1 italic">{c.contentAngle}</div>
                  )}
                </div>
                <Badge status={c.status} />
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
