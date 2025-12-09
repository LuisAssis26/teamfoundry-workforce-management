package com.teamfoundry.backend.superadmin.config.home;

import com.teamfoundry.backend.superadmin.model.home.IndustryShowcase;
import com.teamfoundry.backend.superadmin.model.home.PartnerShowcase;
import com.teamfoundry.backend.superadmin.repository.home.IndustryShowcaseRepository;
import com.teamfoundry.backend.superadmin.repository.home.PartnerShowcaseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(20)
@RequiredArgsConstructor
public class HomeShowcaseSeeder implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeShowcaseSeeder.class);

    private final IndustryShowcaseRepository industries;
    private final PartnerShowcaseRepository partners;

    @Override
    public void run(String... args) {
        seedIndustries();
        seedPartners();
    }

    private void seedIndustries() {
        if (industries.count() > 0) {
            LOGGER.debug("Industries already populated; skipping seed data.");
            return;
        }

        List<IndustryShowcase> defaults = List.of(
                industry(0,
                        "Metalúrgica",
                        "Unimos especialistas em caldeiraria, soldadura e inspeção dimensional para modernizar fábricas metalúrgicas.",
                        "https://res.cloudinary.com/teamfoundry/image/upload/v1765288902/home/metal.avif_f7b5ed25-ada3-4906-bc53-cfe0463ed666.avif",
                        "https://www.teamfoundry.com/industrias/metalurgica"),
                industry(1,
                        "Energia e Utilities",
                        "Equipa multidisciplinar com especialistas em manutenção preditiva para parques eólicos e fotovoltaicos.",
                        "https://res.cloudinary.com/teamfoundry/image/upload/v1765288891/home/paineis.jpeg_f2d55e1c-46e4-4350-8fa7-fa3e926d603e.jpg",
                        "https://www.teamfoundry.com/industrias/energia"),
                industry(2,
                        "Canalização Industrial",
                        "Equipa multidisciplinar para instalar e manter redes de tubagem, sistemas de vapor e processos húmidos complexos.",
                        "https://res.cloudinary.com/teamfoundry/image/upload/v1765288911/home/canalizador.webp_32a66fd8-99de-4734-bdbc-6885acb8aa6e.webp",
                        "https://www.teamfoundry.com/industrias/canalizacao")
        );

        industries.saveAll(defaults);
        LOGGER.info("Seeded {} industry showcase record(s).", defaults.size());
    }

    private void seedPartners() {
        if (partners.count() > 0) {
            LOGGER.debug("Partners already populated; skipping seed data.");
            return;
        }

        List<PartnerShowcase> defaults = List.of(
                partner(0,
                        "MetalWave Robotics",
                        "Joint venture focada em retrofitting de linhas automotivas com robots colaborativos.",
                        "https://res.cloudinary.com/teamfoundry/image/upload/v1765286266/home/photo-1518770660439-4636190af475.jpeg_196c5b77-400b-4d81-9fde-0a18ab46d057.jpg",
                        "https://metalwaverobotics.example.com"),
                partner(1,
                        "Nordic Wind Partners",
                        "Operador europeu de parques eólicos que confia na TeamFoundry para equipas de manutenção offshore.",
                        "https://res.cloudinary.com/teamfoundry/image/upload/v1765288662/home/ventoinhas.avif_67bde8e7-5803-4adb-ad4e-0218fe2f861b.avif",
                        "https://nordicwindpartners.example.com"),
                partner(2,
                        "Pulse Logistics",
                        "Scale-up ibérica de fulfillment que escalou o headcount técnico connosco em menos de 60 dias.",
                        "https://res.cloudinary.com/teamfoundry/image/upload/v1765288674/home/logistica.avif_8caf2b77-3068-4c2a-89b2-16306ba21415.avif",
                        "https://pulselogistics.example.com"),
                partner(3,
                        "Lusitano Shipyards",
                        "Estaleiro Atlântico modernizado com equipas híbridas para soldadura e controlo de qualidade.",
                        "https://res.cloudinary.com/teamfoundry/image/upload/v1765288681/home/barcos.avif_7f56f0ec-d0ef-4c2a-9f9c-dba4fc831065.avif",
                        "https://lusitanoshipyards.example.com")
        );

        partners.saveAll(defaults);
        LOGGER.info("Seeded {} partner showcase record(s).", defaults.size());
    }

    private IndustryShowcase industry(int order,
                                      String name,
                                      String description,
                                      String imageUrl,
                                      String linkUrl) {
        IndustryShowcase entity = new IndustryShowcase();
        entity.setDisplayOrder(order);
        entity.setName(name);
        entity.setDescription(description);
        entity.setImageUrl(imageUrl);
        entity.setLinkUrl(linkUrl);
        entity.setActive(true);
        return entity;
    }

    private PartnerShowcase partner(int order,
                                    String name,
                                    String description,
                                    String imageUrl,
                                    String websiteUrl) {
        PartnerShowcase entity = new PartnerShowcase();
        entity.setDisplayOrder(order);
        entity.setName(name);
        entity.setDescription(description);
        entity.setImageUrl(imageUrl);
        entity.setWebsiteUrl(websiteUrl);
        entity.setActive(true);
        return entity;
    }
}
