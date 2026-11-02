import { redirect } from "next/navigation";
import { UserRound } from "lucide-react";

import Title from "@/components/Title";
import { getCurrentUser } from "@/lib/auth";
import InviteUserSection from "@/components/InviteUserSection";

export const metadata = {
  title: "Profile",
  description: "Your It Goes profile",
};

export default async function ProfilePage() {
  const user = await getCurrentUser();

  if (!user) {
    redirect("/login");
  }

  const displayName = user.firstName || user.username;

  return (
    <section className="min-w-0 w-full">
      <Title title="Profile" />

      <div className="bg-background p-5 sm:p-8 shadow-lg">
        <div className="flex flex-col gap-5 sm:flex-row sm:items-center">
          <div className="flex size-24 shrink-0 items-center justify-center rounded-full border-2 border-black bg-white">
            <UserRound size={48} strokeWidth={1.6} />
          </div>

          <div className="min-w-0 [overflow-wrap:anywhere]">
            <p className="font-anon text-sm font-bold uppercase tracking-normal">
              Logged in as
            </p>
            <h1 className="text-3xl font-black sm:text-4xl">
              {displayName}
            </h1>
            <p className="mt-1 text-lg">@{user.username}</p>
          </div>
        </div>

        <hr className="my-8 border-t border-black" />

        <div className="grid gap-8 lg:grid-cols-2">
          <div className="min-w-0 flex flex-col gap-5 sm:flex-row sm:items-center">
            <div className="min-w-0 w-full">
              <h2 className="text-2xl font-black">User Info</h2>
              <dl className="mt-4 grid gap-4 sm:grid-cols-2">
                <ProfileField label="First Name" value={user.firstName} />
                <ProfileField label="Last Name" value={user.lastName} />
                <ProfileField label="Username" value={`@${user.username}`} />
                <ProfileField label="Email" value={user.email} />
              </dl>
            </div>
          </div>

          <InviteUserSection />
        </div>
      </div>
    </section>
  );
}

function ProfileField({ label, value }) {
  return (
    <div className="min-w-0 border-2 border-black bg-white p-4">
      <dt className="font-anon text-sm font-bold uppercase tracking-normal">
        {label}
      </dt>
      <dd className="mt-2 text-lg font-bold [overflow-wrap:anywhere]">{value}</dd>
    </div>
  );
}
