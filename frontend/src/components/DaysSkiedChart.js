"use client";
import React from "react";
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, BarElement, Title, Tooltip, Legend, Filler, } from "chart.js";
import { Bar } from "react-chartjs-2";

ChartJS.register(CategoryScale, LinearScale, PointElement, BarElement, Title, Tooltip, Legend, Filler );

const DaysSkiedChart = ({ plotData = [], year = 2025 }) => {
  if (plotData.length === 0) {
    return <p className="error-msg">No data to plot</p>;
  }

  // Collect unique locations and unique skiers
  const locations = [...new Set(plotData.map((d) => d.location))];
  const skiers = [...new Set(plotData.map((d) => d.firstName))];

  // Build dataset: one dataset per skier
  const datasets = skiers.map((skier, i) => {
    // For each location, find daysSkied for this skier
    const data = locations.map((loc) => {
      const entry = plotData.find(
        (d) => d.location === loc && d.firstName === skier
      );
      return entry ? entry.daysSkied : 0; // 0 if no data
    });

    return {
      label: skier,
      data,
      backgroundColor: `hsla(${(i * 60) % 360}, 70%, 50%, 0.7)`,
      borderColor: `hsla(${(i * 60) % 360}, 70%, 40%, 1)`,
      borderWidth: 1,
      barPercentage: 0.9,
    };
  });

  const data = {
    labels: locations,
    datasets,
  };

  // Max val of daysSkied plus some padding for graph
  const maxVal = Math.max(
    ...plotData.map((d) => d.daysSkied),
    0
  ) + 2;

  const options = {
    indexAxis: "y", // horizontal bars
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true, // legend to distinguish skiers
      },
      title: {
        display: true,
        text: "Resorts & Zones Skied",
        color: '#000000',
        font: {
          size: 18,
        },
      },
    },
    scales: {
      x: {
        title: {
          display: true,
          text: "Days Skied",
        },
        beginAtZero: true,
        max: maxVal,
        ticks: {
          stepSize: 1, // only whole numbers
          precision: 0
        }
      },
      y: {
        title: {
          display: true,
          text: "Location",
        },
      },
    },
  };

  return (
    <div className="w-full h-96">
      <Bar data={data} options={options} />
    </div>
  );
};

export default DaysSkiedChart;