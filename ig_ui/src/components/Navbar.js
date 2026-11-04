"use client";
import Link from "next/link";

import {Menu, LogIn, X} from "lucide-react";
import {useEffect, useRef, useState} from "react";

// Navbar component
export default function Navbar(props) {
  const linksContent = Object.entries(props.links).map(([key, link]) => key === "Post" ? (
    <PostMenu key={key} />
  ) : (
    <Link
      href={link}
      key={key}
      className={`font-anon font-bold text-2xl lg:text-3xl text-nowrap mt-auto mb-2 hover:underline ${
        ["Log in","Log out"].includes(key) ? "ml-auto mr-4 flex flex-nowrap" : ""
      }`} // Last link (Login / Logout) gets pushed to right
    >
      {key}
    </Link>
  ));

  // Keeps track of when to display ham menu
  const [displayHamMenu, setDisplayHamMenu] = useState(false);

  return (
        <header>
            <nav className="bg-background flex w-full shadow-md">
                <Link href="/">
                  <h1 className="font-bold text-5xl lg:text-6xl m-2 text-nowrap">It Goes</h1>
                </Link>
              <div className="hidden sm:flex space-x-5 ml-4 w-full">
                {linksContent}
              </div>
              <button
                onClick={() => setDisplayHamMenu(true)}
                className="sm:hidden w-full"
                aria-label="Open navigation menu"
              >
                {/*Hide nav menu when ham menu is displayed*/}
                {!displayHamMenu && <Menu className="sm:hidden ml-auto mr-3 size-10 hover:cursor-pointer"/>}
              </button>
            </nav>
          <Hamburger
            links={props.links}
            display = {displayHamMenu}
            setDisplay = {setDisplayHamMenu}
          />
        </header>
    );
}

function Hamburger(props) {
  if (!props.display){
    return null; // Return nothing if not display
  }

  // Make HTML with links
  const linksContent = Object.entries(props.links).map(([key, link]) => key === "Post" ? (
    <PostMenu key={key} mobile onNavigate={() => props.setDisplay(false)} />
  ) : (
    <Link
      href={link}
      key={key}
      className="font-anon font-bold text-2xl text-nowrap py-2 pr-15 hover:underline"
    >
      {key}
    </Link>
  ));

  return (
    <>
      <div
        // Allow user to click out of menu to hide it
        onClick={() => props.setDisplay(false)}
        className="fixed bg-black/20 top-0 left-0 w-full h-full">
      </div>
      <nav
        className="bg-background fixed top-0 right-0 flex flex-col h-full
        border-black border-l-2 p-2 animate-slide-in"
      >
        <button onClick={() => props.setDisplay(false)}
                className="ml-auto mr-2 mt-3"
        ><X size={30} className="hover:cursor-pointer"/></button>
        {linksContent}
      </nav>
    </>
  );
}

function PostMenu({mobile = false, onNavigate}) {
  const detailsRef = useRef(null);

  useEffect(() => {
    function closeOnOutsideClick(event) {
      if (detailsRef.current && !detailsRef.current.contains(event.target)) {
        detailsRef.current.open = false;
      }
    }
    document.addEventListener("pointerdown", closeOnOutsideClick);
    return () => document.removeEventListener("pointerdown", closeOnOutsideClick);
  }, []);

  return (
    <details ref={detailsRef} className={`relative font-anon font-bold ${mobile ? "text-2xl py-2" : "text-2xl lg:text-3xl mt-auto mb-2"}`}>
      <summary className="cursor-pointer list-none hover:underline [&::-webkit-details-marker]:hidden">
        Post
      </summary>
      <div className={`${mobile ? "" : "absolute top-full left-0 z-20 min-w-40 shadow-lg"} flex flex-col border-2 border-t-0 border-black bg-background p-2 text-xl`}>
        <Link href="/post/day" className="hover:underline" onClick={() => {
          detailsRef.current.open = false;
          onNavigate?.();
        }}>Day</Link>
        <span aria-disabled="true" className="text-gray-500">Trip (coming later)</span>
      </div>
    </details>
  );
}
