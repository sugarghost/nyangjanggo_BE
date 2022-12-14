package com.hanghae99_team3.batch.job;

import com.hanghae99_team3.model.board.config.SaveCount;
import com.hanghae99_team3.model.board.domain.Board;
import com.hanghae99_team3.model.board.domain.BoardDocument;
import com.hanghae99_team3.model.board.repository.BoardDocumentRepository;
import com.hanghae99_team3.model.board.repository.BoardRepository;
import com.hanghae99_team3.model.images.Images;
import com.hanghae99_team3.model.images.ImagesRepository;
import com.hanghae99_team3.model.s3.AwsS3Service;
import com.hanghae99_team3.model.user.domain.User;
import com.hanghae99_team3.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final UserRepository userRepository;
    private final ImagesRepository imagesRepository;
    private final AwsS3Service awsS3Service;
    private final ServletWebServerApplicationContext webServerAppCtxt;
    private final SaveCount saveCount;
    private final BoardRepository boardRepository;
    private final BoardDocumentRepository boardDocumentRepository;

    @Bean
    public Job deleteDeadImageJob(){
        return jobBuilderFactory.get("deleteDeadImagejob")
                .start(checkPort())
                    .on("FAILED")
                    .end()
                .from(checkPort())
                    .on("COMPLETED")
                    .to(deleteDeadImage())
                    .on("*")
                    .end()
                .end()
                .build();
    }

    @Bean
    public Job updateGoodCountJob(){
        return jobBuilderFactory.get("updateGoodCountJob")
                .start(checkPort())
                    .on("FAILED")
                    .end()
                .from(checkPort())
                    .on("COMPLETED")
                    .to(updateGoodCount())
                    .on("*")
                    .end()
                .end()
                .build();
    }

    @Bean
    public Step checkPort(){

        return stepBuilderFactory.get("checkPort")
                .tasklet((stepContribution, chunkContext) ->{
                    // ?????? ????????????????????? Port????????? ?????????
                    Integer port = webServerAppCtxt.getWebServer().getPort();

                    // Nginx??? ?????? ?????? Port????????? ?????????
                    try (BufferedReader reader = new BufferedReader(
                            new FileReader("/home/ec2-user/service_url.inc")
                    )) {

                        String str = reader.readLine();
                        str = str.substring(str.length() - 5).substring(0, 4);
                        Integer processingPort = Integer.valueOf(str);

                        // Port ????????? ???????????? Failed ????????? Complete
                        if (!processingPort.equals(port)) {
                            log.warn("checkPort ??????");
                            log.warn("?????? ???????????? : " + port + "???????????? ???????????? : " + processingPort);
                            stepContribution.setExitStatus(ExitStatus.FAILED);
                        }else {
                            log.info("?????? ???????????? : " + port + "???????????? ???????????? : " + processingPort);
                        }

                    } catch (IOException | NumberFormatException e) {
                        log.error(e.getMessage());
                    }
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step deleteDeadImage(){
        return stepBuilderFactory.get("deleteDeadImage")
                .tasklet((stepContribution, chunkContext) ->{
                    // S3??? ?????? ?????? ????????? ????????? ?????????
                    List<String> allObject = awsS3Service.getAllObject().get(0);

                    List<String> imageLinkList = new ArrayList<>();
                    allObject.forEach(imageLink -> {
                        // DB??? ???????????? ??????????????? ??????
                        Optional<Images> optionalImages1 = imagesRepository.findByImageLink(imageLink);
                        Optional<User> optionalImages2 = userRepository.findByUserImg(imageLink);

                        if (optionalImages1.isEmpty() && optionalImages2.isEmpty()) {
                            imageLinkList.add(imageLink);
                        }
                    });

                    // DB??? ???????????? ???????????? ????????? ??????
                    awsS3Service.deleteAllFile(imageLinkList);
                    log.info(imageLinkList.size() + "?????? ???????????? ??????????????????.");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step updateGoodCount(){
        return stepBuilderFactory.get("updateGoodCount")
                .tasklet((stepContribution, chunkContext) ->{
                    // ????????? ????????? BoardId??? ?????????
                    List<Long> boardIdList = new ArrayList<>(saveCount.popAllBoardId());

                    if (!boardIdList.isEmpty()){
                        // BoardDocument??? Board??? ???????????? ????????? ??????????????? ??????
                        List<BoardDocument> boardDocumentList = boardDocumentRepository.findAllByIdIn(boardIdList);
                        List<Board> boardList = boardRepository.findAllByIdIn(boardIdList);

                        // goodCount??? commentCount??? update
                        for (int i = 0; i < boardList.size(); i++){
                            Board board = boardList.get(i);
                            boardDocumentList.get(i).updateCount(board.getGoodCount(),board.getCommentCount());
                        }
                        boardDocumentRepository.saveAll(boardDocumentList);
                        log.info("??????????????? Board ID : " + boardIdList);
                    }

                    return RepeatStatus.FINISHED;
                }).build();
    }


}
