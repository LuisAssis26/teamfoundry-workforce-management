import PropTypes from "prop-types";
import Button from "../../../../components/ui/Button/Button.jsx";
import JobCard from "./JobCard.jsx";

export default function JobOfferCard({ offer, onAccept }) {
    const status = (offer.status || "").toUpperCase();
    const isOpen = status === "OPEN" || status === "";
    const isAccepted = status === "ACCEPTED";
    const isClosed = status === "CLOSED";

    const actionSlot = (
        <div className="flex items-center gap-2">
            <Button
                label="Ver contrato"
                variant="primary"
                fullWidth={false}
                className="min-w-[120px]"
            />
            {isOpen && (
                <Button
                    label="Aceitar"
                    variant="success"
                    fullWidth={false}
                    className="min-w-[120px]"
                    onClick={() => onAccept(offer.requestId ?? offer.id)}
                />
            )}
            {isAccepted && (
                <Button
                    label="Aceite"
                    variant="ghost"
                    fullWidth={false}
                    className="min-w-[120px] cursor-not-allowed"
                    disabled
                />
            )}
            {isClosed && (
                <Button
                    label="Vagas esgotadas"
                    variant="ghost"
                    fullWidth={false}
                    className="min-w-[140px] cursor-not-allowed"
                    disabled
                />
            )}
        </div>
    );

    return (
        <div className="rounded-xl border border-base-300 bg-base-100 shadow-sm overflow-hidden">
            <JobCard job={offer} showAccepted={false} actionSlot={actionSlot} />
        </div>
    );
}

JobOfferCard.propTypes = {
    offer: PropTypes.object.isRequired,
    onAccept: PropTypes.func.isRequired,
};
