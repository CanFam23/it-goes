import React from "react";

export default function DaysSkiedTotal({daysSkiedData = [], year = 2025}) {
  if (daysSkiedData.length === 0) {
    return <p className="error-msg">No data to show</p>;
  }

  const names = daysSkiedData.map((d, i) => {
    return (
      <td key={i} className="font-semibold px-8 py-1">{d.firstName}</td>
    )
  })

  const days = daysSkiedData.map((d, i) => {
    return (
      <td key={i} className="px-8">{d.daysSkied}</td>
    )
  })

  return (
    <table className="table-auto w-fit place-self-center m-5 mb-10">
      <caption className="font-bold text-lg text-nowrap">
        Total Days Skied
      </caption>
      <tbody>
        <tr>
          {/*<td></td>*/}
          {names}
        </tr>
        <tr>
          {/*<td className="font-bold w-fit">Days Skied</td>*/}
          {days}
        </tr>
      </tbody>
    </table>
  )
}