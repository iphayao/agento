"use client";

import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { productApi } from "@/lib/api";
import type { ProductFact, ProductFactRequest } from "@/types";
import ProductForm from "../ProductForm";

export default function EditProductPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [product, setProduct] = useState<ProductFact | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    productApi
      .get(id)
      .then((res) => setProduct(res.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSubmit = async (data: ProductFactRequest) => {
    setError(null);
    try {
      await productApi.update(id, data);
      router.push("/products");
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Save failed");
    }
  };

  if (loading) return <div className="p-8 text-gray-400">Loading...</div>;
  if (error) return <div className="p-8 text-red-500">{error}</div>;
  if (!product) return null;

  return (
    <div className="p-8 max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Edit Product</h1>
      <ProductForm defaultValues={product} onSubmit={handleSubmit} />
    </div>
  );
}
