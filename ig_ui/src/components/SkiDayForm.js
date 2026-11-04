"use client";

import {useEffect, useState} from "react";
import {ChevronDown} from "lucide-react";
import {requestJson} from "@/lib/requestJson.mjs";
import {saveSkiDay, deleteSkiDay} from "@/lib/skiDays.mjs";

const emptyForm = {date: "", locationId: "", name: "", state: "", country: ""};
const selectClass = "w-full appearance-none rounded-lg border-2 border-black bg-white px-3 py-2 pr-10 transition-colors hover:border-border-btn focus:border-border-btn focus:outline-none focus:ring-2 focus:ring-border-btn/30 disabled:opacity-50";

export default function SkiDayForm() {
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [newLocation, setNewLocation] = useState(false);
  const [locations, setLocations] = useState([]);
  const [options, setOptions] = useState({states: [], countries: []});
  const [days, setDays] = useState(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function reload() {
    const [list, available] = await Promise.all([
      requestJson("/api/me/ski-days", {credentials: "include", cache: "no-store"}),
      requestJson("/api/locations", {cache: "no-store"}),
    ]);
    setDays(list);
    setLocations(available);
  }

  useEffect(() => {
    Promise.all([reload(), requestJson("/api/locations/options", {cache: "no-store"}).then(setOptions)])
      .catch((err) => setError(err.message));
  }, []);

  function change(event) {
    const {name, value} = event.target;
    setForm((current) => ({...current, [name]: value, ...(name === "country" && value !== "US" ? {state: ""} : {})}));
  }

  function reset() {
    setEditingId(null);
    setNewLocation(false);
    setForm(emptyForm);
  }

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      await saveSkiDay({form, editingId, newLocation});
      reset();
      await reload();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function remove(id) {
    if (!window.confirm("Delete this ski day?")) return;
    setBusy(true);
    setError("");
    try {
      await deleteSkiDay(id);
      await reload();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return <div className="grid gap-6 lg:grid-cols-2">
    <div className="bg-background p-5 shadow-lg">
      <h2 className="text-2xl font-black">{editingId ? "Edit day" : "Add day"}</h2>
      <form onSubmit={submit} className="mt-5 flex flex-col gap-4">
        <label className="flex flex-col gap-1">Date
          <input type="date" name="date" value={form.date} onChange={change} max={new Date().toISOString().slice(0, 10)} required className="rounded-lg border-2 border-black bg-white p-2 focus:border-border-btn focus:outline-none focus:ring-2 focus:ring-border-btn/30" />
        </label>
        <label className="flex flex-col gap-1">Location
          <span className="relative">
            <select name="locationId" value={form.locationId} onChange={change} required={!newLocation} disabled={newLocation} className={selectClass}>
              <option value="">Choose a location</option>
              {locations.map((location) => <option key={location.id} value={location.id}>{location.name} ({location.state ? `${location.state}, ` : ""}{location.country})</option>)}
            </select>
            <ChevronDown aria-hidden="true" className="pointer-events-none absolute right-3 top-1/2 size-5 -translate-y-1/2" />
          </span>
        </label>
        <label className="flex items-center gap-2">
          <input type="checkbox" checked={newLocation} onChange={(event) => setNewLocation(event.target.checked)} /> Add a new location
        </label>
        {newLocation && <div className="flex flex-col gap-3 border-l-2 border-black pl-4">
          <label className="flex flex-col">Name<input name="name" value={form.name} onChange={change} required maxLength={255} className="border-2 border-black bg-white p-2" /></label>
          <label className="flex flex-col">Country
            <span className="relative">
              <select name="country" value={form.country} onChange={change} required className={selectClass}>
                <option value="">Choose a country</option>
                {options.countries.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
              </select>
              <ChevronDown aria-hidden="true" className="pointer-events-none absolute right-3 top-1/2 size-5 -translate-y-1/2" />
            </span>
          </label>
          {form.country === "US" && <label className="flex flex-col">State
            <span className="relative">
              <select name="state" value={form.state} onChange={change} required className={selectClass}>
                <option value="">Choose a state</option>
                {options.states.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
              </select>
              <ChevronDown aria-hidden="true" className="pointer-events-none absolute right-3 top-1/2 size-5 -translate-y-1/2" />
            </span>
          </label>}
        </div>}
        <div className="flex gap-3">
          <button type="submit" disabled={busy} className="btn disabled:opacity-50">{busy ? "Saving..." : editingId ? "Save changes" : "Log day"}</button>
          {editingId && <button type="button" onClick={reset} className="underline">Cancel</button>}
        </div>
      </form>
      {error && <p role="alert" className="mt-4 text-red-700">{error}</p>}
    </div>
    <div className="bg-background p-5 shadow-lg">
      <h2 className="text-2xl font-black">Your ski days</h2>
      {days === null ? <p className="mt-4">Loading days...</p> : <>
        <p className="mt-3 font-bold">{days.seasonStartYear}–{days.seasonStartYear + 1} season: {days.totalDays} total days</p>
        <p className="text-sm">Each date counts once overall, even if you skied more than one location.</p>
        {days.entries.length === 0 ? <p className="mt-5">No days logged yet.</p> : <ul className="mt-5 divide-y divide-black">
          {days.entries.map((entry) => <li key={entry.id} className="flex flex-wrap items-center justify-between gap-3 py-3">
            <span>{entry.date} · {entry.locationName}</span>
            <span className="flex gap-3">
              <button type="button" className="underline" onClick={() => {setEditingId(entry.id); setNewLocation(false); setForm({...emptyForm, date: entry.date, locationId: String(entry.locationId)});}}>Edit</button>
              <button type="button" className="text-red-700 underline" disabled={busy} onClick={() => remove(entry.id)}>Delete</button>
            </span>
          </li>)}
        </ul>}
      </>}
    </div>
  </div>;
}
