import { FaYoutube, FaTiktok, FaInstagram } from "react-icons/fa";

// Platforms and their icons
const socialIcons = {
  "YouTube": <FaYoutube size="2rem" className=""/>,
  "Instagram": <FaInstagram size="2rem" className=""/>,
  "Tiktok": <FaTiktok size="2rem" className=""/>,
}

export default function Footer(props) {
  return (
    <footer className="bg-background grid grid-cols-3 gap-0 justify-items-center text-start py-10">
      {Object.entries(props.links).map(([platform, accounts]) => {
        const cols = Math.min(Object.keys(accounts).length, 3);
        // Grid classes for account url container
        // Used so the grids will have 1-3 columns depending on how many urls are given
        const colClasses = {
          1: "grid-cols-1 sm:grid-cols-1",
          2: "grid-cols-1 sm:grid-cols-2",
          3: "grid-cols-1 sm:grid-cols-3",
        };

        return (
          <div key={platform}>
            <div className="flex flex-row items-center">
              {socialIcons[platform]}
              <h4 className="font-bold text-xl w-full pl-2">{platform}</h4>
            </div>
            <div
              className={`grid ${colClasses[cols]} text-center text-md gap-4`}
            >
              {/*Make an a tag for each user-url in accounts*/}
              {Object.entries(accounts).map(([user, url]) => (
                <a
                  key={user}
                  href={url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:underline font-medium text-sm md:text-base"
                >
                  {user}
                </a>
              ))}
            </div>
            <div>
            </div>
          </div>
        );
      })}
      <div></div>
      <div className="w-full text-center mt-10 text-nowrap text-sm">
        © 2025 Nick Clouse
      </div>
    </footer>
  )
}