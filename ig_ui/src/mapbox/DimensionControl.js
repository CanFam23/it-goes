/**
 * Custom MapBox control to allow the user to easily toggle between a 2D and 3D map.
 */
class DimensionControl {

  onAdd(map) {
    this.threeD = false;
    this._map = map;

    // container
    this._container = document.createElement("div");
    this._container.className = "mapboxgl-ctrl";

    // button
    this._button = document.createElement("button");
    this._button.className =
      "p-2 bg-white font-bold rounded hover:cursor-pointer hover:!bg-white";
    this._button.textContent = this.threeD ? "2D" : "3D";

    // click handler
    this._button.addEventListener("click", () => {
      if (!this._map) return;

      const current = this._map.getPitch();
      const next = current < 30 ? 60 : 0;

      this.threeD = !this.threeD;

      // Update only text
      this._button.textContent = this.threeD ? "2D" : "3D";

      this._map.easeTo({
        pitch: next,
        duration: 600
      });
    });

    this._container.appendChild(this._button);
    return this._container;
  }

  onRemove() {
    this._container.remove();
    this._map = undefined;
  }
}

export default DimensionControl;