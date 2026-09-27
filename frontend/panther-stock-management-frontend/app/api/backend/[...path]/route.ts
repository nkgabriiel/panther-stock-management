import { NextRequest, NextResponse } from "next/server";

const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";
const BACKEND_API_KEY = process.env.BACKEND_API_KEY ?? "";

async function forward(request: NextRequest, path: string[]) {
  const targetUrl = `${BACKEND_URL}/api/${path.join("/")}${request.nextUrl.search}`;

  const headers = new Headers();
  const incomingContentType = request.headers.get("content-type");
  if (incomingContentType) {
    headers.set("Content-Type", incomingContentType);
  }
  headers.set("X-API-Key", BACKEND_API_KEY);

  const init: RequestInit = {
    method: request.method,
    headers,
  };

  if (request.method !== "GET" && request.method !== "HEAD") {
    const bodyBuffer = await request.arrayBuffer();
    if (bodyBuffer.byteLength > 0) {
      init.body = bodyBuffer;
    }
  }

  const response = await fetch(targetUrl, init);
  const bodyBuffer = await response.arrayBuffer();

  const responseHeaders = new Headers();
  const contentType = response.headers.get("Content-Type");
  if (contentType) {
    responseHeaders.set("Content-Type", contentType);
  }
  const contentDisposition = response.headers.get("Content-Disposition");
  if (contentDisposition) {
    responseHeaders.set("Content-Disposition", contentDisposition);
  }

  // Responses with these statuses must not have a body (the Fetch API throws
  // if you try to construct one with a body, even an empty one).
  const isNullBodyStatus = response.status === 204 || response.status === 205 || response.status === 304;

  return new NextResponse(isNullBodyStatus ? null : bodyBuffer, {
    status: response.status,
    headers: responseHeaders,
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
