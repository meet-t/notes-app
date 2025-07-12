package io.noter.note.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.domain.Slice

/**
 * A generic DTO for paginated responses.
 *
 * @param T the type of elements in the data list.
 * @property elementsCount the total number of elements available (optional).
 * @property pagesCount the total number of pages available (optional).
 * @property hasPrevious indicates if there is a previous page.
 * @property hasNext indicates if there is a next page.
 * @property data the list of elements in the current page.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PageDto<T>(
    var elementsCount: Long? =0,
    var pagesCount: Int? =0,
    var hasPrevious: Boolean,
    var hasNext: Boolean,
    var data: List<T>,
) {

    constructor(slice: Slice<T>): this(
        elementsCount = null,
        pagesCount = null,
        hasNext = slice.hasNext(),
        hasPrevious = slice.hasPrevious(),
        data = slice.content
    )
}
