package com.spring.findmypet.config

import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.PetType
import com.spring.findmypet.repository.LostPetRepository
import com.spring.findmypet.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@Configuration
@Profile("dev")
class TestDataInitializer {
    
    private val logger = LoggerFactory.getLogger(TestDataInitializer::class.java)
    
    @Bean
    fun initTestData(
        userRepository: UserRepository,
        lostPetRepository: LostPetRepository
    ): CommandLineRunner {
        return CommandLineRunner { args ->
            if (lostPetRepository.count() > 0) {
                logger.info("Baza već sadrži podatke o nestalim ljubimcima, preskačem inicijalizaciju testnih podataka")
                return@CommandLineRunner
            }
            
            val users = userRepository.findAll()
            if (users.isEmpty()) {
                logger.warn("Nema korisnika u bazi, ne mogu da kreiram testne podatke za nestale ljubimce")
                return@CommandLineRunner
            }
            
            logger.info("Počinjem inicijalizaciju testnih podataka za nestale ljubimce...")
            
            // Podaci za pse
            val dogNames = listOf(
                "Max", "Bobi", "Laki", "Aron", "Šarko", "Reks", "Čarli", "Leo", "Džeki", "Badi",
                "Lusi", "Bela", "Meda", "Tera", "As", "Bak", "Zoi", "Dona", "Fleki", "Lola",
                "Riko", "Tedi", "Maks", "Beni", "Doni", "Mila", "Niki", "Luna", "Sam", "Lajka"
            )
            val dogBreeds = listOf(
                "Labrador", "Nemački ovčar", "Zlatni retriver", "Haski", "Buldog", "Bigl", "Pudla", "Mešanac",
                "Rotvajler", "Jazavičar", "Mops", "Border koli", "Dalmatinac", "Bokser", "Koker španijel",
                "Bernski planinski pas", "Šarplaninac", "Maltezer", "Čivava", "Šnaucer", "Šarpej",
                "Doberman", "King Čarls španijel", "Pekinezer", "Pinč", "Seter", "Samojed", "Akita"
            )
            val dogColors = listOf(
                "Zlatna", "Crna", "Braon", "Bela", "Crno-bela", "Siva", "Crvenkasta", "Smeđa", 
                "Krem", "Žućkasta", "Sivo-bela", "Tri-color", "Tigrasta", "Tamno-braon"
            )
            
            // Podaci za mačke
            val catNames = listOf(
                "Mačak", "Mrvica", "Maza", "Meda", "Flekica", "Mica", "Čupko", "Cica", "Garfi", "Tom",
                "Luna", "Leo", "Mali", "Peri", "Lola", "Mici", "Panda", "Maca", "Siki", "Pero",
                "Sima", "Mici", "Caki", "Sivko", "Belka", "Žucko", "Crni", "Miki", "Bela", "Šapica"
            )
            val catBreeds = listOf(
                "Persijska", "Sijamska", "Maine Coon", "Bengalska", "Ruska plava", "Britanska kratkodlaka", 
                "Evropska kratkodlaka", "Mešanac", "Abesinska", "Egipatska Mau", "Ragdoll", "Sfinks",
                "Sibirska", "Norveška šumska", "Burma", "Devon Reks", "Škotska preklopna", "Himalajska", 
                "Orijentalna", "Turska Van", "Savana", "Domaća"
            )
            val catColors = listOf(
                "Siva", "Crna", "Bela", "Narandžasta", "Šarena", "Crno-bela", "Trobojna", "Tigrasta",
                "Srebrna", "Krem", "Sivo-bela", "Plava", "Čokolada", "Lila", "Kaliko", "Tabi", "Crvena"
            )
            
            // Podatci za ostale ljubimce
            val otherNames = listOf(
                "Hopper", "Bubica", "Ptica", "Skočko", "Zvonko", "Soni", "Mali", "Toto", "Mika",
                "Bambi", "Peri", "Cvrkuti", "Zeka", "Koko", "Jumbo", "Pipa", "Ušati", "Žućko", "Trčko"
            )
            val otherTypes = listOf(
                "Zec", "Papagaj", "Hrčak", "Zamorče", "Kornjača", "Morsko prase", "Činčila",
                "Iguana", "Boa", "Kanarinski papagaj", "Ara", "Veverica", "Afrički tvor", "Jež"
            )
            val otherColors = listOf(
                "Bela", "Siva", "Zelena", "Šarena", "Braon", "Žuta", "Plava", "Crvena", 
                "Narandžasta", "Crna", "Zlatna", "Ljubičasta", "Crveno-žuta", "Zeleno-plava"
            )

            // Proširena lista slika
            val dogPhotos = listOf(
                "dog_1.jpg", "dog_2.jpg", "dog_3.jpg", 
                "dog_4.jpg", "dog_5.jpg", "dog_6.jpg", "dog_7.jpg", "dog_8.jpg", 
                "dog_9.jpg", "dog_10.jpg", "dog_11.jpg", "dog_12.jpg",
                "dog_13.jpg", "dog_14.jpg", "dog_15.jpg", "dog_16.jpg", "dog_17.jpg", 
                "dog_18.jpg", "dog_19.jpg", "dog_20.jpg"
            )
            
            val catPhotos = listOf(
                "cat_1.jpg", "cat_2.jpg", "cat_3.jpg", 
                "cat_4.jpg", "cat_5.jpg", "cat_6.jpg", "cat_7.jpg", "cat_8.jpg",
                "cat_9.jpg", "cat_10.jpg", "cat_11.jpg", "cat_12.jpg", "cat_13.jpg", 
                "cat_14.jpg", "cat_15.jpg", "cat_16.jpg", "cat_17.jpg", "cat_18.jpg"
            )
            
            val otherPhotos = listOf(
                "other_1.jpg", "other_2.jpg", "other_3.jpg", "other_4.jpg", 
                "other_5.jpg", "other_6.jpg", "other_7.jpg", "other_8.jpg",
                "other_9.jpg", "other_10.jpg", "other_11.jpg", "other_12.jpg", 
                "other_13.jpg", "other_14.jpg", "other_15.jpg"
            )
            
            // Generisaćemo za neke ljubimce više slika
            val multiPhotoChance = 0.35 // Povećano na 35% šansa da će ljubimac imati više slika
            
            // Opšti opisi - proširena lista
            val generalDescriptions = listOf(
                "Izgubljen u parku tokom šetnje. Veoma je prijateljski nastrojen prema ljudima. Ima ogrlicu sa imenom.",
                "Pobegao iz dvorišta. Veoma se plaši glasnih zvukova. Reaguje na svoje ime.",
                "Nestao iz dvorišta. Ima specifičnu šaru na leđima. Veoma je umiljat.",
                "Izgubljen tokom selidbe. Ima čip i ogrlicu sa kontakt informacijama.",
                "Pobegao kroz otvorena vrata. Veoma je srećan kada vidi ljude, odmah prilazi.",
                "Nestao iz blizine kuće. Ima malo oštećenje na desnom uhu. Jako je privržen.",
                "Izgubljen prilikom posete veterinaru. Ima specifičan hod zbog stare povrede.",
                "Pobegao zbog grmljavine. Vrlo je plašljiv, ali će prići ako mu se ponudi hrana.",
                "Izgubljen u šetnji. Ima posebnu šaru na nosu u obliku srca. Veoma je umiljat prema deci.",
                "Nestao u blizini marketa. Ima crnu fleku oko levog oka. Ima problema sa vidom.",
                "Pobegao prilikom puštanja sa povodca. Energičan je i voli da trči. Reaguje na zvuk pištaljke.",
                "Nestao tokom porodičnog piknika. Nosi zelenu ogrlicu sa imenom i brojem telefona.",
                "Izgubljen tokom letovanja. Ima čip, ali ne nosi ogrlicu. Vrlo je privržen porodici.",
                "Pobegao sa dvorišta zbog vatrometa. Veoma se plaši jakih zvukova. Neće prići strancima.",
                "Nestao u blizini škole. Ima jedinstvenu šaru na repu. Voli da se igra sa loptom."
            )
            
            // Proširena lista detaljnih opisa
            val detailedDescriptionsDog = listOf(
                "Naš voljeni pas je nestao tokom jučerašnje šetnje u parku. Vrlo je prijateljski nastrojen prema ljudima i drugim psima. Ima prepoznatljivu ogrlicu sa ID pločicom i čipovan je. Molimo sve koji ga vide da nas odmah kontaktiraju.",
                "Izgubio se našoj kućni ljubimac. Jako se plaši glasnih zvukova, posebno grmljavine. Obično se krije u mirnim, tamnim mestima kada je uplašen. Ima malo sivo krzno sa belim šarama i nosi plavu ogrlicu.",
                "Naš pas je pobegao kada je komšija ostavio kapiju otvorenu. On je star i ima problema sa sluhom, pa možda neće reagovati kada ga zovete. Ima braon boju sa belim flekama na grudima i nosi crvenu ogrlicu.",
                "Izgubljen je naš mali pas tokom jučerašnje šetnje u okolini. Odaziva se na svoje ime i vrlo je umiljat. Ima specifičnu šaru na leđima nalik na srce. Ima ogrlicu sa kontaktom i čipovan je.",
                "Naš voljeni pas je pobegao iz dvorišta. On je veoma prijateljski nastrojen, ali može biti plašljiv prema nepoznatim ljudima. Ima crnu dlaku sa belim flekama na grudima i nogama. Molimo za pomoć u pronalaženju!",
                "Naš pas je nestao jutros iz dvorišta. Izuzetno je druželjubiv i voli da prilazi deci. Ima svetlo braon krzno sa belim tačkama. Nosi crvenu ogrlicu sa našim kontaktom. Veoma nam nedostaje, molimo za pomoć!",
                "Izgubljen je naš porodični ljubimac tokom jučerašnje oluje. Veoma je uplašen i verovatno se krije negde. Ima svetlo sivu dlaku sa belim šapama. Star je 10 godina i potrebni su mu lekovi. Ako ga vidite, molimo vas da nas kontaktirate.",
                "Naš maleni pas je pobegao dok smo bili u kupovini. Ima teget ogrlicu sa ID tagom. Veoma je umiljat i voli da se igra. Ima crnu kratku dlaku sa belim flekama po stomaku. Molimo za pomoć u pronalaženju.",
                "Izgubljen je naš pas tokom šetnje na Adi. Veoma je druželjubiv i voli da trči za loptom. Ima braon boju sa crnim tačkama po leđima. Nosi plavu ogrlicu sa adresom. Molimo za pomoć!",
                "Naš stariji pas je nestao dok smo bili u poseti prijateljima. Ima problema sa kretanjem i potrebni su mu lekovi. Ima sivu dlaku sa belim šapama. Veoma je miran i prići će ako ga pozovete po imenu. Molimo za pomoć!"
            )
            
            val detailedDescriptionsCat = listOf(
                "Naša mačka je nestala iz dvorišta. Ima prepoznatljivu crno-belu boju sa specifičnom šarom oko očiju koja podseća na masku. Vrlo je umiljata i odaziva se na svoje ime. Molim vas da nas kontaktirate ako je vidite.",
                "Izgubljena je naša mačka. Ima jedinstvenu narandžastu boju krzna sa belim šapama. Jako je privržena i obično prilazi ljudima. Ona je čipovana i ima svetlo plavu ogrlicu sa ID pločicom.",
                "Naša mačka je nestala pre dva dana. Ima posebnu trobojna šaru sa dominantnom sivom bojom. Vrlo je plašljiva prema nepoznatima i verovatno se krije negde. Molimo za pomoć u pronalaženju!",
                "Izgubljena je naša starija mačka. Ima potpuno crno krzno sa malim belim flekom na grudima. Ona je veoma mirna i prijateljska. Brine nas jer uzima lekove i potrebna joj je posebna nega.",
                "Naša mačka je pobegla kroz otvoreni prozor. Ona ima prelepo dugačko sivo krzno i zelene oči. Nije navikla da bude napolju i verovatno je uplašena. Molimo za pomoć u pronalaženju!",
                "Izgubljena je naša mala mačka. Ima potpuno belu dlaku sa crnim repom. Vrlo je plašljiva prema strancima, ali je inače veoma umiljata. Nije navikla da bude napolju. Molimo za pomoć u pronalaženju!",
                "Nestala je naša mačka tokom selidbe. Ima narandžasto-bele pruge i zelene oči. Veoma je znatiželjna i možda je ušla u nečije dvorište. Molimo vas da pogledate svoja dvorišta i garaže.",
                "Izgubljena je naša mačka. Ona ima unikatnu sivu boju krzna sa tamnijim prugama. Veoma je stidljiva i verovatno se sakrila negde. Molimo da pogledate oko svojih kuća i podruma.",
                "Naša mačka je nestala sinoć. Ima potpuno crno krzno bez ikakvih belih oznaka. Vrlo je umiljata i obično prilazi ljudima. Ako je vidite, molimo vas da nas kontaktirate.",
                "Izgubljena je naša mačka. Ima tigrastopaternirano smeđe-crno krzno i velike žute oči. Odaziva se na svoje ime i veoma je privržena. Molimo za pomoć u pronalaženju."
            )
            
            val detailedDescriptionsOther = listOf(
                "Izgubljen je naš mali zec. Ima potpuno belo krzno sa crnim ušima. Vrlo je plašljiv i verovatno se sakrio negde. Molimo vas da nas kontaktirate ako ga vidite.",
                "Naš papagaj je izleteo kroz otvoreni prozor. Ima prepoznatljivu zelenu boju sa žutom glavom. Zna da izgovori nekoliko reči. Molimo za pomoć u pronalaženju!",
                "Izgubljen je naš mali hrčak tokom čišćenja kaveza. Ima zlatno-braon krzno. Vrlo je miran i prijateljski nastrojen. Molimo vas da nas kontaktirate ako ga vidite.",
                "Naša kornjača je nestala iz bašte. Ima specifičnu šaru na oklopu. Nije brza, pa se verovatno nije daleko odmakla. Molimo za pomoć u pronalaženju!",
                "Izgubljeno je naše malo zamorče. Ima crno-belo krzno sa specifičnom šarom koja podseća na naočare oko očiju. Vrlo je plašljivo, ali će prići ako mu ponudite hranu.",
                "Izgubljen je naš kućni zec tokom čišćenja kaveza. Ima potpuno sivo krzno sa belim ušima. Veoma je pitom i naviknut je na ljude. Molimo za pomoć u pronalaženju!",
                "Naš papagaj je odleteo kroz otvoreni prozor. Ima živopisno žuto-plavo perje. Odaziva se na svoje ime i zna nekoliko reči. Molimo vas da nas kontaktirate ako ga vidite!",
                "Izgubljena je naša činčila. Ima meko srebrno krzno. Veoma je plašljiva i verovatno se sakrila negde u blizini. Molimo da proverite svoja dvorišta i garaže.",
                "Nestala je naša iguana iz terarijuma. Ima svetlo zeleno telo sa tamnim prugama. Potrebni su joj posebni uslovi i hrana. Molimo za pomoć u pronalaženju!",
                "Izgubljen je naš afrički tvor. Ima smeđe krzno sa crnim šarama. Veoma je radoznao i možda je ušao u nečije dvorište. Molimo za pomoć!"
            )
            
            val detailedDescriptions = mapOf(
                PetType.DOG to detailedDescriptionsDog,
                PetType.CAT to detailedDescriptionsCat,
                PetType.OTHER to detailedDescriptionsOther
            )
            
            // Lokacije u Beogradu (format: naziv, latitude, longitude)
            val locations = listOf(
                Triple("Kalemegdan", 44.8233, 20.4489),
                Triple("Tasmajdanski park", 44.8044, 20.4724),
                Triple("Knez Mihailova", 44.8176, 20.4587),
                Triple("Ada Ciganlija", 44.7866, 20.4016),
                Triple("Zemun", 44.8500, 20.4026),
                Triple("Košutnjak", 44.7633, 20.4284),
                Triple("Novi Beograd - Blok 45", 44.8017, 20.3805),
                Triple("Voždovac", 44.7804, 20.4949),
                Triple("Vračar", 44.8007, 20.4793),
                Triple("Zvezdara", 44.8025, 20.5030),
                Triple("Dedinje", 44.7853, 20.4558),
                Triple("Dorćol", 44.8289, 20.4642),
                Triple("Banovo brdo", 44.7704, 20.4139),
                Triple("Rakovica", 44.7438, 20.4422),
                Triple("Slavija", 44.8023, 20.4660),
                Triple("Tašmajdan", 44.8070, 20.4736),
                Triple("Pionirski park", 44.8107, 20.4646),
                Triple("Park Manjež", 44.8068, 20.4595),
                Triple("Park kod Vukovog spomenika", 44.8037, 20.4769),
                Triple("Ušće", 44.8156, 20.4346),
                Triple("Bulevar kralja Aleksandra", 44.8075, 20.4845),
                Triple("Trg Republike", 44.8162, 20.4599),
                Triple("Terazije", 44.8127, 20.4603),
                Triple("Nušićeva ulica", 44.8142, 20.4588),
                Triple("Skadarlija", 44.8195, 20.4656)
            )
            
            val lostPets = mutableListOf<LostPet>()
            val random = Random(System.currentTimeMillis())
            
            // Kreiranje pasa (40 pasa)
            repeat(40) { index ->
                val user = users[random.nextInt(users.size)]
                val timeOffset = random.nextLong(0, 15) // do 15 dana unazad
                val hours = random.nextInt(0, 24)
                val minutes = random.nextInt(0, 60)
                
                val createdAt = LocalDateTime.now()
                    .minus(timeOffset, ChronoUnit.DAYS)
                    .minus(hours.toLong(), ChronoUnit.HOURS)
                    .minus(minutes.toLong(), ChronoUnit.MINUTES)
                
                val location = locations[random.nextInt(locations.size)]
                val name = dogNames[random.nextInt(dogNames.size)]
                
                // Osiguravamo raznovrsnost rasa i boja za bolje testiranje filtera
                val breed = if (index % 4 == 0 && index < dogBreeds.size) {
                    // Isti pas može biti više puta, ali na različitim lokacijama
                    dogBreeds[index % dogBreeds.size] 
                } else {
                    dogBreeds[random.nextInt(dogBreeds.size)]
                }
                
                val color = if (index % 5 == 0 && index < dogColors.size) { 
                    // Osiguravamo da imamo dovoljno pasa iste boje za testiranje filtera
                    dogColors[index % dogColors.size]
                } else {
                    dogColors[random.nextInt(dogColors.size)]
                }
                
                val description = detailedDescriptions[PetType.DOG]!![random.nextInt(detailedDescriptions[PetType.DOG]!!.size)]
                val gender = if (index % 2 == 0) "MALE" else "FEMALE" // Balansiramo polove
                val hasChip = index % 3 == 0 // Osiguravamo da neki imaju čip, neki ne
                
                // Određivanje broja slika - neki ljubimci imaju više slika
                val photos = if (random.nextDouble() < multiPhotoChance) {
                    // 35% šansa za više slika (2-6)
                    val photoCount = random.nextInt(2, minOf(6, dogPhotos.size) + 1)
                    dogPhotos.shuffled().take(photoCount)
                } else {
                    // 65% šansa za jednu sliku
                    listOf(dogPhotos[random.nextInt(dogPhotos.size)])
                }
                
                val lostPet = LostPet(
                    user = user,
                    petType = PetType.DOG,
                    title = name,
                    breed = breed,
                    color = color,
                    description = description,
                    gender = gender,
                    hasChip = hasChip,
                    address = location.first,
                    latitude = location.second + (random.nextDouble() - 0.5) * 0.005, // mala nasumična varijacija lokacije
                    longitude = location.third + (random.nextDouble() - 0.5) * 0.005,
                    photos = photos,
                    createdAt = createdAt
                )
                
                lostPets.add(lostPet)
            }
            
            repeat(30) { index ->
                val user = users[random.nextInt(users.size)]
                val timeOffset = random.nextLong(0, 15) // do 15 dana unazad
                val hours = random.nextInt(0, 24)
                val minutes = random.nextInt(0, 60)
                
                val createdAt = LocalDateTime.now()
                    .minus(timeOffset, ChronoUnit.DAYS)
                    .minus(hours.toLong(), ChronoUnit.HOURS)
                    .minus(minutes.toLong(), ChronoUnit.MINUTES)
                
                val location = locations[random.nextInt(locations.size)]
                val name = catNames[random.nextInt(catNames.size)]
                
                // Osiguravamo raznovrsnost rasa i boja za bolje testiranje filtera
                val breed = if (index % 4 == 0 && index < catBreeds.size) {
                    catBreeds[index % catBreeds.size]
                } else {
                    catBreeds[random.nextInt(catBreeds.size)]
                }
                
                val color = if (index % 5 == 0 && index < catColors.size) {
                    catColors[index % catColors.size]
                } else {
                    catColors[random.nextInt(catColors.size)]
                }
                
                val description = detailedDescriptions[PetType.CAT]!![random.nextInt(detailedDescriptions[PetType.CAT]!!.size)]
                val gender = if (index % 2 == 0) "MALE" else "FEMALE" // Balansiramo polove
                val hasChip = index % 3 == 0 // Osiguravamo da neki imaju čip, neki ne
                
                // Određivanje broja slika
                val photos = if (random.nextDouble() < multiPhotoChance) {
                    // 35% šansa za više slika (2-5)
                    val photoCount = random.nextInt(2, minOf(5, catPhotos.size) + 1)
                    catPhotos.shuffled().take(photoCount)
                } else {
                    // 65% šansa za jednu sliku
                    listOf(catPhotos[random.nextInt(catPhotos.size)])
                }
                
                val lostPet = LostPet(
                    user = user,
                    petType = PetType.CAT,
                    title = name,
                    breed = breed,
                    color = color,
                    description = description,
                    gender = gender,
                    hasChip = hasChip,
                    address = location.first,
                    latitude = location.second + (random.nextDouble() - 0.5) * 0.005, 
                    longitude = location.third + (random.nextDouble() - 0.5) * 0.005,
                    photos = photos,
                    createdAt = createdAt
                )
                
                lostPets.add(lostPet)
            }
            
            // Kreiranje ostalih ljubimaca (15 ostalih)
            repeat(15) { index ->
                val user = users[random.nextInt(users.size)]
                val timeOffset = random.nextLong(0, 15) // do 15 dana unazad
                val hours = random.nextInt(0, 24)
                val minutes = random.nextInt(0, 60)
                
                val createdAt = LocalDateTime.now()
                    .minus(timeOffset, ChronoUnit.DAYS)
                    .minus(hours.toLong(), ChronoUnit.HOURS)
                    .minus(minutes.toLong(), ChronoUnit.MINUTES)
                
                val location = locations[random.nextInt(locations.size)]
                val name = otherNames[random.nextInt(otherNames.size)]
                
                // Osiguravamo raznovrsnost rasa i boja za bolje testiranje filtera
                val breed = if (index % 3 == 0 && index < otherTypes.size) {
                    otherTypes[index % otherTypes.size]
                } else {
                    otherTypes[random.nextInt(otherTypes.size)]
                }
                
                val color = if (index % 3 == 0 && index < otherColors.size) {
                    otherColors[index % otherColors.size]
                } else {
                    otherColors[random.nextInt(otherColors.size)]
                }
                
                val description = detailedDescriptions[PetType.OTHER]!![random.nextInt(detailedDescriptions[PetType.OTHER]!!.size)]
                val gender = if (index % 2 == 0) "MALE" else "FEMALE" // Balansiramo polove
                val hasChip = index % 5 == 0 // Mali procenat ostalih ljubimaca ima čip
                
                // Određivanje broja slika
                val photos = if (random.nextDouble() < multiPhotoChance) {
                    // 35% šansa za više slika (2-4)
                    val photoCount = random.nextInt(2, minOf(4, otherPhotos.size) + 1)
                    otherPhotos.shuffled().take(photoCount)
                } else {
                    // 65% šansa za jednu sliku
                    listOf(otherPhotos[random.nextInt(otherPhotos.size)])
                }
                
                val lostPet = LostPet(
                    user = user,
                    petType = PetType.OTHER,
                    title = name,
                    breed = breed,
                    color = color,
                    description = description,
                    gender = gender,
                    hasChip = hasChip,
                    address = location.first,
                    latitude = location.second + (random.nextDouble() - 0.5) * 0.005,
                    longitude = location.third + (random.nextDouble() - 0.5) * 0.005,
                    photos = photos,
                    createdAt = createdAt
                )
                
                lostPets.add(lostPet)
            }
            
            lostPetRepository.saveAll(lostPets)
            logger.info("Uspešno kreirano ${lostPets.size} testnih podataka za nestale ljubimce")
        }
    }
} 