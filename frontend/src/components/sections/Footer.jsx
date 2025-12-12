import logo from "../../assets/images/logo/teamFoundry_LogoWhite.png";

export default function Footer() {
  return (
    <footer className="bg-primary text-primary-content">
      <div className="max-w-6xl mx-auto px-6 py-12 space-y-10">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-8">
          <div className="flex items-center gap-4">
            <img src={logo} alt="TeamFoundry" className="h-14 w-14 object-contain" />
            <div>
              <p className="text-lg font-semibold tracking-[0.25em] uppercase">TeamFoundry</p>
              <p className="text-sm text-primary-content/80">
                Forjamos equipas, movemos a indústria.
              </p>
            </div>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 text-sm">
            <div>
              <p className="font-semibold uppercase tracking-wide text-xs mb-2">Contactos</p>
              <p>hello@teamfoundry.pt</p>
              <p>+351 21 000 0000</p>
            </div>
            <div>
              <p className="font-semibold uppercase tracking-wide text-xs mb-2">Escritório</p>
              <p>R. João Saraiva 7</p>
              <p>Lisboa — Portugal</p>
            </div>
          </div>
        </div>
        <div className="border-t border-white/20 pt-6 flex flex-col md:flex-row gap-3 items-center justify-between text-sm text-primary-content/80">
          <p>© {new Date().getFullYear()} TeamFoundry. Todos os direitos reservados.</p>
          <div className="flex gap-4">
            <a href="/faq" className="hover:text-white transition-colors">
              FAQ&apos;s
            </a>
            <a href="/sobre-nos" className="hover:text-white transition-colors">
              Sobre nós
            </a>
            <a href="/privacy" className="hover:text-white transition-colors">
              Privacidade
            </a>
            <a href="/terms" className="hover:text-white transition-colors">
              Termos
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}
