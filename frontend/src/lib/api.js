const BASE = import.meta.env.VITE_API_URL || "/api";

const isAbsolute = (u) => /^https?:\/\//i.test(u);
const trimSlashEnd = (s) => s.replace(/\/+$/, "");
const trimSlashStart = (s) => s.replace(/^\/+/, "");

function buildUrl(path) {
  const cleanPath = trimSlashStart(path || "");
  if (isAbsolute(BASE)) {
    return `${trimSlashEnd(BASE)}/${cleanPath}`;
  }
  // Relative base like "/api"
  const basePath = `/${trimSlashStart(BASE)}`;
  return `${trimSlashEnd(basePath)}/${cleanPath}`;
}

async function request(method, path, body) {
  const url = buildUrl(path);
  const res = await fetch(url, {
    method,
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await res.text();
  const data = text
    ? (() => {
        try {
          return JSON.parse(text);
        } catch {
          return { message: text };
        }
      })()
    : null;

  if (!res.ok) {
    const err = new Error(data?.message || data?.error || "Request failed");
    err.response = { status: res.status, data };
    throw err;
  }
  return data;
}

export const api = {
  get: (path) => request("GET", path),
  post: (path, body) => request("POST", path, body),
  put: (path, body) => request("PUT", path, body),
  patch: (path, body) => request("PATCH", path, body),
  delete: (path) => request("DELETE", path),
};
