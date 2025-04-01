import { useLocation, useNavigate } from "react-router-dom";

const Pagination = ({ totalPages }) => {
    const location = useLocation();
    const navigate = useNavigate();
    const searchParams = new URLSearchParams(location.search);
    const page = searchParams.get("page") ? Number(searchParams.get("page")) : 1;

    if (totalPages === 0) {
        return null;
    }

    const handlePrevious = () => {
        if (page > 0) {
            const newPage = page - 1;
            updatePageInURL(newPage);
        }
    };

    const handleNext = () => {
        if (page < totalPages - 1) {
            const newPage = page + 1;
            updatePageInURL(newPage);
        }
    };

    const updatePageInURL = (newPage) => {
        searchParams.set("page", newPage);
        navigate(`${location.pathname}?${searchParams.toString()}`);
    };

    return (
        <div>
            <button onClick={handlePrevious} disabled={page === 0}>
                Previous
            </button>
            <span>Page {page + 1} of {totalPages}</span>
            <button onClick={handleNext} disabled={page === totalPages - 1}>
                Next
            </button>
        </div>
    );
};

export default Pagination;
