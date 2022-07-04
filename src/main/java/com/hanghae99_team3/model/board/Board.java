package com.hanghae99_team3.model.board;


import com.hanghae99_team3.model.Timestamped;
import com.hanghae99_team3.model.board.dto.BoardRequestDtoStepMain;
import com.hanghae99_team3.model.comment.Comment;
import com.hanghae99_team3.model.good.Good;
import com.hanghae99_team3.model.recipestep.RecipeStep;
import com.hanghae99_team3.model.resource.Resource;
import com.hanghae99_team3.model.user.domain.User;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column
    private String title;

    @Column
    private String subTitle;

    @Column
    private String mainImage;

    @Column
    private String content;

    @Setter
    @Column
    private String status;

    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<Good> goodList = new ArrayList<>();

/*
    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<Images> imagesList = new ArrayList<>();
*/

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<Resource> resourceList = new ArrayList<>();

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<RecipeStep> recipeStepList = new ArrayList<>();

    public void addComment(Comment comment) {
        comment.setBoard(this);
        this.commentList.add(comment);
    }

    public void addGood(Good good) {
        good.setBoard(this);
        this.goodList.add(good);
    }

/*
    public void addImages(Images images) {
        images.setBoard(this);
        this.imagesList.add(images);
    }
*/

    public void addResource(Resource resource) {
        resource.setBoard(this);
        this.resourceList.add(resource);
    }

    public void addRecipeStep(RecipeStep recipeStep) {
        recipeStep.setBoard(this);
        this.recipeStepList.add(recipeStep);
    }

    @Builder
    public Board(@NotNull User user,
                 @NotNull BoardRequestDtoStepMain boardRequestDtoStepMain,
                 @NotNull String mainImage,
                 @NotNull String status) {
        user.addBoard(this);
        this.title = boardRequestDtoStepMain.getTitle();
        this.subTitle = boardRequestDtoStepMain.getSubTitle();
        this.content = boardRequestDtoStepMain.getContent();
        this.mainImage = mainImage;
        this.status = status;
    }

    public void updateStepMain(BoardRequestDtoStepMain boardRequestDtoStepMain,
                               String mainImage) {
        this.title = boardRequestDtoStepMain.getTitle();
        this.subTitle = boardRequestDtoStepMain.getSubTitle();
        this.content = boardRequestDtoStepMain.getContent();
        this.mainImage = mainImage;
    }


}
