"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { productApi } from "@/lib/api";
import type { ProductFact } from "@/types";

export default function ProductsPage() {
  const [products, setProducts] = useState<ProductFact[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    productApi
      .list()
      .then((res) => setProducts(res.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this product?")) return;
    try {
      await productApi.delete(id);
      setProducts((prev) => prev.filter((p) => p.id !== id));
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Delete failed");
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Product Facts</h1>
          <p className="text-gray-500 text-sm mt-1">Verified product specifications used in content generation</p>
        </div>
        <Link href="/products/new" className="btn-primary">
          + New Product
        </Link>
      </div>

      {loading && <p className="text-gray-400">Loading...</p>}
      {error && <p className="text-red-500 text-sm">{error}</p>}

      {!loading && products.length === 0 && (
        <div className="card text-center py-12">
          <p className="text-gray-500 mb-4">No product facts yet.</p>
          <Link href="/products/new" className="btn-primary">
            Add Product Facts
          </Link>
        </div>
      )}

      <div className="space-y-4">
        {products.map((product) => (
          <div key={product.id} className="card">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-3">
                  <h2 className="text-lg font-semibold text-gray-900">{product.productName}</h2>
                  {product.sku && (
                    <span className="badge bg-gray-100 text-gray-600">SKU: {product.sku}</span>
                  )}
                </div>
                <div className="mt-2 grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
                  <div>
                    <span className="text-gray-500">Ply:</span>{" "}
                    <span className="font-medium">{product.ply}-ply</span>
                  </div>
                  <div>
                    <span className="text-gray-500">Sheets/box:</span>{" "}
                    <span className="font-medium">{product.sheetCount}</span>
                  </div>
                  <div>
                    <span className="text-gray-500">Pack size:</span>{" "}
                    <span className="font-medium">{product.packSize} boxes</span>
                  </div>
                  <div>
                    <span className="text-gray-500">Carton:</span>{" "}
                    <span className="font-medium">{product.cartonSize} packs</span>
                  </div>
                </div>
                {product.keyBenefits.length > 0 && (
                  <div className="mt-2 flex flex-wrap gap-1">
                    {product.keyBenefits.map((benefit, i) => (
                      <span key={i} className="badge bg-green-50 text-green-700">
                        {benefit}
                      </span>
                    ))}
                  </div>
                )}
              </div>
              <div className="flex gap-2 ml-4">
                <Link href={`/products/${product.id}`} className="btn-secondary btn-sm">
                  Edit
                </Link>
                <button
                  onClick={() => handleDelete(product.id)}
                  className="btn-danger btn-sm"
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
