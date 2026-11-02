"use client";

import ErrorMessage from "@/components/ErrorMessage";

export default function Error({ reset }) {
  return (
    <section className="w-full">
      <ErrorMessage message="Unable to load this page. Please try again shortly." />
      <button className="btn" onClick={reset}>Try again</button>
    </section>
  );
}
