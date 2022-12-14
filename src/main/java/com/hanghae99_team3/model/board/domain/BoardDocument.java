package com.hanghae99_team3.model.board.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.elasticsearch.annotations.DateFormat.date_hour_minute_second_millis;
import static org.springframework.data.elasticsearch.annotations.DateFormat.epoch_millis;

@Getter
@Document(indexName = "board")
@NoArgsConstructor
@Mapping(mappingPath = "elastic/board-mapping.json")
@Setting(settingPath = "elastic/jaso-setting.json")
public class BoardDocument {

    @Id
    private Long id;
    private String userNickname;
    private String userImageLink;
    private String mainImageLink;
    private String title;
    private String content;
    private String status;
    private Integer goodCount;
    private Integer commentCount;
    private List<ResourceInBoard> resourceInBoardList = new ArrayList<>();

    @Field(type = FieldType.Date, format = {date_hour_minute_second_millis, epoch_millis})
    private LocalDateTime createdAt;
    @Field(type = FieldType.Date, format = {date_hour_minute_second_millis, epoch_millis})
    private LocalDateTime modifiedAt;

    @Builder
    public BoardDocument(Board board) {
        this.id = board.getId();
        this.userNickname = board.getUser().getNickname();
        this.userImageLink = board.getUser().getUserImg();
        this.mainImageLink = board.getMainImageLink();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.status = board.getStatus();
        this.goodCount = board.getGoodCount();
        this.commentCount = board.getCommentCount();
        this.resourceInBoardList = board.getResourceList().stream()
                .map(ResourceInBoard::new).collect(Collectors.toList());
        this.createdAt = board.getCreatedAt();
        this.modifiedAt = board.getModifiedAt();
    }

    public void updateCount(Integer goodCount, Integer commentCount) {
        this.goodCount = goodCount;
        this.commentCount = commentCount;
    }

    public void updateBoardDocument(Board board) {
        this.userNickname = board.getUser().getNickname();
        this.userImageLink = board.getUser().getUserImg();
        this.mainImageLink = board.getMainImageLink();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.status = board.getStatus();
        this.goodCount = board.getGoodCount();
        this.commentCount = board.getCommentCount();
        this.resourceInBoardList = board.getResourceList().stream()
                .map(ResourceInBoard::new).collect(Collectors.toList());
        this.createdAt = board.getCreatedAt();
        this.modifiedAt = board.getModifiedAt();
    }
}
