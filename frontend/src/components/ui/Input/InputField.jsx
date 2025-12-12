
import PropTypes from "prop-types";

/**
 * Campo de input com label, placeholder e ícone opcional.
 * Props:
 * - label: texto exibido acima do campo
 * - placeholder: texto dentro do input
 * - icon: elemento React opcional (ex: <i className="bi bi-envelope" />)
 * - type: tipo do input (text, email, password, etc.)
 */
export default function InputField({
                                       label,
                                       placeholder,
                                       icon,
                                       type = "text",
                                       as = "input",
                                       error,
                                       hint,
                                       className = "",
                                       inputClassName = "",
                                       fullWidth = true,
                                       inputRef,
                                       children,
                                       ...props
                                   }) {
    const hasError = Boolean(error);
    const Component = as;

    const canShowPlaceholder = Component === "input" || Component === "textarea";
    const inputClasses = [
        Component === "textarea" ? "textarea" : Component === "select" ? "select" : "input",
        "input-bordered",
        fullWidth ? "w-full" : "",
        icon ? "pl-10" : "",
        hasError ? "input-error" : "",
        inputClassName,
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <div
            className={[
                "form-control",
                fullWidth ? "w-full" : "",
                className,
            ]
                .filter(Boolean)
                .join(" ")}
        >
            {label && (
                <label className="label">
                    <span className="label-text font-medium">{label}</span>
                </label>
            )}

            <div className="relative">
                {icon && (
                    <span className="absolute inset-y-0 left-3 flex items-center text-base-content z-10">
            {icon}
          </span>
                )}

                {Component === "select" ? (
                    <Component
                        className={`${inputClasses} focus:outline-none`}
                        aria-invalid={hasError}
                        ref={inputRef}
                        {...props}
                    >
                        {placeholder && (
                            <option value="" disabled hidden>
                                {placeholder}
                            </option>
                        )}
                        {children}
                    </Component>
                ) : Component === "textarea" ? (
                    <Component
                        placeholder={placeholder}
                        className={`${inputClasses} focus:outline-none`}
                        aria-invalid={hasError}
                        ref={inputRef}
                        {...props}
                    >
                        {children}
                    </Component>
                ) : (
                    <Component
                        type={type}
                        placeholder={canShowPlaceholder ? placeholder : undefined}
                        className={`${inputClasses} focus:outline-none`}
                        aria-invalid={hasError}
                        ref={inputRef}
                        {...props}
                    />
                )}
            </div>

            {hasError ? (
                <p className="mt-2 text-sm text-error">{error}</p>
            ) : (
                hint && <p className="mt-2 text-sm text-base-content/70">{hint}</p>
            )}
        </div>
    );
}

InputField.propTypes = {
    label: PropTypes.string.isRequired,
    placeholder: PropTypes.string,
    icon: PropTypes.node,
    type: PropTypes.string,
    as: PropTypes.oneOf(["input", "textarea", "select"]),
    error: PropTypes.string,
    hint: PropTypes.string,
    className: PropTypes.string,
    inputClassName: PropTypes.string,
    fullWidth: PropTypes.bool,
    inputRef: PropTypes.oneOfType([PropTypes.func, PropTypes.shape({ current: PropTypes.any })]),
    children: PropTypes.node,
};
