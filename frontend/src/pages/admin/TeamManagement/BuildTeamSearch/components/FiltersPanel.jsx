import MultiSelectDropdown from "../../../../../components/ui/Dropdown/MultiSelectDropdown.jsx";
import SelectDropdown from "../../../../../components/ui/Dropdown/SelectDropdown.jsx";

export default function FiltersPanel({
  companyName,
  role,
  geoOptions = [],
  geoSelected = [],
  skillOptions = [],
  skillsSelected = [],
  functionOptions = [],
  preferredRolesSelected = [],
  statusOptions = [],
  statusSelected = "",
  onGeoChange = () => {},
  onSkillsChange = () => {},
  onPreferredRolesChange = () => {},
  onStatusChange = () => {},
  onSearch = () => {},
}) {
  return (
    <aside className="w-full rounded-2xl bg-base-100 p-6 shadow-md lg:w-80">
      <div className="space-y-4">
        <FilterTitle label="Empresa" value={companyName} />
        <FilterTitle label="Funcao" value={role} />
      </div>

      <div className="mt-6 space-y-6">
        <div className="flex justify-end ">
          <button type="button" className="btn btn-primary btn-md w-full" onClick={onSearch}>
            Pesquisar
          </button>
        </div>

        <MultiSelectDropdown
          label="Funcao preferencial"
          options={functionOptions}
          selectedOptions={preferredRolesSelected}
          onChange={onPreferredRolesChange}
          placeholder="Selecione funcao(oes)"
        />

        <MultiSelectDropdown
          label="Area Geografica"
          options={geoOptions}
          selectedOptions={geoSelected}
          onChange={onGeoChange}
          placeholder="Selecione area(s)"
        />
        <MultiSelectDropdown
          label="Competencias"
          options={skillOptions}
          selectedOptions={skillsSelected}
          onChange={onSkillsChange}
          placeholder="Selecione competencias"
        />
        <SelectDropdown
          label="Estado da proposta"
          options={statusOptions}
          value={statusSelected}
          onChange={onStatusChange}
          placeholder="Todos"
        />
      </div>
    </aside>
  );
}

function FilterTitle({ label, value }) {
  return (
    <div className="rounded-xl bg-base-200 px-4 py-2">
      <p className="text-lg font-semibold text-base-content">
        {label}: <span className="text-base-content">{value}</span>
      </p>
    </div>
  );
}
