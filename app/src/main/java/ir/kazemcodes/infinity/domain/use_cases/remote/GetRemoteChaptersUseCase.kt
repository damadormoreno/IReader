package ir.kazemcodes.infinity.domain.use_cases.remote

import ir.kazemcodes.infinity.api_feature.network.InfinityInstance
import ir.kazemcodes.infinity.domain.network.models.ParsedHttpSource
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.local_feature.domain.util.InvalidBookException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.io.IOException

class GetRemoteChaptersUseCase {

    @Throws(InvalidBookException::class)
    operator fun invoke(book: Book, source: ParsedHttpSource): Flow<Resource<List<Chapter>>> =
        flow {
                emit(Resource.Loading())
            try {
                Timber.d("Timber: GetRemoteChaptersUseCase was Called")
                val chapters = mutableListOf<Chapter>()
                var hasNext = true
               var page : Int = 1
                while (hasNext && InfinityInstance.inDetailScreen){
                    val req = source.chapterListRequest(book,page)
                    val res = source.client.newCall(req).await()
                    val content = source.chapterListParse(res)
                    chapters.addAll(content.chapters)
                    hasNext = content.hasNextPage
                    page += 1
                    Timber.d("Timber: GetRemoteChaptersUseCase + ${chapters.size} Chapters Added , current page: $page" )
                }

                Timber.d("Timber: GetRemoteChaptersUseCase was Finished Successfully with $page tries")
//                val elements = api.fetchElements(book.link, headers = mutableMapOf(
//                    Pair<String, String>("Referer", book.link)
//                ))
//                val chapters = api.fetchChapters(book, elements = elements)
                emit(Resource.Success<List<Chapter>>(chapters))
            } catch (e: HttpException) {
                emit(
                    Resource.Error<List<Chapter>>(
                        message = e.localizedMessage ?: "An Unexpected Error Occurred."
                    )
                )
            } catch (e: IOException) {
                emit(Resource.Error<List<Chapter>>(message = "Couldn't Read Remote Server, Check Your Internet Connection."))
            } catch (e: Exception) {
                emit(Resource.Error<List<Chapter>>(message = e.localizedMessage?:"An Unexpected Error Occurred"))
            }
        }

}