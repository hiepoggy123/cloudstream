package com.hhpanda

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.nodes.Element

class HhpandaProvider : MainAPI() {
    override var mainUrl = "https://hhpanda.st"
    override var name = "HHPanda"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)
    override var lang = "vi"
    override val hasMainPage = true
    override val hasQuickSearch = true

    companion object {
        private const val SERVER_4K_V1 = "vip4k"
        private const val SERVER_4K_V2 = "vip4kv2"
        private const val SERVER_1080P_V1 = "tiktik"
        private const val SERVER_1080P_V2 = "pro"
        private const val SV_VIETSUB = "1"
        private const val SV_THUYET_MINH = "2"
    }

    override val mainPage = mainPageOf(
        "$mainUrl/moi-cap-nhat" to "Mới Cập Nhật",
        "$mainUrl/hoan-thanh" to "Hoàn Thành",
        "$mainUrl/most-viewed" to "Top Xem Nhiều",
        "$mainUrl/the-loai/tu-tien" to "Tu Tiên",
        "$mainUrl/the-loai/kiem-hiep" to "Kiếm Hiệp",
        "$mainUrl/the-loai/huyen-huyen" to "Huyền Huyễn",
        "$mainUrl/the-loai/co-trang" to "Cổ Trang",
        "$mainUrl/the-loai/do-thi" to "Đô Thị",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page == 1) request.data else "${request.data}/page/$page"
        val document = app.get(url, referer = mainUrl).document

        val items = document.select("article.item, div.film_list-wrap > div.flw-item, .halim-box, .post-item").mapNotNull {
            it.toSearchResult()
        }

        if (items.isEmpty()) {
            val fallbackItems = document.select("a[href*='$mainUrl/']").filter { el ->
                val href = el.attr("href")
                href.matches(Regex("$mainUrl/[a-z0-9-]+/?$")) &&
                    el.select("img").isNotEmpty()
            }.mapNotNull { it.toSearchResultFromLink() }
            return newHomePageResponse(request.name, fallbackItems)
        }

        return newHomePageResponse(request.name, items)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val link = selectFirst("a[href*='$mainUrl/']") ?: selectFirst("a") ?: return null
        val href = link.attr("href").takeIf { it.isNotBlank() } ?: return null
        if (href == mainUrl || href == "$mainUrl/") return null

        val img = selectFirst("img")
        val posterUrl = img?.attr("data-src")?.takeIf { it.isNotBlank() }
            ?: img?.attr("src")?.takeIf { it.isNotBlank() }

        val titleEl = selectFirst("h2, h3, .entry-title, .film-name, .title, a[title]")
        val title = titleEl?.text()?.trim()
            ?: link.attr("title").takeIf { it.isNotBlank() }
            ?: img?.attr("alt")?.takeIf { it.isNotBlank() }
            ?: return null

        val epInfo = selectFirst(".ep, .episode, .halim-episode-count")?.text()
        val epNum = epInfo?.let { Regex("(\\d+)").find(it)?.groupValues?.get(1)?.toIntOrNull() }

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
            addDubStatus(isDub = false, epNum)
        }
    }

    private fun Element.toSearchResultFromLink(): SearchResponse? {
        val href = attr("href").takeIf { it.isNotBlank() } ?: return null
        if (href == mainUrl || href == "$mainUrl/") return null
        val img = selectFirst("img") ?: return null
        val title = attr("title").takeIf { it.isNotBlank() }
            ?: img.attr("alt").takeIf { it.isNotBlank() }
            ?: text().trim().takeIf { it.isNotBlank() }
            ?: return null

        val posterUrl = img.attr("data-src").takeIf { it.isNotBlank() }
            ?: img.attr("src").takeIf { it.isNotBlank() }

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String, page: Int): SearchResponseList? {
        val url = if (page == 1) "$mainUrl/?s=${query}" else "$mainUrl/page/$page/?s=$query"
        val document = app.get(url, referer = mainUrl).document

        val items = document.select("article, .item, .post, .film_list-wrap > div, .search-results > div").mapNotNull {
            it.toSearchResult()
        }.ifEmpty {
            document.select("a[href]").filter { el ->
                val href = el.attr("href")
                href.matches(Regex("$mainUrl/[a-z0-9-]+/?$")) &&
                    href != mainUrl &&
                    (el.select("img").isNotEmpty() || el.text().trim().length > 2)
            }.distinctBy { it.attr("href") }.mapNotNull { it.toSearchResultFromLink() }
        }

        return items.toNewSearchResponseList()
    }

    override suspend fun quickSearch(query: String): List<SearchResponse>? = search(query, 1)

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url, referer = mainUrl).document

        val postId = Regex("""var\s+DoPostInfo\s*=\s*\{[^}]*id:\s*(\d+)""")
            .find(document.html())?.groupValues?.get(1)
            ?: document.selectFirst("[data-post-id]")?.attr("data-post-id")
            ?: throw ErrorLoadingException("Cannot find post ID")

        val title = Regex("""name:\s*"([^"]+)"""").find(document.html())?.groupValues?.get(1)
            ?: document.selectFirst("h1, .entry-title")?.text()?.trim()
            ?: throw ErrorLoadingException("Cannot find title")

        val posterUrl = Regex("""image:\s*'([^']+)'""").find(document.html())?.groupValues?.get(1)
            ?: document.selectFirst("meta[property='og:image']")?.attr("content")
            ?: document.selectFirst(".poster img, .film-poster img, img")?.let {
                it.attr("data-src").takeIf { s -> s.isNotBlank() } ?: it.attr("src")
            }

        val description = document.selectFirst("meta[name='description']")?.attr("content")
            ?: document.selectFirst(".description, .entry-content, .plot")?.text()

        val tags = document.select("a[href*='/the-loai/']").map { it.text().trim() }.filter { it.isNotBlank() }.distinct()

        val score = Regex("""ratingValue["\s:]+([0-9.]+)""").find(document.html())
            ?.groupValues?.get(1)?.toDoubleOrNull()?.times(200)?.toInt()

        val year = Regex("""(\d{4})""").find(document.html())?.groupValues?.get(1)?.toIntOrNull()

        val episodes = mutableListOf<Episode>()
        val epLinks = document.select("a[data-ep][data-post-id]")
        val epMap = mutableMapOf<String, MutableMap<String, String>>()

        for (epLink in epLinks) {
            val ep = epLink.attr("data-ep") ?: continue
            val sv = epLink.attr("data-sv") ?: "1"
            val href = epLink.attr("href").takeIf { it.isNotBlank() } ?: continue
            epMap.getOrPut(ep) { mutableMapOf() }[sv] = href
        }

        for ((epKey, svMap) in epMap.toSortedMap(compareByDescending { it })) {
            val epNum = Regex("""tap-(\d+)""").find(epKey)?.groupValues?.get(1)?.toIntOrNull()
            val href = svMap[SV_VIETSUB] ?: svMap.values.firstOrNull() ?: continue

            episodes.add(
                newEpisode(href) {
                    this.episode = epNum
                    this.name = "Tập $epNum"
                    this.data = "$postId|$epKey"
                }
            )
        }

        if (episodes.isEmpty()) {
            val epRegex = Regex("""watch-[^/]+/(tap-\d+-sv\d+\.html)""")
            val foundEps = epRegex.findAll(document.html()).map { it.groupValues[1] }.distinct()
            for (epFile in foundEps) {
                val epNum = Regex("""tap-(\d+)""").find(epFile)?.groupValues?.get(1)?.toIntOrNull()
                episodes.add(
                    newEpisode("$mainUrl/watch-${url.substringAfterLast("/")}/$epFile") {
                        this.episode = epNum
                        this.name = "Tập $epNum"
                        this.data = "$postId|${epFile.replace("-sv1.html", "").replace(".html", "")}"
                    }
                )
            }
        }

        return newTvSeriesLoadResponse(title, url, TvType.Anime, episodes) {
            this.posterUrl = posterUrl
            this.plot = description
            this.tags = tags
            this.score = score
            this.year = year
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val parts = data.split("|")
        if (parts.size < 2) return false

        val postId = parts[0]
        val epKey = parts[1]
        var foundLinks = false

        val servers = listOf(
            SERVER_4K_V1 to "4K V1",
            SERVER_4K_V2 to "4K V2",
            SERVER_1080P_V1 to "1080P V1",
            SERVER_1080P_V2 to "1080P V2"
        )

        for ((serverType, serverName) in servers) {
            try {
                for (sv in listOf(SV_VIETSUB, SV_THUYET_MINH)) {
                    val svLabel = if (sv == SV_VIETSUB) "Vietsub" else "Thuyết Minh"
                    val playerUrl = "$mainUrl/player/player.php?" +
                        "action=dox_ajax_player&post_id=$postId&chapter_st=$epKey&type=$serverType&sv=$sv"

                    val response = app.get(playerUrl, referer = "$mainUrl/").text

                    val iframeSrc = Regex("""src="(https?://[^"]+)"""")
                        .find(response)?.groupValues?.get(1) ?: continue

                    loadExtractor(iframeSrc, "$mainUrl/", subtitleCallback) { link ->
                        val newLink = newExtractorLink(
                            source = this.name,
                            name = "$serverName ($svLabel)",
                            url = link.url,
                            referer = link.referer,
                            quality = link.quality,
                            isM3u8 = link.isM3u8,
                            headers = link.headers,
                            extractorData = link.extractorData
                        )
                        callback(newLink)
                        foundLinks = true
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }

        return foundLinks
    }
}
