// Auth is now handled by next-auth + Keycloak.
// Use next-auth/react hooks (useSession, signIn, signOut) directly in components.
export { signIn, signOut, useSession } from "next-auth/react";
