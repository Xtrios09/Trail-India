package com.example.data.catalog

import com.example.domain.model.Difficulty
import com.example.domain.model.GeoPointData
import com.example.domain.model.RouteType
import com.example.domain.model.Trail
import com.example.domain.model.TrailDataSource
import com.example.domain.model.TrailWaypoint
import com.example.domain.model.TrekRegion
import com.example.domain.model.WaypointType

internal object VerifiedTrailsHimalayasNortheast {

    val trails: List<Trail> = listOf(
        Trail(
            id = "him_triund",
            name = "Triund Ridge Trek",
            region = TrekRegion.HIMALAYAS,
            state = "Himachal Pradesh",
            area = "Dhauladhar Range • Kangra",
            difficulty = Difficulty.MODERATE,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 9.0,
            elevationGainM = 1040,
            maxElevationM = 2842,
            minElevationM = 1802,
            durationMinutes = 270,
            bestSeason = "Mar – Jun, Sep – Dec",
            description = "Classic Dhauladhar ridge hike starting from Galu Devi temple above McLeod Ganj. The trail climbs steadily through oak and rhododendron forest before opening onto the grassy Triund meadow facing the Moon Peak and Indrahar Pass wall.",
            highlights = listOf("Dhauladhar snowline views", "Galu Devi ridge path", "Magic View Cafe tea stop"),
            trailheadName = "Galu Devi Temple, Dharamkot",
            trailheadLat = 32.2425,
            trailheadLon = 76.3323,
            pathPoints = listOf(
                GeoPointData(32.2425, 76.3323, 2110.0),
                GeoPointData(32.2462, 76.3361, 2240.0),
                GeoPointData(32.2498, 76.3396, 2380.0),
                GeoPointData(32.2535, 76.3422, 2540.0),
                GeoPointData(32.2572, 76.3455, 2690.0),
                GeoPointData(32.2608, 76.3490, 2842.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Galu Devi Checkpost", WaypointType.TRAILHEAD, 32.2425, 76.3323, 2110, 0.0, "Forest permit check and stone-paved start"),
                TrailWaypoint("Magic View Tea Point", WaypointType.VIEWPOINT, 32.2535, 76.3422, 2540, 2.7, "Mid-trail ridge rest stop overlooking Kangra valley"),
                TrailWaypoint("Triund Meadow Top", WaypointType.PEAK, 32.2608, 76.3490, 2842, 4.5, "Open alpine ridge facing Dhauladhar massif")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "him_kedarkantha",
            name = "Kedarkantha Summit Trail",
            region = TrekRegion.HIMALAYAS,
            state = "Uttarakhand",
            area = "Govind Wildlife Sanctuary • Uttarkashi",
            difficulty = Difficulty.MODERATE,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 20.0,
            elevationGainM = 1850,
            maxElevationM = 3810,
            minElevationM = 1960,
            durationMinutes = 1680,
            bestSeason = "Dec – Apr, May – Jun",
            description = "One of Garhwal's most accessible winter and spring summit climbs from Sankri village. Ascends through chir pine and silver oak forests past the frozen basin of Juda Ka Talab to a 360-degree summit ridge overlooking Swargarohini and Bandarpoonch.",
            highlights = listOf("Juda Ka Talab forest clearing", "360° Garhwal summit panorama", "Sankri village base"),
            trailheadName = "Sankri Base Village",
            trailheadLat = 31.0748,
            trailheadLon = 78.1816,
            pathPoints = listOf(
                GeoPointData(31.0748, 78.1816, 1960.0),
                GeoPointData(31.0615, 78.1822, 2320.0),
                GeoPointData(31.0492, 78.1801, 2700.0),
                GeoPointData(31.0365, 78.1765, 3250.0),
                GeoPointData(31.0285, 78.1738, 3540.0),
                GeoPointData(31.0233, 78.1717, 3810.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Sankri Trailhead", WaypointType.TRAILHEAD, 31.0748, 78.1816, 1960, 0.0, "Entry to Govind Pashu Vihar sanctuary"),
                TrailWaypoint("Juda Ka Talab", WaypointType.CAMPSITE, 31.0492, 78.1801, 2700, 4.2, "High-altitude forest clearing and pond"),
                TrailWaypoint("Kedarkantha Base Camp", WaypointType.CAMPSITE, 31.0365, 78.1765, 3250, 7.6, "Sub-alpine meadow below summit ridge"),
                TrailWaypoint("Kedarkantha Summit", WaypointType.PEAK, 31.0233, 78.1717, 3810, 10.0, "Summit shrine with views of Swargarohini")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "him_valley_of_flowers",
            name = "Valley of Flowers National Park",
            region = TrekRegion.HIMALAYAS,
            state = "Uttarakhand",
            area = "Chamoli • Nanda Devi Biosphere",
            difficulty = Difficulty.MODERATE,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 10.0,
            elevationGainM = 650,
            maxElevationM = 3658,
            minElevationM = 3048,
            durationMinutes = 330,
            bestSeason = "Jul – Sep (Monsoon bloom)",
            description = "UNESCO World Heritage alpine valley trek from Ghangaria along the Pushpawati river. Well-maintained stone trails cross glacial streams into a high-altitude meadow famous for Brahmakamal, blue poppies, and Himalayan balsam.",
            highlights = listOf("Pushpawati river gorge", "Alpine monsoon flora", "UNESCO Heritage Reserve"),
            trailheadName = "Ghangaria Park Gate",
            trailheadLat = 30.7014,
            trailheadLon = 79.5944,
            pathPoints = listOf(
                GeoPointData(30.7014, 79.5944, 3048.0),
                GeoPointData(30.7082, 79.5961, 3190.0),
                GeoPointData(30.7145, 79.5988, 3360.0),
                GeoPointData(30.7210, 79.6019, 3510.0),
                GeoPointData(30.7280, 79.6053, 3658.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Ghangaria Gate", WaypointType.TRAILHEAD, 30.7014, 79.5944, 3048, 0.0, "Forest permit checkpost; day access only"),
                TrailWaypoint("Pushpawati Bridge", WaypointType.WATER, 30.7145, 79.5988, 3360, 2.2, "Steel footbridge over glacial river"),
                TrailWaypoint("Main Flower Meadow", WaypointType.VIEWPOINT, 30.7280, 79.6053, 3658, 5.0, "Broad glacial valley floor")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "him_hampta_pass",
            name = "Hampta Pass Crossover",
            region = TrekRegion.HIMALAYAS,
            state = "Himachal Pradesh",
            area = "Kullu to Lahaul • Pir Panjal",
            difficulty = Difficulty.HARD,
            routeType = RouteType.POINT_TO_POINT,
            distanceKm = 26.0,
            elevationGainM = 1450,
            maxElevationM = 4270,
            minElevationM = 2820,
            durationMinutes = 1920,
            bestSeason = "Jun – Sep",
            description = "Dramatic crossover trek linking the lush green Kullu valley at Jobra with the stark, rain-shadow desert of Lahaul at Chatru. Follows Rani Nallah past Balu Ka Ghera up a steep pass ridge.",
            highlights = listOf("Green Kullu to arid Lahaul contrast", "Balu Ka Ghera river basin", "4,270 m Hampta Pass col"),
            trailheadName = "Jobra Roadhead, Manali",
            trailheadLat = 32.2667,
            trailheadLon = 77.2692,
            pathPoints = listOf(
                GeoPointData(32.2667, 77.2692, 2980.0),
                GeoPointData(32.2689, 77.2950, 3150.0),
                GeoPointData(32.2718, 77.3310, 3600.0),
                GeoPointData(32.2750, 77.3636, 4270.0),
                GeoPointData(32.2921, 77.3910, 3720.0),
                GeoPointData(32.3094, 77.4192, 3320.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Jobra Trailhead", WaypointType.TRAILHEAD, 32.2667, 77.2692, 2980, 0.0, "Forest trailhead above Prini"),
                TrailWaypoint("Chika Meadow", WaypointType.CAMPSITE, 32.2689, 77.2950, 3150, 3.2, "Rani Nallah riverside camp"),
                TrailWaypoint("Balu Ka Ghera", WaypointType.CAMPSITE, 32.2718, 77.3310, 3600, 10.5, "Boulder flats before pass ascent"),
                TrailWaypoint("Hampta Pass", WaypointType.PASS, 32.2750, 77.3636, 4270, 15.8, "High col on Pir Panjal crest"),
                TrailWaypoint("Chatru Roadhead", WaypointType.VIEWPOINT, 32.3094, 77.4192, 3320, 26.0, "Chandra river valley exit")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "him_markha_valley",
            name = "Markha Valley & Kongmaru La",
            region = TrekRegion.HIMALAYAS,
            state = "Ladakh",
            area = "Hemis National Park • Leh",
            difficulty = Difficulty.CHALLENGING,
            routeType = RouteType.POINT_TO_POINT,
            distanceKm = 65.0,
            elevationGainM = 2400,
            maxElevationM = 5260,
            minElevationM = 3250,
            durationMinutes = 3360,
            bestSeason = "Jun – Sep",
            description = "Iconic trans-Himalayan homestay trek through Hemis National Park. Follows the Markha River canyon past stone monasteries, barley fields, and high Nimaling pastures beneath Kang Yatse before crossing Kongmaru La at 5,260 m.",
            highlights = listOf("Nimaling plateau & Kang Yatse", "Kongmaru La 5,260 m pass", "Traditional Ladakhi homestays"),
            trailheadName = "Chilling / Skiu Bridge",
            trailheadLat = 34.0342,
            trailheadLon = 77.2067,
            pathPoints = listOf(
                GeoPointData(34.0342, 77.2067, 3250.0),
                GeoPointData(33.9985, 77.2580, 3400.0),
                GeoPointData(33.8920, 77.4210, 3760.0),
                GeoPointData(33.8165, 77.5610, 4720.0),
                GeoPointData(33.8350, 77.6190, 5260.0),
                GeoPointData(33.8992, 77.7219, 3730.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Chilling Trailhead", WaypointType.TRAILHEAD, 34.0342, 77.2067, 3250, 0.0, "Zanskar river crossing into Hemis NP"),
                TrailWaypoint("Markha Village", WaypointType.CAMPSITE, 33.8920, 77.4210, 3760, 29.0, "Mid-valley homestay settlement"),
                TrailWaypoint("Nimaling Camp", WaypointType.CAMPSITE, 33.8165, 77.5610, 4720, 48.0, "High pasture below Kang Yatse II"),
                TrailWaypoint("Kongmaru La Pass", WaypointType.PASS, 33.8350, 77.6190, 5260, 54.0, "Highest pass of the Markha circuit")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "him_goechala",
            name = "Goechala — Dzongri Ridge",
            region = TrekRegion.HIMALAYAS,
            state = "Sikkim",
            area = "Khangchendzonga National Park",
            difficulty = Difficulty.CHALLENGING,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 88.0,
            elevationGainM = 3200,
            maxElevationM = 4940,
            minElevationM = 1780,
            durationMinutes = 4320,
            bestSeason = "Apr – May, Oct – Nov",
            description = "Grand Sikkim expedition from Yuksom ascending through Rathong gorge, rhododendron forests at Tsokha, and the alpine Dzongri plateau to sunrise viewpoints facing the southeast face of Kangchendzonga.",
            highlights = listOf("Dzongri La & Thansing meadow", "Samiti Lake turquoise basin", "Kangchendzonga massif"),
            trailheadName = "Yuksom Park Office",
            trailheadLat = 27.3712,
            trailheadLon = 88.2225,
            pathPoints = listOf(
                GeoPointData(27.3712, 88.2225, 1780.0),
                GeoPointData(27.4310, 88.2040, 2200.0),
                GeoPointData(27.4720, 88.1870, 3000.0),
                GeoPointData(27.5085, 88.1650, 4025.0),
                GeoPointData(27.5620, 88.1810, 4200.0),
                GeoPointData(27.6033, 88.1869, 4940.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Yuksom", WaypointType.TRAILHEAD, 27.3712, 88.2225, 1780, 0.0, "Permit verification & trail start"),
                TrailWaypoint("Tsokha", WaypointType.CAMPSITE, 27.4720, 88.1870, 3000, 14.0, "Rhododendron belt settlement"),
                TrailWaypoint("Dzongri Plateau", WaypointType.VIEWPOINT, 27.5085, 88.1650, 4025, 23.0, "Panoramic ridge campsite"),
                TrailWaypoint("Goechala Viewpoint 1", WaypointType.PASS, 27.6033, 88.1869, 4940, 44.0, "Closest permitted viewpoint to Kangchendzonga")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        // Northeast India
        Trail(
            id = "ne_dzukou",
            name = "Dzukou Valley Ridge Trek",
            region = TrekRegion.NORTHEAST_INDIA,
            state = "Nagaland",
            area = "Kohima • Nagaland – Manipur Border",
            difficulty = Difficulty.MODERATE,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 16.0,
            elevationGainM = 780,
            maxElevationM = 2452,
            minElevationM = 1720,
            durationMinutes = 390,
            bestSeason = "Jun – Sep (Lilies), Nov – Feb",
            description = "Lush undulating bamboo-covered valley straddling Nagaland and Manipur. Steep initial stone steps from Viswema or Zakhama lead onto a sweeping emerald ridge that drops into the serene Dzukou basin.",
            highlights = listOf("Emerald dwarf bamboo hills", "Seasonal Dzukou Lily blooms", "Meandering valley streams"),
            trailheadName = "Zakhama / Viswema Trailhead",
            trailheadLat = 25.5880,
            trailheadLon = 94.1225,
            pathPoints = listOf(
                GeoPointData(25.5880, 94.1225, 1720.0),
                GeoPointData(25.5795, 94.1095, 2050.0),
                GeoPointData(25.5712, 94.0955, 2420.0),
                GeoPointData(25.5635, 94.0842, 2452.0),
                GeoPointData(25.5567, 94.0736, 2390.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Zakhama Forest Gate", WaypointType.TRAILHEAD, 25.5880, 94.1225, 1720, 0.0, "Forest permit checkpost"),
                TrailWaypoint("Dzukou Crest Viewpoint", WaypointType.VIEWPOINT, 25.5635, 94.0842, 2452, 5.8, "First sweeping vista of the bamboo bowl"),
                TrailWaypoint("Dzukou Rest House", WaypointType.CAMPSITE, 25.5567, 94.0736, 2390, 8.0, "Community-run dorm & camping ledge")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "ne_nongriat",
            name = "Nongriat Living Root Bridge",
            region = TrekRegion.NORTHEAST_INDIA,
            state = "Meghalaya",
            area = "Sohra (Cherrapunji) • East Khasi Hills",
            difficulty = Difficulty.MODERATE,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 6.4,
            elevationGainM = 620,
            maxElevationM = 890,
            minElevationM = 285,
            durationMinutes = 210,
            bestSeason = "Oct – Apr",
            description = "Iconic Khasi canyon descent of over 3,500 stone steps from Tyrna village across wire suspension bridges to Nongriat's Double Decker Living Root Bridge (Jingkieng Jri) and turquoise pools toward Rainbow Falls.",
            highlights = listOf("Umshiang Double Decker Root Bridge", "Canyon suspension bridges", "Rainbow Falls trail extension"),
            trailheadName = "Tyrna Village Steps",
            trailheadLat = 25.2528,
            trailheadLon = 91.6889,
            pathPoints = listOf(
                GeoPointData(25.2528, 91.6889, 890.0),
                GeoPointData(25.2520, 91.6820, 610.0),
                GeoPointData(25.2516, 91.6762, 410.0),
                GeoPointData(25.2512, 91.6716, 285.0),
                GeoPointData(25.2494, 91.6625, 340.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Tyrna Village", WaypointType.TRAILHEAD, 25.2528, 91.6889, 890, 0.0, "Stone staircase descent into canyon"),
                TrailWaypoint("Double Decker Root Bridge", WaypointType.VIEWPOINT, 25.2512, 91.6716, 285, 2.0, "Living Ficus elastica bridge over Umshiang river"),
                TrailWaypoint("Rainbow Falls", WaypointType.WATER, 25.2494, 91.6625, 340, 3.2, "Waterfall pool deep inside canyon")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "ne_sandakphu",
            name = "Sandakphu — Singalila Ridge",
            region = TrekRegion.NORTHEAST_INDIA,
            state = "West Bengal",
            area = "Singalila National Park • Darjeeling",
            difficulty = Difficulty.HARD,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 46.0,
            elevationGainM = 1710,
            maxElevationM = 3636,
            minElevationM = 1926,
            durationMinutes = 1920,
            bestSeason = "Mar – May, Oct – Dec",
            description = "Famous Eastern Himalayan border ridge trek from Manebhanjan along India–Nepal crest through Tumling and Kalipokhri to West Bengal's highest point at Sandakphu, overlooking Sleeping Buddha (Kangchendzonga) and Everest.",
            highlights = listOf("Four 8,000 m peaks panorama", "Singalila rhododendron forests", "Kalipokhri sacred pond"),
            trailheadName = "Manebhanjan Checkpost",
            trailheadLat = 26.9875,
            trailheadLon = 88.1206,
            pathPoints = listOf(
                GeoPointData(26.9875, 88.1206, 1926.0),
                GeoPointData(27.0185, 88.0810, 2800.0),
                GeoPointData(27.0325, 88.0680, 2970.0),
                GeoPointData(27.0680, 88.0310, 3180.0),
                GeoPointData(27.1056, 88.0011, 3636.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Manebhanjan", WaypointType.TRAILHEAD, 26.9875, 88.1206, 1926, 0.0, "Singalila NP permit entry"),
                TrailWaypoint("Tumling Ridge", WaypointType.CAMPSITE, 27.0325, 88.0680, 2970, 11.0, "Tea-house hamlet on the crest"),
                TrailWaypoint("Kalipokhri", WaypointType.WATER, 27.0680, 88.0310, 3180, 17.5, "Black-water alpine lake"),
                TrailWaypoint("Sandakphu Summit", WaypointType.PEAK, 27.1056, 88.0011, 3636, 23.0, "Highest point in West Bengal")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        )
    )
}
