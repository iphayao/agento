"use client";

import { signIn } from "next-auth/react";
import { useSearchParams } from "next/navigation";
import { useState } from "react";

export default function LoginPage() {
  const searchParams = useSearchParams();
  const callbackUrl = searchParams.get("callbackUrl") ?? "/";
  const [loading, setLoading] = useState(false);

  async function handleSignIn() {
    setLoading(true);
    await signIn("keycloak", { callbackUrl });
  }

  return (
    <div className="min-h-screen bg-zinc-100 dark:bg-zinc-950 flex items-center justify-center p-6 transition-colors duration-200">
      <div className="w-full max-w-sm">
        {/* Logo + title */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-10 h-10 bg-zinc-900 dark:bg-white rounded-xl mb-5">
            <svg className="w-5 h-5 text-white dark:text-zinc-900" fill="currentColor" viewBox="0 0 24 24">
              <path d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <h1 className="text-xl font-semibold text-zinc-900 dark:text-white mb-1">Welcome back</h1>
          <p className="text-zinc-500 text-sm">Sign in to Agento</p>
        </div>

        {/* Sign-in card */}
        <div className="bg-white border border-zinc-200 dark:bg-zinc-900 dark:border-zinc-800 rounded-xl p-6">
          <button
            onClick={handleSignIn}
            disabled={loading}
            className="w-full py-2.5 bg-zinc-900 text-white dark:bg-white dark:text-zinc-900 text-sm font-semibold rounded-lg hover:bg-zinc-700 dark:hover:bg-zinc-100 active:bg-zinc-800 dark:active:bg-zinc-200 disabled:opacity-40 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <svg className="w-4 h-4 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                Redirecting&hellip;
              </>
            ) : (
              "Continue with Keycloak →"
            )}
          </button>
        </div>

        <p className="text-zinc-400 text-xs text-center mt-6">
          BN Paper Content Management System v1.0
        </p>
      </div>
    </div>
  );
}
