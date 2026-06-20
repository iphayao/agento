'use client';

import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { campaignsApi } from '@/lib/api';
import { CampaignRequest } from '@/types';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Textarea from '@/components/ui/Textarea';
import Link from 'next/link';

const CHANNELS = ['TikTok', 'Facebook', 'Shopee', 'Lazada', 'Reseller'];

export default function NewCampaignPage() {
  const router = useRouter();
  const [form, setForm] = useState<CampaignRequest>({
    name: '',
    objective: '',
    channel: 'TikTok',
    targetAudience: '',
    contentAngle: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const c = await campaignsApi.create(form);
      router.push(`/campaigns/${c.id}`);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Save failed');
      setSaving(false);
    }
  };

  return (
    <div className="max-w-2xl">
      <div className="flex items-center gap-3 mb-6">
        <Link href="/campaigns" className="text-sm text-gray-500 hover:text-gray-700">← Campaigns</Link>
        <h1 className="text-2xl font-bold text-gray-900">New Campaign</h1>
      </div>

      {error && <div className="mb-4 p-3 rounded-md bg-red-50 text-red-700 text-sm">{error}</div>}

      <form onSubmit={handleSubmit} className="bg-white rounded-lg border border-gray-200 p-6 space-y-4">
        <Input
          label="Campaign Name *"
          required
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          placeholder="TikTok June — ฝุ่นน้อย"
        />
        <div className="space-y-1">
          <label className="block text-sm font-medium text-gray-700">Channel *</label>
          <select
            required
            value={form.channel}
            onChange={(e) => setForm({ ...form, channel: e.target.value })}
            className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
          >
            {CHANNELS.map((ch) => <option key={ch}>{ch}</option>)}
          </select>
        </div>
        <Textarea
          label="Objective"
          value={form.objective ?? ''}
          onChange={(e) => setForm({ ...form, objective: e.target.value })}
          placeholder="Increase awareness of low-dust positioning among office buyers"
          rows={2}
        />
        <Textarea
          label="Target Audience"
          value={form.targetAudience ?? ''}
          onChange={(e) => setForm({ ...form, targetAudience: e.target.value })}
          placeholder="ผู้หญิง Gen Y ที่ซื้อของใช้ในบ้าน"
          rows={2}
        />
        <Textarea
          label="Content Angle"
          value={form.contentAngle ?? ''}
          onChange={(e) => setForm({ ...form, contentAngle: e.target.value })}
          placeholder="ทิชชู่ฝุ่นน้อย ต่างจากยี่ห้ออื่นอย่างไร"
          rows={2}
        />
        <div className="flex justify-end gap-3 pt-2">
          <Link href="/campaigns"><Button variant="secondary" type="button">Cancel</Button></Link>
          <Button type="submit" loading={saving}>Create Campaign</Button>
        </div>
      </form>
    </div>
  );
}
