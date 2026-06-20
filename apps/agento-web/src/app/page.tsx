"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { brandApi, campaignApi, contentApi, productApi } from "@/lib/api";

interface Stats {
  brands: number;
  products: number;
  campaigns: number;
  content: number;
  drafts: number;
  approved: number;
}

export default function DashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      brandApi.list(),
      productApi.list(),
      campaignApi.list(),
      contentApi.list(),
    ])
      .then(([brands, products, campaigns, content]) => {
        const drafts = content.data.filter((c) => c.status === "DRAFT").length;
        const approved = content.data.filter((c) => c.status === "APPROVED").length;
        setStats({
          brands: brands.data.length,
          products: products.data.length,
          campaigns: campaigns.data.length,
          content: content.data.length,
          drafts,
          approved,
        });
      })
      .catch((e) => setError(e.message));
  }, []);

  const statCards = stats
    ? [
        { label: "Brand Profiles", value: stats.brands, href: "/brand", color: "blue" },
        { label: "Product Facts", value: stats.products, href: "/products", color: "purple" },
        { label: "Campaigns", value: stats.campaigns, href: "/campaigns", color: "indigo" },
        { label: "Total Content", value: stats.content, href: "/content", color: "gray" },
        { label: "Awaiting Review", value: stats.drafts, href: "/content", color: "yellow" },
        { label: "Approved", value: stats.approved, href: "/content", color: "green" },
      ]
    : [];

  return (
    <div className="p-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-500 mt-1">SoClean content system overview</p>
      </div>

      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
          Could not connect to API: {error}. Make sure agento-api is running on port 8080.
        </div>
      )}

      {/* Stats grid */}
      {stats ? (
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-10">
          {statCards.map((card) => (
            <Link key={card.label} href={card.href}>
              <div className="card hover:shadow-md transition-shadow cursor-pointer">
                <div className="text-3xl font-bold text-blue-700">{card.value}</div>
                <div className="text-sm text-gray-600 mt-1">{card.label}</div>
              </div>
            </Link>
          ))}
        </div>
      ) : !error ? (
        <div className="text-gray-400 text-sm mb-10">Loading stats...</div>
      ) : null}

      {/* Quick actions */}
      <div className="mb-8">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Quick Actions</h2>
        <div className="flex flex-wrap gap-3">
          <Link href="/brand/new" className="btn-primary btn-sm">
            + New Brand Profile
          </Link>
          <Link href="/products/new" className="btn-primary btn-sm">
            + New Product
          </Link>
          <Link href="/campaigns/new" className="btn-primary btn-sm">
            + New Campaign
          </Link>
          <Link href="/content" className="btn-secondary btn-sm">
            Review Content
          </Link>
        </div>
      </div>

      {/* Getting started */}
      {stats && stats.brands === 0 && (
        <div className="card border-blue-200 bg-blue-50">
          <h3 className="font-semibold text-blue-900 mb-2">Get Started</h3>
          <ol className="text-sm text-blue-800 space-y-1 list-decimal list-inside">
            <li>
              <Link href="/brand/new" className="underline">Create a brand profile</Link> with SoClean brand details
            </li>
            <li>
              <Link href="/products/new" className="underline">Add product facts</Link> (sheets, ply, benefits)
            </li>
            <li>
              <Link href="/campaigns/new" className="underline">Create a campaign</Link> with objective and content angle
            </li>
            <li>Open the campaign and click <strong>Generate Content</strong></li>
            <li>Review and approve the generated content</li>
          </ol>
        </div>
      )}
    </div>
  );
}
