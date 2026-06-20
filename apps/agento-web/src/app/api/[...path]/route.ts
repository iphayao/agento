/**
 * Catch-all proxy that forwards browser requests to the Spring Boot API.
 *
 * The API_KEY env var is server-side only (no NEXT_PUBLIC_ prefix), so it
 * never appears in the browser bundle. The browser calls /api/* and this
 * handler injects the key and forwards to the backend.
 */
import { type NextRequest, NextResponse } from "next/server";

const API_URL = process.env.API_URL ?? "http://localhost:8080/api";
const API_KEY = process.env.API_KEY ?? "";

const METHODS_WITH_BODY = new Set(["POST", "PUT", "PATCH"]);

async function proxyRequest(
  request: NextRequest,
  { params }: { params: { path: string[] } }
): Promise<NextResponse> {
  const pathStr = params.path.join("/");
  const { searchParams } = new URL(request.url);
  const qs = searchParams.toString();
  const target = `${API_URL}/${pathStr}${qs ? `?${qs}` : ""}`;

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };
  if (API_KEY) {
    headers["X-Api-Key"] = API_KEY;
  }

  const init: RequestInit = { method: request.method, headers };
  if (METHODS_WITH_BODY.has(request.method)) {
    init.body = await request.text();
  }

  try {
    const res = await fetch(target, init);
    const data = await res.json().catch(() => null);
    return NextResponse.json(data, { status: res.status });
  } catch {
    return NextResponse.json(
      { success: false, message: "Failed to reach the API server", data: null },
      { status: 502 }
    );
  }
}

export const GET = proxyRequest;
export const POST = proxyRequest;
export const PUT = proxyRequest;
export const PATCH = proxyRequest;
export const DELETE = proxyRequest;
