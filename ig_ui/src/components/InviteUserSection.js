"use client";

import { useEffect, useState } from "react";
import { Mail, Copy, Check } from "lucide-react";
import { requestJson } from "@/lib/requestJson.mjs";

export default function InviteUserSection() {
  const [email, setEmail] = useState("");
  const [invite, setInvite] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (!copied) return;
    const timeout = setTimeout(() => setCopied(false), 3000);
    return () => clearTimeout(timeout);
  }, [copied]);

  async function createCode(event) {
    event.preventDefault();
    setError("");
    setInvite(null);
    setCopied(false);
    setLoading(true);
    try {
      const createdInvite = await requestJson("/api/access-codes", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
        credentials: "include",
      });
      setInvite(createdInvite);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function copyCode() {
    try {
      await navigator.clipboard.writeText(invite.code);
      setError("");
      setCopied(true);
    } catch {
      setCopied(false);
      setError("Could not copy the code. Please copy it manually.");
    }
  }

  return (
    <section className="min-w-0 border-2 border-black bg-white p-5">
      <div className="flex items-center gap-3">
        <Mail size={26} strokeWidth={1.7} />
        <div className="min-w-0">
          <h2 className="text-2xl font-black">Invite User</h2>
          <p className="text-sm leading-6">Generate a code to share with someone.</p>
        </div>
      </div>
      <form className="mt-6 flex flex-col gap-3 sm:flex-row" onSubmit={createCode}>
        <label className="sr-only" htmlFor="inviteEmail">Email</label>
        <input
          id="inviteEmail"
          type="email"
          required
          value={email}
          onChange={(event) => { setEmail(event.target.value); setInvite(null); setCopied(false); }}
          placeholder="skier@example.com"
          className="min-w-0 flex-1 border-2 border-black bg-background p-2"
        />
        <button type="submit" disabled={loading} className="btn hover:cursor-pointer disabled:cursor-not-allowed disabled:opacity-50">
          {loading ? "Generating..." : "Generate Code"}
        </button>
      </form>
      {error && <p className="mt-3 text-sm text-red-700" role="alert">{error}</p>}
      {invite && (
        <div className="mt-4" role="status">
          <p className="[overflow-wrap:anywhere]">Share this code with {invite.email}:</p>
          <div className="mt-2 flex flex-wrap items-center gap-3">
            <strong className="font-mono text-xl tracking-widest">{invite.code}</strong>
            <button type="button" onClick={copyCode} aria-label={copied ? "Access code copied" : "Copy access code"} className="hover:cursor-pointer">
              {copied ? <Check size={20} /> : <Copy size={20} />}
            </button>
          </div>
          <p className="mt-2 text-sm">Expires {new Date(invite.expiresAt).toLocaleString()}.</p>
        </div>
      )}
    </section>
  );
}
