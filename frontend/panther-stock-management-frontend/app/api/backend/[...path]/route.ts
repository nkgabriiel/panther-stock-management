import { NextRequest, NextResponse } from "next/server";

const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";
const BACKEND_API_KEY = process.env.BACKEND_API_KEY ?? "";

async function forward(request: NextRequest, path: string[]) {
  const targetUrl = `${BACKEND_URL}/api/${path.join("/")}${request.nextUrl.search}`;

  const init: RequestInit = {
    method: request.method,
    headers: {
      "Content-Type": "application/json",
      "X-API-Key": BACKEND_API_KEY,
    },
  };

  if (request.method !== "GET" && request.method !== "HEAD") {
    const body = await request.text();
    if (body) {
      init.body = body;
    }
  }

  const response = await fetch(targetUrl, init);
  const text = await response.text();

  return new NextResponse(text, {
    status: response.status,
    headers: {
      "Content-Type": response.headers.get("Content-Type") ?? "application/json",
    },
  });
}

type RouteContext = { params: Promise<{ path: string[] }> };

export async function GET(request: NextRequest, ctx: RouteContext) {
  const { path } = await ctx.params;
  return forward(request, path);
}

export async function POST(request: NextRequest, ctx: RouteContext) {
  const { path } = await ctx.params;
  return forward(request, path);
}

export async function PATCH(request: NextRequest, ctx: RouteContext) {
  const { path } = await ctx.params;
  return forward(request, path);
}

export async function PUT(request: NextRequest, ctx: RouteContext) {
  const { path } = await ctx.params;
  return forward(request, path);
}

export async function DELETE(request: NextRequest, ctx: RouteContext) {
  const { path } = await ctx.params;
  return forward(request, path);
}
