package com.hanghae99_team3;

import com.hanghae99_team3.config.TestConfig;
import com.hanghae99_team3.exception.ErrorMessage;
import com.hanghae99_team3.exception.newException.IdDifferentException;
import com.hanghae99_team3.login.jwt.PrincipalDetails;
import com.hanghae99_team3.model.board.BoardController;
import com.hanghae99_team3.model.board.domain.Board;
import com.hanghae99_team3.model.board.dto.request.BoardRequestDto;
import com.hanghae99_team3.model.board.dto.response.BoardDetailResponseDto;
import com.hanghae99_team3.model.board.dto.response.BoardResponseDto;
import com.hanghae99_team3.model.board.repository.BoardRepository;
import com.hanghae99_team3.model.good.GoodService;
import com.hanghae99_team3.model.recipestep.RecipeStepService;
import com.hanghae99_team3.model.recipestep.dto.RecipeStepRequestDto;
import com.hanghae99_team3.model.resource.dto.ResourceRequestDto;
import com.hanghae99_team3.model.resource.service.ResourceService;
import com.hanghae99_team3.model.s3.AwsS3Service;
import com.hanghae99_team3.model.user.UserRepository;
import com.hanghae99_team3.model.user.domain.AuthProvider;
import com.hanghae99_team3.model.user.domain.User;
import com.hanghae99_team3.model.user.domain.UserRole;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hanghae99_team3.exception.ErrorMessage.BOARD_NOT_FOUND;
import static com.hanghae99_team3.exception.ErrorMessage.USER_ID_DIFFERENT;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TestConfig.class})
@DisplayName("?????? ?????????")
public class IntegrationTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    BoardController boardController;
    @Autowired
    RecipeStepService recipeStepService;
    @Autowired
    ResourceService resourceService;
    @Autowired
    GoodService goodService;
    @Autowired
    AwsS3Service awsS3Service;

    User baseUser;
    PrincipalDetails baseUserDetails;
    Board baseBoard;
    BoardRequestDto baseBoardRequestDto;



    @BeforeEach
    void setUp() {
        baseUser = User.testRegister()
                .email("email@test.com")
                .password("password")
                .userImg("userImgLink")
                .provider(AuthProvider.kakao)
                .providerId("providerId")
                .nickname("nickname")
                .role(UserRole.USER)
                .userDescription("description")
                .build();


        baseBoard = Board.emptyBuilder()
                .user(baseUser)
                .build();

        List<ResourceRequestDto> resourceRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            resourceRequestDtoList.add(ResourceRequestDto.builder()
                    .resourceName("?????? ??????")
                    .amount("?????? ??????")
                    .category("?????? ????????????")
                    .build()
            );
        }

        List<RecipeStepRequestDto> recipeStepRequestDtoList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            recipeStepRequestDtoList.add(RecipeStepRequestDto.builder()
                    .stepNum(i)
                    .stepContent("????????? Step ??????")
                    .imageLink("????????? ????????? Link")
                    .build()
            );
        }

        baseBoardRequestDto = BoardRequestDto.builder()
                .title("??????")
                .content("??????")
                .mainImageLink("????????? Link")
                .resourceRequestDtoList(resourceRequestDtoList)
                .recipeStepRequestDtoList(recipeStepRequestDtoList)
                .build();

        boardRepository.save(baseBoard);
        userRepository.save(baseUser);

        baseUserDetails = new PrincipalDetails(baseUser);
    }

    @Nested
    @DisplayName("?????? ?????????")
    class SuccessTest {

        @Test
        @Transactional
        @DisplayName("????????? ?????? ????????????")
        void getOneBoard() {
            //given
            baseBoard.updateStepMain(baseBoardRequestDto);
            resourceService.createResource(baseBoardRequestDto.getResourceRequestDtoList(), baseBoard);
            recipeStepService.createRecipeStep(baseBoardRequestDto.getRecipeStepRequestDtoList(), baseBoard);
            baseBoard.setStatus("complete");

            //when
            BoardDetailResponseDto result = boardController.getOneBoard(baseBoard.getId());

            //then
            assertThat(result.getBoardId()).isEqualTo(baseBoard.getId());
            assertThat(result.getStatus()).isEqualTo("complete");
            assertThat(result.getNickname()).isEqualTo("nickname");
        }

        @Test
        @Transactional
        @DisplayName("???????????? ????????? ????????????")
        void getBoardByUserGood() {
            //given
            List<Board> boardList = new ArrayList<>();
            for(int j = 0; j < 5; j++){
                Board board = Board.emptyBuilder()
                        .user(baseUser)
                        .build();

                List<ResourceRequestDto> resourceRequestDtoList = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    resourceRequestDtoList.add(
                            ResourceRequestDto.builder()
                            .resourceName("?????? ??????")
                            .amount("?????? ??????")
                            .category("?????? ????????????")
                            .build()
                    );
                }

                List<RecipeStepRequestDto> recipeStepRequestDtoList = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    recipeStepRequestDtoList.add(
                            RecipeStepRequestDto.builder()
                            .stepNum(i)
                            .stepContent("????????? Step ??????")
                            .imageLink("????????? ????????? Link")
                            .build()
                    );
                }

                BoardRequestDto boardRequestDto = BoardRequestDto.builder()
                        .title("??????")
                        .content("??????")
                        .mainImageLink("????????? Link")
                        .resourceRequestDtoList(resourceRequestDtoList)
                        .recipeStepRequestDtoList(recipeStepRequestDtoList)
                        .build();

                board.updateStepMain(boardRequestDto);
                resourceService.createResource(boardRequestDto.getResourceRequestDtoList(), board);
                recipeStepService.createRecipeStep(boardRequestDto.getRecipeStepRequestDtoList(), board);
                board.setStatus("complete");

                boardList.add(board);
            }
            boardRepository.saveAll(boardList);

            boardList.forEach(board -> goodService.createAndRemoveGood(baseUserDetails,board.getId()));
            goodService.createAndRemoveGood(baseUserDetails,boardList.get(0).getId());

            Pageable pageable = PageRequest.of(0,10);

            //when
            Page<BoardResponseDto> result = boardController.getBoardByUserGood(baseUserDetails, pageable);

            //then
            assertThat(result.getTotalElements()).isEqualTo(4);
        }

        @Test
        @Transactional
        @DisplayName("????????? 10?????? ????????????")
        void getBoardsBySortPreview() {
            //given
            List<Board> boardList = new ArrayList<>();
            for(int j = 0; j < 11; j++){
                Board board = Board.emptyBuilder()
                        .user(baseUser)
                        .build();

                List<ResourceRequestDto> resourceRequestDtoList = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    resourceRequestDtoList.add(
                            ResourceRequestDto.builder()
                            .resourceName("?????? ??????")
                            .amount("?????? ??????")
                            .category("?????? ????????????")
                            .build()
                    );
                }

                List<RecipeStepRequestDto> recipeStepRequestDtoList = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    recipeStepRequestDtoList.add(
                            RecipeStepRequestDto.builder()
                            .stepNum(i)
                            .stepContent("????????? Step ??????")
                            .imageLink("????????? ????????? Link")
                            .build()
                    );
                }

                BoardRequestDto boardRequestDto = BoardRequestDto.builder()
                        .title("??????")
                        .content("??????")
                        .mainImageLink("????????? Link")
                        .resourceRequestDtoList(resourceRequestDtoList)
                        .recipeStepRequestDtoList(recipeStepRequestDtoList)
                        .build();

                board.updateStepMain(boardRequestDto);
                resourceService.createResource(boardRequestDto.getResourceRequestDtoList(), board);
                recipeStepService.createRecipeStep(boardRequestDto.getRecipeStepRequestDtoList(), board);
                board.setStatus("complete");

                boardList.add(board);
            }
            boardRepository.saveAll(boardList);

            boardList.forEach(board -> goodService.createAndRemoveGood(baseUserDetails,board.getId()));

            //when
            List<BoardResponseDto> resultGoodCount = boardController.getBoardsBySortPreview("goodCount");
            List<BoardResponseDto> resultCreatedAt = boardController.getBoardsBySortPreview("createdAt");

            //then
            assertThat(resultGoodCount).hasSize(10);
            assertThat(resultCreatedAt).hasSize(10);
        }

        @Test
        @Transactional
        @DisplayName("?????? ????????? ????????????")
        void getAllBoards() {
            //given
            List<Board> boardList = new ArrayList<>();
            for(int j = 0; j < 11; j++){
                Board board = Board.emptyBuilder()
                        .user(baseUser)
                        .build();

                List<ResourceRequestDto> resourceRequestDtoList = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    resourceRequestDtoList.add(
                            ResourceRequestDto.builder()
                            .resourceName("?????? ??????")
                            .amount("?????? ??????")
                            .category("?????? ????????????")
                            .build()
                    );
                }

                List<RecipeStepRequestDto> recipeStepRequestDtoList = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    recipeStepRequestDtoList.add(
                            RecipeStepRequestDto.builder()
                            .stepNum(i)
                            .stepContent("????????? Step ??????")
                            .imageLink("????????? ????????? Link")
                            .build()
                    );
                }

                BoardRequestDto boardRequestDto = BoardRequestDto.builder()
                        .title("??????")
                        .content("??????")
                        .mainImageLink("????????? Link")
                        .resourceRequestDtoList(resourceRequestDtoList)
                        .recipeStepRequestDtoList(recipeStepRequestDtoList)
                        .build();

                board.updateStepMain(boardRequestDto);
                resourceService.createResource(boardRequestDto.getResourceRequestDtoList(), board);
                recipeStepService.createRecipeStep(boardRequestDto.getRecipeStepRequestDtoList(), board);
                board.setStatus("complete");

                boardList.add(board);
            }
            boardRepository.saveAll(boardList);

            boardList.forEach(board -> goodService.createAndRemoveGood(baseUserDetails,board.getId()));

            Pageable pageableGoodCount = PageRequest.of(0,10, Sort.by(Sort.Direction.DESC,"goodCount"));
            Pageable pageableCreatedAt = PageRequest.of(0,10, Sort.by(Sort.Direction.DESC,"createdAt"));

            //when
            Page<BoardResponseDto> resultGoodCount = boardController.getAllBoards(pageableGoodCount);
            Page<BoardResponseDto> resultCreatedAt = boardController.getAllBoards(pageableCreatedAt);

            //then
            assertThat(resultGoodCount).hasSize(10);
            assertThat(resultCreatedAt).hasSize(10);
        }

        // github actions ??????..
//        @Test
//        @Transactional
//        @DisplayName("????????? ??????")
//        void createImage() throws IOException {
//            //given
//            File file = new File("src\\test\\resources\\image\\" +
//                                    "test_image.jpg");
//            FileItem fileItem = new DiskFileItem("test1.jpg",
//                    Files.probeContentType(file.toPath()), false, file.getName(), (int) file.length(), file.getParentFile());
//
//            try {
//                IOUtils.copy(new FileInputStream(file), fileItem.getOutputStream());
//            } catch (IOException ex) {
//                System.err.println("????????? ?????? ! ex.getMessage() = " + ex.getMessage());
//            }
//
//            MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
//
//            //when
//            Map<String, String> result = boardController.createImage(multipartFile, baseBoard.getId());
//            String imageLink = result.get("imageLink");
//
//            //then
//            assertThat(result).containsEntry("imageLink",imageLink);
//            awsS3Service.deleteFile(imageLink);
//        }

        @Test
        @Transactional
        @DisplayName("???????????? ????????? ?????? - ?????? ???")
        void checkModifyingBoard1() {
            //given
            //when
            BoardDetailResponseDto result = boardController.checkModifyingBoard(baseUserDetails);

            //then
            assertThat(result.getBoardId()).isEqualTo(baseBoard.getId());
            assertThat(result.getResourceResponseDtoList()).isEqualTo(new ArrayList<>());
            assertThat(result.getRecipeStepResponseDtoList()).isEqualTo(new ArrayList<>());
        }

        @Test
        @Transactional
        @DisplayName("???????????? ????????? ?????? - ?????? ???")
        void checkModifyingBoard2() {
            //given
            baseBoard.updateStepMain(baseBoardRequestDto);
            resourceService.createResource(baseBoardRequestDto.getResourceRequestDtoList(),baseBoard);

            //when
            BoardDetailResponseDto result = boardController.checkModifyingBoard(baseUserDetails);

            //then
            assertThat(result.getBoardId()).isEqualTo(baseBoard.getId());
            assertThat(result.getTitle()).isEqualTo("??????");
            assertThat(result.getResourceResponseDtoList().get(0).getResourceName()).isEqualTo("?????? ??????");
            assertThat(result.getRecipeStepResponseDtoList()).isEqualTo(new ArrayList<>());
        }

        @Test
        @Transactional
        @DisplayName("???????????? ????????? ??????")
        void createTempBoard() {
            //given
            //when
            boardController.createTempBoard(baseUserDetails, baseBoard.getId(),baseBoardRequestDto);

            //then
            assertThat(baseBoard.getStatus()).isEqualTo("modifying");
            assertThat(baseBoard.getTitle()).isEqualTo("??????");
            assertThat(baseBoard.getResourceList().get(0).getResourceName()).isEqualTo("?????? ??????");
            assertThat(baseBoard.getRecipeStepList()).hasSize(2);
        }

        @Test
        @Transactional
        @DisplayName("????????? ??????")
        void createBoard() {
            //given
            //when
            boardController.createBoard(baseUserDetails, baseBoard.getId(),baseBoardRequestDto);

            //then
            assertThat(baseBoard.getStatus()).isEqualTo("complete");
            assertThat(baseBoard.getTitle()).isEqualTo("??????");
            assertThat(baseBoard.getResourceList().get(0).getResourceName()).isEqualTo("?????? ??????");
            assertThat(baseBoard.getRecipeStepList()).hasSize(2);
        }

//        @Test
//        @Transactional
//        @DisplayName("????????? ??????")
//        void updateBoard() {
//            //given
//            boardController.createBoard(baseUserDetails, baseBoard.getId(),baseBoardRequestDto);
//
//            //when
//            boardController.updateBoard(baseUserDetails, baseBoard.getId(),baseBoardRequestDto);
//
//            //then
//            assertThat(baseBoard.getStatus()).isEqualTo("complete");
//            assertThat(baseBoard.getTitle()).isEqualTo("??????");
//            assertThat(baseBoard.getResourceList().get(0).getResourceName()).isEqualTo("?????? ??????");
//            assertThat(baseBoard.getRecipeStepList()).hasSize(4);
//        }

        @Test
        @Transactional
        @DisplayName("????????? ??????")
        void deleteBoard() {
            //given
            Long boardId = baseBoard.getId();

            //when
            boardController.deleteBoard(baseUserDetails, baseBoard.getId());
            Optional<Board> result = boardRepository.findById(boardId);

            //then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("?????? ?????????")
    class FailTest{

        @Test
        @Transactional
        @DisplayName("???????????? ?????? ????????? ??????")
        void getOneBoard() {
            //given
            Long wrongBoardId = -1L;

            //when
            Exception result = Assertions.assertThrows(IllegalArgumentException.class, () ->
                    boardController.getOneBoard(wrongBoardId));

            //then
            assertThat(result.getMessage()).isEqualTo(BOARD_NOT_FOUND);

        }

        @Test
        @Transactional
        @DisplayName("???????????? ?????? ?????? ??????")
        void getBoardByUserGood() {
            //given
            Pageable pageable = PageRequest.of(0,10);
            PrincipalDetails principalDetails = new PrincipalDetails();

            //when
            Exception result = Assertions.assertThrows(IllegalArgumentException.class, () ->
                    boardController.getBoardByUserGood(principalDetails,pageable));

            //then
            assertThat(result.getMessage()).isEqualTo("?????? ????????? ????????????.");

        }

        @Test
        @Transactional
        @DisplayName("????????? 10?????? ???????????? - ????????? ColumName")
        void getBoardsBySortPreview() {
            //given
            String wrongColumName = "WrongWrong";

            //when
            Exception result = Assertions.assertThrows(PropertyReferenceException.class, () ->
                    boardController.getBoardsBySortPreview(wrongColumName));

            //then
            assertThat(result.getMessage()).isEqualTo("No property 'wrongWrong' found for type 'Board'");

        }

        @Test
        @Transactional
        @DisplayName("?????? ????????? ???????????? - ????????? ColumName")
        void getAllBoards() {
            //given
            String wrongColumName = "WrongWrong";
            Pageable pageable = PageRequest.of(0,10,Sort.Direction.DESC,wrongColumName);

            //when
            Exception result = Assertions.assertThrows(PropertyReferenceException.class, () ->
                    boardController.getAllBoards(pageable));

            //then
            assertThat(result.getMessage()).isEqualTo("No property 'wrongWrong' found for type 'Board'");

        }
        // github actions ??????..
//        @Test
//        @Transactional
//        @DisplayName("????????? ?????? - ????????? ?????? ?????????")
//        void createImage() throws IOException {
//            //given
//            File file1 = new File("src\\test\\resources\\image\\" +
//                    "test_txt.txt");
//            File file2 = new File("src\\test\\resources\\image\\" +
//                    "test_fake_image.jpg");
//
//            // ????????? ??????
//            FileItem fileItem1 = new DiskFileItem("test_txt.txt",
//                    Files.probeContentType(file1.toPath()), false, file1.getName(), (int) file1.length(), file1.getParentFile());
//            // ????????? ??????????????? ???????????? jpg ??? ?????? ??????
//            FileItem fileItem2 = new DiskFileItem("test_fake_image.jpg",
//                    Files.probeContentType(file2.toPath()), false, file2.getName(), (int) file2.length(), file2.getParentFile());
//
//            try {
//                IOUtils.copy(new FileInputStream(file1), fileItem1.getOutputStream());
//                IOUtils.copy(new FileInputStream(file2), fileItem2.getOutputStream());
//            } catch (IOException ex) {
//                System.err.println("????????? ?????? ! ex.getMessage() = " + ex.getMessage());
//            }
//
//            MultipartFile txtMultipartFile = new CommonsMultipartFile(fileItem1);
//            MultipartFile fakeImageMultipartFile = new CommonsMultipartFile(fileItem2);
//
//            //when
//            Exception txtResult = Assertions.assertThrows(IllegalArgumentException.class, () ->
//                    boardController.createImage(txtMultipartFile,baseBoard.getId()));
//            Exception fakeImageResult = Assertions.assertThrows(IllegalArgumentException.class, () ->
//                    boardController.createImage(fakeImageMultipartFile,baseBoard.getId()));
//
//            //then
//            assertThat(txtResult.getMessage()).isEqualTo("AwsS3 : ????????? ????????? ????????? ???????????????.");
//            assertThat(fakeImageResult.getMessage()).isEqualTo("AwsS3 : ????????? ????????? ????????? ???????????????.");
//
//        }

        @Test
        @Transactional
        @DisplayName("?????? ????????? ???????????? ??????")
        void createTempBoard() {
            //given
            User user = User.testRegister()
                    .email("email@test.com2")
                    .password("password2")
                    .userImg("userImgLink2")
                    .provider(AuthProvider.kakao)
                    .providerId("providerId2")
                    .nickname("nickname2")
                    .role(UserRole.USER)
                    .userDescription("description2")
                    .build();
            userRepository.save(user);
            PrincipalDetails principalDetails = new PrincipalDetails(user);

            //when
            Exception txtResult = Assertions.assertThrows(IdDifferentException.class, () ->
                    boardController.createTempBoard(principalDetails, baseBoard.getId(),baseBoardRequestDto));

            //then
            assertThat(txtResult.getMessage()).isEqualTo(USER_ID_DIFFERENT);
        }

    }


}
