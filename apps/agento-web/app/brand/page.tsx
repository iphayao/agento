'use client';

import { useEffect, useState } from 'react';
import { brandApi } from '@/lib/api';
import { BrandProfile, BrandProfileRequest } from '@/types';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Textarea from '@/components/ui/Textarea';

export default function BrandPage() {
  const [profile, setProfile] = useState<BrandProfile | null>(null);
  const [form, setForm] = useState<BrandProfileRequest>({
    brandName: '',
    slogan: '',
    toneOfVoice: '',
    targetAudience: '',
    keyMessages: [],
    prohibitedClaims: [],
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  useEffect(() => {
    brandApi.get()
      .then((p) => {
        if (p) {
          setProfile(p);
          setForm({
            brandName: p.brandName,
            slogan: p.slogan ?? '',
            toneOfVoice: p.toneOfVoice ?? '',
            targetAudience: p.targetAudience ?? '',
            keyMessages: p.keyMessages ?? [],
            prohibitedClaims: p.prohibitedClaims ?? [],
          });
        }
      })
      .finally(() => setLoading(false));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setMessage(null);
    try {
      const saved = profile
        ? await brandApi.update(profile.id, form)
        : await brandApi.create(form);
      setProfile(saved);
      setMessage({ type: 'success', text: 'Brand profile saved.' });
    } catch (err: unknown) {
      setMessage({ type: 'error', text: err instanceof Error ? err.message : 'Save failed' });
    } finally {
      setSaving(false);
    }
  };

  const setList = (field: 'keyMessages' | 'prohibitedClaims', value: string) => {
    setForm((f) => ({ ...f, [field]: value.split('\n').map((s) => s.trim()).filter(Boolean) }));
  };

  if (loading) return <div className="text-sm text-gray-500">Loading...</div>;

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Brand Profile</h1>
      <p className="text-sm text-gray-500 mb-6">
        Define the SoClean brand identity used in all AI-generated content.
      </p>

      {message && (
        <div className={`mb-4 p-3 rounded-md text-sm ${
          message.type === 'success' ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
        }`}>
          {message.text}
        </div>
      )}

      <form onSubmit={handleSubmit} className="bg-white rounded-lg border border-gray-200 p-6 space-y-4">
        <Input
          label="Brand Name *"
          required
          value={form.brandName}
          onChange={(e) => setForm({ ...form, brandName: e.target.value })}
          placeholder="SoClean"
        />
        <Input
          label="Slogan"
          value={form.slogan ?? ''}
          onChange={(e) => setForm({ ...form, slogan: e.target.value })}
          placeholder="สะอาด เนียนนุ่ม ฝุ่นน้อย"
        />
        <Textarea
          label="Tone of Voice"
          value={form.toneOfVoice ?? ''}
          onChange={(e) => setForm({ ...form, toneOfVoice: e.target.value })}
          placeholder="Warm, honest, practical. Speaks Thai naturally."
        />
        <Textarea
          label="Target Audience"
          value={form.targetAudience ?? ''}
          onChange={(e) => setForm({ ...form, targetAudience: e.target.value })}
          placeholder="Women Gen Y, households, office buyers, resellers"
        />
        <Textarea
          label="Key Messages (one per line)"
          value={(form.keyMessages ?? []).join('\n')}
          onChange={(e) => setList('keyMessages', e.target.value)}
          placeholder={"เนียนนุ่ม\nฝุ่นน้อย\nคุ้มค่า"}
          rows={5}
        />
        <Textarea
          label="Prohibited Claims (one per line)"
          value={(form.prohibitedClaims ?? []).join('\n')}
          onChange={(e) => setList('prohibitedClaims', e.target.value)}
          placeholder={"100% dust-free\nไร้ฝุ่น 100%\nantibacterial"}
          rows={4}
        />
        <div className="flex justify-end pt-2">
          <Button type="submit" loading={saving}>
            {profile ? 'Update Brand Profile' : 'Save Brand Profile'}
          </Button>
        </div>
      </form>
    </div>
  );
}
