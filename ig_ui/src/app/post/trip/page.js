import {redirect} from "next/navigation";
import {getCurrentUser} from "@/lib/auth";
import Title from "@/components/Title";
import TripCreateForm from "@/components/TripCreateForm";

export const metadata = {title: "Create a trip"};

export default async function PostTripPage() {
  if (!(await getCurrentUser())) redirect("/login");
  return <section className="w-full min-w-0"><Title title="Create a Trip" /><TripCreateForm /></section>;
}
