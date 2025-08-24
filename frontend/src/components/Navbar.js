import {Menu, LogIn} from "lucide-react";

export default function Navbar(props) {
  const linksContent = props.links.map((link, key) => (
    <p
      key={key}
      className={`font-anon font-bold text-2xl lg:text-3xl text-nowrap mt-auto mb-2 ${
        key === props.links.length - 1 ? "ml-auto mr-4 flex flex-nowrap" : ""
      }`}
    >
      {link}
    </p>
  ));

  return (
        <header>
            <nav className="bg-background flex w-full">
                <h1 className="font-bold text-5xl lg:text-6xl m-2 text-nowrap">It Goes</h1>
              <div className="hidden sm:flex space-x-5 ml-4 w-full">
                {linksContent}
              </div>
              <button
                className="sm:hidden w-full"
                aria-label="Open navigation menu"
              >
                <Menu className="sm:hidden ml-auto mr-3 size-10 hover:cursor-pointer"/>
              </button>
            </nav>
        </header>
    );
}