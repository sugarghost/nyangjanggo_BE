package com.hanghae99_team3.model.board.dto;


import com.hanghae99_team3.model.board.Board;
import com.hanghae99_team3.model.comment.Comment;
import com.hanghae99_team3.model.comment.dto.CommentResponseDto;
import com.hanghae99_team3.model.good.Good;
import com.hanghae99_team3.model.good.GoodResponseDto;
import com.hanghae99_team3.model.images.Images;
import com.hanghae99_team3.model.images.ImagesResponseDto;
import com.hanghae99_team3.model.recipestep.dto.RecipeStepResponseDto;
import com.hanghae99_team3.model.resource.dto.ResourceResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class BoardDetailResponseDto {

    private Long boardId;
    private String nickname;
    private String userImg;
    private String title;
    private String subTitle;
    private String content;
    private List<ImagesResponseDto> imgList;
    private List<ResourceResponseDto> resourceResponseDtoList;
    private List<RecipeStepResponseDto> recipeStepResponseDtoList;
    private List<CommentResponseDto> commentList;
    private List<GoodResponseDto> goodList;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;


    public BoardDetailResponseDto(Board board) {
        this.boardId = board.getId();
        this.nickname = board.getUser().getNickname();
        this.userImg = board.getUser().getUserImg();
        this.title = board.getTitle();
        this.subTitle = board.getSubTitle();
        this.content = board.getContent();
        this.imgList = board.getImagesList().stream().map(ImagesResponseDto::new).collect(Collectors.toList());
        this.resourceResponseDtoList = board.getResourceList().stream().map(ResourceResponseDto::new).collect(Collectors.toList());
        this.recipeStepResponseDtoList = board.getRecipeStepList().stream().map(RecipeStepResponseDto::new).collect(Collectors.toList());
        this.commentList = board.getCommentList().stream().map(CommentResponseDto::new).collect(Collectors.toList());
        this.goodList = board.getGoodList().stream().map(GoodResponseDto::new).collect(Collectors.toList());
        this.createdAt = board.getCreatedAt();
        this.modifiedAt = board.getModifiedAt();
    }

}
