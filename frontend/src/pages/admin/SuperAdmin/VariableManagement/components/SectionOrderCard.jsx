import { useState } from "react";
import PropTypes from "prop-types";
import { SECTION_LABELS } from "../constants.js";

export default function SectionOrderCard({ sections = [], onMove, onToggle }) {
  const [dragId, setDragId] = useState(null);
  const [dragIndex, setDragIndex] = useState(null);
  const [overIndex, setOverIndex] = useState(null);
  const [clickedId, setClickedId] = useState(null);

  const handleDragStart = (sectionId, index) => {
    setDragId(sectionId);
    setDragIndex(index);
  };

  const handleDragOver = (event, index) => {
    event.preventDefault();
    if (overIndex !== index) setOverIndex(index);
  };

  const handleDrop = async (index) => {
    if (dragId == null || dragIndex == null || dragIndex === index || typeof onMove !== "function") {
      setDragId(null);
      setDragIndex(null);
      setOverIndex(null);
      return;
    }

    const direction = dragIndex < index ? "down" : "up";
    const steps = Math.abs(index - dragIndex);

    for (let i = 0; i < steps; i += 1) {
      // eslint-disable-next-line no-await-in-loop
      await onMove(dragId, direction);
    }

    setDragId(null);
    setDragIndex(null);
    setOverIndex(null);
  };

  const handleDragEnd = () => {
    setDragId(null);
    setDragIndex(null);
    setOverIndex(null);
  };

  const handleArrowClick = async (sectionId, direction) => {
    if (typeof onMove !== "function") return;
    setClickedId(sectionId);
    try {
      await onMove(sectionId, direction);
    } finally {
      setTimeout(() => {
        setClickedId((current) => (current === sectionId ? null : current));
      }, 180);
    }
  };

  const itemClasses = (index, sectionId) => {
    let extra = "transition-transform duration-150 ease-out";

    if (dragId === sectionId) {
      extra += " ring-2 ring-primary/70 shadow-lg scale-[1.02]";
    } else if (clickedId === sectionId) {
      extra += " ring-2 ring-primary/60 shadow-md scale-[1.01]";
    } else if (overIndex === index && dragId != null) {
      extra += " bg-base-200/80";
    }

    return `flex items-center justify-between gap-4 rounded-2xl border border-base-300 bg-base-100 px-4 py-3 ${extra}`;
  };

  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-4">
        <div className="flex flex-col gap-1">
          <h2 className="card-title text-2xl">Ordem das sec‡äes</h2>
          <p className="text-base-content/70">
            Defina a sequˆncia com que cada bloco aparece para os visitantes.
          </p>
        </div>
        <ol className="space-y-3">
          {sections.map((section, index) => (
            <li
              key={section.id}
              draggable
              onDragStart={() => handleDragStart(section.id, index)}
              onDragOver={(event) => handleDragOver(event, index)}
              onDrop={() => handleDrop(index)}
              onDragEnd={handleDragEnd}
              className={itemClasses(index, section.id)}
            >
              <div className="flex items-center gap-3">
                <span className="font-semibold text-primary cursor-grab select-none">
                  {index + 1}.
                </span>
                <div>
                  <p className="font-semibold">
                    {SECTION_LABELS[section.type] ?? section.type}
                  </p>
                  <p className="text-sm text-base-content/70">{section.title}</p>
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-3">
                {typeof onToggle === "function" && (
                  <label className="label cursor-pointer gap-3">
                    <span className="text-sm text-base-content/70">
                      {section.active ? "Vis¡vel" : "Oculta"}
                    </span>
                    <input
                      type="checkbox"
                      className="toggle toggle-primary"
                      checked={section.active}
                      onChange={() => onToggle(section)}
                    />
                  </label>
                )}
                <button
                  type="button"
                  className="btn btn-sm btn-ghost"
                  onClick={() => handleArrowClick(section.id, "up")}
                  disabled={index === 0}
                >
                  <i className="bi bi-arrow-up" />
                </button>
                <button
                  type="button"
                  className="btn btn-sm btn-ghost"
                  onClick={() => handleArrowClick(section.id, "down")}
                  disabled={index === sections.length - 1}
                >
                  <i className="bi bi-arrow-down" />
                </button>
              </div>
            </li>
          ))}
        </ol>
      </div>
    </div>
  );
}

SectionOrderCard.propTypes = {
  sections: PropTypes.arrayOf(PropTypes.shape({ id: PropTypes.oneOfType([PropTypes.number, PropTypes.string]) })),
  onMove: PropTypes.func,
  onToggle: PropTypes.func,
};

SectionOrderCard.defaultProps = {
  sections: [],
  onMove: null,
  onToggle: null,
};
