"use client";

import React, { useEffect, useRef } from 'react';

import 'mapbox-gl/dist/mapbox-gl.css';
import mapboxgl from "mapbox-gl";

mapboxgl.accessToken = process.env.NEXT_PUBLIC_MAPBOX_TOKEN;

export default function Map({location, route}) {
  const mapContainerRef = useRef();
  const mapRef = useRef();

  useEffect(() => {
    mapRef.current = new mapboxgl.Map({
      container: mapContainerRef.current,
      style: 'mapbox://styles/mapbox/standard',
      config: {
        basemap: {
          showPedestrianRoads: false,
          showPointOfInterestLabels: false,
          showTransitLabels: false,
          showAdminBoundaries: false,
          font: "Inter",
          showLandmarkIconLabels: false
        }
      },
      center: [-111.395, 45.35],
      zoom: 11.12,
      bearing: 0.00,
      pitch: 0.00,
    });

    mapRef.current.on('load', () => {
      mapRef.current.addSource('point', {
        type: 'geojson',
        data: {
          type: 'Feature',
          geometry: {
            type: 'Point',
            coordinates: [location[0],location[1]]
          },
          properties: {
            "name":"Beehive Basin"
          }
        }
      });

      mapRef.current.addSource('mapbox-dem', {
        'type': 'raster-dem',
        'url': 'mapbox://mapbox.mapbox-terrain-dem-v1',
        'tileSize': 512,
        'maxzoom': 14
      });

      mapRef.current.setTerrain({'source': 'mapbox-dem', 'exaggeration': 1.5});

      mapRef.current.addLayer({
        id: 'point-layer',
        type: 'circle',
        source: 'point',
        paint: {
          'circle-radius': 4,
          'circle-stroke-width': 2,
          'circle-color': 'red',
          'circle-stroke-color': 'white'
        }
      });

      mapRef.current.addSource('route',{
        type:'geojson',
        data: {
          type: 'Feature',
          geometry: {
            type: 'LineString',
            coordinates: route.coordinates
          },
          properties: {
            id: route.id,
          }
        }
      });

      mapRef.current.addLayer({
        id: 'route',
        type: 'line',
        source: 'route',
        paint: {
          'line-color': "#ff0000",
          'line-width': 2,
        }
      });
    });

    return () => mapRef.current.remove();
  });

  return (
    <div
      ref={mapContainerRef}
      className="map-container w-full h-[400px]"
    />
  );
}