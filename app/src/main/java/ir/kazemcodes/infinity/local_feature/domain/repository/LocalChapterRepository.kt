package ir.kazemcodes.infinity.local_feature.domain.repository

import ir.kazemcodes.infinity.local_feature.domain.model.ChapterEntity
import kotlinx.coroutines.flow.Flow

interface LocalChapterRepository {
    suspend fun insertChapters(chapterEntity: List<ChapterEntity>)
    suspend fun updateChapter(readingContent : String , bookName: String , chapterTitle: String)

    fun getChapterByName(bookName : String): Flow<List<ChapterEntity>>
    fun getChapterByChapter(chapterTitle : String, bookName: String): Flow<ChapterEntity?>

    fun getChapter(bookName : String): Flow<ChapterEntity?>

    fun deleteChapters(bookName: String)

}