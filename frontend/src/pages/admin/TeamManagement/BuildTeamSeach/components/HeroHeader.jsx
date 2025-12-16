export default function HeroHeader({ teamName, role }) {
  return (
    <header className="space-y-1">
      <p className="text-sm text-primary/80">Selecionar funcionarios</p>
      <h1 className="text-3xl font-bold leading-tight text-primary">
        {teamName} {role ? `- ${role}` : ""}
      </h1>
    </header>
  );
}

