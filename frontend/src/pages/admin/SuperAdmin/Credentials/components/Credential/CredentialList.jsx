import { useMemo, useState } from "react";
import PropTypes from "prop-types";
import CredentialCard from "./CredentialCard.jsx";
import SearchBar from "../../../../../../components/ui/Input/SearchBar.jsx";

const DEFAULT_FIELDS = [
    { key: "companyName", label: "Nome Empresa:" },
    { key: "nif", label: "NIF:" },
    { key: "country", label: "Pais:" },
    { key: "responsibleName", label: "Nome Responsavel:" },
    { key: "responsibleEmail", label: "Email Responsavel:" },
];

export default function CredentialList({
                                                   companies,
                                                   onViewMore,
                                                   onAccept,
                                                   onSearch,
                                                   title = "Credentials",
                                                   fieldConfig,
                                                   viewLabel = "Ver Mais",
                                                   acceptLabel = "Aceitar",
                                                   viewVariant = "primary",
                                                   acceptVariant = "success",
                                                   viewButtonClassName = "",
                                                   acceptButtonClassName = "",
                                                   headerActions,
                                                   searchPlaceholder = "Pesquisar",
                                               }) {
    const [query, setQuery] = useState("");
    const effectiveFields = fieldConfig?.length ? fieldConfig : DEFAULT_FIELDS;

    const filteredCompanies = useMemo(() => {
        if (!query.trim()) return companies;
        const lower = query.toLowerCase();
        return companies.filter((company) =>
            effectiveFields
                .map(({ key, getValue }) => {
                    const value =
                        typeof getValue === "function" ? getValue(company) : company[key];
                    return value ? String(value).toLowerCase() : "";
                })
                .some((value) => value.includes(lower))
        );
    }, [companies, query]);

    const handleSearch = (event) => {
        const value = event.target.value;
        setQuery(value);
        onSearch?.(value);
    };

    const listShouldScroll = filteredCompanies.length > 3;
    const listContainerClass = [
        "space-y-6 mb-4",
        listShouldScroll &&
        "max-h-96 overflow-y-auto pr-3 company-credentials-scroll ",
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <section className="bg-base-100 border border-base-200 rounded-3xl shadow-xl p-8 space-y-6 md:p-10">
            <header className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between pr-6">
                <h2 className="text-3xl md:text-4xl font-extrabold text-primary">
                    {title}
                </h2>

                <div className="flex flex-col gap-3 md:flex-row md:items-center md:gap-4 w-full md:w-auto md:justify-end">
                    <SearchBar
                        value={query}
                        onChange={handleSearch}
                        placeholder={searchPlaceholder}
                        className="w-full md:w-72"
                    />

                    {headerActions && (
                        <div className="w-full md:w-auto">{headerActions}</div>
                    )}
                </div>
            </header>

            <div className={listContainerClass}>
                {filteredCompanies.map((company) => (
                    <div key={company.id} className="flex-shrink-0">
                        <CredentialCard
                            company={company}
                            fieldConfig={effectiveFields}
                            onViewMore={onViewMore}
                            onAccept={onAccept}
                            viewLabel={viewLabel}
                            acceptLabel={acceptLabel}
                            viewVariant={viewVariant}
                            acceptVariant={acceptVariant}
                            viewButtonClassName={viewButtonClassName}
                            acceptButtonClassName={acceptButtonClassName}
                        />
                    </div>
                ))}

                {filteredCompanies.length === 0 && (
                    <div className="alert alert-info shadow">
                        <span>Nenhuma credencial encontrada.</span>
                    </div>
                )}
            </div>
        </section>
    );
}

CredentialList.propTypes = {
    companies: PropTypes.arrayOf(PropTypes.object).isRequired,
    onViewMore: PropTypes.func.isRequired,
    onAccept: PropTypes.func.isRequired,
    onSearch: PropTypes.func,
    title: PropTypes.string,
    fieldConfig: PropTypes.arrayOf(
        PropTypes.shape({
            key: PropTypes.string,
            label: PropTypes.string.isRequired,
            getValue: PropTypes.func,
            className: PropTypes.string,
        })
    ),
    viewLabel: PropTypes.string,
    acceptLabel: PropTypes.string,
    viewVariant: PropTypes.string,
    acceptVariant: PropTypes.string,
    viewButtonClassName: PropTypes.string,
    acceptButtonClassName: PropTypes.string,
    headerActions: PropTypes.node,
    searchPlaceholder: PropTypes.string,
};
