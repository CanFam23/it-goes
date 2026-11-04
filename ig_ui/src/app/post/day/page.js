import {redirect} from "next/navigation";
import {getCurrentUser} from "@/lib/auth";
import Title from "@/components/Title";
import SkiDayForm from "@/components/SkiDayForm";

export const metadata = {title: "Log a ski day"};

export default async function PostDayPage() {
  if (!(await getCurrentUser())) redirect("/login");
  return <section className="w-full min-w-0">
    <Title title="Log a Ski Day" />
    <SkiDayForm />
  </section>;
}
