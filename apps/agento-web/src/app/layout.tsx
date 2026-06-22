"use client";

import { useEffect, useState } from "react";
import { usePathname } from "next/navigation";
import Link from "next/link";
import { getUsername, logout } from "@/lib/auth";
import "./globals.css";

const navLinks = [
  { href: "/", label: "Dashboard", icon: "grid" },
  { href: "/brand", label: "Brand Profile", icon: "bookmark" },
  { href: "/products", label: "Products", icon: "package" },
  { href: "/campaigns", label: "Campaigns", icon: "megaphone" },
  { href: "/calendars", label: "Content Calendar", icon: "calendar" },
  { href: "/content", label: "Content Review", icon: "eye" },
  { href: "/knowledge", label: "Knowledge Base", icon: "lightbulb" },
  { href: "/performance", label: "Performance", icon: "chart" },
  { href: "/exports", label: "Export", icon: "download" },
];

const iconMap: { [key: string]: JSX.Element } = {
  grid: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h4a2 2 0 012 2v4a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h4a2 2 0 012 2v4a2 2 0 01-2 2h-4a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h4a2 2 0 012 2v4a2 2 0 01-2 2H6a2 2 0 01-2-2v-4zM14 16a2 2 0 012-2h4a2 2 0 012 2v4a2 2 0 01-2 2h-4a2 2 0 01-2-2v-4z" />
    </svg>
  ),
  bookmark: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h6a2 2 0 012 2v14l-5-2.5L5 19V5z" />
    </svg>
  ),
  package: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 3l8 4-8 4-8-4zm8 4v10l-8 4-8-4V7m8 14V11" />
    </svg>
  ),
  megaphone: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
    </svg>
  ),
  calendar: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
    </svg>
  ),
  eye: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
    </svg>
  ),
  lightbulb: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
    </svg>
  ),
  chart: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
    </svg>
  ),
  download: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
    </svg>
  ),
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const isLoginPage = pathname === "/login";
  const [username, setUsername] = useState<string | null>(null);
  const [collapsed, setCollapsed] = useState(false);
  const [dark, setDark] = useState(true);

  useEffect(() => {
    setUsername(getUsername());
    const saved = localStorage.getItem("theme");
    const isDark = saved ? saved === "dark" : true;
    setDark(isDark);
    document.documentElement.classList.toggle("dark", isDark);
  }, []);

  function toggleTheme() {
    const next = !dark;
    setDark(next);
    localStorage.setItem("theme", next ? "dark" : "light");
    document.documentElement.classList.toggle("dark", next);
  }

  return (
    <html lang="th">
      <body>
        <div className="min-h-screen flex">
          {/* Sidebar */}
          {!isLoginPage && (
            <aside className={`${collapsed ? "w-16" : "w-64"} bg-white border-r border-zinc-200 dark:bg-zinc-900 dark:border-zinc-800 flex flex-col shrink-0 transition-[width] duration-200 overflow-hidden`}>

              {/* Workspace header */}
              <div className={`flex items-center h-14 border-b border-zinc-200 dark:border-zinc-800 shrink-0 ${collapsed ? "justify-center" : "justify-between px-3"}`}>
                {!collapsed && (
                  <div className="flex items-center gap-2 min-w-0">
                    <div className="w-5 h-5 bg-zinc-900 dark:bg-white rounded flex items-center justify-center shrink-0">
                      <svg className="w-3 h-3 text-white dark:text-zinc-900" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M13 10V3L4 14h7v7l9-11h-7z" />
                      </svg>
                    </div>
                    <span className="text-sm font-semibold text-zinc-900 dark:text-white truncate">Agento</span>
                    <svg className="w-3.5 h-3.5 text-zinc-400 dark:text-zinc-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </div>
                )}
                <button
                  onClick={() => setCollapsed(!collapsed)}
                  className="p-1.5 text-zinc-400 hover:text-zinc-700 hover:bg-zinc-100 dark:text-zinc-500 dark:hover:text-zinc-200 dark:hover:bg-zinc-800 rounded-md transition-colors shrink-0"
                  title={collapsed ? "Expand sidebar" : "Collapse sidebar"}
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    {collapsed ? (
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 5l7 7-7 7" />
                    ) : (
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M15 19l-7-7 7-7" />
                    )}
                  </svg>
                </button>
              </div>

              {/* Nav links */}
              <nav className="flex-1 px-2 py-3 space-y-0.5 overflow-y-auto">
                {navLinks.map((link) => {
                  const isActive = link.href === "/"
                    ? pathname === "/"
                    : pathname.startsWith(link.href);
                  return (
                    <Link
                      key={link.href}
                      href={link.href}
                      title={collapsed ? link.label : undefined}
                      className={`group flex items-center ${collapsed ? "justify-center px-2" : "gap-3 px-3"} py-2 text-sm rounded-lg transition-all duration-100 ${
                        isActive
                          ? "bg-zinc-100 text-zinc-900 dark:bg-zinc-800 dark:text-white"
                          : "text-zinc-600 hover:bg-zinc-50 hover:text-zinc-900 dark:text-zinc-400 dark:hover:bg-zinc-800/60 dark:hover:text-zinc-100"
                      }`}
                    >
                      <span className={`flex-shrink-0 transition-colors ${
                        isActive
                          ? "text-zinc-700 dark:text-zinc-200"
                          : "text-zinc-400 group-hover:text-zinc-600 dark:text-zinc-500 dark:group-hover:text-zinc-300"
                      }`}>
                        {iconMap[link.icon]}
                      </span>
                      {!collapsed && link.label}
                    </Link>
                  );
                })}
              </nav>

              {/* Footer */}
              <div className="px-2 pb-3 pt-2 border-t border-zinc-200 dark:border-zinc-800 space-y-0.5 shrink-0">
                {/* Theme toggle */}
                <button
                  onClick={toggleTheme}
                  title={dark ? "Switch to light mode" : "Switch to dark mode"}
                  className={`w-full flex items-center ${collapsed ? "justify-center px-2" : "gap-3 px-3"} py-2 text-sm rounded-lg text-zinc-500 hover:bg-zinc-50 hover:text-zinc-700 dark:text-zinc-400 dark:hover:bg-zinc-800/60 dark:hover:text-zinc-100 transition-all duration-100`}
                >
                  {dark ? (
                    /* Sun — switch to light */
                    <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                    </svg>
                  ) : (
                    /* Moon — switch to dark */
                    <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
                    </svg>
                  )}
                  {!collapsed && (dark ? "Light mode" : "Dark mode")}
                </button>

                {/* User */}
                {username && (
                  <div className={`flex items-center ${collapsed ? "justify-center px-2" : "gap-3 px-3"} py-2 rounded-lg`}>
                    <div
                      className="w-6 h-6 rounded-full bg-zinc-200 dark:bg-zinc-700 flex items-center justify-center shrink-0"
                      title={collapsed ? username : undefined}
                    >
                      <span className="text-xs text-zinc-700 dark:text-zinc-300 font-semibold">{username.charAt(0).toUpperCase()}</span>
                    </div>
                    {!collapsed && <span className="text-sm text-zinc-500 dark:text-zinc-400 truncate">{username}</span>}
                  </div>
                )}

                {/* Sign out */}
                <button
                  onClick={logout}
                  title={collapsed ? "Sign out" : undefined}
                  className={`w-full flex items-center ${collapsed ? "justify-center px-2" : "gap-3 px-3"} py-2 text-sm rounded-lg text-zinc-500 hover:bg-zinc-50 hover:text-zinc-700 dark:text-zinc-400 dark:hover:bg-zinc-800/60 dark:hover:text-zinc-100 transition-all duration-100`}
                >
                  <svg className="w-5 h-5 shrink-0 text-zinc-400 dark:text-zinc-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  {!collapsed && "Sign out"}
                </button>
              </div>
            </aside>
          )}

          {/* Main content */}
          <main className="flex-1 overflow-auto">
            {children}
          </main>
        </div>
      </body>
    </html>
  );
}
