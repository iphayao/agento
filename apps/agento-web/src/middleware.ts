import { NextRequest, NextResponse } from "next/server";

const TOKEN_KEY = "agento_jwt";

function isTokenValid(token: string | undefined): boolean {
  if (!token) return false;
  try {
    const [, payload] = token.split(".");
    if (!payload) return false;
    const decoded = JSON.parse(atob(payload));
    return decoded.exp * 1000 > Date.now();
  } catch {
    return false;
  }
}

export function middleware(request: NextRequest) {
  const pathname = request.nextUrl.pathname;

  // Allow login page and API routes without auth
  if (pathname === "/login" || pathname.startsWith("/api/")) {
    return NextResponse.next();
  }

  // Check for token in cookies
  const token = request.cookies.get(TOKEN_KEY)?.value;

  // If no valid token, redirect to login
  if (!isTokenValid(token)) {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    "/((?!_next/static|_next/image|favicon.ico).*)",
  ],
};
