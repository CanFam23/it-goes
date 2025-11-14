import { anonymousPro, inter } from "./ui/fonts";
import "./globals.css";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";

export const metadata = {
  title: {
    template: "%s | It Goes",
    default: "It Goes",
  },
  description: "Ski Blog",
  keywords: ['Ski Blog', 'Ski', 'Blog', 'Snow', 'Powder', 'Backcountry','Backcountry Skiing'],
  authors: [{ name: 'Nick Clouse',url: 'https://nickclouse.com'}, { name: 'Jake Sweatland'}, { name: 'Connor Marland'}],
  creator: 'Nick Clouse',
  publisher: 'Nick Clouse',
};

const links = {
  "About":"/about",
  "Trips":"/trips",
  "Map":"/map",
  "Gallery":"/gallery",
  "Log in":"/login"
}

const socialLinks = {
  YouTube: {
    "It Goes Productions": "https://www.youtube.com/itgoesproductions"
  },
  Instagram: {
    "nick.clouse": "https://www.instagram.com/nick.clouse",
    "sweatskis": "https://www.instagram.com/sweatskis",
    "connor.marland": "https://www.instagram.com/connor.marland",
  },
  Tiktok: {
    "sweatskis": "https://www.tiktok.com/@bubblebutt69"
  }
}

export default function RootLayout({ children }) {
  return (
    <html lang="en" className={`${inter.variable} ${anonymousPro.variable}`}>
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      </head>
      <body
        className="font-inter antialiased bg-[url('/topography.svg')] bg-repeat"
      >
      <Navbar links={links} />
      <main className="flex my-10 mx-10 sm:mx-20 lg:mx-30">
        {/*<p className="h-lvh"></p>*/}
        {children}
      </main>
      <Footer links={socialLinks} />
      </body>
    </html>
  );
}
