package ir.kazemcodes.infinity.data.network.models

import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter


data class BooksPage(val books: List<Book> = emptyList(), val hasNextPage: Boolean = false)
data class ChaptersPage(val chapters: List<Chapter> = emptyList(), val hasNextPage: Boolean = false,
                        val progress : Float = 0f)
data class ChapterPage(val content: String, val hasNextPage: Boolean? = null)