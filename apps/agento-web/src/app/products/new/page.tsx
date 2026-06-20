"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { productApi } from "@/lib/api";
import type { ProductFactRequest } from "@/types";
import ProductForm from "../ProductForm";

export default function NewProductPage() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (data: ProductFactRequest) => {
    setError(null);
    try {
      await productApi.create(data);
      router.push("/products");
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Save failed");
    }
  };

  return (
    <div className="p-8 max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">New Product</h1>
      {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
      <ProductForm onSubmit={handleSubmit} />
    </div>
  );
}
