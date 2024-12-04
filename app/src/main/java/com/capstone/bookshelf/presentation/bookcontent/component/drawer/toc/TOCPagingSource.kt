package com.capstone.bookshelf.presentation.bookcontent.component.drawer.toc

//class TOCPagingSource(
//    private val repository: BookRepository,
//    private val bookId: Int,
//    private val initialIndex: Int
//) : PagingSource<Int, TableOfContentEntity>() {
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TableOfContentEntity> {
//        val pageSize = params.loadSize
//        val pageIndex = params.key ?: (initialIndex / pageSize)
//        val offset = if (params.key == null) initialIndex else pageIndex * pageSize
//
//        val items = repository.getPagedTableOfContents(bookId, offset, pageSize)
//
//        return LoadResult.Page(
//            data = items,
//            prevKey = if (pageIndex == 0) null else pageIndex - 1,
//            nextKey = if (items.isEmpty()) null else pageIndex + 1
//        )
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, TableOfContentEntity>): Int? {
//        return state.anchorPosition?.let { anchorPosition ->
//            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
//                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
//        }
//    }
//}