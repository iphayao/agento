import type { Metadata } from "next";
import Link from "next/link";
import "./globals.css";

export const metadata: Metadata = {
  title: "Agento — SoClean Content System",
  description: "AI-powered marketing content system for SoClean by BN Paper",
};

const navLinks = [
  { href: "/", label: "Dashboard" },
  { href: "/brand", label: "Brand Profile" },
  { href: "/products", label: "Products" },
  { href: "/campaigns", label: "Campaigns" },
  { href: "/content", label: "Content Review" },
];

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="th">
      <body>
        <div className="min-h-screen flex">
          {/* Sidebar */}
          <aside className="w-56 bg-white border-r border-gray-200 flex flex-col shrink-0">
            <div className="p-4 border-b border-gray-200">
              <div className="font-bold text-blue-700 text-lg">Agento</div>
              <div className="text-xs text-gray-500">SoClean Content System</div>
            </div>
            <nav className="flex-1 p-3 space-y-1">
              {navLinks.map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  className="flex items-center px-3 py-2 text-sm rounded-md text-gray-700 hover:bg-gray-100 hover:text-blue-700 transition-colors"
                >
                  {link.label}
                </Link>
              ))}
            </nav>
            <div className="p-3 border-t border-gray-200">
              <div className="text-xs text-gray-400">Phase 01 — MVP</div>
            </div>
          </aside>

          {/* Main content */}
          <main className="flex-1 overflow-auto">
            {children}
          </main>
        </div>
      </body>
    </html>
  );
}
