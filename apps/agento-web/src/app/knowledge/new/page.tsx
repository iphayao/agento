"use client";

import Link from "next/link";
import KnowledgeForm from "../KnowledgeForm";

export default function NewKnowledgePage() {
  return (
    <div className="p-8 max-w-3xl">
      <div className="mb-6">
        <Link href="/knowledge" className="text-sm text-gray-500 hover:text-gray-700">
          ← Knowledge Base
        </Link>
        <h1 className="text-2xl font-bold text-gray-900 mt-2">Add Knowledge Document</h1>
        <p className="text-gray-500 text-sm mt-1">
          Add brand memory that AI agents will retrieve during content generation.
        </p>
      </div>

      <div className="card">
        <KnowledgeForm />
      </div>
    </div>
  );
}
