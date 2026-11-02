'use server';

import { cookies } from 'next/headers';

/**
 * Helper function to verify logged in users by calling
 * backend api to validate token.
 * 
 * @returns User data or null if auth failed
 */
export async function getCurrentUser() {
  const cookieStore = await cookies();
  const token = cookieStore.get('token');
  
  if (!token) {
    return null;
  }

  try {
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';
    const response = await fetch(`${backendUrl}/api/auth/protected`, {
      headers: {
        'Cookie': `token=${token.value}`
      },
      cache: 'no-store'
    });
    
    if (response.ok) {
      return await response.json();
    }
  } catch (error) {
    console.error('Failed to fetch user:', error);
  }
  
  return null;
}
