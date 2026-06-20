'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { productsApi } from '@/lib/api';
import { ProductFact } from '@/types';
import Button from '@/components/ui/Button';

export default function ProductsPage() {
  const [products, setProducts] = useState<ProductFact[]>([]);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const load = () =>
    productsApi.list()
      .then(setProducts)
      .finally(() => setLoading(false));

  useEffect(() => { load(); }, []);

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this product fact?')) return;
    setDeletingId(id);
    try {
      await productsApi.delete(id);
      setProducts((prev) => prev.filter((p) => p.id !== id));
    } finally {
      setDeletingId(null);
    }
  };

  if (loading) return <div className="text-sm text-gray-500">Loading...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Products</h1>
          <p className="text-sm text-gray-500 mt-0.5">Product facts used in AI content generation</p>
        </div>
        <Link href="/products/new">
          <Button>+ Add Product</Button>
        </Link>
      </div>

      {products.length === 0 ? (
        <div className="bg-white rounded-lg border border-dashed border-gray-300 p-8 text-center">
          <p className="text-gray-500 text-sm mb-3">No products yet.</p>
          <Link href="/products/new"><Button variant="secondary">Add First Product</Button></Link>
        </div>
      ) : (
        <div className="space-y-3">
          {products.map((p) => (
            <div key={p.id} className="bg-white rounded-lg border border-gray-200 p-4 flex items-start justify-between">
              <div>
                <div className="font-medium text-gray-900">{p.productName}</div>
                {p.sku && <div className="text-xs text-gray-500 mt-0.5">SKU: {p.sku}</div>}
                <div className="text-sm text-gray-600 mt-1">
                  {[p.ply && `${p.ply}-ply`, p.sheetCount && `${p.sheetCount} sheets`, p.packSize && `Pack of ${p.packSize}`]
                    .filter(Boolean).join(' · ')}
                </div>
                {p.keyBenefits.length > 0 && (
                  <div className="flex flex-wrap gap-1 mt-2">
                    {p.keyBenefits.map((b, i) => (
                      <span key={i} className="text-xs bg-blue-50 text-blue-700 px-2 py-0.5 rounded">{b}</span>
                    ))}
                  </div>
                )}
              </div>
              <div className="flex gap-2 shrink-0 ml-4">
                <Link href={`/products/${p.id}`}>
                  <Button variant="secondary" className="text-xs px-3 py-1.5">Edit</Button>
                </Link>
                <Button
                  variant="danger"
                  className="text-xs px-3 py-1.5"
                  loading={deletingId === p.id}
                  onClick={() => handleDelete(p.id)}
                >
                  Delete
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
