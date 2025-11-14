/**
 * Mapbox control for toggling topo / satellite style.
 */
class StyleControl {
  onAdd(map) {
    this.threeD = false;
    this._map = map;

    // container
    this._container = document.createElement("div");
    this._container.className = "mapboxgl-ctrl rounded-lg overflow-hidden";

    // button
    this._radio = document.createElement("fieldset");
    this._radio.className = "bg-white ";

    this._radio.innerHTML = `
      <div>
        <input type="radio" id="topo" name="drone" value="topo" class="peer hidden" checked/>
        <label for="topo"
        class="block cursor-pointer px-4 py-2 bg-white peer-checked:bg-gray-300"
        ><Strong>Topo</Strong></label>
      </div>
    
      <div>
        <input type="radio" id="satellite" name="drone" value="satellite" class="peer hidden"/>
        <label for="satellite"
        class="block cursor-pointer px-4 py-2 bg-white peer-checked:bg-gray-300"
        ><strong>Satellite</strong></label>
      </div>
    `;

    this._radio.addEventListener("change", (e) => {
      if (e.target.name !== "drone") return;

      const selected = e.target.value;

      console.log("Selected:", selected);

      // switch styles
      if (selected === "topo") {
        this._map.setLayoutProperty('satellite-base', 'visibility', 'none');
      }
      if (selected === "satellite") {
        this._map.setLayoutProperty('satellite-base', 'visibility', 'visible');
      }
    });

    this._container.appendChild(this._radio);
    return this._container;
  }

  onRemove() {
    this._container.remove();
    this._map = undefined;
  }
}

export default StyleControl;