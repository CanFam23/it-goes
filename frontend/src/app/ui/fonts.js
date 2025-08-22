import { Inter } from 'next/font/google';
import { Anonymous_Pro } from 'next/font/google'

export const anonymousPro = Anonymous_Pro({
  subsets: ['latin'],
  weight: ['400', '700'],
  style: ['normal', 'italic'],
  fallback: ['system-ui', 'arial'],
  variable: "--font-anon-pro"
});

export const inter = Inter({ subsets: ['latin'],
  weight: ['400','700'],
  fallback: ['system-ui', 'arial'],
  variable: "--font-inter"
 });