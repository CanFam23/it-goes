// Next.js proxy so i dont have to hardcode localhost in all api requests
const BACKEND_URL = process.env.BACKEND_URL || "http://localhost:8080";

module.exports = {
  async rewrites() {
    return [{source: "/api/:path*", destination: `${BACKEND_URL}/api/:path*`}];
  },
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'it-goes.s3.us-east-2.amazonaws.com',
        pathname: '/**', // allow all paths under bucket
      },
    ],
  },
};