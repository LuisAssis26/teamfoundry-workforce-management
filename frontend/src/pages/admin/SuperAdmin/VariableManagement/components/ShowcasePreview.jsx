import PropTypes from "prop-types";

export default function ShowcasePreview({ src, alt }) {
  if (!src) {
    return (
      <div className="h-full w-full flex items-center justify-center bg-base-200 text-base-content/40 text-xs">
        Sem imagem
      </div>
    );
  }
  return <img src={src} alt={alt} className="h-full w-full object-cover" loading="lazy" />;
}

ShowcasePreview.propTypes = {
  src: PropTypes.string,
  alt: PropTypes.string,
};

ShowcasePreview.defaultProps = {
  src: "",
  alt: "",
};
