package com.hanghae99_team3.model.board.repository;

import com.hanghae99_team3.model.board.domain.BoardDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface BoardSearchRepository extends ElasticsearchRepository<BoardDocument,Long> {

    List<BoardDocument> findAll();

    List<BoardDocument> findByTitle(String title);
}
