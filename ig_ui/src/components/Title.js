export default function Title({title}){
  return (
    <div className="flex flex-row my-10">
      <hr className="w-full border-t border-2 border-black place-self-center"/>
      <h2 className="w-fit font-anon text-3xl sm:text-4xl font-bold text-nowrap px-5">{title}</h2>
      <hr className="w-full border-t border-2 border-black place-self-center"/>
    </div>
  )
}