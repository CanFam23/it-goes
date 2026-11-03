import Link from "next/link";

export default function TripNotFound() {
  return (
    <section className="w-full text-center bg-background p-8 shadow-lg">
      <h1 className="text-3xl font-black">Trip not found</h1>
      <p className="my-4">This trip does not exist.</p>
      <Link href="/trips" className="btn inline-block">Browse trips</Link>
    </section>
  );
}
