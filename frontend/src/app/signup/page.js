"use client";

import { useState } from "react";

import { Eye, EyeOff, X, Check } from "lucide-react";

export default function page() {
  const minTextLength = 1;
  const maxTextLength = 30;

  const minUsernameLength = 4;
  const maxUsernameLength = 20;

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const requirements = [
    { label: "At least 8 characters", test: (pw) => pw.length >= 8 },
    { label: "One uppercase letter", test: (pw) => /[A-Z]/.test(pw) },
    { label: "One lowercase letter", test: (pw) => /[a-z]/.test(pw) },
    { label: "One number", test: (pw) => /\d/.test(pw) },
    {
      label: "One special character",
      test: (pw) => /[!@#$%^&*(),.?":{}|<>]/.test(pw),
    },
  ];

  const passwordsMatch = password === confirmPassword && confirmPassword !== "";
  const allRequirementsMet = requirements.every((req) => req.test(password));

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  async function onSubmit(e) {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData.entries()); // Convert to object

    console.log(data);

    try {
      // API route
      const res = await fetch(`/api/auth/signup`, {
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
    <section className="flex flex-col bg-background w-fit rounded-xl p-10 pb-0 items-center mx-auto">
      <h1 className="text-2xl font-bold">Sign Up</h1>

      <form
        className="flex flex-col p-2 m-10 mb-0 space-y-5"
        onSubmit={onSubmit}
      >
        <div className="flex flex-col">
          <label htmlFor="accessCode">Access Code</label>
          <input
            id="accessCode"
            type="text"
            name="accessCode"
            className="bg-white border-2 rounded-lg p-1"
            required
          ></input>
        </div>

        <div className="flex flex-col">
          <label htmlFor="firstName">First Name</label>
          <input
            id="firstName"
            type="text"
            name="firstName"
            placeholder="John"
            className="bg-white border-2 rounded-lg p-1"
            minLength={minTextLength}
            maxLength={maxTextLength}
            required
          ></input>
        </div>

        <div className="flex flex-col">
          <label htmlFor="lastName">Last Name</label>
          <input
            id="lastName"
            type="text"
            name="lastName"
            placeholder="Doe"
            className="bg-white border-2 rounded-lg p-1"
            minLength={minTextLength}
            maxLength={maxTextLength}
            required
          ></input>
        </div>

        <div className="flex flex-col">
          <label htmlFor="username" required>
            Username
          </label>
          <input
            type="text"
            id="username"
            name="username"
            placeholder="johndoe123"
            className="bg-white border-2 rounded-lg p-1"
            minLength={minUsernameLength}
            maxLength={maxUsernameLength}
            required
          ></input>
        </div>

        <div className="flex flex-col">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            name="email"
            placeholder="johndoe@example.com"
            className="bg-white border-2 rounded-lg p-1"
            required
          ></input>
        </div>

        <div className="flex flex-col">
          <div className="flex flex-row items-center gap-2">
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
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="bg-white border-2 rounded-lg p-1"
            required
          />

          {/* Password error messages */}
          {password && (
            <div className="mt-2 space-y-1">
              {requirements.map((req, idx) => (
                <div key={idx} className="flex items-center gap-2 text-sm">
                  {req.test(password) ? (
                    <Check size={16} className="text-green-600" />
                  ) : (
                    <X size={16} className="text-red-600" />
                  )}
                  <span
                    className={
                      req.test(password) ? "text-green-600" : "text-gray-600"
                    }
                  >
                    {req.label}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="flex flex-col">
          <div className="flex flex-row mt-4">
            <label htmlFor="password">Confirm Password</label>
            <p
              className="text-xs hover:cursor-pointer hover:underline"
              onClick={() => setShowConfirmPassword(!showPassword)}
            >
              {showConfirmPassword ? (
                <EyeOff strokeWidth={1.6} />
              ) : (
                <Eye strokeWidth={1.5} />
              )}
            </p>
          </div>
          <input
            type={showPassword ? "text" : "password"}
            id="password"
            name="confirmPassword"
            className="bg-white border-2 rounded-lg p-1"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          ></input>

          {/* Tells user if passwords match or not */}
          {confirmPassword && (
            <div className="flex items-center gap-2 text-sm mt-2">
              {passwordsMatch ? (
                <>
                  <Check size={16} className="text-green-600" />
                  <span className="text-green-600">Passwords match</span>
                </>
              ) : (
                <>
                  <X size={16} className="text-red-600" />
                  <span className="text-red-600">Passwords don't match</span>
                </>
              )}
            </div>
          )}
        </div>

        {/* <div className="flex flex-col">
          <label htmlFor="profilePic">Profile Picture (Optional)</label>
          <input
            type="file"
            id="profilePic"
            name="profilePic"
            accept="image/*"
            className="text-sm text-gray-600 cursor-pointer
          file:bg-[var(--background-btn)] file:border-[var(--border-btn)]
          file:font-bold file:border-2 file:rounded-xl file:py-[5px] file:px-[10px]
          file:mr-4 file:cursor-pointer
          hover:file:bg-gray-100 hover:file:border-black"
          />
        </div> */}

        {error && (
          <div style={{ color: "red" }} className="text-sm">
            {error}
          </div>
        )}
        <button
          type="submit"
          className="btn hover:cursor-pointer mt-15 disabled:opacity-50 disabled:cursor-not-allowed"
          disabled={isLoading || !allRequirementsMet || !passwordsMatch}
        >
          {isLoading ? "Loading..." : "Sign Up"}
        </button>
      </form>
    </section>
  );
}
