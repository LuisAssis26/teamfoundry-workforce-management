package com.teamfoundry.backend.superadmin.config.home;

import com.teamfoundry.backend.superadmin.enums.HomeLoginSectionType;
import com.teamfoundry.backend.superadmin.model.home.HomeLoginSection;
import com.teamfoundry.backend.superadmin.model.other.WeeklyTip;
import com.teamfoundry.backend.superadmin.repository.home.HomeLoginSectionRepository;
import com.teamfoundry.backend.superadmin.repository.other.WeeklyTipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds default content for the authenticated home (HomeLogin) so all
 * environments share the same initial data without manual setup.
 */
@Component
@RequiredArgsConstructor
@Profile("!test")
public class HomeLoginContentInitializer implements CommandLineRunner {

    private final HomeLoginSectionRepository sections;
    private final WeeklyTipRepository weeklyTips;

    @Override
    public void run(String... args) {
        if (sections.count() == 0) {
            sections.saveAll(defaultSections());
        }
        if (weeklyTips.count() == 0) {
            weeklyTips.saveAll(defaultWeeklyTips());
        }
    }

    private List<HomeLoginSection> defaultSections() {
        return List.of(
                createSection(
                        HomeLoginSectionType.HERO,
                        0,
                        "Perfil 80%",
                        "Perfil =%",
                        "Equipa atual: Montagem - Empresa Alfa\nRequisicoes disponiveis: 2 novas oportunidades",
                        "Atualizar perfil",
                        "/candidato/dados-pessoais",
                        false,
                        null,
                        null,
                        6,
                        "Ola",
                        true,
                        "Empresa atual",
                        "Ofertas disponiveis"
                ),
                createSection(
                        HomeLoginSectionType.WEEKLY_TIP,
                        1,
                        "Dica da Semana",
                        "Seguranca em primeiro lugar!",
                        "Antes de comecares o turno, confirma se todos os equipamentos estao em boas condicoes.\nPequenos cuidados evitam grandes acidentes.",
                        "Ver mais dicas",
                        "/dicas",
                        false,
                        null,
                        null,
                        6,
                        null,
                        true,
                        null,
                        null
                ),
                createSection(
                        HomeLoginSectionType.NEWS,
                        2,
                        "Noticias (GDELT)",
                        "As manchetes sao sincronizadas automaticamente a partir do GDELT. Ajuste quantos cards deseja mostrar (maximo de 6).",
                        null,
                        "Ver mais",
                        "#",
                        true,
                        null,
                        null,
                        6,
                        null,
                        true,
                        null,
                        null
                )
        );
    }

    private HomeLoginSection createSection(
            HomeLoginSectionType type,
            int order,
            String title,
            String subtitle,
            String content,
            String primaryLabel,
            String primaryUrl,
            boolean apiEnabled,
            String apiUrl,
            String apiToken,
            Integer apiMaxItems,
            String greetingPrefix,
            boolean profileBarVisible,
            String labelCurrentCompany,
            String labelOffers
    ) {
        HomeLoginSection section = new HomeLoginSection();
        section.setType(type);
        section.setDisplayOrder(order);
        section.setActive(true);
        section.setTitle(title);
        section.setSubtitle(subtitle);
        section.setContent(content);
        section.setPrimaryCtaLabel(primaryLabel);
        section.setPrimaryCtaUrl(primaryUrl);
        section.setApiEnabled(apiEnabled);
        section.setApiUrl(apiUrl);
        section.setApiToken(apiToken);
        section.setApiMaxItems(apiMaxItems);
        section.setGreetingPrefix(greetingPrefix);
        section.setProfileBarVisible(profileBarVisible);
        section.setLabelCurrentCompany(labelCurrentCompany);
        section.setLabelOffers(labelOffers);
        return section;
    }

    private List<WeeklyTip> defaultWeeklyTips() {
        WeeklyTip tip1 = new WeeklyTip();
        tip1.setCategory("Seguranca");
        tip1.setTitle("Seguranca em primeiro lugar!");
        tip1.setDescription("Antes de comecares o turno, confirma se todos os equipamentos estao em boas condicoes.\nPequenos cuidados evitam grandes acidentes.");
        tip1.setPublishedAt(null);
        tip1.setFeatured(false);
        tip1.setActive(true);
        tip1.setDisplayOrder(0);

        WeeklyTip tip2 = new WeeklyTip();
        tip2.setCategory("Seguranca");
        tip2.setTitle("Equipamento de protecao individual (EPI)");
        tip2.setDescription("Verifica se o teu EPI esta completo e em bom estado: capacete, luvas, oculos, protetores auriculares e calcado adequado.\nNunca inicies uma tarefa sem o EPI necessario.");
        tip2.setPublishedAt(null);
        tip2.setFeatured(false);
        tip2.setActive(true);
        tip2.setDisplayOrder(1);

        WeeklyTip tip3 = new WeeklyTip();
        tip3.setCategory("Seguranca");
        tip3.setTitle("Pausa e hidratacao");
        tip3.setDescription("Faz pequenas pausas ao longo do turno e bebe agua regularmente.\nO cansaço e a desidratacao aumentam o risco de erros e acidentes.");
        tip3.setPublishedAt(null);
        tip3.setFeatured(false);
        tip3.setActive(true);
        tip3.setDisplayOrder(2);

        WeeklyTip tip4 = new WeeklyTip();
        tip4.setCategory("Produtividade");
        tip4.setTitle("Organiza o posto de trabalho");
        tip4.setDescription("Mantem as ferramentas arrumadas e o espaco limpo.\nUm posto organizado poupa tempo, reduz falhas e melhora a seguranca.");
        tip4.setPublishedAt(null);
        tip4.setFeatured(false);
        tip4.setActive(true);
        tip4.setDisplayOrder(3);

        WeeklyTip tip5 = new WeeklyTip();
        tip5.setCategory("Comunicacao");
        tip5.setTitle("Tira duvidas antes de comecar");
        tip5.setDescription("Se algo nao estiver claro na tarefa, pergunta ao responsavel ou colega mais experiente.\nE melhor perguntar duas vezes do que fazer uma vez errado.");
        tip5.setPublishedAt(null);
        tip5.setFeatured(false);
        tip5.setActive(true);
        tip5.setDisplayOrder(4);

        WeeklyTip tip6 = new WeeklyTip();
        tip6.setCategory("Qualidade");
        tip6.setTitle("Confere o teu trabalho");
        tip6.setDescription("No final de cada etapa, faz uma revisao rapida ao que fizeste.\nPequenas correcoes na hora evitam retrabalho mais tarde.");
        tip6.setPublishedAt(null);
        tip6.setFeatured(false);
        tip6.setActive(true);
        tip6.setDisplayOrder(5);

        WeeklyTip tip7 = new WeeklyTip();
        tip7.setCategory("Seguranca");
        tip7.setTitle("Zonas de circulacao");
        tip7.setDescription("Respeita as zonas marcadas para peoes e para veiculos.\nEvita atalhos e caminha sempre por onde e mais seguro, nao apenas mais rapido.");
        tip7.setPublishedAt(null);
        tip7.setFeatured(false);
        tip7.setActive(true);
        tip7.setDisplayOrder(6);

        WeeklyTip tip8 = new WeeklyTip();
        tip8.setCategory("Desenvolvimento");
        tip8.setTitle("Aprende com os colegas");
        tip8.setDescription("Observa como colegas experientes executam tarefas criticas.\nPergunta por que utilizam aquele metodo e o que aprenderam com erros anteriores.");
        tip8.setPublishedAt(null);
        tip8.setFeatured(false);
        tip8.setActive(true);
        tip8.setDisplayOrder(7);

        WeeklyTip tip9 = new WeeklyTip();
        tip9.setCategory("Bem-estar");
        tip9.setTitle("Alongamentos rapidos");
        tip9.setDescription("Antes de tarefas pesadas ou repetitivas, faz 2–3 minutos de alongamentos simples.\nAjuda a prevenir lesoes musculares e fadiga.");
        tip9.setPublishedAt(null);
        tip9.setFeatured(false);
        tip9.setActive(true);
        tip9.setDisplayOrder(8);

        WeeklyTip tip10 = new WeeklyTip();
        tip10.setCategory("Seguranca");
        tip10.setTitle("Sinaliza riscos");
        tip10.setDescription("Se reparares em um cabo solto, liquido no chao ou equipamento danificado, sinaliza e informa o responsavel.\nPequena atitude, grande impacto na seguranca da equipa.");
        tip10.setPublishedAt(null);
        tip10.setFeatured(false);
        tip10.setActive(true);
        tip10.setDisplayOrder(9);

        WeeklyTip tip11 = new WeeklyTip();
        tip11.setCategory("Atitude");
        tip11.setTitle("Profissionalismo todos os dias");
        tip11.setDescription("Chegar a horas, cumprir procedimentos e respeitar colegas e supervisores constroi a tua reputacao.\nA melhor oportunidade costuma vir para quem mostra consistencia todos os dias.");
        tip11.setPublishedAt(null);
        tip11.setFeatured(false);
        tip11.setActive(true);
        tip11.setDisplayOrder(10);

        return List.of(
                tip1, tip2, tip3, tip4, tip5,
                tip6, tip7, tip8, tip9, tip10, tip11
        );
    }
}
