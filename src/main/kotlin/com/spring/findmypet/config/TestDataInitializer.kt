package com.spring.findmypet.config

import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.PetType
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.domain.model.Role
import com.spring.findmypet.repository.LostPetRepository
import com.spring.findmypet.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Configuration
@Profile("dev")
class TestDataInitializer {

    private val logger = LoggerFactory.getLogger(TestDataInitializer::class.java)

    @Bean
    fun initTestData(
        userRepository: UserRepository,
        lostPetRepository: LostPetRepository,
        passwordEncoder: PasswordEncoder
    ): CommandLineRunner {
        return CommandLineRunner { args ->
            if (lostPetRepository.count() > 0) {
                logger.info("Baza već sadrži podatke o nestalim ljubimcima, preskačem inicijalizaciju testnih podataka")
                return@CommandLineRunner
            }

            val users = if (userRepository.count() == 0L) {
                logger.info("Početak kreiranja test korisnika...")
                val hashedPassword = passwordEncoder.encode("Lozinka123#")
                val validAvatarIds = listOf("DOG", "CAT", "RABBIT", "BIRD", "HAMSTER", "TURTLE", "GUINEA_PIG", "LIZARD")

                val testUsers = mutableListOf<User>()

                // Admin nalog
                val admin = User(
                    fullName = "Administrator",
                    email = "admin@gmail.com",
                    phoneNumber = "+381601234560",
                    password = hashedPassword,
                    role = Role.ADMIN
                )
                admin.setAvatarId("DOG")
                testUsers.add(admin)

                // Regular korisnici
                val regularUsers = listOf(
                    Triple("Marko Petrović", "marko.petrovic@gmail.com", "+381601234561"),
                    Triple("Ana Nikolić", "ana.nikolic@gmail.com", "+381601234562"),
                    Triple("Stefan Jovanović", "stefan.jovanovic@gmail.com", "+381601234563"),
                    Triple("Milica Stojanović", "milica.stojanovic@gmail.com", "+381601234564"),
                    Triple("Nikola Đorđević", "nikola.djordjevic@gmail.com", "+381601234565"),
                    Triple("Jovana Milosavljević", "jovana.milosavljevic@gmail.com", "+381601234566"),
                    Triple("Miloš Radović", "milos.radovic@gmail.com", "+381601234567"),
                    Triple("Tijana Stanković", "tijana.stankovic@gmail.com", "+381601234568"),
                    Triple("Aleksandar Popović", "aleksandar.popovic@gmail.com", "+381601234569"),
                    Triple("Dragana Ilić", "dragana.ilic@gmail.com", "+381601234570"),
                    Triple("Vladimir Marković", "vladimir.markovic@gmail.com", "+381601234571"),
                    Triple("Jelena Živković", "jelena.zivkovic@gmail.com", "+381601234572"),
                    Triple("Petar Pavlović", "petar.pavlovic@gmail.com", "+381601234573"),
                    Triple("Marija Janković", "marija.jankovic@gmail.com", "+381601234574"),
                    Triple("Dušan Todorović", "dusan.todorovic@gmail.com", "+381601234575"),
                    Triple("Ivana Kostić", "ivana.kostic@gmail.com", "+381601234576"),
                    Triple("Nemanja Đorđević", "nemanja.djordjevic@gmail.com", "+381601234577"),
                    Triple("Katarina Simić", "katarina.simic@gmail.com", "+381601234578"),
                    Triple("Luka Vasić", "luka.vasic@gmail.com", "+381601234579"),
                    Triple("Teodora Ilić", "teodora.ilic@gmail.com", "+381601234580")
                )

                regularUsers.forEachIndexed { index, (name, email, phone) ->
                    val user = User(
                        fullName = name,
                        email = email,
                        phoneNumber = phone,
                        password = hashedPassword,
                        role = Role.USER
                    )
                    user.setAvatarId(validAvatarIds[index % validAvatarIds.size])
                    testUsers.add(user)
                }

                val savedUsers = userRepository.saveAll(testUsers)
                logger.info("Kreirano ${savedUsers.size} test korisnika (1 admin + ${savedUsers.size - 1} regular)")
                savedUsers.toList()
            } else {
                userRepository.findAll()
            }

            logger.info("Počinjem inicijalizaciju testnih podataka za nestale ljubimce...")

            // Belgrade lokacije
            val locations = listOf(
                Triple("Kalemegdan, Beograd", 44.8233, 20.4489),
                Triple("Tasmajdanski park, Beograd", 44.8044, 20.4724),
                Triple("Knez Mihailova, Beograd", 44.8176, 20.4587),
                Triple("Ada Ciganlija, Beograd", 44.7866, 20.4016),
                Triple("Zemun, Beograd", 44.8500, 20.4026),
                Triple("Košutnjak, Beograd", 44.7633, 20.4284),
                Triple("Novi Beograd - Blok 45", 44.8017, 20.3805),
                Triple("Voždovac, Beograd", 44.7804, 20.4949),
                Triple("Vračar, Beograd", 44.8007, 20.4793),
                Triple("Zvezdara, Beograd", 44.8025, 20.5030),
                Triple("Dedinje, Beograd", 44.7853, 20.4558),
                Triple("Dorćol, Beograd", 44.8289, 20.4642),
                Triple("Banovo brdo, Beograd", 44.7704, 20.4139),
                Triple("Rakovica, Beograd", 44.7438, 20.4422),
                Triple("Slavija, Beograd", 44.8023, 20.4660),
                Triple("Pionirski park, Beograd", 44.8107, 20.4646),
                Triple("Ušće, Beograd", 44.8156, 20.4346),
                Triple("Bulevar kralja Aleksandra, Beograd", 44.8075, 20.4845),
                Triple("Trg Republike, Beograd", 44.8162, 20.4599),
                Triple("Terazije, Beograd", 44.8127, 20.4603),
                Triple("Skadarlija, Beograd", 44.8195, 20.4656),
                Triple("Savski nasip, Beograd", 44.8034, 20.4156),
                Triple("Cvijićeva ulica, Beograd", 44.8094, 20.4822),
                Triple("Autokomanda, Beograd", 44.7889, 20.4739),
                Triple("Senjak, Beograd", 44.7983, 20.4412),
                Triple("Palilula, Beograd", 44.8233, 20.5109),
                Triple("Crveni krst, Beograd", 44.7714, 20.4593),
                Triple("Mirijevo, Beograd", 44.7805, 20.5388)
            )

            val lostPets = mutableListOf<LostPet>()

            // PASI - 12 prijava

            // Dog 1: Akita - Riki (FOUND)
            lostPets.add(LostPet(
                user = users[1],
                petType = PetType.DOG,
                title = "Riki",
                breed = "Akita",
                color = "Svetlo braon sa belim",
                description = "Naš pas Riki je nestao tokom šetnje u parku pre nekoliko dana. Ima vrlo prijateljsku narav i voli decu. Nosi plavu ogrlicu sa ID pločicom i čipovan je. Riki je veoma umiljat i odmah prilazi ljudima. Ima specifičnu svetlu braon boju sa belim grudima i šapama. Molimo sve koji ga vide da nas odmah kontaktiraju. Porodica nam veoma nedostaje!",
                gender = "MALE",
                hasChip = true,
                address = locations[0].first,
                latitude = locations[0].second + 0.002,
                longitude = locations[0].third - 0.001,
                photos = listOf("dog_akita_1.1", "dog_akita_1.2.jpg", "dog_akita_1.3.jpg"),
                createdAt = LocalDateTime.now().minusDays(12),
                found = true,
                foundAt = LocalDateTime.now().minusDays(10)
            ))

            // Dog 2: Baset - Badi
            lostPets.add(LostPet(
                user = users[2],
                petType = PetType.DOG,
                title = "Badi",
                breed = "Baset",
                color = "Braon-beli",
                description = "Naš voljeni baset Badi je izgubljen u blizini Tasmajdana. Ima duge uši i tužan pogled što je tipično za ovu rasu. Badi je vrlo miran i prijateljski nastrojen. Ima braon-belu boju sa karakterističnim dugim ušima. Nosi crvenu ogrlicu i čipovan je. Ako ga vidite, molimo vas da nas odmah kontaktirate. Veoma nam nedostaje!",
                gender = "MALE",
                hasChip = true,
                address = locations[1].first,
                latitude = locations[1].second - 0.001,
                longitude = locations[1].third + 0.002,
                photos = listOf("dog_baset_2.1.jpg", "dog_baset_2.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(8)
            ))

            // Dog 3: Baset - Lola
            lostPets.add(LostPet(
                user = users[3],
                petType = PetType.DOG,
                title = "Lola",
                breed = "Baset",
                color = "Braon-crno-beli (tricolor)",
                description = "Naša Lola je nestala iz dvorišta juče ujutro. Ima prelepu tricolor šaru - braon, crnu i belu. Vrlo je umiljata i obično prilazi ljudima. Lola ima karakteristične duge uši i kraće noge tipične za basete. Potrebni su joj lekovi i veoma smo zabrinuti. Molimo vas, ako je vidite, pozovite nas odmah!",
                gender = "FEMALE",
                hasChip = false,
                address = locations[2].first,
                latitude = locations[2].second + 0.001,
                longitude = locations[2].third - 0.002,
                photos = listOf("dog_baset_3.1.jpg"),
                createdAt = LocalDateTime.now().minusDays(1)
            ))

            // Dog 4: Dalmatinac - Fleki
            lostPets.add(LostPet(
                user = users[4],
                petType = PetType.DOG,
                title = "Fleki",
                breed = "Dalmatinac",
                color = "Beli sa crnim tačkama",
                description = "Naš dalmatinac Fleki je pobegao tokom šetnje na Adi Ciganiliji. Ima klasičnu belu boju sa crnim tačkama. Fleki je vrlo energičan i voli da trči. Nosi zelenu ogrlicu sa našim kontakt informacijama. Čipovan je. Ako ga vidite, molimo vas da nas kontaktirate jer nam veoma nedostaje!",
                gender = "MALE",
                hasChip = true,
                address = locations[3].first,
                latitude = locations[3].second + 0.003,
                longitude = locations[3].third + 0.001,
                photos = listOf("dog_dalmatinac_4.1.jpg", "dog_dalmatinac_4.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(5)
            ))

            // Dog 5: Bokser - Boks (FOUND)
            lostPets.add(LostPet(
                user = users[5],
                petType = PetType.DOG,
                title = "Boks",
                breed = "Bokser",
                color = "Braon-beli",
                description = "Naš bokser Boks je nestao iz dvorišta u Zemunu. Ima karakterističnu braon boju sa belim oznakama na grudima i šapama. Boks je vrlo zaštitničke prirode ali i izuzetno nežan sa decom. Nosi crnu ogrlicu i čipovan je. Molimo sve koji ga vide da nas kontaktiraju!",
                gender = "MALE",
                hasChip = true,
                address = locations[4].first,
                latitude = locations[4].second - 0.002,
                longitude = locations[4].third + 0.003,
                photos = listOf("dog_bokser_5.1.jpg", "dog_bokser_5.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(7),
                found = true,
                foundAt = LocalDateTime.now().minusDays(5)
            ))

            // Dog 6: Chow Chow - Lav
            lostPets.add(LostPet(
                user = users[6],
                petType = PetType.DOG,
                title = "Lav",
                breed = "Chow Chow",
                color = "Narandžasto-braon",
                description = "Naš chow chow Lav je izgubljen u Košutnjaku tokom šetnje. Ima prelepu gustu narandžasto-braon dlaku koja ga čini sličnim lavu. Lav je vrlo nezavisan ali privržen porodici. Ima ljubičasti jezik što je karakteristika ove rase. Čipovan je i nosi plavu ogrlicu. Molimo za pomoć u pronalaženju!",
                gender = "MALE",
                hasChip = true,
                address = locations[5].first,
                latitude = locations[5].second + 0.002,
                longitude = locations[5].third - 0.001,
                photos = listOf("dog_chow_chow_6.1.jpg", "dog_chow_chow_6.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(4)
            ))

            // Dog 7: Chow Chow - Teddy
            lostPets.add(LostPet(
                user = users[7],
                petType = PetType.DOG,
                title = "Teddy",
                breed = "Chow Chow",
                color = "Crni",
                description = "Naš crni chow chow Teddy je nestao iz dvorišta u Novom Beogradu. Za razliku od tipičnih narandžastih chow chowa, Teddy ima retku potpuno crnu boju. Ima gustu dlaku i ljubičasti jezik. Teddy je vrlo privreden porodici ali može biti rezervisan prema strancima. Čipovan je. Molimo za pomoć!",
                gender = "MALE",
                hasChip = true,
                address = locations[6].first,
                latitude = locations[6].second - 0.001,
                longitude = locations[6].third + 0.002,
                photos = listOf("dog_chow_chow_7.1(crne boje).jpg"),
                createdAt = LocalDateTime.now().minusDays(3)
            ))

            // Dog 8: Haski - Luna
            lostPets.add(LostPet(
                user = users[8],
                petType = PetType.DOG,
                title = "Luna",
                breed = "Haski",
                color = "Crno-beli",
                description = "Naša haski Luna je pobegla tokom šetnje na Voždovcu. Ima prelepe plave oči i crno-belu dlaku sa karakterističnim maskama oko očiju. Luna je vrlo energična i voli da trči. Nosi ružičastu ogrlicu sa našim kontaktom. Čipovana je. Ako je vidite, molimo vas da nas odmah pozovete!",
                gender = "FEMALE",
                hasChip = true,
                address = locations[7].first,
                latitude = locations[7].second + 0.001,
                longitude = locations[7].third - 0.003,
                photos = listOf("dog_haski_8.1.jpg", "dog_haski_8.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(6)
            ))

            // Dog 9: Hokaido - Hachi
            lostPets.add(LostPet(
                user = users[9],
                petType = PetType.DOG,
                title = "Hachi",
                breed = "Hokaido",
                color = "Svetlo braon sa belim",
                description = "Naš hokaido Hachi je nestao na Vračaru. Ovo je retka japanska rasa, Hachi ima svetlo braon dlaku sa belim oznakama. Vrlo je inteligentan i privržen porodici. Hachi je smiren ali može biti rezervisan prema strancima. Čipovan je i nosi crvenu ogrlicu. Molimo za pomoć u pronalaženju našeg voljenog ljubimca!",
                gender = "MALE",
                hasChip = true,
                address = locations[8].first,
                latitude = locations[8].second - 0.002,
                longitude = locations[8].third + 0.001,
                photos = listOf("dog_hokaido_9.1.jpeg"),
                createdAt = LocalDateTime.now().minusDays(9)
            ))

            // Dog 10: Čivava - Miki
            lostPets.add(LostPet(
                user = users[10],
                petType = PetType.DOG,
                title = "Miki",
                breed = "Čivava",
                color = "Crno-smeđi",
                description = "Naš mali čivava Miki je izgubljen na Zvezdari. Ima crno-smeđu boju sa smeđim oznakama na nogama i iznad očiju. Miki je vrlo mali i tek teži 2 kilograma. Jako se plaši i verovatno se sakrio negde. Nosi malu plavu ogrlicu. Molimo vas, ako vidite malog crno-smeđeg psa, pozovite nas odmah!",
                gender = "MALE",
                hasChip = false,
                address = locations[9].first,
                latitude = locations[9].second + 0.002,
                longitude = locations[9].third - 0.002,
                photos = listOf("dog_chivava_10.1(crne boje).jpg", "dog_chivava_10.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(2)
            ))

            // Dog 11: Pekinezer - Princ (FOUND)
            lostPets.add(LostPet(
                user = users[11],
                petType = PetType.DOG,
                title = "Princ",
                breed = "pekinezer",
                color = "Crvenkasto braon",
                description = "Naš pekinezer Princ je nestao u Dedinju. Ima prelepu dugu crvenkasto-braon dlaku i karakterističan spljošteni njušak. Princ je vrlo umiljat i voli pažnju. Ima problema sa disanjem što je tipično za ovu rasu, pa smo jako zabrinuti. Nosi zlatnu ogrlicu i čipovan je. Molimo za pomoć!",
                gender = "MALE",
                hasChip = true,
                address = locations[10].first,
                latitude = locations[10].second - 0.001,
                longitude = locations[10].third + 0.003,
                photos = listOf("dog_pekinezer_11.1.jpg", "dog_pekinezer_11.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(11),
                found = true,
                foundAt = LocalDateTime.now().minusDays(9)
            ))

            // Dog 12: Malmut - Rex
            lostPets.add(LostPet(
                user = users[12],
                petType = PetType.DOG,
                title = "Rex",
                breed = "Malmut",
                color = "Sivo-beli",
                description = "Naš aljaski malmut Rex je pobegao tokom šetnje u Dorćolu. Rex je veliki pas sa gustom sivo-belom dlakom. Vrlo je prijateljski nastrojen prema svima i voli pažnju. Rex ima karakteristične oznake na glavi i gustu dlaku. Nosi crnu ogrlicu sa našim kontaktom i čipovan je. Molimo za pomoć u pronalaženju!",
                gender = "MALE",
                hasChip = true,
                address = locations[11].first,
                latitude = locations[11].second + 0.001,
                longitude = locations[11].third - 0.001,
                photos = listOf("dog_malmut_12.1.jpg", "dog_malmut_12.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(10)
            ))

            // MAČKE - 10 prijava

            // Cat 1: Sfinks - Kleopatra
            lostPets.add(LostPet(
                user = users[13],
                petType = PetType.CAT,
                title = "Kleopatra",
                breed = "Sfinks",
                color = "Sivo-roze bez dlake",
                description = "Naša sfinks mačka Kleopatra je pobegla kroz prozor na Banovom brdu. Kleopatra je potpuno bez dlake sa sivo-roze kožom. Vrlo je umiljata i voli toplinu. Nije navikla da bude napolju i sigurno joj je hladno. Ima zelene oči i velike uši. Molimo vas, ako je vidite, odmah nas kontaktirajte jer joj je potrebna posebna nega!",
                gender = "FEMALE",
                hasChip = true,
                address = locations[12].first,
                latitude = locations[12].second - 0.002,
                longitude = locations[12].third + 0.001,
                photos = listOf("cat_sfinks_1.1.jpg", "cat_sfinks_1.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(3)
            ))

            // Cat 2: Manks - Stumpy
            lostPets.add(LostPet(
                user = users[14],
                petType = PetType.CAT,
                title = "Stumpy",
                breed = "Manks",
                color = "Braon tabby sa belim",
                description = "Naša manks mačka Stumpy je nestala u Rakovici. Stumpy ima jedinstvenu karakteristiku - potpuno je bez repa što je tipično za manks rasu. Ima braon tabby šaru sa belim oznakama. Vrlo je umiljata i obično prilazi ljudima. Stumpy je čipovana i nosi ružičastu ogrlicu. Molimo za pomoć!",
                gender = "FEMALE",
                hasChip = true,
                address = locations[13].first,
                latitude = locations[13].second + 0.001,
                longitude = locations[13].third - 0.002,
                photos = listOf("cat_manks_2.1.jpg", "cat_manks_2.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(7)
            ))

            // Cat 3: Manks - Maza (FOUND)
            lostPets.add(LostPet(
                user = users[15],
                petType = PetType.CAT,
                title = "Maza",
                breed = "Manks",
                color = "Belo-braon",
                description = "Naša mačka Maza je pobegla sa terase na Slaviji. Maza je manks rasa, što znači da nema rep. Ima prelepu belu boju sa braon flekama. Vrlo je plašljiva i verovatno se sakrila negde. Čipovana je. Ako je vidite, molimo vas da pokušate da je privučete hranom i kontaktirajte nas. Jako nam nedostaje!",
                gender = "FEMALE",
                hasChip = true,
                address = locations[14].first,
                latitude = locations[14].second - 0.001,
                longitude = locations[14].third + 0.003,
                photos = listOf("cat_manks_3.1.jpg"),
                createdAt = LocalDateTime.now().minusDays(13),
                found = true,
                foundAt = LocalDateTime.now().minusDays(11)
            ))

            // Cat 4: Korat - Sivi
            lostPets.add(LostPet(
                user = users[16],
                petType = PetType.CAT,
                title = "Sivi",
                breed = "Korat",
                color = "Sivo-plava",
                description = "Naš korat mačak Sivi je nestao u Pionirskom parku. Ima prelepu srebrnastu sivo-plavu dlaku i zelene oči koje svetle. Korat je retka tajlandska rasa. Sivi je vrlo inteligentan i radoznao. Čipovan je i nosi sivu ogrlicu. Ako ga vidite, molimo vas da nas odmah kontaktirate!",
                gender = "MALE",
                hasChip = true,
                address = locations[15].first,
                latitude = locations[15].second + 0.002,
                longitude = locations[15].third - 0.001,
                photos = listOf("cat_korat_4.1.jpg", "cat_korat_4.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(5)
            ))

            // Cat 5: Korat - Luna
            lostPets.add(LostPet(
                user = users[17],
                petType = PetType.CAT,
                title = "Luna",
                breed = "Korat",
                color = "Sivo-plava",
                description = "Naša korat mačka Luna je izgubljena na Ušću. Luna ima karakterističnu srebrnastu sivo-plavu dlaku i prelepe zelene oči. Vrlo je privržena porodici ali plašljiva prema strancima. Luna je čipovana. Molimo vas, ako je vidite, pokušajte da je privučete i pozovite nas odmah!",
                gender = "FEMALE",
                hasChip = true,
                address = locations[16].first,
                latitude = locations[16].second - 0.002,
                longitude = locations[16].third + 0.002,
                photos = listOf("cat_korat_5.1.jpg"),
                createdAt = LocalDateTime.now().minusDays(4)
            ))

            // Cat 6: Savanah - Simba
            lostPets.add(LostPet(
                user = users[18],
                petType = PetType.CAT,
                title = "Simba",
                breed = "Savanah",
                color = "Zlatno-braon sa crnim pegama",
                description = "Naš savanah mačak Simba je pobegao sa Bulevara kralja Aleksandra. Simba je egzotična rasa koja izgleda kao mali leopard - ima zlatno-braonu dlaku sa crnim pegama. Vrlo je visok i atletski građen. Simba je čipovan i nosi zelenu ogrlicu. Ovo je veoma retka i skupa rasa, molimo za pomoć u pronalaženju!",
                gender = "MALE",
                hasChip = true,
                address = locations[17].first,
                latitude = locations[17].second + 0.001,
                longitude = locations[17].third - 0.003,
                photos = listOf("cat_savanah_6.1.jpeg", "cat_savanah_6.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(6)
            ))

            // Cat 7: Radgol - Bella
            lostPets.add(LostPet(
                user = users[19],
                petType = PetType.CAT,
                title = "Bella",
                breed = "Radgol",
                color = "Belo-braon",
                description = "Naša ragdoll mačka Bella je nestala na Trgu Republike. Bella ima prelepu dugu belu dlaku sa braon poentama na ušima, njušci i šapama. Ragdoll mačke su poznate po tome što postanu potpuno opuštene kada ih podignete. Bella je vrlo mirna i umiljata. Čipovana je. Molimo za pomoć!",
                gender = "FEMALE",
                hasChip = true,
                address = locations[18].first,
                latitude = locations[18].second - 0.001,
                longitude = locations[18].third + 0.001,
                photos = listOf("cat_radgol_7.1.jpg", "cat_radgol_7.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(8)
            ))

            // Cat 8: Radgol - Dušan
            lostPets.add(LostPet(
                user = users[20],
                petType = PetType.CAT,
                title = "Dušan",
                breed = "Radgol",
                color = "Sivo-beli",
                description = "Naš ragdoll mačak Dušan je izgubljen na Terazijama. Za razliku od tipičnih ragdoll mačaka, Dušan ima sivo-belu boju sa dugom mekom dlakom. Vrlo je umiljat i voli da bude nošen. Dušan je čipovan i nosi plavu ogrlicu sa našim kontaktom. Ako ga vidite, molimo vas da nas pozovete!",
                gender = "MALE",
                hasChip = true,
                address = locations[19].first,
                latitude = locations[19].second + 0.002,
                longitude = locations[19].third - 0.002,
                photos = listOf("cat_radgol_8.1.jpg", "cat_radgol_8.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(9)
            ))

            // Cat 9: Peterbald - Pharaoh
            lostPets.add(LostPet(
                user = users[1],
                petType = PetType.CAT,
                title = "Pharaoh",
                breed = "Peterbald",
                color = "Siva bez dlake",
                description = "Naš peterbald mačak Pharaoh je nestao u Skadarliji. Pharaoh je egzotična ruska rasa bez dlake, ima sivu kožu i izuzetno velike uši. Vrlo je elegantan i atletski građen. Nije navikao da bude napolju i potrebna mu je toplina. Čipovan je. Molimo vas, ako vidite mačku bez dlake, odmah nas kontaktirajte!",
                gender = "MALE",
                hasChip = true,
                address = locations[20].first,
                latitude = locations[20].second - 0.002,
                longitude = locations[20].third + 0.001,
                photos = listOf("cat_peterbald_9_1.jpeg", "cat_peterbald_9.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(2)
            ))

            // Cat 10: Burmila - Snežana
            lostPets.add(LostPet(
                user = users[2],
                petType = PetType.CAT,
                title = "Snežana",
                breed = "Burmila",
                color = "Bela",
                description = "Naša burmila mačka Snežana je pobegla sa Savskog nasipa. Snežana ima potpuno belu dlaku i zelene oči. Burmila je retka rasa nastala ukrštanjem burme i činčile. Vrlo je umiljata i druželjubiva. Snežana je čipovana i nosi belu ogrlicu sa srebrnim privescima. Molimo za pomoć u pronalaženju!",
                gender = "FEMALE",
                hasChip = true,
                address = locations[21].first,
                latitude = locations[21].second + 0.001,
                longitude = locations[21].third - 0.003,
                photos = listOf("cat_burmila_10.1.jpg", "cat_burmila_10.2.jpg"),
                createdAt = LocalDateTime.now().minusDays(1)
            ))

            // OSTALI - 6 prijava

            // Other 1: Nimfa papagaj - Pera
            lostPets.add(LostPet(
                user = users[3],
                petType = PetType.OTHER,
                title = "Pera",
                breed = null,
                color = "Sivo-žuto-narandžasta",
                description = "Naš nimfa papagaj Pera je odleteo kroz otvoreni prozor na Cvijićevoj ulici. Pera ima sivo telo, žutu glavu sa narandžastim flekama na obrazima i karakteristični ćubasti vrh na glavi. Zna da zviždi nekoliko melodija. Vrlo je privreden i dolazi kada ga zovemo po imenu. Molimo sve koji ga vide da nas kontaktiraju!",
                gender = "MALE",
                hasChip = false,
                address = locations[22].first,
                latitude = locations[22].second - 0.001,
                longitude = locations[22].third + 0.002,
                photos = listOf("other_nimfa_papagaj_1.1.jpg"),
                createdAt = LocalDateTime.now().minusDays(4)
            ))

            // Other 2: Kakadu papagaj - Beli
            lostPets.add(LostPet(
                user = users[4],
                petType = PetType.OTHER,
                title = "Beli",
                breed = null,
                color = "Beli sa žutim",
                description = "Naš kakadu papagaj Beli je odleteo sa terase na Autokomandi. Beli je veliki beli papagaj sa preleepim žutim ćubastim vrhom na glavi. Jako je glasno cvrkuće i zna da izgovori nekoliko reči uključujući svoje ime. Beli je vrlo druželjubiv i voli pažnju. Molimo za pomoć u pronalaženju našeg voljenog papagaja!",
                gender = "MALE",
                hasChip = false,
                address = locations[23].first,
                latitude = locations[23].second + 0.002,
                longitude = locations[23].third - 0.001,
                photos = listOf("other_kakadu_papagaj_2.1.jpg"),
                createdAt = LocalDateTime.now().minusDays(7)
            ))

            // Other 3: Morsko prase - Bubica
            lostPets.add(LostPet(
                user = users[5],
                petType = PetType.OTHER,
                title = "Bubica",
                breed = null,
                color = "Braon-beli",
                description = "Naše morsko prase Bubica je pobeglo iz kaveza na Senjaku. Bubica ima braon-belu dlaku sa specifičnom šarom oko očiju. Vrlo je plašljivo i verovatno se sakrilo negde. Morska prasad nisu brza, pa se sigurno nije daleko odmakla. Ako je vidite, molimo vas da je uhvatite i kontaktirate nas!",
                gender = "FEMALE",
                hasChip = false,
                address = locations[24].first,
                latitude = locations[24].second - 0.002,
                longitude = locations[24].third + 0.003,
                photos = listOf("other_morsko_prase_3.1.JPG"),
                createdAt = LocalDateTime.now().minusDays(2)
            ))

            // Other 4: Hrčak - Hopper
            lostPets.add(LostPet(
                user = users[6],
                petType = PetType.OTHER,
                title = "Hopper",
                breed = null,
                color = "Svetlo narandžasti",
                description = "Naš hrčak Hopper je pobegao iz kaveza na Paliluli. Hopper ima svetlo narandžastu dlaku i vrlo je mali. Verovatno se sakrio negde u blizini jer hrčci ne odlaze daleko. Hopper je very aktivan noću. Ako ga vidite, molimo vas da pokušate da ga uhvatite pažljivo i kontaktirajte nas!",
                gender = "MALE",
                hasChip = false,
                address = locations[25].first,
                latitude = locations[25].second + 0.001,
                longitude = locations[25].third - 0.002,
                photos = listOf("other_hrcak_4.1.jpg"),
                createdAt = LocalDateTime.now().minusDays(1)
            ))

            // Other 5: Hrčak - Mali
            lostPets.add(LostPet(
                user = users[7],
                petType = PetType.OTHER,
                title = "Mali",
                breed = null,
                color = "Zlatno-braon",
                description = "Naš hrčak Mali je nestao tokom čišćenja kaveza na Crvenom krstu. Mali ima zlatno-braon dlaku i karakteristične velike obraze u kojima čuva hranu. Vrlo je miran i prijazan. Molimo vas da proverite svoje garaže i podrume jer se hrčci često sakrivaju na tamnim mestima. Ako ga vidite, kontaktirajte nas!",
                gender = "MALE",
                hasChip = false,
                address = locations[26].first,
                latitude = locations[26].second - 0.001,
                longitude = locations[26].third + 0.001,
                photos = listOf("other_hrcak_5.1.jpg"),
                createdAt = LocalDateTime.now().minusDays(3)
            ))

            // Other 6: Zec - Zeka
            lostPets.add(LostPet(
                user = users[8],
                petType = PetType.OTHER,
                title = "Zeka",
                breed = null,
                color = "Sivo-braon",
                description = "Naš kućni zec Zeka je pobegao iz dvorišta u Mirijevu. Zeka ima sivo-braonu dlaku i duge uši. Vrlo je plašljiv i verovatno se sakrio u nečijem dvorištu ili parku. Zečevi su brzi ali ne odlaze daleko od poznatog terena. Ako vidite zeca, molimo vas da pokušate da ga uhvatite pažljivo i kontaktirajte nas!",
                gender = "MALE",
                hasChip = false,
                address = locations[27].first,
                latitude = locations[27].second + 0.002,
                longitude = locations[27].third - 0.003,
                photos = listOf("other_zec_6.1.jpg"),
                createdAt = LocalDateTime.now().minusDays(5)
            ))

            lostPetRepository.saveAll(lostPets)
            logger.info("Uspešno kreirano ${lostPets.size} testnih podataka za nestale ljubimce")
            logger.info("  - Psi: 12 prijava (2 pronađena)")
            logger.info("  - Mačke: 10 prijava (1 pronađena)")
            logger.info("  - Ostali ljubimci: 6 prijava")
        }
    }
}
