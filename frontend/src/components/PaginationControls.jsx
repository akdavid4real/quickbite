export default function PaginationControls({ page, totalPages, onPageChange, label = 'results' }) {
  if (!totalPages || totalPages <= 1) return null

  return (
    <nav className="pagination-controls" aria-label={`${label} pages`}>
      <button type="button" disabled={page <= 0} onClick={() => onPageChange(page - 1)}>Previous</button>
      <span>Page {page + 1} of {totalPages}</span>
      <button type="button" disabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>Next</button>
    </nav>
  )
}
