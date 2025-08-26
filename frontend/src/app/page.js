import { headers } from 'next/headers';
import Image from "next/image";
import TripSummaryCard from "@/components/TripSummaryCard";
import Link from "next/link";

export default function Home() {
  return (
    <section className="w-full">
      <Image src={"/images/group_pic.jpg"}
             alt={"Group picture of it goes group"}
             width={1200}
             height={600}
             className="w-full md:max-w-2/3 place-self-center"
      />
      <div className="flex flex-row my-10">
        <hr className="w-full border-t border-2 border-black place-self-center"/>
        <h2 className="w-fit font-anon text-3xl sm:text-4xl font-bold text-nowrap px-5">About Us</h2>
        <hr className="w-full border-t border-2 border-black place-self-center"/>
      </div>
      <div className="bg-background w-full p-5 flex flex-col text-center rounded-m">
        <article>
          Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
          incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud
          exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure
          dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
          Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
        </article>
        <Link
          href=""
          className="text-text-btn bg-bg-btn font-bold border-border-btn
          border-2 rounded-lg p-1 self-center my-5
          transform transition-transform duration-200
          hover:scale-105 hover:bg-gray-100"
        >
          Learn More
        </Link>
      </div>
      <div className="flex flex-row my-10">
        <hr className="w-full border-t border-2 border-black place-self-center"/>
        <h2 className="w-fit font-anon text-3xl sm:text-4xl font-bold text-nowrap px-5">Recent Trips</h2>
        <hr className="w-full border-t border-2 border-black place-self-center"/>
      </div>
      <RecentTrips />
    </section>
  );
}

async function RecentTrips(){
  const h = await headers();
  const proto = h.get('x-forwarded-proto') || 'http';
  const host = h.get('host');
  const base = `${proto}://${host}`; // e.g. http://localhost:3000 in dev

  const res = await fetch(`${base}/api/getRecentTrips`, {
    cache: 'no-store', // or: next: { revalidate: 60 }
  });
  if (!res.ok) {
    console.log(res);
    return (
      <p className="error-msg">
        Error fetching recent trips
      </p>
    )
  }

  const trips = await res.json();

  console.log(`Successfully retrieved trips`);
  console.log(trips);

  return (
    <div>
      {Object.entries(trips).map(([key, trip]) => (
        <TripSummaryCard key={key} trip={trip}/>
      ))}
    </div>
  );

}
