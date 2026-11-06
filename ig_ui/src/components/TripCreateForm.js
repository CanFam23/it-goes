"use client";

import {useEffect, useState} from "react";
import {useRouter} from "next/navigation";
import {ChevronDown} from "lucide-react";
import {requestJson} from "@/lib/requestJson.mjs";
import {createTrip, parseGpx} from "@/lib/tripCreate.mjs";
import TripEditor from "@/components/TripEditor";

const fieldClass = "rounded border-2 border-black bg-white p-2 transition-colors hover:border-border-btn focus:border-border-btn focus:outline-none focus:ring-2 focus:ring-border-btn/30";
const selectClass = "w-full appearance-none rounded-lg border-2 border-black bg-white px-3 py-2 pr-10 transition-colors hover:border-border-btn focus:border-border-btn focus:outline-none focus:ring-2 focus:ring-border-btn/30 disabled:opacity-50";

export default function TripCreateForm() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [dateOfTrip, setDate] = useState("");
  const [locationId, setLocation] = useState("");
  const [newLocation, setNewLocation] = useState(false);
  const [locationDraft, setLocationDraft] = useState({name: "", country: "", state: ""});
  const [participantIds, setParticipants] = useState([]);
  const [body, setBody] = useState(null);
  const [route, setRoute] = useState(null);
  const [routeFileName, setRouteFileName] = useState("");
  const [locations, setLocations] = useState(null);
  const [locationOptions, setLocationOptions] = useState(null);
  const [people, setPeople] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  async function loadOptions() {
    setLoading(true);
    setError("");
    try {
      const [places, options, users] = await Promise.all([
        requestJson("/api/locations", {cache: "no-store"}),
        requestJson("/api/locations/options", {cache: "no-store"}),
        requestJson("/api/users/options", {credentials: "include", cache: "no-store"}),
      ]);
      setLocations(places);
      setLocationOptions(options);
      setPeople(users);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadOptions();
  }, []);

  function toggleParticipant(id) {
    setParticipants((current) => current.includes(id) ? current.filter((value) => value !== id) : [...current, id]);
  }

  function changeLocationDraft(event) {
    const {name, value} = event.target;
    setLocationDraft((current) => ({...current, [name]: value,
      ...(name === "country" && value !== "US" ? {state: ""} : {})}));
  }

  async function selectRoute(event) {
    setError("");
    const file = event.target.files?.[0];
    if (!file) {setRoute(null); setRouteFileName(""); return;}
    try {setRoute(parseGpx(await file.text())); setRouteFileName(file.name);}
    catch (err) {setRoute(null); setRouteFileName(""); event.target.value = ""; setError(err.message);}
  }

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const trip = await createTrip({title, dateOfTrip, locationId,
        newLocation: newLocation ? locationDraft : null, participantIds, body, route});
      router.push(`/trips/${trip.id}`);
      router.refresh();
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  }

  return <div className="mx-auto w-full max-w-4xl bg-background p-5 shadow-lg">
    <p className="mb-5 rounded border border-black bg-white p-3">Photos can be added in a later update. This trip will show a placeholder cover.</p>
    {loading ? <p>Loading trip form...</p> : locations === null || locationOptions === null || people === null ?
      <button type="button" className="btn" onClick={loadOptions}>Retry loading form</button> :
      <form onSubmit={submit} className="flex flex-col gap-5">
      <label className="flex flex-col gap-1">Title
        <input required maxLength={255} value={title} onChange={(event) => setTitle(event.target.value)} className={fieldClass} />
      </label>
      <label className="flex flex-col gap-1">Trip date
        <input required type="date" max={new Date().toISOString().slice(0, 10)} value={dateOfTrip} onChange={(event) => setDate(event.target.value)} className={fieldClass} />
      </label>
      <label className="flex flex-col gap-1">Location
        <span className="relative">
          <select required={!newLocation} disabled={newLocation} value={locationId} onChange={(event) => setLocation(event.target.value)} className={selectClass}>
            <option value="">Choose a location</option>
            {locations.map((place) => <option key={place.id} value={place.id}>{place.name} ({place.state ? `${place.state}, ` : ""}{place.country})</option>)}
          </select>
          <ChevronDown aria-hidden="true" className="pointer-events-none absolute right-3 top-1/2 size-5 -translate-y-1/2" />
        </span>
      </label>
      <label className="flex items-center gap-2">
        <input type="checkbox" checked={newLocation} onChange={(event) => setNewLocation(event.target.checked)} className="accent-border-btn hover:ring-2 hover:ring-border-btn/30 focus:outline-none focus:ring-2 focus:ring-border-btn/30" /> Add a new location
      </label>
      {newLocation && <div className="flex flex-col gap-3 border-l-2 border-black pl-4">
        <label className="flex flex-col gap-1">Name
          <input name="name" value={locationDraft.name} onChange={changeLocationDraft} required maxLength={255} className={fieldClass} />
        </label>
        <label className="flex flex-col gap-1">Country
          <span className="relative">
            <select name="country" value={locationDraft.country} onChange={changeLocationDraft} required className={selectClass}>
              <option value="">Choose a country</option>
              {locationOptions.countries.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
            </select>
            <ChevronDown aria-hidden="true" className="pointer-events-none absolute right-3 top-1/2 size-5 -translate-y-1/2" />
          </span>
        </label>
        {locationDraft.country === "US" && <label className="flex flex-col gap-1">State
          <span className="relative">
            <select name="state" value={locationDraft.state} onChange={changeLocationDraft} required className={selectClass}>
              <option value="">Choose a state</option>
              {locationOptions.states.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
            </select>
            <ChevronDown aria-hidden="true" className="pointer-events-none absolute right-3 top-1/2 size-5 -translate-y-1/2" />
          </span>
        </label>}
      </div>}
      <fieldset className="flex flex-col gap-2"><legend>Who skied with you? (optional)</legend>
        {people.length ? people.map((person) => <label key={person.id} className="flex items-center gap-2">
          <input type="checkbox" checked={participantIds.includes(person.id)} onChange={() => toggleParticipant(person.id)} className="accent-border-btn hover:ring-2 hover:ring-border-btn/30 focus:outline-none focus:ring-2 focus:ring-border-btn/30" />{person.name}
        </label>) : <p>No other users available.</p>}
      </fieldset>
      <label className="flex flex-col gap-1">Route (optional GPX file)
        <span className="group relative flex flex-wrap items-center gap-3 rounded border-2 border-black bg-white p-2 transition-colors hover:border-border-btn focus-within:border-border-btn focus-within:ring-2 focus-within:ring-border-btn/30">
          <span aria-hidden="true" className="rounded border-2 border-black bg-background px-3 py-1 font-semibold transition-colors group-hover:border-border-btn group-focus-within:border-border-btn">{routeFileName ? "Change file" : "Choose file"}</span>
          <span aria-hidden="true" className="text-sm text-gray-600">{routeFileName || "No file chosen"}</span>
          <input type="file" accept=".gpx,application/gpx+xml" onChange={selectRoute} className="absolute inset-0 h-full w-full cursor-pointer opacity-0" />
        </span>
      </label>
      <div><p className="mb-1">Trip story</p><TripEditor onChange={setBody} /></div>
      <button type="submit" disabled={busy} className="btn self-start disabled:opacity-50">{busy ? "Publishing..." : "Publish trip"}</button>
    </form>}
    {error && <p role="alert" className="mt-4 text-red-700">{error}</p>}
  </div>;
}
