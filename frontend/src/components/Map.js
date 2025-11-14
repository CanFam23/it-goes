"use client";

import React, { useEffect, useRef } from 'react';

import 'mapbox-gl/dist/mapbox-gl.css';
import mapboxgl from "mapbox-gl";

import DimensionControl from "@/mapbox/DimensionControl";

mapboxgl.accessToken = process.env.NEXT_PUBLIC_MAPBOX_TOKEN;

export default function Map({routeData}) {
  const mapContainerRef = useRef();
  const mapRef = useRef();

  useEffect(() => {
    mapRef.current = new mapboxgl.Map({
      container: mapContainerRef.current,
      style: 'mapbox://styles/mapbox/standard-satellite',
      config: {
        basemap: {
          showPedestrianRoads: false,
          showPointOfInterestLabels: true,
          showTransitLabels: false,
          showAdminBoundaries: false,
          font: "Inter",
          showLandmarkIconLabels: true
        }
      },
      center: [-111.395, 45.35],
      zoom: 11.12,
      bearing: 0.00,
      pitch: 0.00,
      keyboard: false,
    });

    mapRef.current.addControl(new mapboxgl.NavigationControl());
    mapRef.current.addControl(new mapboxgl.GeolocateControl());
    mapRef.current.addControl(new mapboxgl.ScaleControl({unit: "imperial"}));

    mapRef.current.addControl(new mapboxgl.FullscreenControl(), "bottom-right");

    const deltaDistance = 100;
    const deltaDegrees = 25;
    const deltaZoom = 0.5;

    function easing(t) {
      return t * (2 - t);
    }

    mapRef.current.on('load', () => {

      mapRef.current.getCanvas().focus();
      mapRef.current
        .getCanvas()
        .parentNode.classList.remove('mapboxgl-interactive');

      mapRef.current.getCanvas().addEventListener(
        'keydown',
        (e) => {
          console.log(e.which);
          e.preventDefault();
          if (e.which === 38) {
            mapRef.current.panBy([0, -deltaDistance], {
              easing: easing
            });
          } else if (e.which === 40) {
            mapRef.current.panBy([0, deltaDistance], {
              easing: easing
            });
          } else if (e.which === 37) {
            mapRef.current.easeTo({
              bearing: mapRef.current.getBearing() - deltaDegrees,
              easing: easing
            });
          } else if (e.which === 39) {
            mapRef.current.easeTo({
              bearing: mapRef.current.getBearing() + deltaDegrees,
              easing: easing
            });
          } else if (e.which === 90) {
            mapRef.current.easeTo({
              zoom: mapRef.current.getZoom() + deltaZoom,
              easing: easing
            });
          } else if (e.which === 88) {
            mapRef.current.easeTo({
              zoom: mapRef.current.getZoom() - deltaZoom,
              easing: easing
            });
          }
        },
        true
      );

      mapRef.current.addSource('mapbox-dem', {
        'type': 'raster-dem',
        'url': 'mapbox://mapbox.mapbox-terrain-dem-v1',
        'tileSize': 512,
        'maxzoom': 14
      });

      mapRef.current.setTerrain({'source': 'mapbox-dem', 'exaggeration': 1.5});

      // Add route data source
      mapRef.current.addSource('routes',{
        type: 'geojson',
        generateId: true,
        data:routeData
      });

      // Add layer with route data
      mapRef.current.addLayer({
        id: 'routes',
        type: 'line',
        source: 'routes',
        paint: {
          'line-color': "#ff0000",
          'line-width': 2,
        }
      });
    });

    // Add interaction to display route info on click
    mapRef.current.addInteraction('routes-click-interaction', {
      type: 'click',
      target: { layerId: 'routes' },
      handler: (e) => {
        const description = `
        <strong class="w-full place-self-center self-center">${e.feature.properties.title}</strong>
        <hr>
        <div class="flex">
          <p class="pr-2">${e.feature.properties.locationName}</p>
          <p class="pl-2 bg-background">${e.feature.properties.date}</p>
        </div>
        <p>${e.feature.properties.distance.toFixed(2)} miles</p>
        <p>${e.feature.properties.elevation.toFixed(2)} ft elevation gain</p>
        <a href="" class="hover:underline"><strong>View Post</strong></a>
        `;

        new mapboxgl.Popup()
          .setLngLat(e.lngLat)
          .setHTML(description)
          .addTo(mapRef.current);
      }
    });

    // Change the cursor to a pointer when the mouse is over a POI.
    mapRef.current.addInteraction('routes-mouseenter-interaction', {
      type: 'mouseenter',
      target: { layerId: 'routes' },
      handler: () => {
        mapRef.current.getCanvas().style.cursor = 'pointer';
      }
    });

    // Change the cursor back to a pointer when it stops hovering over a POI.
    mapRef.current.addInteraction('routes-mouseleave-interaction', {
      type: 'mouseleave',
      target: { layerId: 'routes' },
      handler: () => {
        mapRef.current.getCanvas().style.cursor = '';
      }
    });

    return () => mapRef.current.remove();
  });

  return (
    <div
      ref={mapContainerRef}
      className="map-container w-full h-[600px]"
    />
  );
}