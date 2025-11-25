"use client";

import Link from "next/Link";
import { useRouter } from "next/navigation";

import { useState } from "react";

import { Eye, EyeOff } from "lucide-react";

export default function page() {
  const [showPassword, setShowPassword] = useState(false);

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const router = useRouter();

  async function onSubmit(e) {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData.entries()); // Convert to object

    try {
      // API route
      const res = await fetch(`/api/auth/signin`, {
        method: "POST",
        body: JSON.stringify(data),
        headers: {
          "Content-Type": "application/json",
        },
        cache: "force-cache",
        credentials: "include"
      });

      const resData = await res.json();

      if (!res.ok) {
        setError(resData.message ? resData.message : "Error, please try again");
        return;
      }

      localStorage.setItem("username", res.username || "Unknown username");
      localStorage.setItem("name", resData.firstName || "User");

      router.push("/");
      router.refresh();
    } catch (err) {
      setError(error.message);
    } finally {
      setIsLoading(false); // Set loading to false when the request completes
    }
  }

  return (
    <section
      onSubmit={onSubmit}
      className="flex flex-col bg-background w-fit rounded-xl p-10 pb-0 items-center mx-auto"
    >
      <h1 className="text-2xl font-bold">Log In</h1>
      <p className="text-sm">
        Have an access code?
        <Link href="" className="hover:underline p-1">
          Sign up here
        </Link>
      </p>
      <form className="flex flex-col p-2 m-10 mb-0">
        <label htmlFor="id" required>
          Username
        </label>
        <input
          type="text"
          id="username"
          name="username"
          className="bg-white border-2 rounded-lg p-1"
          required
        ></input>

        <div className="flex flex-row mt-4">
          <label htmlFor="password">Password</label>
          <p
            className="text-xs hover:cursor-pointer hover:underline"
            onClick={() => setShowPassword(!showPassword)}
          >
            {showPassword ? (
              <EyeOff strokeWidth={1.6} />
            ) : (
              <Eye strokeWidth={1.5} />
            )}
          </p>
        </div>
        <input
          type={showPassword ? "text" : "password"}
          id="password"
          name="password"
          className="bg-white border-2 rounded-lg p-1"
          required
        ></input>

        {error && (
          <div style={{ color: "red" }} className="text-sm">
            {error}
          </div>
        )}
        <button
          type="submit"
          className="btn hover:cursor-pointer mt-15"
          disabled={isLoading}
        >
          {isLoading ? "Loading..." : "Log In"}
        </button>

        <Link href="" className="text-sm hover:underline self-center mt-5">
          Reset Password
        </Link>
      </form>
    </section>
  );
}
