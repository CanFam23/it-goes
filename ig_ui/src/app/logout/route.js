import { cookies } from 'next/headers';
import { NextResponse } from 'next/server';

export async function GET(request) {
  const url = new URL('/', request.url); // absolute URL based on current request
  const res = NextResponse.redirect(url);

  res.cookies.delete('token');

  return res;
}

