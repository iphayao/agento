package com.bnpaper.agento.security;

public enum Role {
    ADMIN,   // full access: user management, brand, content, knowledge, exports
    EDITOR,  // create/edit brand/content/knowledge, approve/reject, export
    VIEWER   // read-only across all resources
}
