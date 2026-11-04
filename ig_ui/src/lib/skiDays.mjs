import {requestJson} from "./requestJson.mjs";

export async function saveSkiDay({form, editingId, newLocation}, fetcher = fetch) {
  let locationId = Number(form.locationId);
  if (newLocation) {
    const created = await requestJson("/api/locations", {
      method: "POST", credentials: "include", headers: {"Content-Type": "application/json"},
      body: JSON.stringify({name: form.name, state: form.country === "US" ? form.state : null, country: form.country}),
    }, fetcher);
    locationId = created.id;
  }
  return requestJson(editingId ? `/api/me/ski-days/${editingId}` : "/api/me/ski-days", {
    method: editingId ? "PATCH" : "POST", credentials: "include", headers: {"Content-Type": "application/json"},
    body: JSON.stringify({date: form.date, locationId}),
  }, fetcher);
}

export async function deleteSkiDay(id, fetcher = fetch) {
  const response = await fetcher(`/api/me/ski-days/${id}`, {method: "DELETE", credentials: "include"});
  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.message || "Could not delete this day.");
  }
}
