"use client";

import Link from "next/link";
import { useMemo } from "react";

export default function Pagination({
  page,
  pageSize,
  totalPages,
  basePath}) {

  page = Number(page);
  pageSize = Number(pageSize);
  totalPages = Number(totalPages);

  let validData = true;

  // TODO better error handling
  if ((!page && page !== 0) || (!pageSize && pageSize !== 0) || (!totalPages && totalPages !== 0) || !basePath) {
    console.error(`Trip Page: Invalid Pagination Parameters:
    page ${page}, pageSize ${pageSize}, totalPages ${totalPages} basePath ${basePath}`);
    validData = false;
  }

  // Get how many pages to display
  // Right now, it will display pages from page-2 to page+2 if page+n
  // is in 1-totalPages, but want to replace with better logic eventually
  const pages = useMemo(() => {
    if (!validData) return null;
    const start = page-2;
    const end = page+2;
    const pageNums = [];

    // Add page number to list
    for (let i = start; i <= end; i++) {
      if (i >= 1 && i <= totalPages) {
        pageNums.push(i);
      }
    }
    return pageNums;
  }, [page, totalPages, validData]);

  if (!validData) return null;

  // Make url path
  const makeHref = (targetPage) => {
    const qp = new URLSearchParams();
    qp.set("page", String(targetPage));
    qp.set("pageSize", String(pageSize));

    return `${basePath}?${qp.toString()}`;
  };

  // Check if page is first or last page
  const isFirst = page < 1;
  const isLast = page + 1 >= totalPages;

  if (totalPages <= 1) return null;

  return (
    <nav aria-label="Pagination" className="flex items-center justify-center gap-2 w-full">
      {/*Link to go to first page*/}
      <Link
        href={isFirst ? "#" : makeHref(0)}
        aria-disabled={isFirst}
        className={`btn text-sm text-nowrap ${
          isFirst ? "cursor-not-allowed opacity-50" : "hover:bg-gray-50"
        }`}
        tabIndex={isFirst ? -1 : 0}
      >
        « First
      </Link>
      {/*Link to go to previous page*/}
      <Link
        href={isFirst ? "#" : makeHref(page - 1)}
        aria-disabled={isFirst}
        className={`btn text-sm text-nowrap ${
          isFirst ? "cursor-not-allowed opacity-50" : "hover:bg-gray-50"
        }`}
        tabIndex={isFirst ? -1 : 0}
      >
        ‹ Prev
      </Link>

      {pages.map((p) => {
        // Link for each page to show
        const isActive = p === Number(page)+1;

        return (
          <Link
            key={p}
            href={makeHref(p-1)}
            aria-current={isActive ? "page" : undefined}
            className={`btn text-sm text-nowrap ${
              isActive
                ? "active-page"
                : ""
            }`}
          >
            {p}
          </Link>
        );
      })}

      {/*Link for going to next page*/}
      <Link
        href={isLast ? "#" : makeHref(page + 1)}
        aria-disabled={isLast}
        className={`btn text-sm text-nowrap ${
          isLast ? "cursor-not-allowed opacity-50" : "hover:bg-gray-50"
        }`}
        tabIndex={isLast ? -1 : 0}
      >
        Next ›
      </Link>
      {/*Link for going to last page.*/}
      <Link
        href={isLast ? "#" : makeHref(totalPages-1)}
        aria-disabled={isLast}
        className={`btn text-sm text-nowrap ${
          isLast ? "cursor-not-allowed opacity-50" : "hover:bg-gray-50"
        }`}
        tabIndex={isLast ? -1 : 0}
      >
        Last »
      </Link>
    </nav>
  );
}