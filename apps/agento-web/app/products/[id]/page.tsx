'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { productsApi } from '@/lib/api';
import { ProductFactRequest } from '@/types';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Textarea from '@/components/ui/Textarea';
import Link from 'next/link';

export default function EditProductPage() {
  const router = useRouter();
  const params = useParams();
  const id = Number(params.id);

  const [form, setForm] = useState<ProductFactRequest>({
    productName: '',
    sku: '',
    keyBenefits: [],
    proofPoints: [],
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    productsApi.get(id)
      .then((p) => {
        setForm({
          productName: p.productName,
          sku: p.sku ?? '',
          sheetCount: p.sheetCount ?? undefined,
          ply: p.ply ?? undefined,
          packSize: p.packSize ?? undefined,
          cartonSize: p.cartonSize ?? undefined,
          keyBenefits: p.keyBenefits ?? [],
          proofPoints: p.proofPoints ?? [],
        });
      })
      .catch(() => setError('Product not found'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await productsApi.update(id, form);
      router.push('/products');
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Save failed');
      setSaving(false);
    }
  };

  const setList = (field: 'keyBenefits' | 'proofPoints', value: string) => {
    setForm((f) => ({ ...f, [field]: value.split('\n').map((s) => s.trim()).filter(Boolean) }));
  };

  if (loading) return <div className="text-sm text-gray-500">Loading...</div>;

  return (
    <div className="max-w-2xl">
      <div className="flex items-center gap-3 mb-6">
        <Link href="/products" className="text-sm text-gray-500 hover:text-gray-700">← Products</Link>
        <h1 className="text-2xl font-bold text-gray-900">Edit Product</h1>
      </div>

      {error && <div className="mb-4 p-3 rounded-md bg-red-50 text-red-700 text-sm">{error}</div>}

      <form onSubmit={handleSubmit} className="bg-white rounded-lg border border-gray-200 p-6 space-y-4">
        <Input
          label="Product Name *"
          required
          value={form.productName}
          onChange={(e) => setForm({ ...form, productName: e.target.value })}
        />
        <Input
          label="SKU"
          value={form.sku ?? ''}
          onChange={(e) => setForm({ ...form, sku: e.target.value })}
        />
        <div className="grid grid-cols-2 gap-4">
          <Input label="Sheet Count" type="number" value={form.sheetCount ?? ''}
            onChange={(e) => setForm({ ...form, sheetCount: e.target.value ? Number(e.target.value) : undefined })} />
          <Input label="Ply" type="number" value={form.ply ?? ''}
            onChange={(e) => setForm({ ...form, ply: e.target.value ? Number(e.target.value) : undefined })} />
          <Input label="Pack Size" type="number" value={form.packSize ?? ''}
            onChange={(e) => setForm({ ...form, packSize: e.target.value ? Number(e.target.value) : undefined })} />
          <Input label="Carton Size" type="number" value={form.cartonSize ?? ''}
            onChange={(e) => setForm({ ...form, cartonSize: e.target.value ? Number(e.target.value) : undefined })} />
        </div>
        <Textarea label="Key Benefits (one per line)"
          value={(form.keyBenefits ?? []).join('\n')}
          onChange={(e) => setList('keyBenefits', e.target.value)} rows={4} />
        <Textarea label="Proof Points (one per line)"
          value={(form.proofPoints ?? []).join('\n')}
          onChange={(e) => setList('proofPoints', e.target.value)} rows={4} />
        <div className="flex justify-end gap-3 pt-2">
          <Link href="/products"><Button variant="secondary" type="button">Cancel</Button></Link>
          <Button type="submit" loading={saving}>Save Changes</Button>
        </div>
      </form>
    </div>
  );
}
