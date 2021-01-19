package socialnetwork.paging;

import java.util.stream.Stream;

public interface Page<E> {
    socialnetwork.paging.Pageable getPageable();

    Pageable nextPageable();

    Stream<E> getContent();

}
