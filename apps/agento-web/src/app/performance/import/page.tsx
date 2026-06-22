"use client";

import { useRef, useState } from "react";
import Link from "next/link";
import { performanceApi } from "@/lib/api";

const CSV_EXAMPLE = `generatedContentId,channel,publishedAt,impressions,views,clicks,likes,comments,shares,orders,revenue,cost,notes
550e8400-e29b-41d4-a716-446655440000,tiktok,2024-01-15T12:00:00,10000,8500,250,1200,45,120,15,3750.00,500.00,TikTok Jan
550e8400-e29b-41d4-a716-446655440001,shopee,2024-01-20,5000,4200,180,320,20,60,22,5500.00,300.00,Shopee flash sale`;

export default function CsvImportPage() {
  const fileRef = useRef<HTMLInputElement>(null);
  const [file, setFile] = useState<File | null>(null);
  const [importing, setImporting] = useState(false);
  const [result, setResult] = useState<{ imported: number } | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleImport = async () => {
    if (!file) return;
    setImporting(true);
    setError(null);
    setResult(null);
    try {
      const res = await performanceApi.importCsv(file);
      if (res.success) {
        setResult(res.data);
      } else {
        setError(res.message || "Import failed");
      }
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Import failed");
    } finally {
      setImporting(false);
    }
  };

  return (
    <div style={{ padding: 32, maxWidth: 760, margin: "0 auto" }}>
      <div style={{ marginBottom: 20 }}>
        <Link
          href="/performance"
          style={{ color: "#3f3f46", fontSize: 13, textDecoration: "none" }}
        >
          ← Back to Performance
        </Link>
      </div>

      <h1 style={{ margin: "0 0 8px", fontSize: 22, fontWeight: 700 }}>Import CSV</h1>
      <p style={{ margin: "0 0 24px", color: "#6b7280", fontSize: 14 }}>
        Upload a CSV file to bulk-import performance records.
      </p>

      {/* CSV Format Reference */}
      <div
        style={{
          background: "#f8fafc",
          border: "1px solid #e2e8f0",
          borderRadius: 8,
          padding: 20,
          marginBottom: 24,
        }}
      >
        <h3 style={{ margin: "0 0 8px", fontSize: 14, fontWeight: 700 }}>CSV Format</h3>
        <p style={{ margin: "0 0 12px", fontSize: 13, color: "#374151" }}>
          Row 1 must be the header. Each subsequent row is one performance record.
        </p>
        <div
          style={{
            background: "#1e293b",
            color: "#e2e8f0",
            padding: 14,
            borderRadius: 6,
            fontFamily: "monospace",
            fontSize: 11,
            overflowX: "auto",
            whiteSpace: "pre",
          }}
        >
          {CSV_EXAMPLE}
        </div>
        <div style={{ marginTop: 12 }}>
          <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 12 }}>
            <thead>
              <tr style={{ background: "#f1f5f9" }}>
                <th style={{ padding: "6px 10px", textAlign: "left", borderBottom: "1px solid #e2e8f0" }}>Column</th>
                <th style={{ padding: "6px 10px", textAlign: "left", borderBottom: "1px solid #e2e8f0" }}>Type</th>
                <th style={{ padding: "6px 10px", textAlign: "left", borderBottom: "1px solid #e2e8f0" }}>Notes</th>
              </tr>
            </thead>
            <tbody>
              {[
                ["generatedContentId", "UUID", "Required — must match an existing content record"],
                ["channel", "string", "tiktok / shopee / lazada / facebook / reseller"],
                ["publishedAt", "datetime", "ISO format: 2024-01-15T12:00:00 or 2024-01-15"],
                ["impressions", "integer", ""],
                ["views", "integer", ""],
                ["clicks", "integer", ""],
                ["likes", "integer", ""],
                ["comments", "integer", ""],
                ["shares", "integer", ""],
                ["orders", "integer", ""],
                ["revenue", "decimal", "Thai Baht, e.g. 3750.00"],
                ["cost", "decimal", "Optional — ad spend"],
                ["notes", "string", "Optional — free text"],
              ].map(([col, type, note]) => (
                <tr key={col} style={{ borderBottom: "1px solid #f1f5f9" }}>
                  <td style={{ padding: "5px 10px", fontFamily: "monospace", color: "#18181b" }}>{col}</td>
                  <td style={{ padding: "5px 10px", color: "#6b7280" }}>{type}</td>
                  <td style={{ padding: "5px 10px", color: "#374151" }}>{note}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p style={{ margin: "12px 0 0", fontSize: 12, color: "#6b7280" }}>
          Rows with parsing errors are skipped and logged — the import continues.
          engagementRate, conversionRate, and ROAS are computed automatically.
        </p>
      </div>

      {/* Upload Area */}
      <div
        onClick={() => fileRef.current?.click()}
        style={{
          border: "2px dashed #d1d5db",
          borderRadius: 10,
          padding: 40,
          textAlign: "center",
          cursor: "pointer",
          background: file ? "#f0fdf4" : "#f9fafb",
          borderColor: file ? "#86efac" : "#d1d5db",
          marginBottom: 16,
        }}
      >
        <input
          ref={fileRef}
          type="file"
          accept=".csv,text/csv"
          style={{ display: "none" }}
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        />
        {file ? (
          <div>
            <div style={{ fontSize: 24, marginBottom: 8 }}>✓</div>
            <div style={{ fontWeight: 600, color: "#166534" }}>{file.name}</div>
            <div style={{ fontSize: 12, color: "#6b7280", marginTop: 4 }}>
              {(file.size / 1024).toFixed(1)} KB — click to change
            </div>
          </div>
        ) : (
          <div>
            <div style={{ fontSize: 32, marginBottom: 8 }}>📄</div>
            <div style={{ fontWeight: 600, color: "#374151" }}>Click to select CSV file</div>
            <div style={{ fontSize: 13, color: "#9ca3af", marginTop: 4 }}>
              or drag and drop
            </div>
          </div>
        )}
      </div>

      {error && (
        <div
          style={{
            background: "#fef2f2",
            border: "1px solid #fca5a5",
            color: "#b91c1c",
            padding: "10px 14px",
            borderRadius: 6,
            marginBottom: 12,
            fontSize: 13,
          }}
        >
          {error}
        </div>
      )}

      {result && (
        <div
          style={{
            background: "#f0fdf4",
            border: "1px solid #86efac",
            color: "#166534",
            padding: "10px 14px",
            borderRadius: 6,
            marginBottom: 12,
            fontSize: 13,
          }}
        >
          Successfully imported <strong>{result.imported}</strong> records.{" "}
          <Link href="/performance" style={{ color: "#16a34a", fontWeight: 600 }}>
            View performance →
          </Link>
        </div>
      )}

      <div className="flex gap-2">
        <button
          onClick={handleImport}
          disabled={!file || importing}
          className="btn-primary"
        >
          {importing ? "Importing…" : "Import"}
        </button>
        <Link href="/performance" className="btn-secondary">
          Cancel
        </Link>
      </div>
    </div>
  );
}
