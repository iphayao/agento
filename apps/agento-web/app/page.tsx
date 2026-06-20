'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { brandApi, campaignsApi, contentApi, productsApi } from '@/lib/api';

interface Stats {
  hasBrand: boolean;
  productCount: number;
  campaignCount: number;
  draftCount: number;
  approvedCount: number;
}

export default function DashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      brandApi.get(),
      productsApi.list(),
      campaignsApi.list(),
      contentApi.list(),
    ])
      .then(([brand, products, campaigns, content]) => {
        setStats({
          hasBrand: brand !== null,
          productCount: products.length,
          campaignCount: campaigns.length,
          draftCount: content.filter((c) => c.status === 'DRAFT').length,
          approvedCount: content.filter((c) => c.status === 'APPROVED').length,
        });
      })
      .catch(() => setError('Could not connect to API. Is the backend running?'));
  }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Dashboard</h1>
      <p className="text-sm text-gray-500 mb-6">Agento — SoClean Content System</p>

      {error && (
        <div className="mb-6 bg-red-50 border border-red-200 rounded-md p-4 text-sm text-red-700">
          {error}
        </div>
      )}

      {stats && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          <StatCard label="Brand Profile" value={stats.hasBrand ? '✓ Set up' : '— Not set'} href="/brand" />
          <StatCard label="Products" value={String(stats.productCount)} href="/products" />
          <StatCard label="Campaigns" value={String(stats.campaignCount)} href="/campaigns" />
          <StatCard label="Drafts to Review" value={String(stats.draftCount)} href="/content" />
        </div>
      )}

      <div className="bg-white rounded-lg border border-gray-200 p-5">
        <h2 className="text-sm font-semibold text-gray-700 mb-3">Quick Start</h2>
        <ol className="space-y-2 text-sm text-gray-600 list-decimal list-inside">
          <li><Link href="/brand" className="text-brand-600 hover:underline">Set up brand profile</Link> — name, slogan, tone, target audience</li>
          <li><Link href="/products" className="text-brand-600 hover:underline">Add a product fact</Link> — SoClean tissue specs + key benefits</li>
          <li><Link href="/campaigns/new" className="text-brand-600 hover:underline">Create a campaign</Link> — choose TikTok, set objective and content angle</li>
          <li>Open the campaign and click <strong>Generate Content</strong></li>
          <li><Link href="/content" className="text-brand-600 hover:underline">Review generated content</Link> — approve or reject each draft</li>
        </ol>
      </div>
    </div>
  );
}

function StatCard({ label, value, href }: { label: string; value: string; href: string }) {
  return (
    <Link href={href} className="bg-white rounded-lg border border-gray-200 p-4 hover:border-brand-300 transition-colors">
      <div className="text-xs text-gray-500 mb-1">{label}</div>
      <div className="text-lg font-semibold text-gray-900">{value}</div>
    </Link>
  );
}
