"use client";
import Link from "next/Link";

import {Menu, LogIn, X} from "lucide-react";
import {useState} from "react";

// Navbar component
export default function Navbar(props) {
  const linksContent = Object.entries(props.links).map(([key, link]) => (
    <Link
      href={link}
      key={key}
      className={`font-anon font-bold text-2xl lg:text-3xl text-nowrap mt-auto mb-2 hover:underline ${
        key === "Log in" ? "ml-auto mr-4 flex flex-nowrap" : ""
      }`} // Last link (Login) gets pushed to right
    >
      {key}
    </Link>
  ));

  // Keeps track of when to display ham menu
  const [displayHamMenu, setDisplayHamMenu] = useState(false);

  return (
        <header>
            <nav className="bg-background flex w-full">
                <h1 className="font-bold text-5xl lg:text-6xl m-2 text-nowrap">It Goes</h1>
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
  const linksContent = Object.entries(props.links).map(([key, link]) => (
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