package com.example.data.catalog

import com.example.domain.model.Difficulty
import com.example.domain.model.GeoPointData
import com.example.domain.model.RouteType
import com.example.domain.model.Trail
import com.example.domain.model.TrailDataSource
import com.example.domain.model.TrailWaypoint
import com.example.domain.model.TrekRegion
import com.example.domain.model.WaypointType

internal object VerifiedTrailsGhatsSouth {

    val trails: List<Trail> = listOf(
        // Eastern Ghats
        Trail(
            id = "eg_deomali",
            name = "Deomali Peak Ridge Trail",
            region = TrekRegion.EASTERN_GHATS,
            state = "Odisha",
            area = "Koraput • Chandragiri-Pottangi Hills",
            difficulty = Difficulty.EASY,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 6.5,
            elevationGainM = 340,
            maxElevationM = 1672,
            minElevationM = 1332,
            durationMinutes = 165,
            bestSeason = "Oct – Feb",
            description = "Highest peak in Odisha rising above the Koraput plateau in the Eastern Ghats. Rolling grassy ridges, cool morning cloud inversions, and tribal hamlet valleys make it a rewarding summit walk.",
            highlights = listOf("Highest peak of Odisha (1,672 m)", "Eastern Ghats cloud sea", "Rolling grassy ridges"),
            trailheadName = "Deomali Plateau Saddle",
            trailheadLat = 18.6708,
            trailheadLon = 82.9781,
            pathPoints = listOf(
                GeoPointData(18.6708, 82.9781, 1332.0),
                GeoPointData(18.6725, 82.9794, 1445.0),
                GeoPointData(18.6741, 82.9808, 1560.0),
                GeoPointData(18.6758, 82.9822, 1672.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Deomali Ridge Base", WaypointType.TRAILHEAD, 18.6708, 82.9781, 1332, 0.0, "Plateau trail start"),
                TrailWaypoint("Deomali Summit Watchtower", WaypointType.PEAK, 18.6758, 82.9822, 1672, 3.25, "Summit ridge overlooking Koraput valleys")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "eg_arma_konda",
            name = "Arma Konda (Jindhagada) Trail",
            region = TrekRegion.EASTERN_GHATS,
            state = "Andhra Pradesh",
            area = "Alluri Sitharama Raju • Paderu Hills",
            difficulty = Difficulty.MODERATE,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 9.2,
            elevationGainM = 690,
            maxElevationM = 1680,
            minElevationM = 990,
            durationMinutes = 270,
            bestSeason = "Nov – Feb",
            description = "Tallest summit of the Eastern Ghats near Paderu and Araku Valley. Winds past coffee shade groves and steep forest gullies onto an open windswept peak.",
            highlights = listOf("Tallest peak in Eastern Ghats", "Araku coffee shade forest", "Panoramic Andhra–Odisha border"),
            trailheadName = "Paderu – Kothavalasa Approach",
            trailheadLat = 18.2150,
            trailheadLon = 82.7120,
            pathPoints = listOf(
                GeoPointData(18.2150, 82.7120, 990.0),
                GeoPointData(18.2195, 82.7155, 1180.0),
                GeoPointData(18.2244, 82.7196, 1430.0),
                GeoPointData(18.2292, 82.7239, 1680.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Kothavalasa Trailhead", WaypointType.TRAILHEAD, 18.2150, 82.7120, 990, 0.0, "Village approach into forested ridge"),
                TrailWaypoint("Arma Konda Peak", WaypointType.PEAK, 18.2292, 82.7239, 1680, 4.6, "Highest point in Andhra Pradesh & Eastern Ghats")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "eg_swamimalai_yelagiri",
            name = "Swamimalai Hill Trail (Yelagiri)",
            region = TrekRegion.EASTERN_GHATS,
            state = "Tamil Nadu",
            area = "Yelagiri Hills • Tirupattur",
            difficulty = Difficulty.EASY,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 6.0,
            elevationGainM = 420,
            maxElevationM = 1320,
            minElevationM = 900,
            durationMinutes = 150,
            bestSeason = "Sep – Feb",
            description = "Well-defined forest footpath climbing from Mangalam village in Yelagiri through scrub woodland and rocky switchbacks to Swamimalai hill summit in the southern Eastern Ghats.",
            highlights = listOf("Mangalam forest arch trail", "Views of Yelagiri plateau", "Beginner-friendly climb"),
            trailheadName = "Mangalam Trail Arch",
            trailheadLat = 12.5872,
            trailheadLon = 78.6344,
            pathPoints = listOf(
                GeoPointData(12.5872, 78.6344, 900.0),
                GeoPointData(12.5918, 78.6290, 1040.0),
                GeoPointData(12.5965, 78.6238, 1190.0),
                GeoPointData(12.6006, 78.6186, 1320.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Mangalam Forest Arch", WaypointType.TRAILHEAD, 12.5872, 78.6344, 900, 0.0, "Start of shaded trail"),
                TrailWaypoint("Swamimalai Summit", WaypointType.PEAK, 12.6006, 78.6186, 1320, 3.0, "Highest peak in Yelagiri hills")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        // Western Ghats
        Trail(
            id = "wg_kalsubai",
            name = "Kalsubai Peak Trek",
            region = TrekRegion.WESTERN_GHATS,
            state = "Maharashtra",
            area = "Sahyadri Range • Bhandardara",
            difficulty = Difficulty.MODERATE,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 6.6,
            elevationGainM = 760,
            maxElevationM = 1646,
            minElevationM = 886,
            durationMinutes = 240,
            bestSeason = "Jun – Feb",
            description = "Maharashtra's highest peak rising dramatically above Arthur Lake in the Sahyadri range. Starting from Bari village, the trail climbs basalt plateaus and three iron ladders to the summit shrine.",
            highlights = listOf("Highest peak in Maharashtra (1,646 m)", "Iconic Sahyadri iron ladders", "Bhandardara backwater views"),
            trailheadName = "Bari Village Base",
            trailheadLat = 19.5892,
            trailheadLon = 73.7192,
            pathPoints = listOf(
                GeoPointData(19.5892, 73.7192, 886.0),
                GeoPointData(19.5924, 73.7165, 1060.0),
                GeoPointData(19.5955, 73.7138, 1240.0),
                GeoPointData(19.5982, 73.7115, 1450.0),
                GeoPointData(19.6011, 73.7094, 1646.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Bari Village Start", WaypointType.TRAILHEAD, 19.5892, 73.7192, 886, 0.0, "Hanuman temple entry path"),
                TrailWaypoint("Upper Plateau Ladder", WaypointType.VIEWPOINT, 19.5982, 73.7115, 1450, 2.5, "Final steep rock chimney ladder"),
                TrailWaypoint("Kalsubai Devi Summit", WaypointType.PEAK, 19.6011, 73.7094, 1646, 3.3, "Highest summit in Sahyadris")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "wg_harishchandragad",
            name = "Harishchandragad — Konkankada",
            region = TrekRegion.WESTERN_GHATS,
            state = "Maharashtra",
            area = "Malshej Region • Ahmednagar",
            difficulty = Difficulty.MODERATE,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 7.4,
            elevationGainM = 560,
            maxElevationM = 1424,
            minElevationM = 864,
            durationMinutes = 255,
            bestSeason = "Sep – Feb",
            description = "Ancient hill fort trek via Pachnai village leading to the carved Kedareshwar cave, Harishchandreshwar temple, and the sweeping 1,100 m concave overhang cliff of Konkankada.",
            highlights = listOf("Konkankada semicircular cliff", "Kedareshwar temple caves", "Taramati peak spur"),
            trailheadName = "Pachnai Village",
            trailheadLat = 19.3894,
            trailheadLon = 73.7603,
            pathPoints = listOf(
                GeoPointData(19.3894, 73.7603, 864.0),
                GeoPointData(19.3878, 73.7662, 1080.0),
                GeoPointData(19.3865, 73.7718, 1290.0),
                GeoPointData(19.3853, 73.7767, 1424.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Pachnai Trailhead", WaypointType.TRAILHEAD, 19.3894, 73.7603, 864, 0.0, "Shortest Sahyadri ascent route"),
                TrailWaypoint("Harishchandreshwar Temple", WaypointType.CAMPSITE, 19.3865, 73.7718, 1290, 2.6, "Stone temple complex & cave shelter"),
                TrailWaypoint("Konkankada Cliff Edge", WaypointType.VIEWPOINT, 19.3853, 73.7767, 1424, 3.7, "Dramatic western escarpment overhang")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "wg_kudremukh",
            name = "Kudremukh Peak Trail",
            region = TrekRegion.WESTERN_GHATS,
            state = "Karnataka",
            area = "Kudremukh National Park • Chikkamagaluru",
            difficulty = Difficulty.MODERATE,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 19.5,
            elevationGainM = 1080,
            maxElevationM = 1894,
            minElevationM = 814,
            durationMinutes = 450,
            bestSeason = "Oct – Feb, Jun – Sep",
            description = "Signature shola-grassland trek inside Kudremukh National Park from Mullodi village. Crosses gentle forest streams and rolling green ridgebacks shaped like a horse's face to Karnataka's third-highest peak.",
            highlights = listOf("Rolling shola grasslands", "Lobo House mid-trail stream", "1,894 m horse-face ridge"),
            trailheadName = "Mullodi Forest Checkpost",
            trailheadLat = 13.1536,
            trailheadLon = 75.2806,
            pathPoints = listOf(
                GeoPointData(13.1536, 75.2806, 814.0),
                GeoPointData(13.1472, 75.2768, 1080.0),
                GeoPointData(13.1401, 75.2725, 1360.0),
                GeoPointData(13.1342, 75.2701, 1620.0),
                GeoPointData(13.1292, 75.2683, 1894.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Mullodi Checkpost", WaypointType.TRAILHEAD, 13.1536, 75.2806, 814, 0.0, "Forest permit verification required"),
                TrailWaypoint("Ontimara Lone Tree", WaypointType.VIEWPOINT, 13.1401, 75.2725, 1360, 5.2, "Ridge transition into open grasslands"),
                TrailWaypoint("Kudremukh Summit", WaypointType.PEAK, 13.1292, 75.2683, 1894, 9.75, "Dramatic cliff-edge crest")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "wg_tadiandamol",
            name = "Tadiandamol Peak Trek",
            region = TrekRegion.WESTERN_GHATS,
            state = "Karnataka",
            area = "Kodagu (Coorg) • Kakkabe",
            difficulty = Difficulty.EASY,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 11.0,
            elevationGainM = 670,
            maxElevationM = 1748,
            minElevationM = 1078,
            durationMinutes = 270,
            bestSeason = "Oct – Mar",
            description = "Highest summit of Kodagu district starting near historic Nalknad Palace. Moves from shaded coffee estates and shola woodland onto open ridge meadows looking out toward the Arabian Sea coastal plains.",
            highlights = listOf("Highest peak in Coorg (1,748 m)", "Big Rock viewpoint", "Shola forest transition"),
            trailheadName = "Nalknad Palace Roadhead",
            trailheadLat = 12.2225,
            trailheadLon = 75.6119,
            pathPoints = listOf(
                GeoPointData(12.2225, 75.6119, 1078.0),
                GeoPointData(12.2208, 75.6108, 1280.0),
                GeoPointData(12.2191, 75.6099, 1510.0),
                GeoPointData(12.2175, 75.6092, 1748.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Nalknad Trailhead", WaypointType.TRAILHEAD, 12.2225, 75.6119, 1078, 0.0, "Estate jeep track to forest gate"),
                TrailWaypoint("Big Rock Rest", WaypointType.VIEWPOINT, 12.2191, 75.6099, 1510, 3.8, "Boulder outcrop before final ridge"),
                TrailWaypoint("Tadiandamol Peak", WaypointType.PEAK, 12.2175, 75.6092, 1748, 5.5, "Highest peak in Kodagu")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        // South India
        Trail(
            id = "si_meesapulimala",
            name = "Meesapulimala Ridge Trek",
            region = TrekRegion.SOUTH_INDIA,
            state = "Kerala",
            area = "Munnar • High Range Western Ghats",
            difficulty = Difficulty.HARD,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 14.0,
            elevationGainM = 1050,
            maxElevationM = 2640,
            minElevationM = 1590,
            durationMinutes = 390,
            bestSeason = "Sep – Mar",
            description = "Second highest peak in South India rising across eight consecutive mist-swept hills above Rhodo Valley near Munnar and Kolukkumalai.",
            highlights = listOf("2nd highest peak in South India", "Eight rolling hills traverse", "High-altitude shola forests"),
            trailheadName = "KFDC Base Camp, Silent Valley",
            trailheadLat = 10.0889,
            trailheadLon = 77.1939,
            pathPoints = listOf(
                GeoPointData(10.0889, 77.1939, 1590.0),
                GeoPointData(10.0915, 77.1972, 1920.0),
                GeoPointData(10.0942, 77.2008, 2240.0),
                GeoPointData(10.0975, 77.2044, 2640.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Rhodo Valley Base", WaypointType.TRAILHEAD, 10.0889, 77.1939, 1590, 0.0, "KFDC guided entry point"),
                TrailWaypoint("Upper Shola Saddle", WaypointType.VIEWPOINT, 10.0942, 77.2008, 2240, 4.4, "Panorama over Kolukkumalai tea ridges"),
                TrailWaypoint("Meesapulimala Summit", WaypointType.PEAK, 10.0975, 77.2044, 2640, 7.0, "2,640 m Kerala–Tamil Nadu border peak")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "si_chembra",
            name = "Chembra Peak & Heart Lake",
            region = TrekRegion.SOUTH_INDIA,
            state = "Kerala",
            area = "Meppadi • Wayanad Hills",
            difficulty = Difficulty.MODERATE,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 7.0,
            elevationGainM = 720,
            maxElevationM = 2100,
            minElevationM = 1380,
            durationMinutes = 240,
            bestSeason = "Sep – Feb",
            description = "Highest peak in Wayanad climbing through tea plantations and mossy evergreen slopes to the perennial heart-shaped Hridaya Saras lake and misty grass ridges.",
            highlights = listOf("Hridaya Saras Heart Lake", "Highest peak in Wayanad", "Wayanad tea valley vistas"),
            trailheadName = "Chembra Forest Watchtower",
            trailheadLat = 11.5297,
            trailheadLon = 76.0867,
            pathPoints = listOf(
                GeoPointData(11.5297, 76.0867, 1380.0),
                GeoPointData(11.5245, 76.0881, 1620.0),
                GeoPointData(11.5194, 76.0894, 1860.0),
                GeoPointData(11.5136, 76.0908, 2100.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Forest Checkpost", WaypointType.TRAILHEAD, 11.5297, 76.0867, 1380, 0.0, "VSS eco-tourism ticket counter"),
                TrailWaypoint("Hridaya Saras (Heart Lake)", WaypointType.WATER, 11.5194, 76.0894, 1860, 2.4, "Perennial heart-shaped mountain tarn"),
                TrailWaypoint("Chembra Upper Ridge", WaypointType.PEAK, 11.5136, 76.0908, 2100, 3.5, "Panoramic Wayanad crest")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        ),
        Trail(
            id = "si_velliangiri",
            name = "Velliangiri Seven Hills Trail",
            region = TrekRegion.SOUTH_INDIA,
            state = "Tamil Nadu",
            area = "Poondi • Nilgiri Biosphere Edge",
            difficulty = Difficulty.CHALLENGING,
            routeType = RouteType.OUT_AND_BACK,
            distanceKm = 11.6,
            elevationGainM = 1280,
            maxElevationM = 1778,
            minElevationM = 498,
            durationMinutes = 480,
            bestSeason = "Feb – May (Permitted window)",
            description = "Steep, relentless climb across seven distinct forested hills starting from Poondi temple near Coimbatore. Famous for steep rock staircases, bamboo forests, and wind-scoured summit boulders.",
            highlights = listOf("Seven consecutive hill ascents", "Pambatti Sunai spring", "Nilgiri biosphere views"),
            trailheadName = "Poondi Temple Foothills",
            trailheadLat = 10.9853,
            trailheadLon = 76.7278,
            pathPoints = listOf(
                GeoPointData(10.9853, 76.7278, 498.0),
                GeoPointData(10.9835, 76.7150, 860.0),
                GeoPointData(10.9818, 76.7025, 1180.0),
                GeoPointData(10.9802, 76.6905, 1460.0),
                GeoPointData(10.9786, 76.6789, 1778.0)
            ),
            waypoints = listOf(
                TrailWaypoint("Poondi Foothills", WaypointType.TRAILHEAD, 10.9853, 76.7278, 498, 0.0, "Forest gate & bamboo stick stalls"),
                TrailWaypoint("Pambatti Sunai (4th Hill)", WaypointType.WATER, 10.9818, 76.7025, 1180, 3.1, "Mountain spring stop"),
                TrailWaypoint("7th Hill Summit Shrine", WaypointType.PEAK, 10.9786, 76.6789, 1778, 5.8, "Sacred cave summit at 1,778 m")
            ),
            dataSource = TrailDataSource.CURATED_ATLAS,
            osmAttributionNote = "Base trail corridor & contour terrain rendered via OpenStreetMap / OpenTopoMap."
        )
    )
}
