"use client";
import React from "react";
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, BarElement, Title, Tooltip, Legend, Filler, } from "chart.js";
import { Bar } from "react-chartjs-2";

ChartJS.register(CategoryScale, LinearScale, PointElement, BarElement, Title, Tooltip, Legend, Filler );

const DaysSkiedChart = ({ plotData = {trips: [], total: 0}, year = 2025 }) => {
  // TODO error handling
  const labels = [];
  const dataset = [];

  if (plotData.trips.length == 0){
    return <p className="error-msg">No data to plot</p>
  }

  // Extract first name and days skied
  plotData.forEach(entry => {
    labels.push(entry.firstName);
    dataset.push(entry.daysSkied);
  });

  // Find max val in dataset
  const maxVal = Math.max(...dataset, 0) + 1;

  // generate colors dynamically so it works for any number of bars
  const backgroundColors = dataset.map((_, i) =>
    `hsla(${(i * 50) % 360}, 70%, 50%, 0.8)`
  );

  const data = {
    labels,
    datasets: [
      {
        // leave label empty so no legend appears
        label: "",
        data: dataset,
        backgroundColor: backgroundColors,
        borderColor: backgroundColors,
        borderWidth: 1,
        barPercentage: 1,
        borderRadius: 5,
      },
    ],
  };

  const options = {
    maintainAspectRatio: false,
    responsive: true,
    plugins: {
      legend: {
        display: false, // hide legend
      },
      title: {
        display: true,
        text: `Days skied in the ${year}-${year + 1} season`,
        font: {
          size: 18,
        },
      },
    },
    scales: {
      y: {
        title: {
          display: true,
          text: "Days Skied",
        },
        beginAtZero: true,
        max: maxVal,
      },
      x: {
        title: {
          display: true,
          text: "Skier",
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