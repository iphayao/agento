/**
 * Catch-all proxy that forwards browser requests to the Spring Boot API.
 *
 * Security model:
 * - Browser sends Authorization: Bearer <JWT> for all API calls
 * - This proxy forwards that header to Spring Boot unchanged
 * - Spring Boot validates the JWT and enforces RBAC
 * - LLM/AI API keys are NEVER exposed to the browser (they live in Spring Boot env vars)
 * - X-Correlation-ID is generated here if absent and propagated through the stack
 */
import { type NextRequest, NextResponse } from "next/server";
import { randomUUID } from "crypto";

const API_URL = process.env.API_URL ?? "http://localhost:8080/api";

const METHODS_WITH_BODY = new Set(["POST", "PUT", "PATCH"]);
const CORRELATION_ID_HEADER = "X-Correlation-ID";

async function proxyRequest(
  request: NextRequest,
  { params }: { params: { path: string[] } }
): Promise<NextResponse> {
  const pathStr = params.path.join("/");
  const { searchParams } = new URL(request.url);
  const qs = searchParams.toString();
  const target = `${API_URL}/${pathStr}${qs ? `?${qs}` : ""}`;

  // Generate or propagate correlation ID
  const correlationId =
    request.headers.get(CORRELATION_ID_HEADER) ?? randomUUID();

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    [CORRELATION_ID_HEADER]: correlationId,
  };

  // Forward JWT from browser to Spring Boot
  const authHeader = request.headers.get("Authorization");
  if (authHeader) {
    headers["Authorization"] = authHeader;
  }

  const init: RequestInit = { method: request.method, headers };
  if (METHODS_WITH_BODY.has(request.method)) {
    const body = await request.text();
    if (body) init.body = body;
  }

  try {
    const res = await fetch(target, init);
    const contentType = res.headers.get("content-type") ?? "";

    if (!contentType.includes("application/json")) {
      const buf = await res.arrayBuffer();
      return new NextResponse(buf, {
        status: res.status,
        headers: {
          "Content-Type": contentType || "application/octet-stream",
          "Content-Disposition": res.headers.get("Content-Disposition") ?? "attachment",
          [CORRELATION_ID_HEADER]: correlationId,
        },
      });
    }

    const data = await res.json().catch(() => null);
    const nextRes = NextResponse.json(data, { status: res.status });
    nextRes.headers.set(CORRELATION_ID_HEADER, correlationId);
    return nextRes;
  } catch {
    return NextResponse.json(
      { success: false, message: "Failed to reach the API server", data: null },
      { status: 502 }
    );
  }
}

export const GET    = proxyRequest;
export const POST   = proxyRequest;
export const PUT    = proxyRequest;
export const PATCH  = proxyRequest;
export const DELETE = proxyRequest;
