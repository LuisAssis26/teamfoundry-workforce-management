import PropTypes from "prop-types";

export default function TabPlaceholder({ title, description }) {
  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-2">
        <h2 className="card-title text-2xl">{title}</h2>
        <p className="text-base-content/70">{description}</p>
      </div>
    </div>
  );
}

TabPlaceholder.propTypes = {
  title: PropTypes.string.isRequired,
  description: PropTypes.string.isRequired,
};
